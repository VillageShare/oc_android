/* ownCloud Android client application
 *   Copyright (C) 2011  Bartek Przybylski
 *   Copyright (C) 2012-2013 ownCloud Inc.
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License version 2,
 *   as published by the Free Software Foundation.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.owncloud.android.ui.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.owncloud.android.DisplayUtils;
import com.owncloud.android.Log_OC;
import com.owncloud.android.R;
import com.owncloud.android.authentication.AccountUtils;
import com.owncloud.android.datamodel.DataStorageManager;
import com.owncloud.android.datamodel.FileDataStorageManager;
import com.owncloud.android.datamodel.OCFile;
import com.owncloud.android.db.DbFriends;
import com.owncloud.android.db.DbShareFile;
import com.owncloud.android.db.ProviderMeta.ProviderTableMeta;
import com.owncloud.android.files.services.FileObserverService;
import com.owncloud.android.files.services.FileDownloader.FileDownloaderBinder;
import com.owncloud.android.files.services.FileUploader.FileUploaderBinder;
import com.owncloud.android.operations.SynchronizeFileOperation;
import com.owncloud.android.ui.activity.FileDisplayActivity;
import com.owncloud.android.ui.activity.TransferServiceGetter;


/**
 * This Adapter populates a ListView with all files and folders in an ownCloud
 * instance.
 * This List Adapter is only instantiated at OCFileListFragment.
 * @author Bartek Przybylski
 * 
 * 
 */




