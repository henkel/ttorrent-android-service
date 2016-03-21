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

package de.sulaco.ttorrent;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;

import com.turn.ttorrent.core.BuildConfig;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.support.v4.ShadowLocalBroadcastManager;
import org.robolectric.shadows.support.v4.Shadows;

import de.sulaco.ttorrent.android.service.BitTorrentDownloadService;
import de.sulaco.ttorrent.android.service.DownloadRequest;
import de.sulaco.ttorrent.android.service.DownloadEndBroadcast;
import de.sulaco.ttorrent.android.service.DownloadProgressBroadcast;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class BitTorrentDownloadManagerTest {

    @Test
    public void testAbort() {
        BitTorrentDownloadManager manager = new BitTorrentDownloadManager(RuntimeEnvironment.application);
        manager.abort();
        Intent nextStartedIntent = ShadowApplication.getInstance().peekNextStartedService();
        Intent referenceAbortIntent = BitTorrentDownloadService.createAbortIntent(RuntimeEnvironment.application);
        assertThat(nextStartedIntent.filterEquals(referenceAbortIntent)).isTrue();
    }

    @Test
    public void testEnqueue() {
        BitTorrentDownloadManager manager = new BitTorrentDownloadManager(RuntimeEnvironment.application);
        DownloadRequest downloadRequest = new DownloadRequest();
        downloadRequest.setTorrentFile(Uri.parse("file"));
        downloadRequest.setDestinationDirectory(Uri.parse("dest"));
        manager.enqueue(downloadRequest);
        Intent nextStartedIntent = ShadowApplication.getInstance().peekNextStartedService();
        Intent downloadIntent = downloadRequest.createIntent(RuntimeEnvironment.application);
        assertThat(nextStartedIntent.equals(downloadIntent)).isTrue();
    }


    private void sendLocalProgressBroadcast(String torrentFile, int progress) {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(RuntimeEnvironment.application);
        ShadowLocalBroadcastManager shadowLocalBroadcastManager = Shadows.shadowOf(localBroadcastManager);
        shadowLocalBroadcastManager.sendBroadcast(
                new DownloadProgressBroadcast()
                        .setTorrentFile(torrentFile)
                        .setProgress(progress)
                        .createIntent());
    }

    @Test
    public void testNotifyDownloadStart() {
        final String torrentFile = "file";
        BitTorrentDownloadManager manager = new BitTorrentDownloadManager(RuntimeEnvironment.application);
        DownloadListener downloadListener = Mockito.mock(DownloadListener.class);
        manager.setDownloadListener(downloadListener);
        sendLocalProgressBroadcast(torrentFile, 0);
        Mockito.verify(downloadListener, Mockito.times(1)).onDownloadStart(torrentFile);
    }

    @Test
    public void testNotifyDownloadProgress() {
        final String torrentFile = "file";
        final int progress = 42;
        BitTorrentDownloadManager manager = new BitTorrentDownloadManager(RuntimeEnvironment.application);
        DownloadListener downloadListener = Mockito.mock(DownloadListener.class);
        manager.setDownloadListener(downloadListener);
        sendLocalProgressBroadcast(torrentFile, progress);
        Mockito.verify(downloadListener, Mockito.times(1)).onDownloadProgress(torrentFile, progress);
    }

    private void sendLocalEndBroadcast(String torrentFile, int downloadState) {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(RuntimeEnvironment.application);
        ShadowLocalBroadcastManager shadowLocalBroadcastManager = Shadows.shadowOf(localBroadcastManager);
        shadowLocalBroadcastManager.sendBroadcast(
                new DownloadEndBroadcast()
                        .setTorrentFile(torrentFile)
                        .setDownloadState(downloadState)
                        .createIntent());
    }


    @Test
    public void testNotifyDownloadEnd() {
        final String torrentFile = "file";
        final int downloadState = 42;
        BitTorrentDownloadManager manager = new BitTorrentDownloadManager(RuntimeEnvironment.application);
        DownloadListener downloadListener = Mockito.mock(DownloadListener.class);
        manager.setDownloadListener(downloadListener);
        sendLocalEndBroadcast(torrentFile, downloadState);
        Mockito.verify(downloadListener, Mockito.times(1)).onDownloadEnd(torrentFile, downloadState);
    }

    @Test
    public void testProgressBroadcastWithoutListener() {
        BitTorrentDownloadManager manager = new BitTorrentDownloadManager(RuntimeEnvironment.application);
        sendLocalProgressBroadcast("file", 42);
    }

    @Test
    public void testEndBroadcastWithoutListener() {
        BitTorrentDownloadManager manager = new BitTorrentDownloadManager(RuntimeEnvironment.application);
        sendLocalEndBroadcast("file", 42);
    }
}