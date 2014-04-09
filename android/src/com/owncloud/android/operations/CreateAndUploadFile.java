package com.owncloud.android.operations;

import java.io.File;
import java.io.IOException;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class CreateAndUploadFile {

    private String TAG = "CreateAndUploadFile";
    
    public void createFile() {
        
    }

    public void createFile(String remotePath) {
        String filePath = "/sdcard/ownCloud"+remotePath+"file17.txt";
        File myfile = new File("/sdcard/ownCloud"+remotePath+"file17.txt");
        try {
            Intent intent = new Intent(Intent.ACTION_EDIT);
            intent.setDataAndType(Uri.parse(filePath),"text/plain");
            boolean cre = myfile.createNewFile();
            Log.d(TAG,"File Created "+cre);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
}