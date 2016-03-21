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

package de.sulaco.bittorrent.android.service;

import android.content.Intent;
import android.net.Uri;

import com.turn.ttorrent.core.BuildConfig;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class DownloadRequestTest {
    @Test
    public void testEmptyRequest() {
        DownloadRequest downloadRequest = new DownloadRequest();
        try {
            downloadRequest.createIntent(RuntimeEnvironment.application);
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        } catch (IllegalStateException expectedException) {
        }
    }

    @Test
    public void testRequestWithTorrentFileOnly() {
        DownloadRequest downloadRequest = new DownloadRequest();
        downloadRequest.setTorrentFile(Uri.parse("file"));
        try {
            downloadRequest.createIntent(RuntimeEnvironment.application);
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        } catch (IllegalStateException expectedException) {
        }
    }

    @Test
    public void testRequestWithDestinationDirectoryOnly() {
        DownloadRequest downloadRequest = new DownloadRequest();
        downloadRequest.setDestinationDirectory(Uri.parse("dest"));
        try {
            downloadRequest.createIntent(RuntimeEnvironment.application);
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        } catch (IllegalStateException expectedException) {
        }
    }

    @Test
    public void testSetTorrentFile() {
        DownloadRequest downloadRequest = new DownloadRequest();
        assertThat(downloadRequest.setTorrentFile(Uri.parse("file"))).isSameAs(downloadRequest);
    }

    @Test
    public void testSetDestinationDirectory() {
        DownloadRequest downloadRequest = new DownloadRequest();
        assertThat(downloadRequest.setDestinationDirectory(Uri.parse("dest"))).isSameAs(downloadRequest);
    }

    @Test
    public void testCreateIntent() {
        String torrentFile = "file";
        String destinationDirectory = "dir";
        DownloadRequest downloadRequest = new DownloadRequest();
        downloadRequest.setTorrentFile(Uri.parse(torrentFile));
        downloadRequest.setDestinationDirectory(Uri.parse(destinationDirectory));
        Intent intent = downloadRequest.createIntent(RuntimeEnvironment.application);
        assertThat(intent.getAction()).isEqualTo(BitTorrentIntentConstants.ACTION_START_DOWNLOAD);
        assertThat(intent.getStringExtra(BitTorrentIntentConstants.EXTRA_TORRENT_FILE))
                .isEqualToIgnoringCase(torrentFile);
        assertThat(intent.getStringExtra(BitTorrentIntentConstants.EXTRA_DESTINATION_DIRECTORY))
                .isEqualToIgnoringCase(destinationDirectory);
    }
}
