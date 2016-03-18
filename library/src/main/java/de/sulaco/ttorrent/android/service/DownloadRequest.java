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
import android.net.Uri;

public class DownloadRequest {
    private Uri torrentFile;
    private Uri destinationDirectory;

    public DownloadRequest() {
    }

    public DownloadRequest setTorrentFile(Uri torrentFile) {
        this.torrentFile = torrentFile;
        return this;
    }

    public DownloadRequest setDestinationDirectory(Uri destinationDirectory) {
        this.destinationDirectory = destinationDirectory;
        return this;
    }

    public Intent createIntent(Context context) {
        if (torrentFile == null) {
            throw new IllegalStateException("torrentFile must not be null");
        }
        if (destinationDirectory == null) {
            throw new IllegalStateException("destinationDirectory must not be null");
        }
        Intent intent = new Intent(context, BitTorrentDownloadService.class);
        intent.setAction(BitTorrentIntentConstants.ACTION_START_DOWNLOAD);
        intent.putExtra(BitTorrentIntentConstants.EXTRA_TORRENT_FILE, torrentFile.getPath());
        intent.putExtra(BitTorrentIntentConstants.EXTRA_DESTINATION_DIRECTORY, destinationDirectory.getPath());
        return intent;
    }
}


