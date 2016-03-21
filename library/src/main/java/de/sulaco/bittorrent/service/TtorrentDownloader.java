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

package de.sulaco.bittorrent.service;

import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.Torrent;

import java.io.File;
import java.net.InetAddress;
import java.util.Observable;
import java.util.Observer;

import de.sulaco.bittorrent.DownloadListener;
import de.sulaco.bittorrent.DownloadState;

class TtorrentDownloader implements Downloader{

    private DownloadListener downloadListener;
    private volatile int progress;
    private final TtorrentClientObserver ttorrentClientObserver = new TtorrentClientObserver();
    private long timeoutMillis = 0;

    public TtorrentDownloader() {
    }

    public synchronized void setTimeout(long millis) {
        timeoutMillis = millis;
    }

    @Override
    public synchronized void setDownloadListener(DownloadListener downloadListener) {
        this.downloadListener = downloadListener;
    }

    @Override
    public void setEnabled(boolean enabled) {
        ttorrentClientObserver.setEnabled(enabled);
    }

    @Override
    public synchronized void download(
            final String torrentFile,
            final String destinationDirectory) {

        progress = 0;

        if (downloadListener == null) {
            throw new NullPointerException("downloadListener must not be null");
        }

        if (torrentFile == null) {
            return;
        }

        downloadListener.onDownloadStart(torrentFile);

        if (destinationDirectory == null) {
            downloadListener.onDownloadEnd(torrentFile, DownloadState.ERROR_DESTINATION_DIR);
            return;
        }

        File destination = new File(destinationDirectory);

        if (!destination.isDirectory() || !destination.canWrite()) {
            downloadListener.onDownloadEnd(torrentFile, DownloadState.ERROR_DESTINATION_DIR);
            return;
        }

        Torrent torrent;

        try {
            torrent = Torrent.load(new File(torrentFile));
        } catch (Exception e) {
            downloadListener.onDownloadEnd(torrentFile, DownloadState.ERROR_TORRENT_FILE);
            return;
        }

        Client client;

        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            client = new Client(inetAddress, new SharedTorrent(torrent, destination));
        } catch (Exception e) {
            downloadListener.onDownloadEnd(torrentFile, DownloadState.ERROR);
            return;
        }

        client.addObserver(new Observer() {
            @Override
            public void update(Observable observable, Object data) {
                Client client = (Client) observable;
                float completion = client.getTorrent().getCompletion();

                if ((int) completion >= progress + 1) {
                    progress = (int) completion;
                    downloadListener.onDownloadProgress(torrentFile, progress);
                }
            }
        });

        client.download();
        int downloadState = ttorrentClientObserver.waitForCompletionOrTimeout(
                client,
                timeoutMillis);
        client.stop();

        downloadListener.onDownloadEnd(torrentFile, downloadState);
    }
}
