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

import de.sulaco.bittorrent.service.intent.AbortRequest;
import de.sulaco.bittorrent.service.intent.BitTorrentIntentConstants;
import de.sulaco.bittorrent.service.intent.DownloadRequest;

public class BitTorrentDownloadManager {

    private Context context;
    private volatile DownloadListener downloadListener;
    private final IntentFilter progressFilter = new IntentFilter(BitTorrentIntentConstants.ACTION_BROADCAST_PROGRESS);
    private final IntentFilter endFilter = new IntentFilter(BitTorrentIntentConstants.ACTION_BROADCAST_END);

    private final BroadcastReceiver progressBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleProgressBroadcast(intent, downloadListener);
        }
    };

    private final BroadcastReceiver endBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleEndBroadcast(intent, downloadListener);
        }
    };

    public BitTorrentDownloadManager(Context context) {
        if (context == null) {
            throw new NullPointerException("context must not be null");
        }
        this.context = context;
    }

    public void registerDownloadListener(DownloadListener listener) {
        if (listener == null) {
            throw new NullPointerException("listener must not be null");
        }
        if (downloadListener != null) {
            throw new IllegalStateException("download listener is already registered");
        }
        downloadListener = listener;
        registerBroadcastReceivers();
    }

    public void unregisterDownloadListener(DownloadListener listener) {
        if (downloadListener != listener) {
            throw new IllegalStateException("download listener is not registered");
        }
        downloadListener = null;
        unregisterBroadcastReceivers();
    }

    public void enqueue(DownloadRequest downloadRequest) {
        context.startService(downloadRequest.createIntent(context));
    }

    public void abort() {
        context.startService(AbortRequest.createIntent(context));
    }

    private void registerBroadcastReceivers() {
        LocalBroadcastManager.getInstance(context).registerReceiver(
                progressBroadcastReceiver,
                progressFilter);
        LocalBroadcastManager.getInstance(context).registerReceiver(
                endBroadcastReceiver,
                endFilter);
    }

    private void unregisterBroadcastReceivers() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(progressBroadcastReceiver);
        LocalBroadcastManager.getInstance(context).unregisterReceiver(endBroadcastReceiver);
    }

    private static void handleProgressBroadcast(Intent intent, DownloadListener listener) {
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

    private static void handleEndBroadcast(Intent intent, DownloadListener listener) {
        if (listener == null) {
            return;
        }
        String torrentFile = intent.getStringExtra(BitTorrentIntentConstants.EXTRA_TORRENT_FILE);
        int downloadState = intent.getIntExtra(BitTorrentIntentConstants.EXTRA_DOWNLOAD_STATE, -1);
        listener.onDownloadEnd(torrentFile, downloadState);
    }
}
