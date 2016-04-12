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

package de.sulaco.bittorrent.service.ttorrent;

import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.tracker.TrackedTorrent;
import com.turn.ttorrent.tracker.Tracker;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;

import de.sulaco.bittorrent.DownloadListener;
import de.sulaco.bittorrent.DownloadState;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ParameterizedRobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public final class TtorrentDownloaderTest {

    private final static int RUN_COUNT = 1;

    private static final String TORRENT_FILE = "src/test/resources/torrents/photo.torrent";
    private static final String CORRUPT_TORRENT_FILE = "src/test/resources/torrents/corrupt.torrent";
    private static final String TEMPORARY_DIRECTORY = "src/test/resources/temporary";
    private static final String CONTENT_DIRECTORY = "src/test/resources/content";
    private static final String CONTENT_NAME = "photo.jpg";
    private static final String CONTENT_FILE_PART = "src/test/resources/content_part/photo.jpg.part";


    @ParameterizedRobolectricTestRunner.Parameters(name = "run {index}/"+(RUN_COUNT-1))
    public static Collection data() {
        return Arrays.asList(new Object[RUN_COUNT][]);
    }

    public TtorrentDownloaderTest() {
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        ShadowLog.stream = System.out;
    }

    @Before
    public void createTempDirectory() throws IOException {
        FileUtils.forceMkdir(new File(TEMPORARY_DIRECTORY));
    }

    @After
    public void deleteTempDirectory() throws IOException {
        FileUtils.deleteDirectory(new File(TEMPORARY_DIRECTORY));
    }

    @Test
    public void testDownloadNullTorrentFile() {
        TtorrentDownloader ttorrentDownloader = new TtorrentDownloader();
        DownloadListener downloadListener = Mockito.mock(DownloadListener.class);
        ttorrentDownloader.setDownloadListener(downloadListener);
        ttorrentDownloader.download(null, "dir");
        Mockito.verify(downloadListener, Mockito.times(0)).onDownloadStart(Mockito.anyString());
    }

    @Test
    public void testDownloadNullDestinationDirectory() {
        TtorrentDownloader ttorrentDownloader = new TtorrentDownloader();
        DownloadListener downloadListener = Mockito.mock(DownloadListener.class);
        ttorrentDownloader.setDownloadListener(downloadListener);
        ttorrentDownloader.download(TORRENT_FILE, null);
        Mockito.verify(downloadListener, Mockito.times(1)).onDownloadStart(TORRENT_FILE);
        Mockito.verify(downloadListener, Mockito.times(1)).onDownloadEnd(TORRENT_FILE, DownloadState.ERROR_DESTINATION_DIR);
    }

    @Test
    public void testDownloadTorrentFileDoesNotExist() {
        final String torrentFile = "torrent_does_not_exist";
        TtorrentDownloader ttorrentDownloader = new TtorrentDownloader();
        DownloadListener downloadListener = Mockito.mock(DownloadListener.class);
        ttorrentDownloader.setDownloadListener(downloadListener);
        ttorrentDownloader.download(torrentFile, TEMPORARY_DIRECTORY);
        Mockito.verify(downloadListener, Mockito.times(1)).onDownloadStart(torrentFile);
        Mockito.verify(downloadListener, Mockito.times(1)).onDownloadEnd(torrentFile, DownloadState.ERROR_TORRENT_FILE);
    }

    @Test
    public void testDownloadDestinationDoesNotExist() {
        final String destinationDirectory = "destination_does_not_exist";
        TtorrentDownloader ttorrentDownloader = new TtorrentDownloader();
        DownloadListener downloadListener = Mockito.mock(DownloadListener.class);
        ttorrentDownloader.setDownloadListener(downloadListener);
        ttorrentDownloader.download(TORRENT_FILE, destinationDirectory);
        Mockito.verify(downloadListener, Mockito.times(1)).onDownloadStart(TORRENT_FILE);
        Mockito.verify(downloadListener, Mockito.times(0)).onDownloadProgress(Mockito.anyString(), Mockito.anyInt());
        Mockito.verify(downloadListener, Mockito.times(1)).onDownloadEnd(TORRENT_FILE, DownloadState.ERROR_DESTINATION_DIR);
    }

    @Test
    public void testDownloadDestinationNotWritable() {
        File directory = new File(TEMPORARY_DIRECTORY);
        assertThat(directory.setWritable(false)).isTrue();
        TtorrentDownloader ttorrentDownloader = new TtorrentDownloader();
        DownloadListener downloadListener = Mockito.mock(DownloadListener.class);
        ttorrentDownloader.setDownloadListener(downloadListener);
        ttorrentDownloader.download(TORRENT_FILE, TEMPORARY_DIRECTORY);
        assertThat(directory.setWritable(true)).isTrue();
        Mockito.verify(downloadListener, Mockito.times(1)).onDownloadStart(TORRENT_FILE);
        Mockito.verify(downloadListener, Mockito.times(0)).onDownloadProgress(Mockito.anyString(), Mockito.anyInt());
        Mockito.verify(downloadListener, Mockito.times(1)).onDownloadEnd(TORRENT_FILE, DownloadState.ERROR_DESTINATION_DIR);
    }

    @Test
    public void testDownloadResumeCompleted() {
        TtorrentDownloader ttorrentDownloader = new TtorrentDownloader();
        DownloadListener downloadListener = Mockito.mock(DownloadListener.class);
        ttorrentDownloader.setDownloadListener(downloadListener);
        ttorrentDownloader.download(TORRENT_FILE, CONTENT_DIRECTORY);
        Mockito.verify(downloadListener, Mockito.times(1)).onDownloadStart(TORRENT_FILE);
        Mockito.verify(downloadListener, Mockito.times(1)).onDownloadProgress(TORRENT_FILE, 100);
        Mockito.verify(downloadListener, Mockito.times(1)).onDownloadEnd(TORRENT_FILE, DownloadState.COMPLETED);
    }

    @Test
    public void testDownloadCorruptTorrentFile() {
        TtorrentDownloader ttorrentDownloader = new TtorrentDownloader();
        DownloadListener downloadListener = Mockito.mock(DownloadListener.class);
        ttorrentDownloader.setDownloadListener(downloadListener);
        ttorrentDownloader.download(CORRUPT_TORRENT_FILE, CONTENT_DIRECTORY);
        Mockito.verify(downloadListener, Mockito.times(1)).onDownloadStart(CORRUPT_TORRENT_FILE);
        Mockito.verify(downloadListener, Mockito.times(1)).onDownloadEnd(CORRUPT_TORRENT_FILE, DownloadState.ERROR_TORRENT_FILE);
    }

    @Test
    public void testDownloadNoTracker() {
        TtorrentDownloader ttorrentDownloader = new TtorrentDownloader();
        DownloadListener downloadListener = Mockito.mock(DownloadListener.class);
        ttorrentDownloader.setDownloadListener(downloadListener);
        ttorrentDownloader.setTimeout(10 * 1000);
        ttorrentDownloader.download(TORRENT_FILE, TEMPORARY_DIRECTORY);
        Mockito.verify(downloadListener, Mockito.times(1)).onDownloadStart(TORRENT_FILE);
        Mockito.verify(downloadListener, Mockito.times(0)).onDownloadProgress(Mockito.anyString(), Mockito.anyInt());
        Mockito.verify(downloadListener, Mockito.times(1)).onDownloadEnd(TORRENT_FILE, DownloadState.TIMED_OUT);
    }

    @Test
    public void testDownloadNoTrackerCancel() {
        final TtorrentDownloader ttorrentDownloader = new TtorrentDownloader();
        final DownloadListener downloadListener = Mockito.mock(DownloadListener.class);
        ttorrentDownloader.setDownloadListener(downloadListener);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                ttorrentDownloader.setEnabled(false);
            }
        }).start();
        ttorrentDownloader.download(TORRENT_FILE, TEMPORARY_DIRECTORY);
        Mockito.verify(downloadListener, Mockito.times(1)).onDownloadStart(TORRENT_FILE);
        Mockito.verify(downloadListener, Mockito.times(0)).onDownloadProgress(Mockito.anyString(), Mockito.anyInt());
        Mockito.verify(downloadListener, Mockito.times(1)).onDownloadEnd(TORRENT_FILE, DownloadState.ABORTED);
    }

    private Tracker startTracker() throws IOException, NoSuchAlgorithmException {
        Tracker tracker = new Tracker(new InetSocketAddress("127.0.0.1", 9876));
        tracker.announce(TrackedTorrent.load(new File(TORRENT_FILE)));
        tracker.start();
        return tracker;
    }

    private Client startSeeder() throws IOException, NoSuchAlgorithmException {
        Torrent torrent = Torrent.load(new File(TORRENT_FILE));
        File destination = new File(CONTENT_DIRECTORY);
        InetAddress addr = InetAddress.getLocalHost();
        Client client = new Client(addr, new SharedTorrent(torrent, destination));
        client.share();
        return client;
    }

    @Test
    public void testDownload() throws IOException, NoSuchAlgorithmException {
        // Start seeder and tracker
        Tracker tracker = startTracker();
        Client client = startSeeder();

        // Download file
        DownloadListener downloadListener = Mockito.mock(DownloadListener.class);
        TtorrentDownloader ttorrentDownloader = new TtorrentDownloader();
        ttorrentDownloader.setDownloadListener(downloadListener);
        ttorrentDownloader.setTimeout(60 * 1000);
        ttorrentDownloader.download(TORRENT_FILE, TEMPORARY_DIRECTORY);

        // Verification
        Mockito.verify(downloadListener, Mockito.times(1)).onDownloadStart(TORRENT_FILE);
        Mockito.verify(downloadListener, Mockito.times(1)).onDownloadProgress(TORRENT_FILE, 100);
        Mockito.verify(downloadListener, Mockito.times(1)).onDownloadEnd(TORRENT_FILE, DownloadState.COMPLETED);
        File source = new File(CONTENT_DIRECTORY + File.separator + CONTENT_NAME);
        File result = new File(TEMPORARY_DIRECTORY + File.separator + CONTENT_NAME);
        assertThat(result.exists()).isTrue();
        assertThat(FileUtils.contentEquals(source, result)).isTrue();

        // Tear down seeder and tracker
        client.stop();
        tracker.stop();
    }

    @Test
    public void testDownloadWithoutDownloadListener()  throws IOException, NoSuchAlgorithmException {
        // Start seeder and tracker
        Tracker tracker = startTracker();
        Client client = startSeeder();

        // Download file
        TtorrentDownloader ttorrentDownloader = new TtorrentDownloader();
        ttorrentDownloader.setTimeout(60 * 1000);
        ttorrentDownloader.download(TORRENT_FILE, TEMPORARY_DIRECTORY);

        // Verification
        File source = new File(CONTENT_DIRECTORY + File.separator + CONTENT_NAME);
        File result = new File(TEMPORARY_DIRECTORY + File.separator + CONTENT_NAME);
        assertThat(result.exists()).isTrue();
        assertThat(FileUtils.contentEquals(source, result)).isTrue();

        // Tear down seeder and tracker
        client.stop();
        tracker.stop();
    }

    @Test
    public void testDownloadResume() throws IOException, NoSuchAlgorithmException {
        // Start seeder and tracker
        Tracker tracker = startTracker();
        Client client = startSeeder();

        // Copy partly available file to download directory
        FileUtils.copyFileToDirectory(new File(CONTENT_FILE_PART), new File(TEMPORARY_DIRECTORY));
        File partFile = new File(CONTENT_FILE_PART);
        File sourceFile = new File(CONTENT_DIRECTORY + File.separator + CONTENT_NAME);
        assertThat(FileUtils.contentEquals(partFile, sourceFile)).isFalse();

        // Download file
        DownloadListener downloadListener = Mockito.mock(DownloadListener.class);
        TtorrentDownloader ttorrentDownloader = new TtorrentDownloader();
        ttorrentDownloader.setDownloadListener(downloadListener);
        ttorrentDownloader.setTimeout(60 * 1000);
        ttorrentDownloader.download(TORRENT_FILE, TEMPORARY_DIRECTORY);

        // Verification
        Mockito.verify(downloadListener, Mockito.times(1)).onDownloadEnd(TORRENT_FILE, DownloadState.COMPLETED);
        Mockito.verify(downloadListener, Mockito.times(1)).onDownloadStart(TORRENT_FILE);
        Mockito.verify(downloadListener, Mockito.times(1)).onDownloadProgress(TORRENT_FILE, 100);
        File downloadFile = new File(TEMPORARY_DIRECTORY + File.separator + CONTENT_NAME);
        assertThat(downloadFile.exists()).isTrue();
        assertThat(FileUtils.contentEquals(sourceFile, downloadFile)).isTrue();

        // Tear down seeder and tracker
        client.stop();
        tracker.stop();
    }

}