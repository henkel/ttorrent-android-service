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

import android.content.Context;
import android.content.Intent;

import de.sulaco.bittorrent.service.BitTorrentDownloadService;

public class AbortRequest {

    public static Intent createIntent(Context context) {
        Intent intent = new Intent(context, BitTorrentDownloadService.class);
        intent.setAction(BitTorrentIntentConstants.ACTION_ABORT_DOWNLOAD);
        return intent;
    }
}
