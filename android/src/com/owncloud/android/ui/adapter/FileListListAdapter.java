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
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;


import android.accounts.Account;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.owncloud.android.googleauth.HelloActivity;
import com.owncloud.android.DisplayUtils;
import com.owncloud.android.Log_OC;
import com.owncloud.android.R;
import com.owncloud.android.authentication.AccountUtils;
import com.owncloud.android.datamodel.DataStorageManager;
import com.owncloud.android.datamodel.OCDataStorageManager;
import com.owncloud.android.datamodel.OCFile;
import com.owncloud.android.db.DbFriends;
import com.owncloud.android.db.DbShareFile;
import com.owncloud.android.db.ProviderMeta.ProviderTableMeta;
import com.owncloud.android.files.services.FileObserverService;
import com.owncloud.android.files.services.FileDownloader.FileDownloaderBinder;
import com.owncloud.android.files.services.FileUploader.FileUploaderBinder;
import com.owncloud.android.operations.ShareFile;
import com.owncloud.android.operations.SynchronizeFileOperation;
import com.owncloud.android.operations.UpdateKeepInSyncFile;
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
    private OCDataStorageManager mStorageManager;
    private Account mAccount;
    private TransferServiceGetter mTransferServiceGetter;
    private ContentResolver mContentResolver;

    private int syncedFiles;        //number of files that should be kept in sync
    private int MAX_SYNCED = 5;
    private String TAG = FileListListAdapter.class.getSimpleName();
    //storages
    DbFriends friendsData;
    DbShareFile fileData;
    
    
    //Context - abstract class to access system specific resources
    //TransferServiceGetter - owncloud specific class for file upload download
    public FileListListAdapter(Context context, TransferServiceGetter transferServiceGetter) {
        mContext = context;
        mAccount = AccountUtils.getCurrentOwnCloudAccount(mContext);
        mTransferServiceGetter = transferServiceGetter; 
        mContentResolver = mContext.getContentResolver();
        mStorageManager = new OCDataStorageManager(mAccount, mContentResolver);
        
        syncedFiles = mContentResolver.query(ProviderTableMeta.CONTENT_URI,      //
                null,
                ProviderTableMeta.FILE_KEEP_IN_SYNC + " = ?", //files = 1
                new String[] {String.valueOf(1)},
                null).getCount();
        Log_OC.d(TAG, "Initially there are " + String.valueOf(syncedFiles) + " files");
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
        
        
        View view = convertView;
        OCFile file = (OCFile)getItem(position);
        //Binder for services
        FileDownloaderBinder downloaderBinder = mTransferServiceGetter.getFileDownloaderBinder();
        FileUploaderBinder uploaderBinder = mTransferServiceGetter.getFileUploaderBinder();
        
        
        //Set view
        if (view == null) {
            LayoutInflater inflator = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflator.inflate(R.layout.oc_list_item, null);
        }
    
        if (mFiles != null && mFiles.size() > position) {       //nonempty list of files, position is in range
          
            //Set filename
            TextView fileName = (TextView) view.findViewById(R.id.OCFilename);
            String name = file.getFileName(); // file at position 
            fileName.setText(name);
    
            //Set file Icon
            ImageView fileIcon = (ImageView) view.findViewById(R.id.FileIcon);
            fileIcon.setImageResource(DisplayUtils.getResourceId(file.getMimetype()));
            
            //Set sharer of the file, if any
            TextView sharer = (TextView)view.findViewById(R.id.OCFileSharer);
            String owner = mStorageManager.wasFileSharedWithMe(file.getFileServerId());
            if (owner != null){
                sharer.setText(owner);
            } else {
                sharer.setText(" ");
            }
            
            //Set icons of the file state
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
            //FIXME
            CheckBox checkBoxV = (CheckBox) view.findViewById(R.id.OCKeepInSync);
            checkBoxV.setChecked(file.keepInSync());//avoid loop when init
            checkBoxV.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {    
            @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) { 
                        OCFile file  =  (OCFile)getItem(position);
                        //updating view
                        if(file.keepInSync() == isChecked){            //this is just updating the view, no real changes
                            return;
                        }
                        if (syncedFiles < MAX_SYNCED || !isChecked ){  //if there is space in max_synced or file is unselected
                            Log_OC.d(TAG, "file is " + file.keepInSync() + " and set to" + isChecked );
                     
                           UpdateKeepInSyncFile operation = new UpdateKeepInSyncFile(mContext, file, isChecked); 
                           new Thread(operation).start(); //update the server and sync adapter
                           //FIXME if sucessfull
                           file.setKeepInSync(isChecked); //update the local instance of the file
                           //update sync files count
                           syncedFiles = mContentResolver.query(ProviderTableMeta.CONTENT_URI,      //
                                   null,
                                   ProviderTableMeta.FILE_KEEP_IN_SYNC + " = ?", //files = 1
                                   new String[] {String.valueOf(1)},
                                   null).getCount();
                           Log_OC.d(TAG, "Now there are " + String.valueOf(syncedFiles) + " files");
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
                           
                        } else {                //all the places in max_synced are taken
                            buttonView.setChecked(!isChecked);
                            Toast.makeText(mContext, "Too many synced files", Toast.LENGTH_SHORT).show();
                        }                      
                }
            });
          
            //Setting the file attributes
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
                  
                    final Dialog dialog = new Dialog(mContext); //dialog window
                    final ArrayAdapter<String> sharedWithAdapter; //with whom it was shared already
                    final List<String> sharedWith = mStorageManager.whomIveSharedFileWith(((OCFile) getItem(position)).getFileServerId());
                   
                    
                    //Set up dialog view
                    dialog.setContentView(R.layout.share_file_with);
                    dialog.setTitle("Share");
                    dialog.show();            
                    
                    //Set up text views to enter name, groups
                    final MultiAutoCompleteTextView textViewF = (MultiAutoCompleteTextView)dialog.findViewById(R.id.multiautocompletesharef);
                    textViewF.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
                    textViewF.setThreshold(1);                           //show hint after 1 char
                                                                        //filling it out with friends got from db
                    textViewF.setAdapter(new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1,mStorageManager.getFriendsList()));
                    textViewF.setFocusableInTouchMode(true);
                    textViewF.setHint("Share with friends");
           
                    final MultiAutoCompleteTextView textViewG = (MultiAutoCompleteTextView)dialog.findViewById(R.id.multiautocompleteshareg);
                    textViewG.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
                    textViewG.setThreshold(1);                           //show hint after 1 char
                                                                        //filling it out with friends got from db
                    textViewG.setAdapter(new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1,mStorageManager.getGroupsList()));
                    textViewG.setFocusableInTouchMode(true);
                    textViewG.setHint("Share with groups");
               
                    
                    //Set up list of people this file was shared with already
                    final ListView listview = (ListView)dialog.findViewById(R.id.alreadySharedWithList);
                    sharedWithAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, sharedWith);
                    listview.setAdapter(sharedWithAdapter);
 
                    /**
                     * Message handler for the current thread.
                     * Processes messages, that are coming from the shareFile thread
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
                      * parses user's input and generates HTTP request
                      * */
                     Button okBtn = (Button)dialog.findViewById(R.id.OKBtn);
                     okBtn.setOnClickListener(new OnClickListener() {
                         
                        @Override
                        public void onClick(View v) {
                            
                            //Parsing entered names, splitting it by commas                         
                            StringTokenizer stf = new StringTokenizer(textViewF.getText().toString() , ", ");
                            StringTokenizer stg = new StringTokenizer(textViewG.getText().toString() , ", ");
                            ArrayList<String> friends = new ArrayList<String>();
                            ArrayList<String> groups = new ArrayList<String>();
                            String token;
                                      
                            if(stf.countTokens() == 0 && stg.countTokens() == 0) {                     //entered text is empty
                                textViewF.setHint("Share with friends");
                                textViewG.setHint("Share with groups");
                                Toast.makeText(mContext, "Please enter friend's names or a group with whom you want to share", Toast.LENGTH_SHORT).show();
                            } else {                                        //text is not empty
                                while(stf.hasMoreTokens()){
                                    token = stf.nextToken();
                                    if(sharedWith.contains(token)) {        // have shared with this people already
                                        Toast.makeText(mContext, "You have shared the file with " + token + " already", Toast.LENGTH_SHORT).show();
                                    } else {                                //if token is not empty add it to the list
                                        if(!token.trim().equals("")){
                                               friends.add(token);
                                        }
                                    }
                                 }
                                                                   //text is not empty
                                while(stg.hasMoreTokens()){
                                    token = stg.nextToken();
                                    if(sharedWith.contains(token)) {        // have shared with this people already
                                        Toast.makeText(mContext, "You have shared the file with " + token + " already", Toast.LENGTH_SHORT).show();
                                    } else {                                //if token is not empty add it to the list
                                        if(!token.trim().equals("")){
                                               groups.add(token);
                                        }
                                    }
                                 }
                             }
                            
                            
                            if(friends.size() > 0 || groups.size() > 0) {     //there is nonempty list of names
                                
                                textViewF.setText("");                       //empty the text field
                                textViewG.setText("");  
                               
                                ShareFile operation = new ShareFile(mContext, (OCFile) getItem(position), friends, groups, handler);
                                new Thread(operation).start();   //share in new thread 
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
                    
                    
                    /**
                     * Youtube Button
                     * Uploads video to Youtube
                     * */
                   ImageButton YoutubeBtn = (ImageButton)dialog.findViewById(R.id.youtubeBtn);
                   OCFile f1 = (OCFile)getItem(position);
                   if(!f1.getMimeTypeFromName().startsWith("video/")) {
                       YoutubeBtn.setEnabled(false);
                       YoutubeBtn.setVisibility(android.view.View.INVISIBLE);
                       Log.d(TAG, "File is not a video so Youtube sharing option is disabled");
                   }
                   
                   YoutubeBtn.setOnClickListener(new OnClickListener() {
                       @Override
                       public void onClick(View v) {
                           // Make a call to UploadVideo
                           OCFile file  =  (OCFile)getItem(position);
                           String filename = file.getFileName();
                           Log.d(TAG, "File is a video and will be uploaded to YouTube");
                           /* TODO THIS IS TEMPORARY */
                           /* TODO Launch a new dialog so user can enter comma separated tags */
                           List<String> tags = new ArrayList<String>();
                           tags.add("tag1");
                           tags.add("tag2");
                           
                           // Highly coupled with the order of contents in main_activity_items
                           Intent intent = new Intent(mContext, HelloActivity.class);
                        
                           if (position == 0) {
                               intent.putExtra(HelloActivity.TYPE_KEY, HelloActivity.Type.FOREGROUND.name());
                           } else if (position == 1) {
                               intent.putExtra(HelloActivity.TYPE_KEY, HelloActivity.Type.BACKGROUND.name());
                           } else if (position == 2) {
                               intent.putExtra(HelloActivity.TYPE_KEY, HelloActivity.Type.BACKGROUND_WITH_SYNC.name());
                           }
                           mContext.startActivity(intent);
                           //UploadVideoActivity uv = new UploadVideoActivity(mContext, filename, tags);
                           //uv.run();
                           dialog.dismiss();
                       }
                   });
                }//end of onclick shareButton              
                
            }); //end of ShareButton
           
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
            mStorageManager = (OCDataStorageManager)updatedStorageManager;
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
