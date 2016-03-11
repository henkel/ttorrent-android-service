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
import android.support.v4.content.LocalBroadcastManager;

import java.util.concurrent.atomic.AtomicInteger;

import de.sulaco.ttorrent.utils.DownloadListener;
import de.sulaco.ttorrent.utils.Downloader;

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

    private final AtomicInteger pendingAbortActionCounter = new AtomicInteger(0);
    private final DownloadListener downloadListener = new DownloadServiceAdapter(this);
    private final Downloader downloader;

    public BitTorrentDownloadService() {
        this(new Downloader());
    }

    BitTorrentDownloadService(Downloader downloader) {
        super("BitTorrentDownload");
        this.downloader = downloader;
        this.downloader.setDownloadListener(downloadListener);
    }

    public static void requestDownload(Context context,
                                       String torrentFile,
                                       String destinationDirectory) {
        context.startService(createDownloadIntent(context, torrentFile, destinationDirectory));
    }

    public static void requestAbort(Context context) {
        context.startService(createAbortIntent(context));
    }

    static Intent createDownloadIntent(Context context,
                                                 String torrentFile,
                                                 String destinationDirectory) {
        if (torrentFile == null) {
            throw new NullPointerException("torrentFile must not be null");
        }

        if (destinationDirectory == null) {
            throw new NullPointerException("destinationDirectory must not be null");
        }

        Intent intent = new Intent(context, BitTorrentDownloadService.class);
        intent.setAction(BitTorrentIntentConstants.ACTION_DOWNLOAD);
        intent.putExtra(BitTorrentIntentConstants.EXTRA_TORRENT_FILE, torrentFile);
        intent.putExtra(BitTorrentIntentConstants.EXTRA_DESTINATION_DIRECTORY, destinationDirectory);
        return intent;
    }

    static Intent createAbortIntent(Context context) {
        Intent intent = new Intent(context, BitTorrentDownloadService.class);
        intent.setAction(BitTorrentIntentConstants.ACTION_ABORT_DOWNLOAD);
        return intent;
    }

    static Intent createProgressIntent(String torrentFile, int progress) {
        if (torrentFile == null) {
            throw new NullPointerException("torrentFile must not be null");
        }

        Intent intent = new Intent(BitTorrentIntentConstants.ACTION_BROADCAST);
        intent.putExtra(BitTorrentIntentConstants.EXTRA_TORRENT_FILE, torrentFile);
        intent.putExtra(BitTorrentIntentConstants.EXTRA_DOWNLOAD_PROGRESS, progress);
        return intent;
    }

    static Intent createEndIntent(String torrentFile, int state) {
        if (torrentFile == null) {
            throw new NullPointerException("torrentFile must not be null");
        }

        Intent intent = new Intent(BitTorrentIntentConstants.ACTION_BROADCAST);
        intent.putExtra(BitTorrentIntentConstants.EXTRA_TORRENT_FILE, torrentFile);
        intent.putExtra(BitTorrentIntentConstants.EXTRA_DOWNLOAD_STATE, state);
        return intent;
    }

    void broadcastProgress(String torrentFile, int progress) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(
                BitTorrentDownloadService.createProgressIntent(
                        torrentFile,
                        progress));
    }

    void broadcastEnd(String torrentFile, int state) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(
                BitTorrentDownloadService.createEndIntent(
                        torrentFile,
                        state));
    }

    boolean hasPendingAbortActions() {
        return pendingAbortActionCounter.get() > 0;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(BitTorrentIntentConstants.ACTION_ABORT_DOWNLOAD)) {
            pendingAbortActionCounter.incrementAndGet();
            downloader.cancel();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent.getAction().equals(BitTorrentIntentConstants.ACTION_ABORT_DOWNLOAD)) {
            pendingAbortActionCounter.decrementAndGet();
        }
        else if (intent.getAction().equals(BitTorrentIntentConstants.ACTION_DOWNLOAD)) {
            final String torrentFile = intent.getStringExtra(
                    BitTorrentIntentConstants.EXTRA_TORRENT_FILE);
            final String destinationDirectory = intent.getStringExtra(
                    BitTorrentIntentConstants.EXTRA_DESTINATION_DIRECTORY);
            downloader.download(torrentFile, destinationDirectory);
        }
    }
}
