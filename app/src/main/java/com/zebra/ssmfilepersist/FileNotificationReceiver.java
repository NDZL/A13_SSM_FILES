package com.zebra.ssmfilepersist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class FileNotificationReceiver extends BroadcastReceiver  {

    @Override
    public void onReceive(final Context context, Intent intent) {

        if (intent != null && intent.getAction().equals("com.zebra.configFile.action.notify")) {

            Bundle extras = intent.getExtras();

            if(extras != null && !extras.isEmpty()) {
                String secure_file_uri = extras.getString("secure_file_uri");
                String secure_file_name = extras.getString("secure_file_name");
                String secure_is_dir = extras.getString("secure_is_dir");
                String secure_file_crc = extras.getString("secure_file_crc");
                String secure_file_persist = extras.getString("secure_file_persist");
            }
        }
    }
}