package com.owncloud.android.operations;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.owncloud.android.Log_OC;
import com.owncloud.android.db.ProviderMeta.ProviderTableMeta;

public class SynchronizeFriends extends RemoteDataOperation
                                implements Runnable{
String TAG = SynchronizeFriends.class.getSimpleName();
    
    public SynchronizeFriends(Context context){
        super(context, "getuserfriends.php");
    }
    @Override
    public void run() {
        
        ArrayList <String>  friends = new ArrayList<String>();
        try{
            Log_OC.d(TAG, "getting friends");
            
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
                            //Log_OC.d(TAG, "server operation was successful");
                            for (int i = 1; i < array.length(); i++){
                                //get info
                                //FIXME temp vars
                                String friendName = ((JSONObject)array.get(i)).getString("friend");

                                //Log_OC.d(TAG, "server replied " + friendName);
                                //Create a list of friends
                                friends.add(friendName);      
                            }
                            //Update FRIENDS table on the client
                            mManager.updateFriends(friends);
                            //mManager.printTable(ProviderTableMeta.CONTENT_URI_FRIENDS);
 
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
