package com.owncloud.android.operations;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.owncloud.android.Log_OC;
import com.owncloud.android.authentication.AccountUtils;
import com.owncloud.android.datamodel.OCDataStorageManager;
import com.owncloud.android.datamodel.OCFile;
import com.owncloud.android.syncadapter.OCDataSyncService;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;

/**
 * Abstract class in addition to RemoteOperation
 * that is used to make HTTP requests to the server
 * in order to obtain data from DB
 * 
 * @author Jane Iedemska
 *
 */
public abstract class RemoteDataOperation {
 
    String mAccountName;        //this user name
    String mAccountLocation;    //this user node
    String mAccountNameLocation; //name@node
    String mServerUrl = "";     //server IP
    List <NameValuePair> mPostParams;
    HttpPost mPost;
    HttpClient mClient;
    HttpResponse mResponse;
    int mStatusCode;
    OCDataStorageManager mManager;
    Account mAccount;
    Context mContext;
    
    String TAG = RemoteDataOperation.class.getSimpleName();
    
    
   public RemoteDataOperation(Context context, String phpScript){
        mContext = context;
        //set account
        mAccount = AccountUtils.getCurrentOwnCloudAccount(context);
        //set manager
        mManager = new OCDataStorageManager(mAccount,context.getContentResolver());
        //set client
        mClient = new DefaultHttpClient();
        //set account info
         
        String [] accountInfo = mAccount.name.split("@");
        if(accountInfo.length > 2)
        {
            mAccountName = accountInfo[0];
            mAccountLocation = accountInfo[1]; 
            mServerUrl = accountInfo[2]; 
        }
        mAccountNameLocation = mAccountName + "@" + mAccountLocation;
        //create post Post
        mPost =  new HttpPost("http://" + mServerUrl + "/owncloud/" + phpScript);
        //Log_OC.d(TAG,"http://" + mServerUrl + "/owncloud/" + phpScript); 
        //Set basic post info
        mPostParams = new ArrayList<NameValuePair>();
        //Log_OC.d(TAG, mAccountNameLocation);
        mPostParams.add( new BasicNameValuePair("name_location", mAccountNameLocation)); // owner@node      
    }
    
   public void sendStickyBroadcast() {
       Intent i = new Intent(OCDataSyncService.SYNC_MESSAGE);
       i.putExtra(OCDataSyncService.IN_PROGRESS, false);
       i.putExtra(OCDataSyncService.ACCOUNT_NAME, mAccountNameLocation);      
       i.putExtra(OCDataSyncService.SYNC_FOLDER_REMOTE_PATH, OCFile.PATH_SEPARATOR);
       
       mContext.sendStickyBroadcast(i);
   }
  
}
