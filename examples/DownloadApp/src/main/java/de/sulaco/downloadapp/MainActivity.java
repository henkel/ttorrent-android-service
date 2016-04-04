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

package de.sulaco.downloadapp;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

import de.sulaco.bittorrent.BitTorrentDownloadManager;
import de.sulaco.bittorrent.DownloadListener;
import de.sulaco.bittorrent.service.DownloadRequest;
import de.sulaco.bittorrent.DownloadState;

public class MainActivity extends Activity implements DownloadListener {
    public static String TAG = "MainActivity";
    private static int REQUEST_CODE_PICK_FILE = 42;
    BitTorrentDownloadManager bitTorrentDownloadManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bitTorrentDownloadManager = new BitTorrentDownloadManager(this);

        Button select = (Button) findViewById(R.id.btn_select_torrent);
        select.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                pickFile();
            }
        });

        Button cancel = (Button) findViewById(R.id.btn_cancel_download);
        cancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                bitTorrentDownloadManager.abort();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        bitTorrentDownloadManager.registerDownloadListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        bitTorrentDownloadManager.unregisterDownloadListener(this);
    }

    private void pickFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");

        try {
            startActivityForResult(intent, REQUEST_CODE_PICK_FILE);
        } catch (ActivityNotFoundException e) {
            // No compatible file manager was found.
            Toast.makeText(this, R.string.no_filemanager_installed,
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_FILE && resultCode == RESULT_OK) {
            downloadBitTorrentFile(data.getData());
        }
    }

    private void downloadBitTorrentFile(Uri torrentFile) {
        Toast.makeText(this, "Selected " + torrentFile, Toast.LENGTH_SHORT).show();
        String destination = new File(torrentFile.getPath()).getParent();
        DownloadRequest request = new DownloadRequest()
                .setTorrentFile(torrentFile)
                .setDestinationDirectory(Uri.parse(destination));
        bitTorrentDownloadManager.enqueue(request);
    }

    @Override
    public void onDownloadStart(String torrentFile) {
        String msg = "onDownloadStart " + torrentFile;
        Log.d(TAG, msg);
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDownloadProgress(String torrentFile, int progress) {
        Log.d(TAG, "onDownloadProgress " + torrentFile + " progress=" + progress + "%");
    }

    @Override
    public void onDownloadEnd(String torrentFile, int downloadState) {
        String msg = "onDownloadEnd  "+ torrentFile +" downloadState=" + downloadStatetoString(downloadState);
        Log.d(TAG, msg);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    static String downloadStatetoString(int downloadState) {
        switch (downloadState) {
            case DownloadState.COMPLETED:
                return "COMPLETED";
            case DownloadState.ABORTED:
                return "ABORTED";
            case DownloadState.TIMED_OUT:
                return "TIMED_OUT";
            case DownloadState.ERROR:
                return "ERROR";
            case DownloadState.ERROR_TORRENT_FILE:
                return "ERROR_TORRENT_FILE";
            case DownloadState.ERROR_DESTINATION_DIR:
                return "ERROR_DESTINATION_DIR";
            default:
                throw new IllegalArgumentException("invalid downloadState");
        }
    }
}
