/*
 * Copyright (C) 2016 Philipp Henkel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.sulaco.ttorrent.utils;

import com.turn.ttorrent.client.Client;

import java.util.Observable;
import java.util.Observer;

public class DownloadStateObserver {
    private long nameTimeOfLastActivity;
    private boolean isCancelled = false;
    private boolean isSomeoneIsWaiting = false;

    public synchronized int waitForCompletionOrTimeout(Client client, long timeoutMillis) {
        isSomeoneIsWaiting = true;
        int result = waitForCompletionOrTimeoutOrCancel(client, timeoutMillis);
        isCancelled = false;
        isSomeoneIsWaiting = false;
        return result;
    }

    public synchronized void cancel() {
        if (isSomeoneIsWaiting) {
            isCancelled = true;
            notifyAll();
        }
    }

    private synchronized int waitForCompletionOrTimeoutOrCancel(Client client, long timeoutMillis) {

        nameTimeOfLastActivity = System.nanoTime();

        client.addObserver(new Observer() {
            float lastCompletion = -1.0f;

            private boolean isInactive(Client client, Client.ClientState clientState) {

                if (clientState != Client.ClientState.SHARING) {
                    return false;
                }

                float completion = client.getTorrent().getCompletion();

                if (completion > lastCompletion) {
                    lastCompletion = completion;
                    return false;
                }

                return true;
            }

            @Override
            public void update(Observable observable, Object data) {

                if (isInactive((Client) observable, (Client.ClientState) data)) {
                    return;
                }

                synchronized (DownloadStateObserver.this) {
                    nameTimeOfLastActivity = System.nanoTime();
                    DownloadStateObserver.this.notifyAll();
                }
            }
        });

        while (!isCancelled) {

            long waitMillis = 0;

            if (timeoutMillis != 0) {
                waitMillis = getMillisUntilTimeout(timeoutMillis);
                if (waitMillis <= 0) {
                    return DownloadState.TIMEOUT;
                }
            }

            try {
                wait(waitMillis);
            } catch (InterruptedException e) {
                isCancelled = true;
            }

            Client.ClientState clientState = client.getState();

            if (clientState == Client.ClientState.DONE) {
                return DownloadState.COMPLETE;
            } else if (clientState == Client.ClientState.ERROR) {
                return DownloadState.ERROR;
            }
        }

        return DownloadState.ABORTED;
    }

    private long getMillisUntilTimeout(long timeoutMillis) {
        long currentNanoTime = System.nanoTime();
        long deltaMillis = (currentNanoTime - nameTimeOfLastActivity) / 1000000;
        return timeoutMillis - deltaMillis;
    }

}