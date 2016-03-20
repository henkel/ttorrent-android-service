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

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import de.sulaco.ttorrent.DownloadListener;

class LocalBroadcaster implements DownloadListener {

    private Context context;

    public LocalBroadcaster(Context context) {
        if (context == null) {
            throw new NullPointerException("context must not be null");
        }
        this.context = context;
    }

    public void onDownloadStart(String torrentFile) {
        broadcast(createProgressIntent(torrentFile, 0));
    }

    public void onDownloadProgress(String torrentFile, int progress) {
        broadcast(createProgressIntent(torrentFile, progress));
    }

    public void onDownloadEnd(String torrentFile, int downloadState) {
        broadcast(createEndIntent(torrentFile, downloadState));
    }

    void broadcast(Intent intent) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    static Intent createProgressIntent(String torrentFile, int progress) {
        return new DownloadProgressBroadcast()
                .setTorrentFile(torrentFile)
                .setProgress(progress)
                .createIntent();
    }

    static Intent createEndIntent(String torrentFile, int downloadState) {
        return new DownloadEndBroadcast()
                .setTorrentFile(torrentFile)
                .setDownloadState(downloadState)
                .createIntent();
    }
}
