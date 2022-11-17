package com.zebra.ssmfilepersist;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SSMFileProcessor extends AppCompatActivity {
    private static final String TAG = "SSMFileProcessor : ";
    Spinner persisFlagSpinner;
    Context mContext = null;
    TextView resultView;

    private final String AUTHORITY_FILE = "content://com.zebra.securestoragemanager.securecontentprovider/files/";
    private final String RETRIEVE_AUTHORITY = "content://com.zebra.securestoragemanager.securecontentprovider/file/*";
    private final String COLUMN_DATA_NAME = "data_name";
    private final String COLUMN_DATA_VALUE = "data_value";
    private final String COLUMN_DATA_TYPE = "data_type";
    private final String COLUMN_DATA_PERSIST_REQUIRED = "data_persist_required";
    private final String COLUMN_TARGET_APP_PACKAGE = "target_app_package";

    private final String signature = "MIIC5DCCAcwCAQEwDQYJKoZIhvcNAQEFBQAwNzEWMBQGA1UEAwwNQW5kcm9pZCBEZWJ1ZzEQMA4GA1UECgwHQW5kcm9pZDELMAkGA1UEBhMCVVMwIBcNMjAxMjA4MDYzNjU0WhgPMjA1MDEyMDEwNjM2NTRaMDcxFjAUBgNVBAMMDUFuZHJvaWQgRGVidWcxEDAOBgNVBAoMB0FuZHJvaWQxCzAJBgNVBAYTAlVTMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsit7mll4gtsFMSHC1YAVZcev0dmZe8UKnFLALEobJg7mBeRYXQ900cQflBq0H2vwU7Vsm2uh+uEiGgf/MJUt9gGPMhrmKhlabtH+x0tUYWoZX68V0mfbaH0PDsBk1i1cBciqPG/ihYOavpQvFHxrLX/JvsPp9kAKJ625p9dcVrFNgO6wp/nwTcLzY4RtifrtlhxON9JP3fISVNEgxdi51RCieLCPHSCbDUtStAvNJ3sClS4ohmxn2mEU21IcoQv63LlMFpouCZKv+2lMp3LkPMW1Xw1X/I27VUzKpUmagp8Qounfc+tWa7mrcS/NPHsvI9kCRMox+2c6Xuy3sqza1wIDAQABMA0GCSqGSIb3DQEBBQUAA4IBAQCW+oBfzA11Io6Q8mnqkfYW/tfRCKf1kjoeOx8TadJQ3HvGMgCAd0ucVsQlhjWQqfPgACGsIWFlp9sU7zl7vpr39+5goMf19WaQrBS8aNEWJPRcn/UsPSHQtbeECObBwL6KqpdNzA3iXP/KbrQuNhI6wZaJ9k4sBX0G3G2qF7pEwoISukwHEp7dy7scTsI2CgQdC9ZFveposX6xDF2VsrU5anjKbCsFhBGWeTJuc/32RDwhX1PGBedwyylUUXVpVUTBDwSRIznPYBhRL1l/DtfNt9XvaV6NFJsl13wp2hicLdrXkMbyV7NlB41M4zbyBbYjDfpgYds1RpmYBcuSYiBB";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this.getApplicationContext();
        resultView = findViewById(R.id.result);

        ((EditText) findViewById(R.id.et_targetPath)).setText("com.zebra.ssmfilepersist/A.txt");

        initializePersistFlagSpinner();

    }

    private void initializePersistFlagSpinner() {
        persisFlagSpinner = findViewById(R.id.persistFlagSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.persist_flag, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        persisFlagSpinner.setAdapter(adapter);
    }

    /*--------- Inserting the file to SSM -----------*/
    public void onClickInsertFile(View view) {
        String sourcePath = ((EditText) findViewById(R.id.et_sourcePath)).getText().toString();
        String targetPath = ((EditText) findViewById(R.id.et_targetPath)).getText().toString();
        Log.i(TAG, "targetPath " + targetPath);
        Log.i(TAG, "sourcePath " + sourcePath);
        Log.i(TAG, "*********************************");
        File file = new File(sourcePath);
        Log.i(TAG, "file path " + file.getPath() + " lengh: " + file.length());
        if (!file.exists()) {
            Toast.makeText(mContext, "File does not exists in the source", Toast.LENGTH_SHORT).show();
        } else {
            Uri cpUriQuery = Uri.parse(AUTHORITY_FILE + mContext.getPackageName());
            Log.i(TAG, "authority  " + cpUriQuery.toString());

            try {
                ContentValues values = new ContentValues();
                values.put(COLUMN_TARGET_APP_PACKAGE, String.format("{\"pkgs_sigs\": [{\"pkg\":\"%s\",\"sig\":\"%s\"}]}", mContext.getPackageName(), signature));
                values.put(COLUMN_DATA_NAME, sourcePath);
                values.put(COLUMN_DATA_TYPE, "3");
                values.put(COLUMN_DATA_VALUE, targetPath);
                values.put(COLUMN_DATA_PERSIST_REQUIRED, persisFlagSpinner.getSelectedItem().toString());
                Uri createdRow = mContext.getContentResolver().insert(cpUriQuery, values);
                Log.i(TAG, "SSM Insert File: " + createdRow.toString());
                Toast.makeText(mContext, "File insert success", Toast.LENGTH_SHORT).show();
                resultView.setText("Query Result");
            } catch (Exception e) {
                Log.e(TAG, "SSM Insert File - error: " + e.getMessage() + "\n\n");
            }
            Log.i(TAG, "*********************************");
        }
    }

    /*--------------------- (File insert API, used source path as file provider uri)Un-comment this for file provider use case --------------------*/
    /*public void onClickInsertFile(View view) {
        String sourcePath = ((EditText)findViewById(R.id.et_sourcePath)).getText().toString();
        String targetPath = ((EditText)findViewById(R.id.et_targetPath)).getText().toString();
        Log.i(TAG, "targetPath "+  targetPath);
        Log.i(TAG, "sourcePath "+  sourcePath);
        Log.i(TAG,"*********************************");
        File file = new File(sourcePath);

        Uri contentUri = FileProvider.getUriForFile(mContext, mContext.getPackageName() + ".provider", file);
        mContext.getApplicationContext().grantUriPermission("com.zebra.securestoragemanager", contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Log.i(TAG, "File Content Uri "+  contentUri);

        Uri cpUriQuery = Uri.parse(AUTHORITY_FILE + mContext.getPackageName());
        Log.i(TAG, "authority  "+  cpUriQuery.toString());

        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_TARGET_APP_PACKAGE, String.format("{\"pkgs_sigs\": [{\"pkg\":\"%s\",\"sig\":\"%s\"}]}", mContext.getPackageName(), signature));
            values.put(COLUMN_DATA_NAME, String.valueOf(contentUri)); // passing the content uri as a input source
            values.put(COLUMN_DATA_TYPE,"3");
            values.put(COLUMN_DATA_VALUE, targetPath);
            values.put(COLUMN_DATA_PERSIST_REQUIRED, persisFlagSpinner.getSelectedItem().toString());
            Uri createdRow  = mContext.getContentResolver().insert(cpUriQuery, values);
            Log.i(TAG, "SSM Insert File: " + createdRow.toString());
            Toast.makeText(mContext, "File insert success", Toast.LENGTH_SHORT).show();
            resultView.setText("Query Result");
        } catch(Exception e){
            Log.e(TAG, "SSM Insert File - error: " + e.getMessage() + "\n\n");
        }
        Log.i(TAG,"*********************************");
    }*/

    /*-------------------- Query file from SSM --------------------------------*/
    @SuppressLint("Range")
    public void onClickQueryFile(View view) {
        Uri uriFile = Uri.parse(RETRIEVE_AUTHORITY);
        String selection = COLUMN_TARGET_APP_PACKAGE + " = '" + mContext.getPackageName() + "'" + " AND " + COLUMN_DATA_PERSIST_REQUIRED + " = '" + persisFlagSpinner.getSelectedItem().toString() + "'";
        Log.i(TAG, "File selection " + selection);
        Log.i(TAG, "File cpUriQuery " + uriFile.toString());

        Cursor cursor = null;
        try {
            Log.i(TAG, "Before calling query API Time");
            cursor = getContentResolver().query(uriFile, null, selection, null, null);
            Log.i(TAG, "After query API called TIme");
        } catch (Exception e) {
            Log.d(TAG, "Error: " + e.getMessage());
        }
        try {
            if (cursor != null && cursor.moveToFirst()) {
                StringBuilder strBuild = new StringBuilder();
                String uriString;
                while (!cursor.isAfterLast()) {
                    uriString = cursor.getString(cursor.getColumnIndex("secure_file_uri"));
                    String fileName = cursor.getString(cursor.getColumnIndex("secure_file_name"));
                    String isDir = cursor.getString(cursor.getColumnIndex("secure_is_dir"));
                    String crc = cursor.getString(cursor.getColumnIndex("secure_file_crc"));
                    strBuild.append("\n");
                    strBuild.append("URI - " + uriString).append("\n").append("FileName - " + fileName).append("\n").append("IS Directory - " + isDir)
                            .append("\n").append("CRC - " + crc).append("\n").append("FileContent - ").append(readFile(mContext, uriString));
                    Log.i(TAG, "File cursor " + strBuild);
                    strBuild.append("\n ----------------------").append("\n");
                    cursor.moveToNext();
                }
                Log.d(TAG, "Query File: " + strBuild);
                Log.d("Client - Query", "Set test to view =  " + System.currentTimeMillis());
                resultView.setText(strBuild);
            } else {
                resultView.setText("No files to query");
            }
        } catch (Exception e) {
            Log.d(TAG, "Files query data error: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private String readFile(Context context, String uriString) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(Uri.parse(uriString));
        InputStreamReader isr = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line);
        }
        Log.d(TAG, "full content = " + sb);
        return sb.toString();
    }

    /*-------------------- Update file in SSM --------------------*/
    public void onClickUpdateFile(View view) {
        Uri cpUriQuery = Uri.parse(AUTHORITY_FILE + mContext.getPackageName());
        String sourcePath = ((EditText) findViewById(R.id.et_sourcePath)).getText().toString();
        String targetPath = ((EditText) findViewById(R.id.et_targetPath)).getText().toString();
        File sourceFilePath = new File(sourcePath);
        if (!sourceFilePath.exists()) {
            Toast.makeText(mContext, "File update failed: file does not exists", Toast.LENGTH_SHORT).show();
        } else {
            try {
                ContentValues values = new ContentValues();
                values.put(COLUMN_TARGET_APP_PACKAGE, String.format("{\"pkgs_sigs\": [{\"pkg\":\"%s\",\"sig\":\"%s\"}]}", mContext.getPackageName(), signature));
                values.put(COLUMN_DATA_NAME, sourcePath);
                values.put(COLUMN_DATA_TYPE, "1");
                values.put(COLUMN_DATA_VALUE, targetPath);
                values.put(COLUMN_DATA_PERSIST_REQUIRED, persisFlagSpinner.getSelectedItem().toString());
                int rowNumbers = getContentResolver().update(cpUriQuery, values, null, null);
                Log.d(TAG, "Files updated: " + rowNumbers);
                resultView.setText("Query Result");
            } catch (Exception e) {
                Log.d(TAG, "onClickFileUpdate - error: " + e.getMessage());
            }
        }
    }

    /*------------------- Delete file from SSM --------------------*/
    public void onClickDeleteFile(View view) {
        Uri cpUriQuery = Uri.parse(AUTHORITY_FILE + mContext.getPackageName());
        try {
            String whereClause = COLUMN_TARGET_APP_PACKAGE + " = '" + mContext.getPackageName() + "'" + " AND " + COLUMN_DATA_PERSIST_REQUIRED + " = '" + persisFlagSpinner.getSelectedItem().toString() + "'";
            int deleteStatus = getContentResolver().delete(cpUriQuery, whereClause, null);
            Log.d(TAG, "File deleted, status = " + deleteStatus);
            resultView.setText("File deleted");
        } catch (Exception e) {
            Log.d(TAG, "Delete file - error: " + e.getMessage());
        }
    }
}
