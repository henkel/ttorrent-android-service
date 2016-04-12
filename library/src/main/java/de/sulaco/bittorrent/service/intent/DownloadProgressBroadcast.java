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

package de.sulaco.bittorrent.service.intent;

import android.content.Intent;

public class DownloadProgressBroadcast {

    private String torrentFile;
    private int progress;

    public DownloadProgressBroadcast() {
    }

    public static Intent createProgressIntent(String torrentFile, int progress) {
        return new DownloadProgressBroadcast()
                .setTorrentFile(torrentFile)
                .setProgress(progress)
                .createIntent();
    }

    public DownloadProgressBroadcast setTorrentFile(String torrentFile) {
        this.torrentFile = torrentFile;
        return this;
    }

    public DownloadProgressBroadcast setProgress(int progress) {
        this.progress = progress;
        return this;
    }

    public Intent createIntent() {
        if (torrentFile == null) {
            throw new IllegalStateException("torrentFile must not be null");
        }
        if (progress < 0 || progress > 100) {
            throw new IllegalStateException("progress out of range");
        }
        Intent intent = new Intent(BitTorrentIntentConstants.ACTION_BROADCAST_PROGRESS);
        intent.putExtra(BitTorrentIntentConstants.EXTRA_TORRENT_FILE, torrentFile);
        intent.putExtra(BitTorrentIntentConstants.EXTRA_DOWNLOAD_PROGRESS, progress);
        return intent;
    }

}
