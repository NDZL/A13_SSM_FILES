package com.zebra.ssmfilepersist;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.format.DateFormat;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class SSMFileProcessor extends AppCompatActivity {
    public static final String TAG = "SSMFileProcessor";
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

    //read at the end of the next line!
    private final String signature = "";
    //private final String signature = "MIIC5DCCAcwCAQEwDQYJKoZIhvcNAQEFBQAwNzEWMBQGA1UEAwwNQW5kcm9pZCBEZWJ1ZzEQMA4GA1UECgwHQW5kcm9pZDELMAkGA1UEBhMCVVMwIBcNMjIxMDEyMDk1NjM5WhgPMjA1MjEwMDQwOTU2MzlaMDcxFjAUBgNVBAMMDUFuZHJvaWQgRGVidWcxEDAOBgNVBAoMB0FuZHJvaWQxCzAJBgNVBAYTAlVTMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAha0WrWTMN8Yh40qZ9dX9f2FvyWwpdX8T0L8khTCs6bydqbKVSQOoe28g2y9uA5lbhor4nRstJwVY39TKC6v3ESQHIw2ESxoxU6oMVyqxKlOw48mW3fBGVH8A220Zm0Yo4ibmH4E61ahg5sbdxq2cfoizxCfDuRE7+78/kn/na6CdTH9vBlJ8MJpv587jsV78OI7+vT7bnd7PRmx8D3vxsEfdw0BzPA/C+hovy3y2jMFUe36wXZEn4hdIxqAIngeemFabEyAj5ViSvX6LcPdgUmlcrTyapz0QkjpJHrvOkBXwtwCntAESvVIJHkYnZHgMLSXml6MLlklybQGzGOrPRQIDAQABMA0GCSqGSIb3DQEBBQUAA4IBAQAE80M6+8TrJW/74A1DFkdE21ZetggUc47WG1U5R5HBw+6BLdHy/MZtyN1H9eL3TIICPuL4QXR6BCEp/iWzRwjukopwmwFhzCo2IgKmQpidkaFSdLutETwtp04L3PaXjbVxeGkhMVkYDjtbB6xbZx/ioShQ+bKvbmNOQxNdktyCvcx7s8BhzWtcPPmzYSFt0DEk2n4br2yWf9VUQBKgbjJpo/yoKWrCbb4Wu/WtHGOXGNy2r0FLkiocWHL7liGtAN+rpo0wRZtPoPYxxikqUY+ZOu4rXDu1WeLgbrpJjT84PKO/BJ8zfTD0F2nGZZaz3HjBikEjXxsoziZ/axBdfhmJ"; //COMPANION APP SIGNATURE

    LocalContentObserver myContentObserver;
    LocalDataSetObserver myDataSetObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this.getApplicationContext();
        resultView = findViewById(R.id.result);

        //choose here who to share with
        //((EditText) findViewById(R.id.et_targetPath)).setText("com.zebra.ssmfilepersist/A.txt");
        ((EditText) findViewById(R.id.et_targetPath)).setText("com.ndzl.sst_companionapp/A.txt");

        initializePersistFlagSpinner();

        //REGISTER FILE NOTIFICATION RECEIVER
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.zebra.configFile.action.notify"); //NEVER RECEIVED!!
        //filter.addAction("com.ndzl.dw63one.READMYCODE");//received!
        filter.addCategory("android.intent.category.DEFAULT");
        registerReceiver(new FileNotificationReceiver(), filter);

        myContentObserver = new LocalContentObserver(null);

    }

    private void initializePersistFlagSpinner() {
        persisFlagSpinner = findViewById(R.id.persistFlagSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.persist_flag, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        persisFlagSpinner.setAdapter(adapter);
    }


    public void onClickInsertOwnGeneratedFile(View view){
        String filename = "K.txt";
        //CREATES A FILE LOCALLY AND SHARES IT THROUGH SSM
        // THERE ARE MAINLY 3 OPTIONS TESTED ON A13 ON DIFFERENT PLATFORMS: Download folder, Documents folder, /enterprise
        String sourcePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/"+filename;
        //String sourcePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)+"/"+filename;
        //String sourcePath = "/enterprise/usr/persist"+"/"+filename;


        try {
            File f = new File(sourcePath);
            if (f.exists()) {
                f.delete();
            }

            f.createNewFile();
            Runtime.getRuntime().exec("chmod 666 " + sourcePath); //chmod needed for /enterprise

            FileOutputStream fos = new FileOutputStream(f);
            fos.write( ("hello zebra this file has been created on "+ DateFormat.format("dd/MM/yyyy hh:mm:ss", System.currentTimeMillis() ).toString()+"\n").getBytes(StandardCharsets.UTF_8) );
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String targetPath = "com.ndzl.sst_companionapp/"+filename;
        Log.i(TAG, "targetPath " + targetPath);
        Log.i(TAG, "sourcePath " + sourcePath);
        Log.i(TAG, "*********************************");
        File file = new File(sourcePath);
        Log.i(TAG, "file path " + file.getPath() + " length: " + file.length());

        StringBuilder _sb = new StringBuilder();
        if (!file.exists()) {
            Toast.makeText(this, "File does not exists in the source", Toast.LENGTH_SHORT).show();
        } else {
            Uri cpUriQuery = Uri.parse(AUTHORITY_FILE + getPackageName());
            Log.i(TAG, "authority  " + cpUriQuery.toString());

            try {
                ContentValues values = new ContentValues();
                values.put(COLUMN_TARGET_APP_PACKAGE, String.format("{\"pkgs_sigs\": [{\"pkg\":\"%s\",\"sig\":\"%s\"}]}", getPackageName(), signature));
                values.put(COLUMN_DATA_NAME, sourcePath);
                values.put(COLUMN_DATA_TYPE, "3");
                values.put(COLUMN_DATA_VALUE, targetPath);
                values.put(COLUMN_DATA_PERSIST_REQUIRED, "false");

                Uri createdRow = getContentResolver().insert(cpUriQuery, values);
                Log.i(TAG, "SSM Insert File: " + createdRow.toString());
                //Toast.makeText(this, "File insert success", Toast.LENGTH_SHORT).show();
                _sb.append("Insert Result: "+createdRow+"\n" );
            } catch (Exception e) {
                Log.e(TAG, "SSM Insert File - error: " + e.getMessage() + "\n\n");
                _sb.append("SSM Insert File - error: " + e.getMessage() + "\n\n");
            }
            resultView.setText(_sb);
            Log.i(TAG, "*********************************");
        }
    }


    /*--------- Inserting the file to SSM -----------*/
    public void onClickInsertFile3rdParty(View view) {
        String sourcePath = ((EditText) findViewById(R.id.et_sourcePath)).getText().toString();
        String targetPath = ((EditText) findViewById(R.id.et_targetPath)).getText().toString();
        Log.i(TAG, "targetPath " + targetPath);
        Log.i(TAG, "sourcePath " + sourcePath);
        Log.i(TAG, "*********************************");
        File file = new File(sourcePath);
        Log.i(TAG, "file path " + file.getPath() + " length: " + file.length());

        StringBuilder _sb = new StringBuilder();
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
                _sb.append("Insert Result: "+createdRow+"\n" );
            } catch (Exception e) {
                Log.e(TAG, "SSM Insert File - error: " + e.getMessage() + "\n\n");
                _sb.append("SSM Insert File - error: " + e.getMessage() + "\n\n");
            }
            resultView.setText(_sb);
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
                resultView.setText("No files to query for local package "+getPackageName()+"\nFiles shared with other packagenames must be queried remotely");
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
        String targetPackageName = getPackageName(); //THIS DELETION WORKS LOCALLY ONLY - REMOTE FILES, SHARED TO ANOTHER PACKAGENAME MUST BE DELETED REMOTELY
        StringBuilder _sb = new StringBuilder();
        _sb.append("DELETING FOR TARGET PACKAGE "+targetPackageName+"\n");
        Uri cpUriQuery = Uri.parse(AUTHORITY_FILE +getPackageName());
        try {
            String whereClause = COLUMN_TARGET_APP_PACKAGE + " = '" + targetPackageName + "'";
            //String whereClause = COLUMN_TARGET_APP_PACKAGE + " = '" + targetPackageName + "'" + " AND " + COLUMN_DATA_PERSIST_REQUIRED + " = '" + persisFlagSpinner.getSelectedItem().toString() + "'";
            int deleteStatus = getContentResolver().delete(cpUriQuery, whereClause, null);
            Log.d(TAG, "File deleted, status = " + deleteStatus);
            _sb.append("Target package delete all files result="+deleteStatus+"\n");

            Uri directUri =  Uri.parse("content://com.zebra.securestoragemanager.SecureFileProvider/files/com.ndzl.sst_companionapp/A.txt");
            int deleteStatusDIRECT = getContentResolver().delete(directUri, null, null);// 0 means success
            _sb.append("Direct target file delete result="+deleteStatusDIRECT+"\n");

        } catch (Exception e) {
            Log.d(TAG, "Delete file - error: " + e.getMessage());
            _sb.append("EXCEPTION in delete result="+e.getMessage());

        }

        resultView.setText( _sb.toString());
    }
}

class LocalContentObserver extends ContentObserver {
    public LocalContentObserver(Handler handler) {
        super(handler);
    }

    @Override
    public void onChange(boolean selfChange) {
        this.onChange(selfChange, null);
        Log.d(SSMFileProcessor.TAG, "### received self change notification from uri: ");
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) { //called on insert/delete etc.
        Log.d(SSMFileProcessor.TAG, "### received notification from uri: " + uri.toString());
    }
}

class LocalDataSetObserver extends DataSetObserver {
    public LocalDataSetObserver() {

    }

    @Override
    public void onInvalidated() { //linked to cursors lifecycle - see the update api implementation
        super.onInvalidated();
        Log.d(SSMFileProcessor.TAG, "onInvalidate");
    }

    @Override
    public void onChanged() {
        super.onChanged();
        Log.d(SSMFileProcessor.TAG, "onChanged");
    }
}
