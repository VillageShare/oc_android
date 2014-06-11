package com.owncloud.android.operations;

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
 * This file synchronizes server IDs for all of the files,
 *  since wedav only updates properties.
 * This should be called onPerformSync after synchronizing root folder.
 * 
 * Jane Iedemska
 */

public class SynchronizeServerIds extends RemoteDataOperation
                                  implements Runnable{
    
    String TAG = SynchronizeServerIds.class.getSimpleName();
    
    public SynchronizeServerIds(Context context){
        super(context, "getfilesserverids.php");
    }
    @Override
    public void run() {
        
        HashMap <Integer , String>  serverIds = new HashMap<Integer, String>();
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
                                int id = Integer.parseInt(((JSONObject)array.get(i)).getString("server_id")); 
                                String localPath = ((JSONObject)array.get(i)).getString("local_path");

                                //Log_OC.d(TAG, "server replied " + id + localPath);
                                //Create a list of downloaded files
                                serverIds.put(id, localPath);      
                            }
                            //Update FILE table on the client
                            mManager.updateServerIds(serverIds);
                            //mManager.printTable(ProviderTableMeta.CONTENT_URI);
 
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
