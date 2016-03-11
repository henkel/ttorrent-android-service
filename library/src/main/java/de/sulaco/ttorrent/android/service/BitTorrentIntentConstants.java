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

public final class BitTorrentIntentConstants {
    public static final String ACTION_DOWNLOAD =
            "de.sulaco.android.ttorrent.service.DOWNLOAD";

    public static final String ACTION_ABORT_DOWNLOAD =
            "de.sulaco.android.ttorrent.service.ABORT_DOWNLOAD";

    public static final String ACTION_BROADCAST =
            "de.sulaco.android.ttorrent.service.BROADCAST";

    public static final String EXTRA_TORRENT_FILE =
            "de.sulaco.android.ttorrent.service.TORRENT_FILE";

    public static final String EXTRA_DESTINATION_DIRECTORY =
            "de.sulaco.android.ttorrent.service.DESTINATION_DIRECTORY";

    public static final String EXTRA_DOWNLOAD_STATE =
            "de.sulaco.android.ttorrent.service.DOWNLOAD_STATE";

    public static final String EXTRA_DOWNLOAD_PROGRESS =
            "de.sulaco.android.ttorrent.service.DOWNLOAD_PROGRESS";

}