public class FileListListAdapter extends    BaseAdapter         //BaseAdapter - implements Adapter that can be used in both ListAdapter and Spinner
                                 implements ListAdapter,        //ListAdapter - interface that fills out ListView
                                            OnClickListener     //OnclickListener -interface for callback to be evoked when a view is clicked
                                            {
    private Context mContext;                                   //contex passed by caller
    private OCFile mFile = null;                                //current file or folder
    private Vector<OCFile> mFiles = null;                       //list of files for the adapter
    private FileDataStorageManager mStorageManager;
    private Account mAccount;
    private TransferServiceGetter mTransferServiceGetter;
    private ContentResolver mContentResolver;

    private static String shareType="0"; //type of sharing (0 user 1 group 3 link)
    private String accountName;
    private String url;
    private int syncedFiles;        //number of files that should be kept in sync
    private int MAX_SYNCED = 5;
    private String TAG = FileListListAdapter.class.getSimpleName();
    //storages
    DbFriends friendsData;
    DbShareFile fileData;
    List<String> sharedWith;
    
    //Context - abstract class to access system specific resources
    //TransferServiceGetter - owncloud specific class for file upload download
    public FileListListAdapter(Context context, TransferServiceGetter transferServiceGetter) {
        mContext = context;
        mAccount = AccountUtils.getCurrentOwnCloudAccount(mContext);
        mTransferServiceGetter = transferServiceGetter; 
        mContentResolver = mContext.getContentResolver();
        mStorageManager = new FileDataStorageManager(mAccount, mContentResolver);
        syncedFiles = mContentResolver.query(ProviderTableMeta.CONTENT_URI,      //
                null,
                ProviderTableMeta.FILE_KEEP_IN_SYNC + " = ?", //files = 1
                new String[] {String.valueOf(1)},
                null).getCount();
        Toast.makeText(mContext, "There are synced files " + syncedFiles, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public int getCount() {
        return mFiles != null ? mFiles.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        if (mFiles == null || mFiles.size() <= position)
            return null;
        return mFiles.get(position);
    }

    @Override
    public long getItemId(int position) {
        if (mFiles == null || mFiles.size() <= position)
            return 0;
        return mFiles.get(position).getFileId();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isEmpty() {
        return (mFiles == null || mFiles.isEmpty());
    }

    @Override
    //Filling out the array of files\folders
    public View getView(final int position, View convertView, ViewGroup parent) {
        //FIXME convertview 
        View view = convertView;
        if (view == null) {
            LayoutInflater inflator = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflator.inflate(R.layout.oc_list_item, null);
        }
    
        if (mFiles != null && mFiles.size() > position) {       //nonempty list of files
            
            //get user name out of account info
            Account account = AccountUtils.getCurrentOwnCloudAccount(mContext);
            String [] accountNames = account.name.split("@");
            if(accountNames.length > 2)
            {
                accountName = accountNames[0]+"@"+accountNames[1];
                url = accountNames[2];
            }
            //Binder for services
            FileDownloaderBinder downloaderBinder = mTransferServiceGetter.getFileDownloaderBinder();
            FileUploaderBinder uploaderBinder = mTransferServiceGetter.getFileUploaderBinder();
            
            
            //set filename
            OCFile file = (OCFile)getItem(position);
            TextView fileName = (TextView) view.findViewById(R.id.OCFilename);
            String name = file.getFileName(); // file at position 
            fileName.setText(name);
    
            //File Icon
            ImageView fileIcon = (ImageView) view.findViewById(R.id.FileIcon);
            fileIcon.setImageResource(DisplayUtils.getResourceId(file.getMimetype()));
            
            // users that shared files with me and what files
            fileData = new DbShareFile(mContext); //get info about the files
            Map<String,String> fileSharers = fileData.getUsersWhoSharedFilesWithMe(accountName);
            
            //Set sharer of the file
            TextView sharer = (TextView)view.findViewById(R.id.OCFileSharer);
            if(fileSharers.size()!=0 && (!file.equals("Shared") && file.getRemotePath().contains("Shared"))) {
               if(fileSharers.containsKey(name)){
                        sharer.setText(fileSharers.get(name));
                        fileSharers.remove(name);
                    } else {
                        sharer.setText(" ");
                    }
            }else {
                        sharer.setText(" ");
                    }  
            
            /**
             * Set icons of file state
             */
            ImageView localStateView = (ImageView) view.findViewById(R.id.DownloadIcon);
            if (downloaderBinder != null && downloaderBinder.isDownloading(mAccount, file)) {   //file is being downloaded 
                localStateView.setImageResource(R.drawable.downloading_file_indicator);
                localStateView.setVisibility(View.VISIBLE);
            } else if (uploaderBinder != null && uploaderBinder.isUploading(mAccount, file)) {  //file is being uploaded
                localStateView.setImageResource(R.drawable.uploading_file_indicator);
                localStateView.setVisibility(View.VISIBLE);
            } else if (file.isDown()) {                             //file is available locally
                localStateView.setImageResource(R.drawable.local_file_indicator);
                localStateView.setVisibility(View.VISIBLE);
            } else {                                                //nothing is happening to the file
                localStateView.setVisibility(View.INVISIBLE);
            }
            
            /**
             * KeepInSync checkbox
             */ 
            CheckBox checkBoxV = (CheckBox) view.findViewById(R.id.OCKeepInSync);
            checkBoxV.setChecked(file.keepInSync());
            checkBoxV.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {    
            @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) { 
                        OCFile file  =  (OCFile)getItem(position);
                        //updating view
                        if(file.keepInSync() && isChecked){            //file is in sync, this is just updating the view
                            return;
                        }
                        if (syncedFiles < MAX_SYNCED || !isChecked ){  //if there is space in max_synced or file is unselected
                           mStorageManager.updateKeepInSync(file, isChecked); //then we just update stats
                           //update sync files
                           syncedFiles = mContentResolver.query(ProviderTableMeta.CONTENT_URI,      //
                                   null,
                                   ProviderTableMeta.FILE_KEEP_IN_SYNC + " = ?", //files = 1
                                   new String[] {String.valueOf(1)},
                                   null).getCount();
                           
                           //update FileObserverService
                           /*
                            Intent intent = new Intent(mContext, FileObserverService.class);
                            intent.putExtra(FileObserverService.KEY_FILE_CMD,
                                       (isChecked ? FileObserverService.CMD_ADD_OBSERVED_FILE: FileObserverService.CMD_DEL_OBSERVED_FILE));
                            intent.putExtra(FileObserverService.KEY_CMD_ARG_FILE, file);
                            intent.putExtra(FileObserverService.KEY_CMD_ARG_ACCOUNT, mAccount);
                            mContext.startService(intent);
                            */
                            if (isChecked) {
                            //force an immediate synchronization      
                                /*
                            SynchronizeFileOperation mLastRemoteOperation = new SynchronizeFileOperation(file, null, mStorageManager, mAccount, true, false, mContext);
                            mLastRemoteOperation.execute(mAccount, mContext);
                                  */
                            }
                          
                           Toast.makeText(mContext, "Now there are synced files " + syncedFiles, Toast.LENGTH_SHORT).show();
                           
                        } else {                //all the places in max_synced are taken
                            buttonView.setChecked(!isChecked);
                            Toast.makeText(mContext, "Too many synced files", Toast.LENGTH_SHORT).show();
                        }
                        
                        
                }
            });
           
            
            
            /**
             * Setting the file attributes
             */
            TextView fileSizeV = (TextView) view.findViewById(R.id.OCFileSize);
            TextView lastModV = (TextView) view.findViewById(R.id.OCLastMod);
            if (!file.isDirectory()) {                              //item is a file
                fileSizeV.setVisibility(View.VISIBLE);
                fileSizeV.setText(DisplayUtils.bytesToHumanReadable(file.getFileLength()));
                lastModV.setVisibility(View.VISIBLE);
                lastModV.setText(DisplayUtils.unixTimeToHumanReadable(file.getModificationTimestamp()));
                checkBoxV.setVisibility(View.VISIBLE);
            } 
            else {                                            //item is a folder
                
                fileSizeV.setVisibility(View.VISIBLE);
                fileSizeV.setText(DisplayUtils.bytesToHumanReadable(file.getFileLength()));
                lastModV.setVisibility(View.VISIBLE);
                lastModV.setText(DisplayUtils.unixTimeToHumanReadable(file.getModificationTimestamp()));
                checkBoxV.setVisibility(View.GONE);
            }
          
            /**
             * Share Button [imageview]
             * implements dialog that allows user to select friends and share file with them
             */
    
            ImageView shareButton = (ImageView) view.findViewById(R.id.OCShareItem);
            shareButton.setOnClickListener(new OnClickListener() {
               
                @Override
                public void onClick (View v) {
                  
                    //vars
                    final Dialog dialog = new Dialog(mContext); //dialog window
                    final OCFile fileToBeShared = (OCFile) getItem(position); //owncloud class
                    final ArrayAdapter<String> sharedWithAdapter; //with whom it was shared already
                    final String filePath; // where file is
                    final String fileName = fileToBeShared.getFileName();
                    final String fileRemotePath = fileToBeShared.getRemotePath(); //path on the server
                 
                    
                    // Set up dialog
                    dialog.setContentView(R.layout.share_file_with);
                    dialog.setTitle("Share");
                    dialog.show();              //start the dialog
                    
                    
                    //set up account
                    Account account = AccountUtils.getCurrentOwnCloudAccount(mContext);
                    String [] accountNames = account.name.split("@");
                    if(accountNames.length > 2)
                    {
                        accountName = accountNames[0]+"@"+accountNames[1]; //
                        url = accountNames[2];  // server ip
                    }
                    
                    /*
                     * Text field for entering multiple friends and groups
                     * */
                    friendsData  = new DbFriends(mContext);
                    final MultiAutoCompleteTextView textView = (MultiAutoCompleteTextView)dialog.findViewById(R.id.multiautocompleteshare);
                    textView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
                    textView.setThreshold(1);                           //show hint after 1 char
                                                                        //filling it out with friends got from db
                    textView.setAdapter(new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1,friendsData.getFriendList(accountName)));
                    textView.setFocusableInTouchMode(true);
                    textView.setHint("Share With");
                    friendsData.close();
    
                    //selecting the text
                    textView.setOnItemClickListener(new OnItemClickListener() {
                        @Override
                        //onclick for adapterview
                        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                            //FIXME
                            //???
                                }
                    });
                    
                  
                    /* 
                     * List of people this file was shared with already
                     * */
                    final ListView listview = (ListView)dialog.findViewById(R.id.alreadySharedWithList);   
                                                                    //array of friends with whom it was shared already
                    sharedWith = new ArrayList<String>(fileData.getUsersWithWhomIhaveSharedFile(fileName,fileRemotePath,
                                                                                     accountName,String.valueOf(1)));
                                                                    //fill in the adapter of friends with whom it was shared already
                    sharedWithAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, sharedWith);
                    listview.setAdapter(sharedWithAdapter);
                    
                    /*
                     * File or folder and full path
                     * */
                    final String itemType;
                    final String itemSource;
                    filePath = "files"+String.valueOf(fileRemotePath);
                    if(fileToBeShared.isDirectory()) {
                        itemType = "folder";
                        int lastSlashInFolderPath = filePath.lastIndexOf('/');
                        itemSource =  filePath.substring(0, lastSlashInFolderPath);
                    }
                    else {
                        itemType="file";
                        itemSource = filePath;
                    }
                           
                    
                    /**
                     * Message handler for the current thread.
                     * Processes messages, that are coming from the other thread.
                     */
                    final Handler handler = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {            //prints share status message
                            sharedWithAdapter.notifyDataSetChanged();       //as a toast
                            Toast.makeText(mContext, (String)msg.obj, Toast.LENGTH_SHORT).show();
                        }
                     };
                     
                     /**
                      * OK button
                      * parses the textfield and generates HTTP request
                      * */
                     Button okBtn = (Button)dialog.findViewById(R.id.OKBtn);
                     okBtn.setOnClickListener(new OnClickListener() {
                         
                        @Override
                        public void onClick(View v) {
                            
                            //getting text that was entered and splitting it to tokens
                            
                            StringTokenizer st = new StringTokenizer(textView.getText().toString() , ", ");
                            final JSONObject toShareWith = new JSONObject();
                            String token;
                                      
                            if(st.countTokens() == 0) {                     //entered text is empty
                                textView.setHint("Share With");
                                Toast.makeText(mContext, "Please enter friends' names or groups with whom you want to share", Toast.LENGTH_SHORT).show();
                            } else {                                        //text is not empty
                                while(st.hasMoreTokens()){
                                    token = st.nextToken();
                                    if(sharedWith.contains(token)) {        // have shared with this people already
                                        Toast.makeText(mContext, "You have shared the file with " + token + " already", Toast.LENGTH_SHORT).show();
                                    } else {                                //if token is not empty add it to the list
                                        if(!token.trim().equals("")){
                                            try {
                                                toShareWith.put(token,shareType);
                                            } catch (JSONException e) {
                                                    e.printStackTrace();
                                            }
                                        }
                                    }
                                 }
                             }
                            
                            if(toShareWith.length() > 0) {                  //there is nonempty list of names
                                
                                textView.setText("");                       //empty the text field
                               
                                Runnable runnable = new Runnable() {        //when running actual sharing
                                    @Override
                                    public void run() {
                                        try{
                                            //create json object with set of params
                                            
                                            JSONObject params = new JSONObject();
                                            params.put("itemType",  itemType);      //file or folder
                                            params.put("itemSource",itemSource);    //absolute path
                                            params.put("shareType", shareType);     //user or group or link
                                            params.put("toShareWith",toShareWith);  //group to share (json object)
                                            params.put("uidOwner",  accountName);   //who own the file 
                                      
                                            //creating HTTP request to the server with entity set to json object
                                                                                                                         //post
                                            HttpPost post = new HttpPost("http://" + url + "/owncloud/androidshare.php");//url = server ip
                                            post.addHeader("Content-type", "application/json; charset=utf-8");                                                                                                              
                                            StringEntity entity = new StringEntity(params.toString());                   //entity [json string]
                                            post.setEntity(entity);                                                                                 
                                            HttpClient client = new DefaultHttpClient();                                 //client
                                            HttpResponse response = client.execute(post);
                                
                                            //getting the reply from server
                                            
                                            Message message = handler.obtainMessage();              //message to be sent back
                                            final int statusCode = response.getStatusLine().getStatusCode(); //server reply
                                            
                                            if ( statusCode == HttpStatus.SC_OK) {                  //server replied OK
                                                /////////////FIXME//////////////
                                                HttpEntity entityresponse = response.getEntity();
                                                String jsonentity = EntityUtils.toString(entityresponse);
                                                
                                                JSONObject obj = new JSONObject(jsonentity);
                                                                                                    //check status
                                                String shareSuccess = "false";
                                                shareSuccess = obj.getString("SHARE_STATUS");
                                           
                                                //sending message to the main thread handler
                                               
                                                if(shareSuccess.equals("true")) {                   //share succeded
                                                   Iterator <String> names = toShareWith.keys(); 
                                                    while(names.hasNext()){
                                                        String name = names.next();                 //update OC server database
                                                        fileData.putNewShares(fileName, fileRemotePath, accountName, name);
                                                        sharedWith.add(name);
                                                    }
                                                    message.obj = "You have successfully shared the file";
                                                } else if(shareSuccess.equals("NO_SUCH_FILE")) {
                                                    message.obj = "File you are trying to share does not exist";
                                                } else if(shareSuccess.equals("INVALID_SHARETYPE")) {
                                                    message.obj = "File Share type is invalid";
                                                }else {
                                                    message.obj = "Failed to share the file.";
                                                }                                                                                  
                                              } else{                                               //server error
                                                  message.obj  = "Server error: " + String.valueOf(statusCode); 
                                              }
                                              handler.sendMessage(message);   //report the status back to main thread
                                            }catch(Exception e) {
                                            e.printStackTrace();
                                            }
                                        }//end of run method
                                    };//end of Runnable
                      
                                new Thread(runnable).start();   //share in new thread 
                                dialog.dismiss();               //close the dialog
                            }//end of nonempty list of names
                        
                        }//end of onClick method
                        
                    });//end of OKBtn
                     
                     /**
                      * Cancel Button
                      * dismisses the dialog
                      * */
                    Button cancelBtn = (Button)dialog.findViewById(R.id.CancelBtn);
                    cancelBtn.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            //dataSourceShareFile.close();
                        }
                    });
                }//end of onclick shareButton              
                
            }); //end of ShareButton
          
            //dataSourceShareFile.close();
           
        }// end of if statement
        return view;
    }// end of getview

    /**
     * Change the adapted directory for a new one
     * @param directory                 New file to adapt. Can be NULL, meaning "no content to adapt".
     * @param updatedStorageManager     Optional updated storage manager; used to replace mStorageManager if is different (and not NULL)
     */
    public void swapDirectory(OCFile directory, DataStorageManager updatedStorageManager) {
        mFile = directory;
        if (updatedStorageManager != null && updatedStorageManager != mStorageManager) {
            mStorageManager = (FileDataStorageManager)updatedStorageManager;
            mAccount = AccountUtils.getCurrentOwnCloudAccount(mContext);
        }
        if (mStorageManager != null) {
            mFiles = mStorageManager.getDirectoryContent(mFile);
        } else {
            mFiles = null;
        }
        notifyDataSetChanged();
    }

    
    @Override
    public void onClick(View arg0) {
        // FIXME ????
        
    }
    
}
