package com.owncloud.android.operations;



import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.owncloud.android.Log_OC;
import com.owncloud.android.db.ProviderMeta.ProviderTableMeta;

import android.content.Context;
/**
 * This file pull data from the server and update the SHARED table in 
 * content provider.
 * In this class the data is only pulled from the server and never pushed back
 * The entry 
 * 
 * @author Jane Iedemska
 *
 */



public class SynchronizeSharedWithMeData  extends RemoteDataOperation
                                    implements Runnable{
    
    String TAG =  SynchronizeSharedWithMeData.class.getSimpleName();
    HashMap <Integer , String[] > serverFiles = new HashMap <Integer , String[] >();
    
    public SynchronizeSharedWithMeData(Context context){
        super(context,"getfilessharedwithuser.php");
        //no additional parameters to add
        
    }

    @Override
    public void run() {
        try{
            Log_OC.d(TAG, "requesting shared files info from server " + mServerUrl + " name " + mAccountName);
            
            mPost.setEntity(new UrlEncodedFormEntity(mPostParams));
            mResponse = mClient.execute(mPost);
        } catch (Exception e) {
            //post error
            e.printStackTrace();
        }
        //getting the reply from server
        mStatusCode = mResponse.getStatusLine().getStatusCode(); //server reply
        
        if ( mStatusCode == HttpStatus.SC_OK) {                  //server replied OK
            HttpEntity entityresponse = mResponse.getEntity();
            //Log_OC.d(TAG, "server returned OK");
            //Unpacking JSON array
            try{
                JSONArray array = new JSONArray(EntityUtils.toString(entityresponse));
                if(array.length() == 0){
                    Log_OC.d(TAG, "array is empty");
                } else {
                    if (((JSONObject)array.get(0)).getString("status").equals("success")){  //1st obj in jsonarray is status:status
                            Log_OC.d(TAG, "server operation was successful");
                            for (int i = 1; i < array.length(); i++){
                                //get info
                                //FIXME temp vars
                                int id = Integer.parseInt(((JSONObject)array.get(i)).getString("id")); 
                                String[] file = {((JSONObject)array.get(i)).getString("filename"),
                                ((JSONObject)array.get(i)).getString("owner_location")};
                                //Log_OC.d(TAG, "server replied " + id +file[0] +file[1]);
                                //Create a list of downloaded files
                                serverFiles.put(id, file);      
                            }
                            //Update SHARED table on the client
                            mManager.updateSharedWithMeFiles(serverFiles);
                            //test
                            //mManager.printTable(ProviderTableMeta.CONTENT_URI_SHARED_FILES);
                            
                            
                    } else {
                        Log_OC.d(TAG, "something went wrong in the server sql");
                    }
                }
                
            } catch (Exception e) {
                //JSON array error
                e.printStackTrace();
            }           
            
        } else {
            //server response error
            Log_OC.d(TAG, "server returned code "+ mStatusCode);
            //Unpacking JSON array
        }     
        
    }

}
