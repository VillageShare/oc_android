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

package com.owncloud.android.syncadapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jackrabbit.webdav.DavException;

import com.owncloud.android.Log_OC;
import com.owncloud.android.R;
import com.owncloud.android.authentication.AccountAuthenticator;
import com.owncloud.android.authentication.AuthenticatorActivity;
import com.owncloud.android.datamodel.DataStorageManager;
import com.owncloud.android.datamodel.OCDataStorageManager;
import com.owncloud.android.datamodel.OCFile;
import com.owncloud.android.db.ProviderMeta.ProviderTableMeta;
import com.owncloud.android.operations.RemoteOperationResult;
import com.owncloud.android.operations.SynchronizeFolderOperation;
import com.owncloud.android.operations.SynchronizeFriends;
import com.owncloud.android.operations.SynchronizeGroups;
import com.owncloud.android.operations.SynchronizeKeepInSyncFiles;
import com.owncloud.android.operations.SynchronizeServerIds;
import com.owncloud.android.operations.SynchronizeSharedByMeData;
import com.owncloud.android.operations.SynchronizeSharedWithMeData;
import com.owncloud.android.operations.UpdateOCVersionOperation;
import com.owncloud.android.operations.RemoteOperationResult.ResultCode;
import com.owncloud.android.ui.activity.ErrorsWhileCopyingHandlerActivity;
import com.owncloud.android.utils.FileStorageUtils;

import android.accounts.Account;
import android.accounts.AccountsException;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;

/**
 * SyncAdapter implementation for syncing sample SyncAdapter contacts to the
 * platform ContactOperations provider.
 * 
 * @author Bartek Przybylski
 * @author David A. Velasco
 */
public class OCDataSyncAdapter extends AbstractOwnCloudSyncAdapter {

    private final static String TAG = "FileSyncAdapter";

    /** 
     * Maximum number of failed folder synchronizations that are supported before finishing the synchronization operation
     */
    private static final int MAX_FAILED_RESULTS = 3; 
    
    private long mCurrentSyncTime;
    private boolean mCancellation;
    private boolean mIsManualSync;
    private int mFailedResultsCounter;    
    private RemoteOperationResult mLastFailedResult;
    private SyncResult mSyncResult;
    private int mConflictsFound;
    private int mFailsInFavouritesFound;
    private Map<String, String> mForgottenLocalFiles;

    
    public OCDataSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void onPerformSync(Account account, Bundle extras,
            String authority, ContentProviderClient provider,
            SyncResult syncResult) {

        mCancellation = false;
        mIsManualSync = extras.getBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, false);
        mFailedResultsCounter = 0;
        mLastFailedResult = null;
        mConflictsFound = 0;
        mFailsInFavouritesFound = 0;
        mForgottenLocalFiles = new HashMap<String, String>();
        mSyncResult = syncResult;
        mSyncResult.fullSyncRequested = false;
        mSyncResult.delayUntil = 60*60*24; // sync after 24h

        this.setAccount(account);
        this.setContentProvider(provider);
        this.setStorageManager(new OCDataStorageManager(account, getContentProvider()));
        try {
            this.initClientForCurrentAccount();
        } catch (IOException e) {
            /// the account is unknown for the Synchronization Manager, or unreachable for this context; don't try this again
            mSyncResult.tooManyRetries = true;
            notifyFailedSynchronization();
            return;
        } catch (AccountsException e) {
            /// the account is unknown for the Synchronization Manager, or unreachable for this context; don't try this again
            mSyncResult.tooManyRetries = true;
            notifyFailedSynchronization();
            return;
        }
        
        Log_OC.d(TAG, "Synchronization of ownCloud account " + account.name + " starting");
        sendStickyBroadcast(true, null, null);  // message to signal the start of the synchronization to the UI
        
