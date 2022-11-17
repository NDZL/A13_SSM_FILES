package com.zebra.ssmfilepersist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ConfigFileReceiver extends BroadcastReceiver {

    static final String TAG = ConfigFileReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Inside onReceive, action =" + intent.getAction());
        if (intent.getAction().equals("com.zebra.configFile.action.notify")) {
            Bundle bundle = intent.getExtras();
            String fileUri = bundle.getString("secure_file_uri");
            String fileName = bundle.getString("secure_file_name");
            String isDir = bundle.getString("secure_is_dir");
            String crc = bundle.getString("secure_file_crc");
            String isPersist = bundle.getString("secure_file_persist");

            Log.d(TAG, "file_uri = " + fileUri);
            Log.d(TAG, "fileName = " + fileName);
            Log.d(TAG, "isDir = " + isDir);
            Log.d(TAG, "crc = " + crc);
            Log.d(TAG, "isPersist = " + isPersist);

            if (!Boolean.parseBoolean(isDir)) {
                if (!readFile(context, fileUri)) {
                    Log.d(TAG, "Inside onReceive - readFile failed");
                }
            }
        }
    }

    private boolean readFile(Context context, String fileUri) {
        try {
            if (fileUri.isEmpty()) {
                Log.d(TAG, "readFile - uri is empty");
                return false;
            } else {
                readFileContent(context, fileUri);
            }
        } catch (Exception e) {
            Log.d(TAG, "readFile , Exception = " + e.getLocalizedMessage());
            return false;
        }
        return true;
    }

    private void readFileContent(Context context, String uriString) throws IOException {

        InputStream inputStream = context.getContentResolver().openInputStream(Uri.parse(uriString));

        InputStreamReader isr = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line);
        }
        Log.d(TAG, "full content = " + sb.toString());
        sb.toString();
    }
}
