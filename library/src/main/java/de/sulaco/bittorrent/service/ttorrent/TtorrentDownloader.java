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

package de.sulaco.bittorrent.service.ttorrent;

import android.support.annotation.Nullable;
import android.util.Log;

import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.Torrent;

import java.io.File;
import java.net.InetAddress;
import java.util.Observable;
import java.util.Observer;

import de.sulaco.bittorrent.DownloadListener;
import de.sulaco.bittorrent.DownloadState;
import de.sulaco.bittorrent.service.Downloader;

public class TtorrentDownloader implements Downloader {

    public final static String TAG = "TtorrentDownloader";

    private final static DownloadListener DUMMY = new DownloadListener() {
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

    private DownloadListener downloadListener = DUMMY;
    private final TtorrentClientObserver ttorrentClientObserver = new TtorrentClientObserver();
    private volatile int progress;
    private long timeoutMillis = 0;

    private class DownloadException extends RuntimeException {
        public DownloadException (String message) {
            super (message);
        }
    }

    public TtorrentDownloader() {
    }

    public synchronized void setTimeout(long millis) {
        timeoutMillis = millis;
    }

    @Override
    public synchronized void setDownloadListener(DownloadListener downloadListener) {
        this.downloadListener = downloadListener != null ? downloadListener : DUMMY;
    }

    @Override
    public void setEnabled(boolean enabled) {
        ttorrentClientObserver.setEnabled(enabled);
    }

    @Override
    public synchronized void download(
            final String torrentFile,
            final String destinationDirectory) {

        try {
            notifyDownloadStart(torrentFile);
            File destinationDir = getWriteableDestination(torrentFile, destinationDirectory);
            Torrent torrent = loadTorrent(torrentFile);
            downloadTorrent(torrentFile, destinationDir, torrent);
        }
        catch (DownloadException e) {
            Log.w(TAG, e.getMessage());
        }
    }

    @Nullable
    private void notifyDownloadStart(String torrentFile) throws DownloadException {
        if (torrentFile == null) throw new DownloadException("torrentFile == null");
        downloadListener.onDownloadStart(torrentFile);
    }

    @Nullable
    private File getWriteableDestination(String torrentFile, String destinationDirectory) {
        if (destinationDirectory == null) {
            downloadListener.onDownloadEnd(torrentFile, DownloadState.ERROR_DESTINATION_DIR);
            throw new DownloadException("destinationDirectory == null");
        }
        File destination = new File(destinationDirectory);
        if (!destination.isDirectory() || !destination.canWrite()) {
            downloadListener.onDownloadEnd(torrentFile, DownloadState.ERROR_DESTINATION_DIR);
            throw new DownloadException("destination is not a directory or cannot be written");
        }
        return destination;
    }

    @Nullable
    private Torrent loadTorrent(String torrentFile) {
        Torrent torrent;
        try {
            torrent = Torrent.load(new File(torrentFile));
        } catch (Exception e) {
            downloadListener.onDownloadEnd(torrentFile, DownloadState.ERROR_TORRENT_FILE);
            throw new DownloadException("error loading torrent file");
        }
        return torrent;
    }

    private void downloadTorrent(final String torrentFile, File destination, Torrent torrent) {
        Client client;

        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            client = new Client(inetAddress, new SharedTorrent(torrent, destination));
        } catch (Exception e) {
            downloadListener.onDownloadEnd(torrentFile, DownloadState.ERROR);
            throw new DownloadException("error creating torrent client");
        }

        progress = 0;

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
