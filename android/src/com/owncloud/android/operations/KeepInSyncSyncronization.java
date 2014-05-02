package com.owncloud.android.operations;


import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.widget.Toast;

import com.owncloud.android.Log_OC;
import com.owncloud.android.authentication.AccountUtils;
import com.owncloud.android.datamodel.OCFile;

public class KeepInSyncSyncronization implements Runnable {

    private boolean mUpdateServer; // updating the server or the client 
    private boolean mNewState; // new state of keep in sycn to which we are updating it
    private String mUid;
    private String mLocation;
    private String mPathName;
    private String mServerUri = "128.111.52.157";
    private String TAG = KeepInSyncSyncronization.class.getSimpleName();
    
    public KeepInSyncSyncronization(OCFile file, Account account, boolean newState, boolean updateServer){
        
        mUpdateServer = updateServer;
        mNewState = newState;
        
        //get user name out of account info
        String [] accountNames = account.name.split("@");
        if(accountNames.length > 2)
        {
            mUid = accountNames[0];
            mLocation = accountNames[1];
        }
        //this way this is the original file path on the server
        mPathName = file.getRemotePath();
    }
    
    
    @Override
    public void run() {
        if(mUpdateServer){
            updateServer();
        } else {
            updateClient();
        }
    }
    /**
     * Update changes of keepInSync on the ServerSide 
     */
    
 private void updateServer(){
     
     Log_OC.d(TAG, "updating server");
     Log_OC.d(TAG ,"iud = " + mUid+ " location = " +mLocation + " pathname = " +mPathName );
     
     
     mPathName ="/home/owncloud/data/j5@Shcool1/files/welcome.txt" ;
     JSONObject params = new JSONObject();
         try{
         params.put("uid", mUid);              //user ID
         params.put("location", mLocation);    //server name
         params.put("pathname", mPathName);    //path on the server
         params.put("state", mNewState ? "TRUE": "FALSE");       //updated state
         
         HttpPost post = new HttpPost("http://" + mServerUri + "/owncloud/updatefiles.php");
         post.addHeader("Content-type", "application/json; charset=utf-8");
         StringEntity entity = new StringEntity(params.toString());                   //entity [json string]
         post.setEntity(entity);                                                                                 
         HttpClient client = new DefaultHttpClient();                                 //client
         HttpResponse response = client.execute(post);
         
         final int statusCode = response.getStatusLine().getStatusCode(); //server reply
         
         if ( statusCode == HttpStatus.SC_OK) {  
             //Toast.makeText(mContext, "keepinsync updated with status OK",   Toast.LENGTH_SHORT).show();
             Log_OC.d(TAG, "Server Reply was ok");
         } else{
             Log_OC.d(TAG, "" + statusCode );
         }
     } catch (Exception e) {
         e.printStackTrace();
     }
     
     
 }
 /**
  * Update changes of keepInSync on the Client side
  */
 private void updateClient(){
     
 }
}
