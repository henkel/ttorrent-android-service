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

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.support.v4.ShadowLocalBroadcastManager;
import org.robolectric.shadows.support.v4.Shadows;

import java.util.List;

import de.sulaco.ttorrent.utils.DownloadListener;
import de.sulaco.ttorrent.utils.Downloader;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class BitTorrentDownloadServiceTest extends TestCase {

    @Test
    public void testCreateDownloadIntent() {
        Intent downloadIntent = BitTorrentDownloadService.createDownloadIntent(RuntimeEnvironment.application, "file", "directory");
        assertEquals(BitTorrentIntentConstants.ACTION_DOWNLOAD, downloadIntent.getAction());
        assertEquals("file", downloadIntent.getStringExtra(BitTorrentIntentConstants.EXTRA_TORRENT_FILE));
        assertEquals("directory", downloadIntent.getStringExtra(BitTorrentIntentConstants.EXTRA_DESTINATION_DIRECTORY));
    }

    @Test
    public void testCreateDownloadIntentWithNullTorrentFile() {
        try {
            BitTorrentDownloadService.createDownloadIntent(RuntimeEnvironment.application, null, "dir");
            fail("NullPointerException expected");
        } catch (NullPointerException expectedException) {
        }
    }

    @Test
    public void testCreateDownloadIntentWithNullDestinationDirectory() {
        try {
            BitTorrentDownloadService.createDownloadIntent(RuntimeEnvironment.application, "file", null);
            fail("NullPointerException expected");
        } catch (NullPointerException expectedException) {
        }
    }

    @Test
    public void testCreateAbortIntent() {
        Intent intent = BitTorrentDownloadService.createAbortIntent(RuntimeEnvironment.application);
        assertEquals(BitTorrentIntentConstants.ACTION_ABORT_DOWNLOAD, intent.getAction());
    }

    @Test
    public void testCreateProgressIntent() {
        Intent intent = BitTorrentDownloadService.createProgressIntent("file", 42);
        assertEquals(BitTorrentIntentConstants.ACTION_BROADCAST, intent.getAction());
        assertEquals(null, intent.getComponent());
        assertEquals("file", intent.getStringExtra(BitTorrentIntentConstants.EXTRA_TORRENT_FILE));
        assertEquals(42, intent.getIntExtra(BitTorrentIntentConstants.EXTRA_DOWNLOAD_PROGRESS, -1));
    }

    @Test
    public void testCreateProgressIntentWithNullTorrentFile() {
        try {
            BitTorrentDownloadService.createProgressIntent(null, 42);
            fail("NullPointerException expected");
        } catch (NullPointerException expectedException) {
        }
    }


    @Test
    public void testCreateEndIntent() {
        Intent intent = BitTorrentDownloadService.createEndIntent("file", 42);
        assertEquals(BitTorrentIntentConstants.ACTION_BROADCAST, intent.getAction());
        assertEquals(null, intent.getComponent());
        assertEquals("file", intent.getStringExtra(BitTorrentIntentConstants.EXTRA_TORRENT_FILE));
        assertEquals(42, intent.getIntExtra(BitTorrentIntentConstants.EXTRA_DOWNLOAD_STATE, -1));
    }

    @Test
    public void testCreateEndIntentWithNullTorrentFile() {
        try {
            BitTorrentDownloadService.createEndIntent(null, 42);
            fail("NullPointerException expected");
        } catch (NullPointerException expectedException) {
        }
    }

    @Test
    public void testConstructionDestruction() {
        Downloader downloader = Mockito.mock(Downloader.class);
        BitTorrentDownloadService bitTorrentDownloadService = new BitTorrentDownloadService(downloader);
        bitTorrentDownloadService.onCreate();
        assertEquals(false, bitTorrentDownloadService.hasPendingAbortActions());
        bitTorrentDownloadService.onDestroy();
        assertEquals(false, bitTorrentDownloadService.hasPendingAbortActions());
        Mockito.verify(downloader, Mockito.times(1)).setDownloadListener(Mockito.any(DownloadListener.class));
        Mockito.verify(downloader, Mockito.times(0)).download(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testDownload() {
        final String torrentFile = "file";
        final String destinationDirectory = "dir";
        Intent downloadIntent = BitTorrentDownloadService.createDownloadIntent(
                RuntimeEnvironment.application,
                torrentFile,
                destinationDirectory);
        Downloader downloader = Mockito.mock(Downloader.class);
        BitTorrentDownloadService bitTorrentDownloadService = new BitTorrentDownloadService(downloader);
        bitTorrentDownloadService.onCreate();
        assertEquals(false, bitTorrentDownloadService.hasPendingAbortActions());
        bitTorrentDownloadService.onStartCommand(downloadIntent, 0, 0);
        bitTorrentDownloadService.onHandleIntent(downloadIntent);
        bitTorrentDownloadService.onDestroy();
        assertEquals(false, bitTorrentDownloadService.hasPendingAbortActions());
        Mockito.verify(downloader, Mockito.times(1)).setDownloadListener(Mockito.any(DownloadListener.class));
        Mockito.verify(downloader, Mockito.times(1)).download(torrentFile, destinationDirectory);
    }

    @Test
    public void testDownloadNoFileNoDirectory() {
        Intent downloadIntent = new Intent(RuntimeEnvironment.application, BitTorrentDownloadService.class);
        downloadIntent.setAction(BitTorrentIntentConstants.ACTION_DOWNLOAD);
        Downloader downloader = Mockito.mock(Downloader.class);
        BitTorrentDownloadService bitTorrentDownloadService = new BitTorrentDownloadService(downloader);
        bitTorrentDownloadService.onCreate();
        assertEquals(false, bitTorrentDownloadService.hasPendingAbortActions());
        bitTorrentDownloadService.onStartCommand(downloadIntent, 0, 0);
        bitTorrentDownloadService.onHandleIntent(downloadIntent);
        bitTorrentDownloadService.onDestroy();
        assertEquals(false, bitTorrentDownloadService.hasPendingAbortActions());
        Mockito.verify(downloader, Mockito.times(1)).setDownloadListener(Mockito.any(DownloadListener.class));
        Mockito.verify(downloader, Mockito.times(1)).download(null, null);
    }

    @Test
    public void testHandleIntentWithUnknownAction() {
        Intent intent = new Intent(RuntimeEnvironment.application, BitTorrentDownloadService.class);
        intent.setAction(Intent.ACTION_VIEW);
        Downloader downloader = Mockito.mock(Downloader.class);
        BitTorrentDownloadService bitTorrentDownloadService = new BitTorrentDownloadService(downloader);
        bitTorrentDownloadService.onCreate();
        assertEquals(false, bitTorrentDownloadService.hasPendingAbortActions());
        bitTorrentDownloadService.onStartCommand(intent, 0, 0);
        bitTorrentDownloadService.onHandleIntent(intent);
        bitTorrentDownloadService.onDestroy();
        assertEquals(false, bitTorrentDownloadService.hasPendingAbortActions());
        Mockito.verify(downloader, Mockito.times(1)).setDownloadListener(Mockito.any(DownloadListener.class));
        Mockito.verify(downloader, Mockito.times(0)).download(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testAbort() {
        Intent abortIntent = BitTorrentDownloadService.createAbortIntent(
                RuntimeEnvironment.application);
        Downloader downloader = Mockito.mock(Downloader.class);
        BitTorrentDownloadService bitTorrentDownloadService = new BitTorrentDownloadService(downloader);
        bitTorrentDownloadService.onCreate();
        assertEquals(false, bitTorrentDownloadService.hasPendingAbortActions());
        bitTorrentDownloadService.onStartCommand(abortIntent, 0, 0);
        assertEquals(true, bitTorrentDownloadService.hasPendingAbortActions());
        bitTorrentDownloadService.onHandleIntent(abortIntent);
        assertEquals(false, bitTorrentDownloadService.hasPendingAbortActions());
        bitTorrentDownloadService.onDestroy();
        assertEquals(false, bitTorrentDownloadService.hasPendingAbortActions());
        Mockito.verify(
                downloader,
                Mockito.times(0)).download(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testAbortAbort() {
        Intent abortIntent = BitTorrentDownloadService.createAbortIntent(
                RuntimeEnvironment.application);
        Downloader downloader = Mockito.mock(Downloader.class);
        BitTorrentDownloadService bitTorrentDownloadService = new BitTorrentDownloadService(downloader);
        bitTorrentDownloadService.onCreate();
        assertEquals(false, bitTorrentDownloadService.hasPendingAbortActions());
        bitTorrentDownloadService.onStartCommand(abortIntent, 0, 0);
        assertEquals(true, bitTorrentDownloadService.hasPendingAbortActions());
        bitTorrentDownloadService.onStartCommand(abortIntent, 0, 0);
        assertEquals(true, bitTorrentDownloadService.hasPendingAbortActions());
        bitTorrentDownloadService.onHandleIntent(abortIntent);
        assertEquals(true, bitTorrentDownloadService.hasPendingAbortActions());
        bitTorrentDownloadService.onHandleIntent(abortIntent);
        assertEquals(false, bitTorrentDownloadService.hasPendingAbortActions());
        bitTorrentDownloadService.onDestroy();
        assertEquals(false, bitTorrentDownloadService.hasPendingAbortActions());
    }


    @Test
    public void testAbortDownloadAbort() {
        final String torrentFile = "file";
        final String destinationDirectory = "dir";
        Intent downloadIntent = BitTorrentDownloadService.createDownloadIntent(
                RuntimeEnvironment.application,
                torrentFile,
                destinationDirectory);
        Intent abortIntent = BitTorrentDownloadService.createAbortIntent(
                RuntimeEnvironment.application);
        Downloader downloader = Mockito.mock(Downloader.class);
        BitTorrentDownloadService bitTorrentDownloadService = new BitTorrentDownloadService(downloader);
        bitTorrentDownloadService.onCreate();
        assertEquals(false, bitTorrentDownloadService.hasPendingAbortActions());
        bitTorrentDownloadService.onStartCommand(abortIntent, 0, 0);
        assertEquals(true, bitTorrentDownloadService.hasPendingAbortActions());
        bitTorrentDownloadService.onStartCommand(downloadIntent, 0, 0);
        assertEquals(true, bitTorrentDownloadService.hasPendingAbortActions());
        bitTorrentDownloadService.onStartCommand(abortIntent, 0, 0);
        assertEquals(true, bitTorrentDownloadService.hasPendingAbortActions());
        bitTorrentDownloadService.onHandleIntent(abortIntent);
        assertEquals(true, bitTorrentDownloadService.hasPendingAbortActions());
        bitTorrentDownloadService.onHandleIntent(downloadIntent);
        assertEquals(true, bitTorrentDownloadService.hasPendingAbortActions());
        bitTorrentDownloadService.onHandleIntent(abortIntent);
        assertEquals(false, bitTorrentDownloadService.hasPendingAbortActions());
        bitTorrentDownloadService.onDestroy();
        assertEquals(false, bitTorrentDownloadService.hasPendingAbortActions());
        Mockito.verify(
                downloader,
                Mockito.times(1)).setDownloadListener(Mockito.any(DownloadListener.class));
        Mockito.verify(
                downloader,
                Mockito.times(1)).download(torrentFile, destinationDirectory);
    }

    @Test
    public void testDownloadDownloadAbort() {
        final String torrentFile1 = "file1";
        final String destinationDirectory1 = "dir2";
        final String torrentFile2 = "file2";
        final String destinationDirectory2 = "dir2";
        Intent downloadIntent1 = BitTorrentDownloadService.createDownloadIntent(
                RuntimeEnvironment.application,
                torrentFile1,
                destinationDirectory1);
        Intent downloadIntent2 = BitTorrentDownloadService.createDownloadIntent(
                RuntimeEnvironment.application,
                torrentFile2,
                destinationDirectory2);
        Intent abortIntent = BitTorrentDownloadService.createAbortIntent(
                RuntimeEnvironment.application);
        Downloader downloader = Mockito.mock(Downloader.class);
        BitTorrentDownloadService bitTorrentDownloadService = new BitTorrentDownloadService(downloader);
        bitTorrentDownloadService.onCreate();
        assertEquals(false, bitTorrentDownloadService.hasPendingAbortActions());
        bitTorrentDownloadService.onStartCommand(downloadIntent1, 0, 0);
        assertEquals(false, bitTorrentDownloadService.hasPendingAbortActions());
        bitTorrentDownloadService.onStartCommand(downloadIntent2, 0, 0);
        assertEquals(false, bitTorrentDownloadService.hasPendingAbortActions());
        bitTorrentDownloadService.onStartCommand(abortIntent, 0, 0);
        assertEquals(true, bitTorrentDownloadService.hasPendingAbortActions());
        bitTorrentDownloadService.onHandleIntent(downloadIntent1);
        assertEquals(true, bitTorrentDownloadService.hasPendingAbortActions());
        bitTorrentDownloadService.onHandleIntent(downloadIntent2);
        assertEquals(true, bitTorrentDownloadService.hasPendingAbortActions());
        bitTorrentDownloadService.onHandleIntent(abortIntent);
        assertEquals(false, bitTorrentDownloadService.hasPendingAbortActions());
        bitTorrentDownloadService.onDestroy();
        assertEquals(false, bitTorrentDownloadService.hasPendingAbortActions());
        Mockito.verify(
                downloader,
                Mockito.times(1)).setDownloadListener(Mockito.any(DownloadListener.class));
        Mockito.verify(
                downloader,
                Mockito.times(1)).download(torrentFile1, destinationDirectory1);
        Mockito.verify(
                downloader,
                Mockito.times(1)).download(torrentFile2, destinationDirectory2);
    }

    @Test
    public void testBroadcastProgress() {
        final String torrentFile = "file";
        final int progress = 42;
        BitTorrentDownloadService bitTorrentDownloadService = new BitTorrentDownloadService();
        bitTorrentDownloadService.onCreate();
        bitTorrentDownloadService.broadcastProgress(torrentFile, progress);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(RuntimeEnvironment.application);
        ShadowLocalBroadcastManager shadowLocalBroadcastManager = Shadows.shadowOf(localBroadcastManager);
        List<Intent> broadcastIntents = shadowLocalBroadcastManager.getSentBroadcastIntents();
        assertEquals(1, broadcastIntents.size());
        assertEquals(BitTorrentIntentConstants.ACTION_BROADCAST, broadcastIntents.get(0).getAction());
        assertEquals(torrentFile, broadcastIntents.get(0).getStringExtra(BitTorrentIntentConstants.EXTRA_TORRENT_FILE));
        assertEquals(progress, broadcastIntents.get(0).getIntExtra(BitTorrentIntentConstants.EXTRA_DOWNLOAD_PROGRESS, -1));
        bitTorrentDownloadService.onDestroy();
    }

    @Test
    public void testBroadcastEnd() {
        final String torrentFile = "file";
        final int state = 42;
        BitTorrentDownloadService bitTorrentDownloadService = new BitTorrentDownloadService();
        bitTorrentDownloadService.onCreate();
        bitTorrentDownloadService.broadcastEnd(torrentFile, state);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(RuntimeEnvironment.application);
        ShadowLocalBroadcastManager shadowLocalBroadcastManager = Shadows.shadowOf(localBroadcastManager);
        List<Intent> broadcastIntents = shadowLocalBroadcastManager.getSentBroadcastIntents();
        assertEquals(1, broadcastIntents.size());
        assertEquals(BitTorrentIntentConstants.ACTION_BROADCAST, broadcastIntents.get(0).getAction());
        assertEquals(torrentFile, broadcastIntents.get(0).getStringExtra(BitTorrentIntentConstants.EXTRA_TORRENT_FILE));
        assertEquals(state, broadcastIntents.get(0).getIntExtra(BitTorrentIntentConstants.EXTRA_DOWNLOAD_STATE, -1));
        bitTorrentDownloadService.onDestroy();
    }
}
