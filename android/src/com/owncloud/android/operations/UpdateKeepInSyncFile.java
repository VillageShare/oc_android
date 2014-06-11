package com.owncloud.android.operations;




import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import android.content.Context;
import com.owncloud.android.Log_OC;
import com.owncloud.android.datamodel.OCFile;
import com.owncloud.android.db.ProviderMeta.ProviderTableMeta;

public class UpdateKeepInSyncFile extends RemoteDataOperation
                                  implements Runnable {
 
    private boolean mNewState; // new state of keep in sync to which we are updating it
    private long mServerId;
    private String mOwner;
    private OCFile mFile;
    private String TAG = UpdateKeepInSyncFile.class.getSimpleName();
    
    public UpdateKeepInSyncFile(Context context, OCFile file, boolean newState){
        super(context,"setkeepinsyncfile.php");
        //owner@location is set in the super class
        mFile = file;
        mNewState = newState;
        mServerId = file.getFileServerId(); //from server id we will get path on the server
        if ((mOwner = mManager.wasFileSharedWithMe(file.getFileServerId()))  == null){
            mOwner = mAccountNameLocation; //saving one query on the server and providing the owner
        }
    }

    /**
     * Update changes of keepInSync on the ServerSide 
     */
    
    public void run(){
     
         Log_OC.d(TAG, "updating server");
         
        
        // Log_OC.d(TAG ,"name_location = " +mAccountNameLocation + " pathname = " +mPath + " state = " + mNewState);
         try{
             mPostParams.add(new BasicNameValuePair("server_id", String.valueOf(mServerId)));    //file server id
             mPostParams.add(new BasicNameValuePair("state", mNewState ? "TRUE": "FALSE"));      //updated state
             mPostParams.add(new BasicNameValuePair("file_owner", mOwner));                 //who is the owner of the file
             
             mPost.setEntity(new UrlEncodedFormEntity(mPostParams));                                                                                 
             HttpResponse response = mClient.execute(mPost);
             
             mStatusCode = response.getStatusLine().getStatusCode(); //server reply
             
             if ( mStatusCode == HttpStatus.SC_OK) {  
                //update the table
                mManager.setKeepInSync(mFile, mNewState);
                //mManager.printTable(ProviderTableMeta.CONTENT_URI);
                 Log_OC.d(TAG, "Server replied OK");
             } else{
                 Log_OC.d(TAG, "Server replied with " + mStatusCode );
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
    }
    
}
    
 