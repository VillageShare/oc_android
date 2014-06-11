package com.owncloud.android.operations;

import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import com.owncloud.android.Log_OC;
import com.owncloud.android.datamodel.OCFile;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class ShareFile  extends RemoteDataOperation
                        implements Runnable{
    
    Handler mHandler;
    Message mMessage;
    String TAG = ShareFile.class.getSimpleName();
    
    public ShareFile(Context context, OCFile file, List<String> friends, List<String> groups, Handler handler){
        super(context, "setsharedfile.php");
        mHandler = handler;
        mMessage = handler.obtainMessage();
        mPostParams.add(new BasicNameValuePair("server_id", String.valueOf(file.getFileServerId())));
        mPostParams.add(new BasicNameValuePair("file_type", file.isDirectory()?"folder":"file"));
        for(String entry: friends){
            mPostParams.add(new BasicNameValuePair("friends[]", entry));
        }
        for(String entry: groups){
            mPostParams.add(new BasicNameValuePair("groups[]", entry));
        }
    }    
    
    public void run(){
        Log_OC.d(TAG,"sharing file");
        try{
            mPost.setEntity(new UrlEncodedFormEntity(mPostParams));
            mResponse = mClient.execute(mPost);
        } catch (Exception e) {
            //post error
            e.printStackTrace();
        }
        mStatusCode = mResponse.getStatusLine().getStatusCode();
        if ( mStatusCode == HttpStatus.SC_OK) {  //server replied OK
            Log_OC.d(TAG, "server returned OK");
            
            //FIXME update database
            mMessage.obj = "You have successfully shared the file";
            //FIXME parse errrors
        } else {
            //server response error
            Log_OC.d(TAG, "server returned code "+ mStatusCode);
            mMessage.obj  = "Server error: " + String.valueOf(mStatusCode);
            //Unpacking JSON array
        } 
        mHandler.sendMessage(mMessage); 
    }
}
