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

package de.sulaco.bittorrent.service;

import android.content.Intent;

import com.turn.ttorrent.core.BuildConfig;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;


@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class DownloadProgressBroadcastTest {

    @Test
    public void testBroadcastWithOutData() {
        DownloadProgressBroadcast broadcast = new DownloadProgressBroadcast();
        try {
            broadcast.createIntent();
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        } catch (IllegalStateException expectedException) {
        }
    }

    @Test
    public void testSetTorrentFile() {
        DownloadProgressBroadcast broadcast = new DownloadProgressBroadcast();
        assertThat(broadcast.setTorrentFile("file")).isSameAs(broadcast);
    }

    @Test
    public void testSetProgress() {
        DownloadProgressBroadcast broadcast = new DownloadProgressBroadcast();
        assertThat(broadcast.setProgress(42)).isSameAs(broadcast);
    }

    @Test
    public void testCreateIntent() {
        final String torrentFile = "file";
        final int progress = 42;
        DownloadProgressBroadcast broadcast = new DownloadProgressBroadcast();
        broadcast.setTorrentFile(torrentFile);
        broadcast.setProgress(progress);
        Intent intent = broadcast.createIntent();
        assertThat(intent.getAction()).isEqualTo(BitTorrentIntentConstants.ACTION_BROADCAST_PROGRESS);
        assertThat(intent.getStringExtra(BitTorrentIntentConstants.EXTRA_TORRENT_FILE))
                .isEqualToIgnoringCase(torrentFile);
        assertThat(intent.getIntExtra(BitTorrentIntentConstants.EXTRA_DOWNLOAD_PROGRESS, -1))
                .isEqualTo(progress);
    }

    @Test
    public void testCreateIntentWithProgressOutOfRange() {
        final String torrentFile = "file";
        final int progress = 200;
        DownloadProgressBroadcast broadcast = new DownloadProgressBroadcast();
        broadcast.setTorrentFile(torrentFile);
        broadcast.setProgress(progress);
        try {
            broadcast.createIntent();
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        } catch (IllegalStateException expectedException) {
        }
    }
}