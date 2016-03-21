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

package de.sulaco.bittorrent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import de.sulaco.bittorrent.service.BitTorrentDownloadService;
import de.sulaco.bittorrent.service.BitTorrentIntentConstants;
import de.sulaco.bittorrent.service.DownloadRequest;

public class BitTorrentDownloadManager {

    private volatile DownloadListener downloadListener;
    private Context context;

    public BitTorrentDownloadManager(Context context) {
        this.context = context;

        IntentFilter progressFilter = new IntentFilter(BitTorrentIntentConstants.ACTION_BROADCAST_PROGRESS);

        // TODO unregister
        LocalBroadcastManager.getInstance(context).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        handleProgressBroadcast(intent, downloadListener);
                    }
                },
                progressFilter);


        IntentFilter endFilter = new IntentFilter(BitTorrentIntentConstants.ACTION_BROADCAST_END);

        // TODO unregister
        LocalBroadcastManager.getInstance(context).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        handleEndBroadcast(intent, downloadListener);
                    }
                },
                endFilter);
    }

    public void setDownloadListener(DownloadListener listener) {
        downloadListener = listener;
    }

    public void enqueue(DownloadRequest downloadRequest) {
        context.startService(downloadRequest.createIntent(context));
    }

    public void abort() {
        context.startService(BitTorrentDownloadService.createAbortIntent(context));
    }

    static void handleProgressBroadcast(Intent intent, DownloadListener listener) {
        if (listener == null) {
            return;
        }
        String torrentFile = intent.getStringExtra(BitTorrentIntentConstants.EXTRA_TORRENT_FILE);
        int progress = intent.getIntExtra(BitTorrentIntentConstants.EXTRA_DOWNLOAD_PROGRESS, -1);
        if (progress == 0) {
            listener.onDownloadStart(torrentFile);
        } else {
            listener.onDownloadProgress(torrentFile, progress);
        }
    }

    static void handleEndBroadcast(Intent intent, DownloadListener listener) {
        if (listener == null) {
            return;
        }
        String torrentFile = intent.getStringExtra(BitTorrentIntentConstants.EXTRA_TORRENT_FILE);
        int downloadState = intent.getIntExtra(BitTorrentIntentConstants.EXTRA_DOWNLOAD_STATE, -1);
        listener.onDownloadEnd(torrentFile, downloadState);
    }
}
