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

import com.turn.ttorrent.core.BuildConfig;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;


@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class BitTorrentDownloadServiceAdapterTest extends TestCase {

    @Test
    public void testConstructionWithNull() {
        try {
            new DownloadServiceAdapter(null);
            fail("NullPointerException expected");
        } catch (NullPointerException expectedException) {
        }
    }

    @Test
    public void testNotifyStart() {
        final String torrentFile = "file";
        BitTorrentDownloadService bitTorrentDownloadService = Mockito.mock(BitTorrentDownloadService.class);
        DownloadServiceAdapter downloadServiceAdapter = new DownloadServiceAdapter(bitTorrentDownloadService);
        downloadServiceAdapter.onDownloadStart(torrentFile);
        Mockito.verify(bitTorrentDownloadService, Mockito.times(1)).broadcastProgress(torrentFile, 0);
    }

    @Test
    public void testNotifyProgress() {
        final String torrentFile = "file";
        final int progress = 42;
        BitTorrentDownloadService bitTorrentDownloadService = Mockito.mock(BitTorrentDownloadService.class);
        DownloadServiceAdapter downloadServiceAdapter = new DownloadServiceAdapter(bitTorrentDownloadService);
        downloadServiceAdapter.onDownloadProgressUpdate(torrentFile, progress);
        Mockito.verify(bitTorrentDownloadService, Mockito.times(1)).broadcastProgress(torrentFile, progress);
    }

    @Test
    public void testNotifyEnd() {
        final String torrentFile = "file";
        final int state = 42;
        BitTorrentDownloadService bitTorrentDownloadService = Mockito.mock(BitTorrentDownloadService.class);
        DownloadServiceAdapter downloadServiceAdapter = new DownloadServiceAdapter(bitTorrentDownloadService);
        downloadServiceAdapter.onDownloadEnd(torrentFile, state);
        Mockito.verify(bitTorrentDownloadService, Mockito.times(1)).broadcastEnd(torrentFile, state);
    }
}