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

import com.turn.ttorrent.core.BuildConfig;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import de.sulaco.bittorrent.service.intent.BitTorrentIntentConstants;
import de.sulaco.bittorrent.service.intent.DownloadEndBroadcast;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;


@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class DownloadEndBroadcastTest {

    @Test
    public void testBroadcastWithOutData() {
        DownloadEndBroadcast downloadEndBroadcast = new DownloadEndBroadcast();
        try {
            downloadEndBroadcast.createIntent();
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        } catch (IllegalStateException expectedException) {
        }
    }

    @Test
    public void testSetTorrentFile() {
        DownloadEndBroadcast downloadEndBroadcast = new DownloadEndBroadcast();
        assertThat(downloadEndBroadcast.setTorrentFile("file")).isSameAs(downloadEndBroadcast);
    }

    @Test
    public void testSetDownloadState() {
        DownloadEndBroadcast downloadEndBroadcast = new DownloadEndBroadcast();
        assertThat(downloadEndBroadcast.setDownloadState(42)).isSameAs(downloadEndBroadcast);
    }

    @Test
    public void testCreateIntent() {
        final String torrentFile = "file";
        final int downloadState = 42;
        DownloadEndBroadcast downloadEndBroadcast = new DownloadEndBroadcast();
        downloadEndBroadcast.setTorrentFile(torrentFile);
        downloadEndBroadcast.setDownloadState(downloadState);
        Intent intent = downloadEndBroadcast.createIntent();
        assertThat(intent.getAction()).isEqualTo(BitTorrentIntentConstants.ACTION_BROADCAST_END);
        assertThat(intent.getStringExtra(BitTorrentIntentConstants.EXTRA_TORRENT_FILE))
                .isEqualToIgnoringCase(torrentFile);
        assertThat(intent.getIntExtra(BitTorrentIntentConstants.EXTRA_DOWNLOAD_STATE, -1))
                .isEqualTo(downloadState);
    }
}