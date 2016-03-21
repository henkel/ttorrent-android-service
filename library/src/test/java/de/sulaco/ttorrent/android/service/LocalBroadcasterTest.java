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

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.turn.ttorrent.core.BuildConfig;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.support.v4.ShadowLocalBroadcastManager;
import org.robolectric.shadows.support.v4.Shadows;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;


@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class LocalBroadcasterTest {

    @Test
    public void testConstructionWithNull() {
        try {
            new LocalBroadcaster(null);
            failBecauseExceptionWasNotThrown(NullPointerException.class);
        } catch (NullPointerException expectedException) {
        }
    }

    @Test
    public void testOnDownloadStart() {
        final String torrentFile = "file";
        LocalBroadcaster localBroadcaster = new LocalBroadcaster(RuntimeEnvironment.application);
        localBroadcaster.onDownloadStart(torrentFile);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(RuntimeEnvironment.application);
        ShadowLocalBroadcastManager shadowLocalBroadcastManager = Shadows.shadowOf(localBroadcastManager);
        List<Intent> broadcastIntents = shadowLocalBroadcastManager.getSentBroadcastIntents();
        assertThat(broadcastIntents.size()).isEqualTo(1);

        // TODO not needed, compare to DownloadProgressBroadcast
        assertThat(broadcastIntents.get(0).getAction()).isEqualTo(BitTorrentIntentConstants.ACTION_BROADCAST_PROGRESS);
        assertThat(broadcastIntents.get(0).getStringExtra(BitTorrentIntentConstants.EXTRA_TORRENT_FILE)).isEqualToIgnoringCase(torrentFile);
        assertThat(broadcastIntents.get(0).getIntExtra(BitTorrentIntentConstants.EXTRA_DOWNLOAD_PROGRESS, -1)).isEqualTo(0);
    }

    @Test
    public void testOnDownloadProgress() {
        final String torrentFile = "file";
        final int progress = 42;
        LocalBroadcaster localBroadcaster = new LocalBroadcaster(RuntimeEnvironment.application);
        localBroadcaster.onDownloadProgress(torrentFile, progress);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(RuntimeEnvironment.application);
        ShadowLocalBroadcastManager shadowLocalBroadcastManager = Shadows.shadowOf(localBroadcastManager);
        List<Intent> broadcastIntents = shadowLocalBroadcastManager.getSentBroadcastIntents();
        assertThat(broadcastIntents.size()).isEqualTo(1);
        Intent referenceBroadcast =
                new DownloadProgressBroadcast()
                        .setTorrentFile(torrentFile)
                        .setProgress(progress)
                        .createIntent();
        assertThat(broadcastIntents.get(0).equals(referenceBroadcast)).isTrue();
    }

    @Test
    public void testOnDownloadEnd() {
        final String torrentFile = "file";
        final int downloadState = 42;
        LocalBroadcaster localBroadcaster = new LocalBroadcaster(RuntimeEnvironment.application);
        localBroadcaster.onDownloadEnd(torrentFile, downloadState);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(RuntimeEnvironment.application);
        ShadowLocalBroadcastManager shadowLocalBroadcastManager = Shadows.shadowOf(localBroadcastManager);
        List<Intent> broadcastIntents = shadowLocalBroadcastManager.getSentBroadcastIntents();
        assertThat(broadcastIntents.size()).isEqualTo(1);
        Intent referenceBroadcast =
                new DownloadEndBroadcast()
                        .setTorrentFile(torrentFile)
                        .setDownloadState(downloadState)
                        .createIntent();
        assertThat(broadcastIntents.get(0).equals(referenceBroadcast)).isTrue();
    }
}