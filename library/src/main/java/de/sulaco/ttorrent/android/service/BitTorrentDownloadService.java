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

package de.sulaco.ttorrent.android.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import de.sulaco.ttorrent.Downloader;
import de.sulaco.ttorrent.ttorrent.TtorrentDownloader;

/**
 * <p/>
 * Downloads a BitTorrent file.
 * <p/>
 * IntentService handles asynchronous download
 * requests (expressed as {@link Intent}s) on demand.  Clients send requests
 * through {@link android.content.Context#startService(Intent)} calls; the
 * service is started as needed, handles each Intent in turn using a worker
 * thread, and stops itself when done.
 */
public class BitTorrentDownloadService extends IntentService {

    private int pendingAbortCount = 0;
    private Downloader downloader;

    public BitTorrentDownloadService() {
        super("BitTorrentDownload");
        setDownloader(new TtorrentDownloader());
    }

    public static Intent createAbortIntent(Context context) {
        Intent intent = new Intent(context, BitTorrentDownloadService.class);
        intent.setAction(BitTorrentIntentConstants.ACTION_ABORT_DOWNLOAD);
        return intent;
    }

    synchronized boolean isAbortPending() {
        return pendingAbortCount > 0;
    }

    void setDownloader(Downloader downloader) {
        this.downloader = downloader;
        this.downloader.setDownloadListener(new LocalBroadcaster(this));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(BitTorrentIntentConstants.ACTION_ABORT_DOWNLOAD)) {
            synchronized (this) {
                pendingAbortCount += 1;
                downloader.setEnabled(false);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent.getAction().equals(BitTorrentIntentConstants.ACTION_ABORT_DOWNLOAD)) {
            synchronized (this) {
                pendingAbortCount -= 1;
                if(pendingAbortCount == 0) {
                    downloader.setEnabled(true);
                }
            }
        }
        else if (isAbortPending()) {
            // skip intent
        }
        else if (intent.getAction().equals(BitTorrentIntentConstants.ACTION_START_DOWNLOAD)) {
            final String torrentFile = intent.getStringExtra(
                    BitTorrentIntentConstants.EXTRA_TORRENT_FILE);
            final String destinationDirectory = intent.getStringExtra(
                    BitTorrentIntentConstants.EXTRA_DESTINATION_DIRECTORY);
            downloader.download(torrentFile, destinationDirectory);
        }
    }
}
