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

import de.sulaco.ttorrent.utils.DownloadListener;

class DownloadServiceAdapter implements DownloadListener {

    private BitTorrentDownloadService bitTorrentDownloadService;

    public DownloadServiceAdapter(BitTorrentDownloadService bitTorrentDownloadService) {
        if (bitTorrentDownloadService == null) {
            throw new NullPointerException("bitTorrentDownloadService must not be null");
        }
        this.bitTorrentDownloadService = bitTorrentDownloadService;
    }

    public void onDownloadStart(String torrentFile) {
        bitTorrentDownloadService.broadcastProgress(torrentFile, 0);
    }

    public void onDownloadProgressUpdate(String torrentFile, int progress) {
        bitTorrentDownloadService.broadcastProgress(torrentFile, progress);
    }

    public void onDownloadEnd(String torrentFile, int downloadState) {
        bitTorrentDownloadService.broadcastEnd(torrentFile, downloadState);
    }
}
