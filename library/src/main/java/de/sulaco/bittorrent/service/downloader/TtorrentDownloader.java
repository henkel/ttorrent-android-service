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

import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.Torrent;

import java.io.File;
import java.net.InetAddress;
import java.util.Observable;
import java.util.Observer;

import de.sulaco.bittorrent.service.intent.DownloadState;

import static de.sulaco.bittorrent.service.util.RequireNonNull.requireNonNull;

public class TtorrentDownloader implements Downloader {

    private final static DownloadListener EMPTY_LISTENER = new DownloadListener() {
        @Override
        public void onDownloadStart(String torrentFile) {
        }

        @Override
        public void onDownloadProgress(String torrentFile, int progress) {
        }

        @Override
        public void onDownloadEnd(String torrentFile, int downloadState) {
        }
    };

    private class DownloadException extends RuntimeException {
        private final int reason;
        public DownloadException (int reason) {
            this.reason = reason;
        }
        public int getReason() {
            return reason;
        }
    }

    private DownloadListener downloadListener = EMPTY_LISTENER;
    private final TtorrentClientObserver ttorrentClientObserver = new TtorrentClientObserver();
    private long timeoutMillis = 0;

    public TtorrentDownloader() {
    }

    public synchronized void setTimeout(long millis) {
        timeoutMillis = millis;
    }

    @Override
    public synchronized void setDownloadListener(DownloadListener downloadListener) {
        this.downloadListener = downloadListener != null ? downloadListener : EMPTY_LISTENER;
    }

    @Override
    public void setEnabled(boolean enabled) {
        ttorrentClientObserver.setEnabled(enabled);
    }

    @Override
    public synchronized void download(
            final String torrentFile,
            final String destinationDirectory) {
        requireNonNull(torrentFile, "torrentFile must not be null");
        requireNonNull(destinationDirectory, "destinationDirectory must not be null");
        try {
            downloadListener.onDownloadStart(torrentFile);
            int downloadState = tryDownload(torrentFile, destinationDirectory);
            downloadListener.onDownloadEnd(torrentFile, downloadState);
        }
        catch (DownloadException e) {
            downloadListener.onDownloadEnd(torrentFile, e.getReason());
        }
    }

    private int tryDownload(String torrentFile, String destinationDirectory) {
        File destinationDir = new File(destinationDirectory);
        validateDestination(destinationDir);
        Torrent torrent = loadTorrent(torrentFile);
        Observer clientObserver = createClientObserver(torrentFile);
        Client client = createClient(torrent, destinationDir, clientObserver);
        return downloadContent(client);
    }

    private void validateDestination(File destination) {
        if (!destination.exists()) {
            throw new DownloadException(DownloadState.ERROR_DESTINATION_NOT_FOUND);
        }
        if (!destination.isDirectory()) {
            throw new DownloadException(DownloadState.ERROR_DESTINATION_IS_NOT_A_DIRECTORY);
        }
        if (!destination.canWrite()) {
            throw new DownloadException(DownloadState.ERROR_DESTINATION_IS_NOT_WRITEABLE);
        }
    }

    private Observer createClientObserver(final String torrentFile) {
        return new Observer() {
            private int progress = 0;

            @Override
            public void update(Observable observable, Object data) {
                Client client = (Client) observable;
                float completion = client.getTorrent().getCompletion();
                if ((int) completion >= progress + 1) {
                    progress = (int) completion;
                    downloadListener.onDownloadProgress(torrentFile, progress);
                }
            }
        };
    }

    private Torrent loadTorrent(String torrentFile) {
        File file = new File(torrentFile);
        if (!file.exists()) {
            throw new DownloadException(DownloadState.ERROR_TORRENT_FILE_NOT_FOUND);
        }
        try {
            return Torrent.load(new File(torrentFile));
        } catch (Exception e) {
            throw new DownloadException(DownloadState.ERROR_LOADING_TORRENT_FILE);
        }
    }

    private Client createClient(Torrent torrent, File destination, Observer observer) {
        Client client;
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            client = new Client(inetAddress, new SharedTorrent(torrent, destination));
        } catch (Exception e) {
            throw new DownloadException(DownloadState.ERROR);
        }
        client.addObserver(observer);
        return client;
    }

    private int downloadContent(Client client) {
        client.download();
        int downloadState = ttorrentClientObserver.waitForCompletionOrTimeout(
                client,
                timeoutMillis);
        client.stop();
        return downloadState;
    }
}