        /**
         * UPDATING DATA
         */
        try {
            updateOCVersion();
            mCurrentSyncTime = System.currentTimeMillis();
            if (!mCancellation) {
                //UPDATE LIST OF FILES
                fetchFilesData(OCFile.PATH_SEPARATOR, DataStorageManager.ROOT_PARENT_ID); //fetch everything from the root
                fetchKeepInSync();
                //TEST
                //((OCDataStorageManager)getStorageManager()).printTable(ProviderTableMeta.CONTENT_URI);//default file table
                //UPDATE SERVER IDs
                fetchServerIds();
                //((OCDataStorageManager)getStorageManager()).printTable(ProviderTableMeta.CONTENT_URI);//default file table              
                //UPDATE SHARED FILES TABLE    
                fetchSharedWithMeData();
                fetchSharedByMeData();
                //UDATE GROUPS
                fetchGroups();
                //TEST
                //((OCDataStorageManager)getStorageManager()).printTable(ProviderTableMeta.CONTENT_URI_SHARED_FILES);
                fetchFriends();
                
                
            } else {
                Log_OC.d(TAG, "Leaving synchronization before any remote request due to cancellation was requested");
            }
            
            
        } finally {
            // it's important making this although very unexpected errors occur; that's the reason for the finally
            
            if (mFailedResultsCounter > 0 && mIsManualSync) {
                /// don't let the system synchronization manager retries MANUAL synchronizations
                //      (be careful: "MANUAL" currently includes the synchronization requested when a new account is created and when the user changes the current account)
                mSyncResult.tooManyRetries = true;
                
                /// notify the user about the failure of MANUAL synchronization
                notifyFailedSynchronization();
                
            }
            if (mConflictsFound > 0 || mFailsInFavouritesFound > 0) {
                notifyFailsInFavourites();
            }
            if (mForgottenLocalFiles.size() > 0) {
                notifyForgottenLocalFiles();
                
            }
            sendStickyBroadcast(false, null, mLastFailedResult);        // message to signal the end to the UI
        }
        
    }
    
    /**
     * Called by system SyncManager when a synchronization is required to be cancelled.
     * 
     * Sets the mCancellation flag to 'true'. THe synchronization will be stopped when before a new folder is fetched. Data of the last folder
     * fetched will be still saved in the database. See onPerformSync implementation.
     */
    @Override
    public void onSyncCanceled() {
        Log_OC.d(TAG, "Synchronization of " + getAccount().name + " has been requested to cancel");
        mCancellation = true;
        super.onSyncCanceled();
    }
    
    
    /**
     * Updates the locally stored version value of the ownCloud server
     */
    private void updateOCVersion() {
        UpdateOCVersionOperation update = new UpdateOCVersionOperation(getAccount(), getContext());
        RemoteOperationResult result = update.execute(getClient());
        if (!result.isSuccess()) {
            mLastFailedResult = result; 
        }
    }
    
    
    /**
     * Synchronize the properties of files and folders contained in a remote folder given by remotePath.
     * 
     * @param remotePath        Remote path to the folder to synchronize.
     * @param parentId          Database Id of the folder to synchronize.
     */
    private void fetchFilesData(String remotePath, long parentId) {
        
        if (mFailedResultsCounter > MAX_FAILED_RESULTS || isFinisher(mLastFailedResult))
            return;
        
        // perform folder synchronization
        SynchronizeFolderOperation synchFolderOp = new SynchronizeFolderOperation(  remotePath, 
                                                                                    mCurrentSyncTime, 
                                                                                    parentId, 
                                                                                    getStorageManager(), 
                                                                                    getAccount(), 
                                                                                    getContext()
                                                                                  );
        RemoteOperationResult result = synchFolderOp.execute(getClient());
        
        
        // synchronized folder -> notice to UI - ALWAYS, although !result.isSuccess
        //sendStickyBroadcast(true, remotePath, null);
        
        if (result.isSuccess() || result.getCode() == ResultCode.SYNC_CONFLICT) {
            
            if (result.getCode() == ResultCode.SYNC_CONFLICT) {
                mConflictsFound += synchFolderOp.getConflictsFound();
                mFailsInFavouritesFound += synchFolderOp.getFailsInFavouritesFound();
            }
            if (synchFolderOp.getForgottenLocalFiles().size() > 0) {
                mForgottenLocalFiles.putAll(synchFolderOp.getForgottenLocalFiles());
            }
            // synchronize children folders 
            List<OCFile> children = synchFolderOp.getChildren();
            fetchChildren(children);    // beware of the 'hidden' recursion here!
            
            sendStickyBroadcast(true, remotePath, null);
            
        } else {
            if (result.getCode() == RemoteOperationResult.ResultCode.UNAUTHORIZED ||
                   // (result.isTemporalRedirection() && result.isIdPRedirection() &&
                    ( result.isIdPRedirection() && 
                            AccountAuthenticator.AUTH_TOKEN_TYPE_SAML_WEB_SSO_SESSION_COOKIE.equals(getClient().getAuthTokenType()))) {
                mSyncResult.stats.numAuthExceptions++;
                
            } else if (result.getException() instanceof DavException) {
                mSyncResult.stats.numParseExceptions++;
                
            } else if (result.getException() instanceof IOException) { 
                mSyncResult.stats.numIoExceptions++;
            }
            mFailedResultsCounter++;
            mLastFailedResult = result;
        }
            
    }

    /**
     * Checks if a failed result should terminate the synchronization process immediately, according to
     * OUR OWN POLICY
     * 
     * @param   failedResult        Remote operation result to check.
     * @return                      'True' if the result should immediately finish the synchronization
     */
    private boolean isFinisher(RemoteOperationResult failedResult) {
        if  (failedResult != null) {
            RemoteOperationResult.ResultCode code = failedResult.getCode();
            return (code.equals(RemoteOperationResult.ResultCode.SSL_ERROR) ||
                    code.equals(RemoteOperationResult.ResultCode.SSL_RECOVERABLE_PEER_UNVERIFIED) ||
                    code.equals(RemoteOperationResult.ResultCode.BAD_OC_VERSION) ||
                    code.equals(RemoteOperationResult.ResultCode.INSTANCE_NOT_CONFIGURED));
        }
        return false;
    }

    /**
     * Synchronize data of folders in the list of received files
     * 
     * @param files         Files to recursively fetch 
     */
    private void fetchChildren(List<OCFile> files) {
        int i;
        for (i=0; i < files.size() && !mCancellation; i++) {
            OCFile newFile = files.get(i);
            Log_OC.d("file ", newFile.getFileName()+" "+newFile.getFileId()+" "+newFile.getEtag());
            if (newFile.isDirectory()) {
                fetchFilesData(newFile.getRemotePath(), newFile.getFileId());
                
                // Update folder size on DB
                getStorageManager().calculateFolderSize(newFile.getFileId());                   
            }
        }
       
        if (mCancellation && i <files.size()) Log_OC.d(TAG, "Leaving synchronization before synchronizing " + files.get(i).getRemotePath() + " because cancelation request");
    }

    
    /**
     * Sends a message to any application component interested in the progress of the synchronization.
     * 
     * @param inProgress        'True' when the synchronization progress is not finished.
     * @param dirRemotePath     Remote path of a folder that was just synchronized (with or without success)
     */
    private void sendStickyBroadcast(boolean inProgress, String dirRemotePath, RemoteOperationResult result) {
        Intent i = new Intent(OCDataSyncService.SYNC_MESSAGE);
        i.putExtra(OCDataSyncService.IN_PROGRESS, inProgress);
        i.putExtra(OCDataSyncService.ACCOUNT_NAME, getAccount().name);
        if (dirRemotePath != null) {
            i.putExtra(OCDataSyncService.SYNC_FOLDER_REMOTE_PATH, dirRemotePath);
        }
        if (result != null) {
            i.putExtra(OCDataSyncService.SYNC_RESULT, result);
        }
        getContext().sendStickyBroadcast(i);
    }

    
    
    /**
     * Notifies the user about a failed synchronization through the status notification bar 
     */
    private void notifyFailedSynchronization() {
        Notification notification = new Notification(R.drawable.icon, getContext().getString(R.string.sync_fail_ticker), System.currentTimeMillis());
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        boolean needsToUpdateCredentials = (mLastFailedResult != null && 
                                             (  mLastFailedResult.getCode() == ResultCode.UNAUTHORIZED ||
                                                // (mLastFailedResult.isTemporalRedirection() && mLastFailedResult.isIdPRedirection() && 
                                                ( mLastFailedResult.isIdPRedirection() && 
                                                 AccountAuthenticator.AUTH_TOKEN_TYPE_SAML_WEB_SSO_SESSION_COOKIE.equals(getClient().getAuthTokenType()))
                                             )
                                           );
        // TODO put something smart in the contentIntent below for all the possible errors
        notification.contentIntent = PendingIntent.getActivity(getContext().getApplicationContext(), (int)System.currentTimeMillis(), new Intent(), 0);
        if (needsToUpdateCredentials) {
            // let the user update credentials with one click
            Intent updateAccountCredentials = new Intent(getContext(), AuthenticatorActivity.class);
            updateAccountCredentials.putExtra(AuthenticatorActivity.EXTRA_ACCOUNT, getAccount());
            updateAccountCredentials.putExtra(AuthenticatorActivity.EXTRA_ENFORCED_UPDATE, true);
            updateAccountCredentials.putExtra(AuthenticatorActivity.EXTRA_ACTION, AuthenticatorActivity.ACTION_UPDATE_TOKEN);
            updateAccountCredentials.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            updateAccountCredentials.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            updateAccountCredentials.addFlags(Intent.FLAG_FROM_BACKGROUND);
            notification.contentIntent = PendingIntent.getActivity(getContext(), (int)System.currentTimeMillis(), updateAccountCredentials, PendingIntent.FLAG_ONE_SHOT);
            notification.setLatestEventInfo(getContext().getApplicationContext(), 
                    getContext().getString(R.string.sync_fail_ticker), 
                    String.format(getContext().getString(R.string.sync_fail_content_unauthorized), getAccount().name), 
                    notification.contentIntent);
        } else {
            notification.setLatestEventInfo(getContext().getApplicationContext(), 
                                            getContext().getString(R.string.sync_fail_ticker), 
                                            String.format(getContext().getString(R.string.sync_fail_content), getAccount().name), 
                                            notification.contentIntent);
        }
        ((NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE)).notify(R.string.sync_fail_ticker, notification);
    }


    /**
     * Notifies the user about conflicts and strange fails when trying to synchronize the contents of kept-in-sync files.
     * 
     * By now, we won't consider a failed synchronization.
     */
    private void notifyFailsInFavourites() {
        if (mFailedResultsCounter > 0) {
            Notification notification = new Notification(R.drawable.icon, getContext().getString(R.string.sync_fail_in_favourites_ticker), System.currentTimeMillis());
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            // TODO put something smart in the contentIntent below
            notification.contentIntent = PendingIntent.getActivity(getContext().getApplicationContext(), (int)System.currentTimeMillis(), new Intent(), 0);
            notification.setLatestEventInfo(getContext().getApplicationContext(), 
                                            getContext().getString(R.string.sync_fail_in_favourites_ticker), 
                                            String.format(getContext().getString(R.string.sync_fail_in_favourites_content), mFailedResultsCounter + mConflictsFound, mConflictsFound), 
                                            notification.contentIntent);
            ((NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE)).notify(R.string.sync_fail_in_favourites_ticker, notification);
            
        } else {
            Notification notification = new Notification(R.drawable.icon, getContext().getString(R.string.sync_conflicts_in_favourites_ticker), System.currentTimeMillis());
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            // TODO put something smart in the contentIntent below
            notification.contentIntent = PendingIntent.getActivity(getContext().getApplicationContext(), (int)System.currentTimeMillis(), new Intent(), 0);
            notification.setLatestEventInfo(getContext().getApplicationContext(), 
                                            getContext().getString(R.string.sync_conflicts_in_favourites_ticker), 
                                            String.format(getContext().getString(R.string.sync_conflicts_in_favourites_content), mConflictsFound), 
                                            notification.contentIntent);
            ((NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE)).notify(R.string.sync_conflicts_in_favourites_ticker, notification);
        } 
    }
    
    /**
     * Notifies the user about local copies of files out of the ownCloud local directory that were 'forgotten' because 
     * copying them inside the ownCloud local directory was not possible.
     * 
     * We don't want links to files out of the ownCloud local directory (foreign files) anymore. It's easy to have 
     * synchronization problems if a local file is linked to more than one remote file.
     * 
     * We won't consider a synchronization as failed when foreign files can not be copied to the ownCloud local directory.
     */
    private void notifyForgottenLocalFiles() {
        Notification notification = new Notification(R.drawable.icon, getContext().getString(R.string.sync_foreign_files_forgotten_ticker), System.currentTimeMillis());
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        /// includes a pending intent in the notification showing a more detailed explanation
        Intent explanationIntent = new Intent(getContext(), ErrorsWhileCopyingHandlerActivity.class);
        explanationIntent.putExtra(ErrorsWhileCopyingHandlerActivity.EXTRA_ACCOUNT, getAccount());
        ArrayList<String> remotePaths = new ArrayList<String>();
        ArrayList<String> localPaths = new ArrayList<String>();
        remotePaths.addAll(mForgottenLocalFiles.keySet());
        localPaths.addAll(mForgottenLocalFiles.values());
        explanationIntent.putExtra(ErrorsWhileCopyingHandlerActivity.EXTRA_LOCAL_PATHS, localPaths);
        explanationIntent.putExtra(ErrorsWhileCopyingHandlerActivity.EXTRA_REMOTE_PATHS, remotePaths);  
        explanationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        notification.contentIntent = PendingIntent.getActivity(getContext().getApplicationContext(), (int)System.currentTimeMillis(), explanationIntent, 0);
        notification.setLatestEventInfo(getContext().getApplicationContext(), 
                                        getContext().getString(R.string.sync_foreign_files_forgotten_ticker), 
                                        String.format(getContext().getString(R.string.sync_foreign_files_forgotten_content), mForgottenLocalFiles.size(), getContext().getString(R.string.app_name)), 
                                        notification.contentIntent);
        ((NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE)).notify(R.string.sync_foreign_files_forgotten_ticker, notification);
        
    }
    /**
     * This method makes call to the server and updates Shared Files
     */
    private void fetchSharedWithMeData(){
        //get data from the server and update table
        SynchronizeSharedWithMeData operation = new SynchronizeSharedWithMeData(getContext());
        new Thread(operation).start();
        //status check is in the operation
    }
    
    /**
     * This method makes call to the server and updates Shared Files
     */
    private void fetchSharedByMeData(){
        //get data from the server and update table
        SynchronizeSharedByMeData operation = new SynchronizeSharedByMeData(getContext());
        new Thread(operation).start();
        //status check is in the operation
    }
    /**
     * This method makes call to the server and updates Shared Files
     */
    private void fetchServerIds(){
        //get data from the server and update table
        SynchronizeServerIds operation = new SynchronizeServerIds(getContext());
        new Thread(operation).start();
        //status check is in the operation
    }
    /**
     * This method makes call to the server and updates Groups
     */
    private void fetchGroups(){
        //get data from the server and update table
        SynchronizeGroups operation = new SynchronizeGroups(getContext());
        new Thread(operation).start();
        //status check is in the operation
    }
    private void fetchFriends(){
        //get data from the server and update table
        SynchronizeFriends operation = new SynchronizeFriends(getContext());
        new Thread(operation).start();
        //status check is in the operation
    }
    private void fetchKeepInSync(){
        SynchronizeKeepInSyncFiles operation = new SynchronizeKeepInSyncFiles(getContext());
        new Thread(operation).start();
    }
}
