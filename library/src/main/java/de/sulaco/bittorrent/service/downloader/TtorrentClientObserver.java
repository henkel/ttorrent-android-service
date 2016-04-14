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

package de.sulaco.bittorrent.service.downloader;

import android.support.annotation.NonNull;

import com.turn.ttorrent.client.Client;

import java.util.Observable;
import java.util.Observer;

import de.sulaco.bittorrent.service.intent.DownloadState;

class TtorrentClientObserver {
    private long nameTimeOfLastActivity;
    private boolean isEnabled = true;

    public synchronized void setEnabled(boolean enabled) {
        isEnabled = enabled;
        notifyAll();
    }

    /**
     * Returns DownloadState.ABORTED if ClientObserver is disabled
     */
    public synchronized int waitForCompletionOrTimeout(Client client, long timeoutMillis) {
        boolean timedOut = false;
        nameTimeOfLastActivity = System.nanoTime();
        client.addObserver(createActivityObserver());
        try {
            while (isEnabled && !isClientDone(client) && !timedOut) {
                timedOut = waitForNextActivity(timeoutMillis);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return determineDownloadState(client, timedOut);
    }

    private int determineDownloadState(Client client, boolean timedOut) {
        Client.ClientState clientState = client.getState();
        if (clientState == Client.ClientState.DONE) {
            return DownloadState.COMPLETED;
        } else if (clientState == Client.ClientState.ERROR) {
            return DownloadState.ERROR;
        } else if (timedOut) {
            return DownloadState.TIMED_OUT;
        } else {
            return DownloadState.ABORTED;
        }
    }

    private boolean waitForNextActivity(long timeoutMillis) throws InterruptedException {
        if (timeoutMillis == 0) {
            waitForeverForNextActivity();
            return false;
        } else {
            return waitTillTimeoutForNextActivity(timeoutMillis);
        }
    }

    private boolean waitTillTimeoutForNextActivity(long timeoutMillis) throws InterruptedException {
        long millisTillTimeout = getMillisUntilTimeout(timeoutMillis);
        if (millisTillTimeout > 0) {
            wait(millisTillTimeout);
            return false;
        } else {
            // timeout reached
            return true;
        }
    }

    private void waitForeverForNextActivity() throws InterruptedException {
        wait();
    }

    boolean isClientDone(Client client) {
        Client.ClientState state = client.getState();
        return state == Client.ClientState.DONE || state == Client.ClientState.ERROR;
    }

    @NonNull
    private Observer createActivityObserver() {
        return new Observer() {
            private float lastCompletion = -1.0f;

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
                notifyActivity();
            }
        };
    }

    private synchronized void notifyActivity() {
        nameTimeOfLastActivity = System.nanoTime();
        notifyAll();
    }

    private long getMillisUntilTimeout(long timeoutMillis) {
        long currentNanoTime = System.nanoTime();
        long deltaMillis = (currentNanoTime - nameTimeOfLastActivity) / 1000000;
        return timeoutMillis - deltaMillis;
    }
}