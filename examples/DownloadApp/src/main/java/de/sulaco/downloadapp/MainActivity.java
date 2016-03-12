package de.sulaco.downloadapp;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

import de.sulaco.ttorrent.android.service.BitTorrentDownloadService;
import de.sulaco.ttorrent.android.service.BitTorrentIntentConstants;
import de.sulaco.ttorrent.utils.DownloadState;

public class MainActivity extends Activity {

    public static String TAG = "MainActivity";

    private static int REQUEST_CODE_PICK_FILE = 42;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button) findViewById(R.id.btn_select_torrent);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                pickFile();
            }
        });

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
            String torrentFile = data.getData().getPath();
            downloadBitTorrentFile(torrentFile);
        }
    }

    private void downloadBitTorrentFile(String torrentFile) {

        Toast.makeText(this, "Download " + torrentFile,
                Toast.LENGTH_LONG).show();

        String destination = new File(torrentFile).getParent();

        // The filter's action is ACTION_BROADCAST
        IntentFilter mStatusIntentFilter = new IntentFilter(
                BitTorrentIntentConstants.ACTION_BROADCAST);

        ResponseReceiver mDownloadStateReceiver = new ResponseReceiver();

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mDownloadStateReceiver,
                mStatusIntentFilter);

        BitTorrentDownloadService.requestDownload(
                this,
                torrentFile,
                destination);
    }

    private class ResponseReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {

            if (!intent.getAction().equals(BitTorrentIntentConstants.ACTION_BROADCAST)) {
                return;
            }

            int downloadState = intent.getIntExtra(BitTorrentIntentConstants.EXTRA_DOWNLOAD_STATE, -1);
            int progress = intent.getIntExtra(BitTorrentIntentConstants.EXTRA_DOWNLOAD_PROGRESS, -1);

            Log.d(TAG, "onReceive state=" + downloadState + "  " + progress + "%");
        }
    }

}
