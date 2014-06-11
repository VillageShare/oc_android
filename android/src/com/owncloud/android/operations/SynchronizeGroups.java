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

public class SynchronizeGroups extends RemoteDataOperation
                               implements Runnable{
    
    
    String TAG =  SynchronizeGroups.class.getSimpleName();
    HashMap <String , Integer> newGroups = new HashMap <String, Integer>();
    
    public SynchronizeGroups(Context context){
        super(context, "getusergroups.php");
    }
    public void run() {
        Log_OC.d(TAG,"syncing groups");
        try{
            mPost.setEntity(new UrlEncodedFormEntity(mPostParams));
            mResponse = mClient.execute(mPost);
        } catch (Exception e) {
            //post error
            e.printStackTrace();
        }
        mStatusCode = mResponse.getStatusLine().getStatusCode();
        
        if ( mStatusCode == HttpStatus.SC_OK) {  //server replied OK
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
                                String group = ((JSONObject)array.get(i)).getString("group");
                                int admin = Integer.parseInt(((JSONObject)array.get(i)).getString("admin")); 
                                //Log_OC.d(TAG, "server replied " + group +" " + admin);
                                //Create a list of downloaded files
                                newGroups.put(group, admin);      
                            }
                            //Update SHARED table on the client
                            mManager.updateGroups(newGroups);
                            //test
                            //mManager.printTable(ProviderTableMeta.CONTENT_URI_GROUPS);                          
                            
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
