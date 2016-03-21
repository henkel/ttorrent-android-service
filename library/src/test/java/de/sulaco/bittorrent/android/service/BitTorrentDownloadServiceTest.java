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
import org.mockito.Mockito;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import de.sulaco.bittorrent.DownloadListener;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class BitTorrentDownloadServiceTest {

    @Test
    public void testConstructionDestruction() {
        Downloader downloader = Mockito.mock(Downloader.class);
        BitTorrentDownloadService bitTorrentDownloadService = new BitTorrentDownloadService();
        bitTorrentDownloadService.setDownloader(downloader);
        bitTorrentDownloadService.onCreate();
        assertThat(bitTorrentDownloadService.isAbortPending()).isFalse();
        bitTorrentDownloadService.onDestroy();
        Mockito.verify(downloader, Mockito.times(1)).setDownloadListener(Mockito.any(DownloadListener.class));
        Mockito.verify(downloader, Mockito.times(0)).download(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(downloader, Mockito.times(0)).setEnabled(false);
    }

    @Test
    public void testDownload() {
        final String torrentFile = "file";
        final String destinationDirectory = "dir";
        Downloader downloader = Mockito.mock(Downloader.class);
        BitTorrentDownloadService bitTorrentDownloadService = new BitTorrentDownloadService();
        bitTorrentDownloadService.setDownloader(downloader);
        bitTorrentDownloadService.onCreate();
        Intent downloadIntent = new DownloadRequest()
                .setTorrentFile(Uri.parse(torrentFile))
                .setDestinationDirectory(Uri.parse(destinationDirectory))
                .createIntent(RuntimeEnvironment.application);
        assertThat(bitTorrentDownloadService.isAbortPending()).isFalse();
        bitTorrentDownloadService.onStartCommand(downloadIntent, 0, 0);
        bitTorrentDownloadService.onHandleIntent(downloadIntent);
        assertThat(bitTorrentDownloadService.isAbortPending()).isFalse();
        bitTorrentDownloadService.onDestroy();
        Mockito.verify(downloader, Mockito.times(1)).setDownloadListener(Mockito.any(DownloadListener.class));
        Mockito.verify(downloader, Mockito.times(1)).download(torrentFile, destinationDirectory);
        Mockito.verify(downloader, Mockito.times(0)).setEnabled(false);
    }

    @Test
    public void testAbort() {
        Downloader downloader = Mockito.mock(Downloader.class);
        BitTorrentDownloadService bitTorrentDownloadService = new BitTorrentDownloadService();
        bitTorrentDownloadService.setDownloader(downloader);
        bitTorrentDownloadService.onCreate();
        Intent abortIntent = BitTorrentDownloadService.createAbortIntent(RuntimeEnvironment.application);
        assertThat(bitTorrentDownloadService.isAbortPending()).isFalse();
        bitTorrentDownloadService.onStartCommand(abortIntent, 0, 0);
        assertThat(bitTorrentDownloadService.isAbortPending()).isTrue();
        bitTorrentDownloadService.onHandleIntent(abortIntent);
        assertThat(bitTorrentDownloadService.isAbortPending()).isFalse();
        bitTorrentDownloadService.onDestroy();
        Mockito.verify(downloader, Mockito.times(1)).setDownloadListener(Mockito.any(DownloadListener.class));
        Mockito.verify(downloader, Mockito.times(1)).setEnabled(false);
        Mockito.verify(downloader, Mockito.times(0)).download(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testDownloadDownloadAndAbortBeforeDownloadStarts() {
        final String torrentFileOne = "file1";
        final String destinationDirectoryOne = "dir1";
        final String torrentFileTwo = "file2";
        final String destinationDirectoryTwo = "dir2";
        Downloader downloader = Mockito.mock(Downloader.class);
        BitTorrentDownloadService bitTorrentDownloadService = new BitTorrentDownloadService();
        bitTorrentDownloadService.setDownloader(downloader);
        bitTorrentDownloadService.onCreate();
        Intent downloadIntentOne = new DownloadRequest()
                .setTorrentFile(Uri.parse(torrentFileOne))
                .setDestinationDirectory(Uri.parse(destinationDirectoryOne))
                .createIntent(RuntimeEnvironment.application);
        Intent downloadIntentTwo = new DownloadRequest()
                .setTorrentFile(Uri.parse(torrentFileTwo))
                .setDestinationDirectory(Uri.parse(destinationDirectoryTwo))
                .createIntent(RuntimeEnvironment.application);
        Intent abortIntent = BitTorrentDownloadService.createAbortIntent(
                RuntimeEnvironment.application);
        assertThat(bitTorrentDownloadService.isAbortPending()).isFalse();
        bitTorrentDownloadService.onStartCommand(downloadIntentOne, 0, 0);
        assertThat(bitTorrentDownloadService.isAbortPending()).isFalse();
        bitTorrentDownloadService.onStartCommand(downloadIntentTwo, 0, 0);
        assertThat(bitTorrentDownloadService.isAbortPending()).isFalse();
        bitTorrentDownloadService.onStartCommand(abortIntent, 0, 0);
        assertThat(bitTorrentDownloadService.isAbortPending()).isTrue();
        bitTorrentDownloadService.onHandleIntent(downloadIntentOne);
        assertThat(bitTorrentDownloadService.isAbortPending()).isTrue();
        bitTorrentDownloadService.onHandleIntent(downloadIntentTwo);
        assertThat(bitTorrentDownloadService.isAbortPending()).isTrue();
        bitTorrentDownloadService.onHandleIntent(abortIntent);
        assertThat(bitTorrentDownloadService.isAbortPending()).isFalse();
        bitTorrentDownloadService.onDestroy();
        Mockito.verify(downloader, Mockito.times(1)).setDownloadListener(Mockito.any(DownloadListener.class));
        Mockito.verify(downloader, Mockito.times(0)).download(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(downloader, Mockito.times(1)).setEnabled(false);
        Mockito.verify(downloader, Mockito.atLeast(1)).setEnabled(true);
    }

    @Test
    public void testDownloadDownloadAndAbortAfterFirstDownload() {
        final String torrentFileOne = "file1";
        final String destinationDirectoryOne = "dir1";
        final String torrentFileTwo = "file2";
        final String destinationDirectoryTwo = "dir2";
        Downloader downloader = Mockito.mock(Downloader.class);
        BitTorrentDownloadService bitTorrentDownloadService = new BitTorrentDownloadService();
        bitTorrentDownloadService.setDownloader(downloader);
        bitTorrentDownloadService.onCreate();
        Intent downloadIntentOne = new DownloadRequest()
                .setTorrentFile(Uri.parse(torrentFileOne))
                .setDestinationDirectory(Uri.parse(destinationDirectoryOne))
                .createIntent(RuntimeEnvironment.application);
        Intent downloadIntentTwo = new DownloadRequest()
                .setTorrentFile(Uri.parse(torrentFileTwo))
                .setDestinationDirectory(Uri.parse(destinationDirectoryTwo))
                .createIntent(RuntimeEnvironment.application);
        Intent abortIntent = BitTorrentDownloadService.createAbortIntent(
                RuntimeEnvironment.application);
        assertThat(bitTorrentDownloadService.isAbortPending()).isFalse();
        bitTorrentDownloadService.onStartCommand(downloadIntentOne, 0, 0);
        assertThat(bitTorrentDownloadService.isAbortPending()).isFalse();
        bitTorrentDownloadService.onStartCommand(downloadIntentTwo, 0, 0);
        assertThat(bitTorrentDownloadService.isAbortPending()).isFalse();
        bitTorrentDownloadService.onHandleIntent(downloadIntentOne);
        assertThat(bitTorrentDownloadService.isAbortPending()).isFalse();
        bitTorrentDownloadService.onStartCommand(abortIntent, 0, 0);
        assertThat(bitTorrentDownloadService.isAbortPending()).isTrue();
        bitTorrentDownloadService.onHandleIntent(downloadIntentTwo);
        assertThat(bitTorrentDownloadService.isAbortPending()).isTrue();
        bitTorrentDownloadService.onHandleIntent(abortIntent);
        assertThat(bitTorrentDownloadService.isAbortPending()).isFalse();
        bitTorrentDownloadService.onDestroy();
        Mockito.verify(downloader, Mockito.times(1)).setDownloadListener(Mockito.any(DownloadListener.class));
        Mockito.verify(downloader, Mockito.times(1)).download(torrentFileOne, destinationDirectoryOne);
        Mockito.verify(downloader, Mockito.times(0)).download(torrentFileTwo, destinationDirectoryTwo);
        Mockito.verify(downloader, Mockito.times(1)).setEnabled(false);
        Mockito.verify(downloader, Mockito.atLeast(1)).setEnabled(true);
    }

    @Test
    public void testDownloadAbortDownload() {
        final String torrentFileOne = "file1";
        final String destinationDirectoryOne = "dir1";
        final String torrentFileTwo = "file2";
        final String destinationDirectoryTwo = "dir2";
        Downloader downloader = Mockito.mock(Downloader.class);
        BitTorrentDownloadService bitTorrentDownloadService = new BitTorrentDownloadService();
        bitTorrentDownloadService.setDownloader(downloader);
        bitTorrentDownloadService.onCreate();
        Intent downloadIntentOne = new DownloadRequest()
                .setTorrentFile(Uri.parse(torrentFileOne))
                .setDestinationDirectory(Uri.parse(destinationDirectoryOne))
                .createIntent(RuntimeEnvironment.application);
        Intent downloadIntentTwo = new DownloadRequest()
                .setTorrentFile(Uri.parse(torrentFileTwo))
                .setDestinationDirectory(Uri.parse(destinationDirectoryTwo))
                .createIntent(RuntimeEnvironment.application);
        Intent abortIntent = BitTorrentDownloadService.createAbortIntent(
                RuntimeEnvironment.application);
        assertThat(bitTorrentDownloadService.isAbortPending()).isFalse();
        bitTorrentDownloadService.onStartCommand(downloadIntentOne, 0, 0);
        assertThat(bitTorrentDownloadService.isAbortPending()).isFalse();
        bitTorrentDownloadService.onStartCommand(abortIntent, 0, 0);
        assertThat(bitTorrentDownloadService.isAbortPending()).isTrue();
        bitTorrentDownloadService.onStartCommand(downloadIntentTwo, 0, 0);
        assertThat(bitTorrentDownloadService.isAbortPending()).isTrue();
        bitTorrentDownloadService.onHandleIntent(downloadIntentOne);
        assertThat(bitTorrentDownloadService.isAbortPending()).isTrue();
        bitTorrentDownloadService.onHandleIntent(abortIntent);
        assertThat(bitTorrentDownloadService.isAbortPending()).isFalse();
        bitTorrentDownloadService.onHandleIntent(downloadIntentTwo);
        assertThat(bitTorrentDownloadService.isAbortPending()).isFalse();
        bitTorrentDownloadService.onDestroy();
        Mockito.verify(downloader, Mockito.times(1)).setDownloadListener(Mockito.any(DownloadListener.class));
        Mockito.verify(downloader, Mockito.times(0)).download(torrentFileOne, destinationDirectoryOne);
        Mockito.verify(downloader, Mockito.times(1)).download(torrentFileTwo, destinationDirectoryTwo);
        Mockito.verify(downloader, Mockito.times(1)).setEnabled(false);
        Mockito.verify(downloader, Mockito.atLeast(1)).setEnabled(true);
    }

    @Test
    public void testAbortAbortDownload() {
        final String torrentFile = "file";
        final String destinationDirectory = "dir";
        Downloader downloader = Mockito.mock(Downloader.class);
        BitTorrentDownloadService bitTorrentDownloadService = new BitTorrentDownloadService();
        bitTorrentDownloadService.setDownloader(downloader);
        bitTorrentDownloadService.onCreate();
        Intent downloadIntent = new DownloadRequest()
                .setTorrentFile(Uri.parse(torrentFile))
                .setDestinationDirectory(Uri.parse(destinationDirectory))
                .createIntent(RuntimeEnvironment.application);
        Intent abortIntent = BitTorrentDownloadService.createAbortIntent(
                RuntimeEnvironment.application);
        assertThat(bitTorrentDownloadService.isAbortPending()).isFalse();
        bitTorrentDownloadService.onStartCommand(abortIntent, 0, 0);
        assertThat(bitTorrentDownloadService.isAbortPending()).isTrue();
        bitTorrentDownloadService.onStartCommand(abortIntent, 0, 0);
        assertThat(bitTorrentDownloadService.isAbortPending()).isTrue();
        bitTorrentDownloadService.onStartCommand(downloadIntent, 0, 0);
        assertThat(bitTorrentDownloadService.isAbortPending()).isTrue();
        bitTorrentDownloadService.onHandleIntent(abortIntent);
        assertThat(bitTorrentDownloadService.isAbortPending()).isTrue();
        bitTorrentDownloadService.onHandleIntent(abortIntent);
        assertThat(bitTorrentDownloadService.isAbortPending()).isFalse();
        bitTorrentDownloadService.onHandleIntent(downloadIntent);
        assertThat(bitTorrentDownloadService.isAbortPending()).isFalse();
        bitTorrentDownloadService.onDestroy();
        Mockito.verify(downloader, Mockito.times(1)).setDownloadListener(Mockito.any(DownloadListener.class));
        Mockito.verify(downloader, Mockito.times(1)).download(torrentFile, destinationDirectory);
        Mockito.verify(downloader, Mockito.atLeast(1)).setEnabled(false);
        Mockito.verify(downloader, Mockito.atLeast(1)).setEnabled(true);
    }

    @Test
    public void testHandleIntentWithUnknownAction() {
        Intent intent = new Intent(RuntimeEnvironment.application, BitTorrentDownloadService.class);
        intent.setAction(Intent.ACTION_VIEW);
        Downloader downloader = Mockito.mock(Downloader.class);
        BitTorrentDownloadService bitTorrentDownloadService = new BitTorrentDownloadService();
        bitTorrentDownloadService.setDownloader(downloader);
        bitTorrentDownloadService.onCreate();
        bitTorrentDownloadService.onStartCommand(intent, 0, 0);
        bitTorrentDownloadService.onHandleIntent(intent);
        bitTorrentDownloadService.onDestroy();
        Mockito.verify(downloader, Mockito.times(0)).download(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testCreateAbortIntent() {
        Intent intent = BitTorrentDownloadService.createAbortIntent(RuntimeEnvironment.application);
        assertThat(intent.getAction()).isEqualTo(BitTorrentIntentConstants.ACTION_ABORT_DOWNLOAD);
    }
}
