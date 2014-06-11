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

package com.owncloud.android.providers;

import java.util.HashMap;

import com.owncloud.android.Log_OC;
import com.owncloud.android.db.ProviderMeta;
import com.owncloud.android.db.ProviderMeta.ProviderTableMeta;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

/**
 * The ContentProvider for the ownCloud App.
 * 
 * @author Bartek Przybylski
 * 
 */
public class FileContentProvider extends ContentProvider {

    private DataBaseHelper mDbHelper;
   
    private static final String TAG = FileContentProvider.class.getSimpleName();
    private static final int SINGLE_FILE = 1;
    private static final int DIRECTORY = 2;
    private static final int ROOT_DIRECTORY = 3;
    private static final int SHARED_WITH_ME = 4;
    private static final int SHARED_BY_ME = 5;
    private static final int FRIEND = 6;
    private static final int GROUP = 7;
    
    // maps from column names that the caller passes into query to database column names
    private static HashMap<String, String> mFilesProjectionMap,
                                           mSharedWithMeProjectionMap,
                                           mSharedByMeProjectionMap,
                                           mGroupsProjectionMap,
                                           mFriendsProjectionMap;
    
    static {
        mFilesProjectionMap = new HashMap<String, String>();
        mFilesProjectionMap.put(ProviderTableMeta._ID, ProviderTableMeta._ID);
        mFilesProjectionMap.put(ProviderTableMeta.FILE_PARENT,
                ProviderTableMeta.FILE_PARENT);
        mFilesProjectionMap.put(ProviderTableMeta.FILE_PATH,
                ProviderTableMeta.FILE_PATH);
        mFilesProjectionMap.put(ProviderTableMeta.FILE_NAME,
                ProviderTableMeta.FILE_NAME);
        mFilesProjectionMap.put(ProviderTableMeta.FILE_CREATION,
                ProviderTableMeta.FILE_CREATION);
        mFilesProjectionMap.put(ProviderTableMeta.FILE_MODIFIED,
                ProviderTableMeta.FILE_MODIFIED);
        mFilesProjectionMap.put(ProviderTableMeta.FILE_MODIFIED_AT_LAST_SYNC_FOR_DATA,
                ProviderTableMeta.FILE_MODIFIED_AT_LAST_SYNC_FOR_DATA);
        mFilesProjectionMap.put(ProviderTableMeta.FILE_CONTENT_LENGTH,
                ProviderTableMeta.FILE_CONTENT_LENGTH);
        mFilesProjectionMap.put(ProviderTableMeta.FILE_CONTENT_TYPE,
                ProviderTableMeta.FILE_CONTENT_TYPE);
        mFilesProjectionMap.put(ProviderTableMeta.FILE_STORAGE_PATH,
                ProviderTableMeta.FILE_STORAGE_PATH);
        mFilesProjectionMap.put(ProviderTableMeta.FILE_LAST_SYNC_DATE,
                ProviderTableMeta.FILE_LAST_SYNC_DATE);
        mFilesProjectionMap.put(ProviderTableMeta.FILE_LAST_SYNC_DATE_FOR_DATA,
                ProviderTableMeta.FILE_LAST_SYNC_DATE_FOR_DATA);
        mFilesProjectionMap.put(ProviderTableMeta.FILE_KEEP_IN_SYNC,
                ProviderTableMeta.FILE_KEEP_IN_SYNC);
        mFilesProjectionMap.put(ProviderTableMeta.FILE_ACCOUNT_OWNER,
                ProviderTableMeta.FILE_ACCOUNT_OWNER);
        mFilesProjectionMap.put(ProviderTableMeta.FILE_SERVER_ID,
                ProviderTableMeta.FILE_SERVER_ID);
    }
    static {
        mSharedWithMeProjectionMap = new HashMap<String, String>();
        mSharedWithMeProjectionMap.put(ProviderTableMeta.SHAREDWM_FILE_SERVER_ID, ProviderTableMeta.SHAREDWM_FILE_SERVER_ID);
        mSharedWithMeProjectionMap.put(ProviderTableMeta.SHAREDWM_FILE_NAME,
                ProviderTableMeta.SHAREDWM_FILE_NAME);
        mSharedWithMeProjectionMap.put(ProviderTableMeta.SHAREDWM_OWNER_LOCATION,
                ProviderTableMeta.SHAREDWM_OWNER_LOCATION);
    }
    static {
        mSharedByMeProjectionMap = new HashMap<String, String>();
        mSharedByMeProjectionMap.put(ProviderTableMeta.SHAREDBM_FILE_SERVER_ID, ProviderTableMeta.SHAREDBM_FILE_SERVER_ID);
        mSharedByMeProjectionMap.put(ProviderTableMeta.SHAREDBM_USER_LOCATION,
                ProviderTableMeta.SHAREDBM_USER_LOCATION);
    }
    static {
        mGroupsProjectionMap = new HashMap<String, String>();
        mGroupsProjectionMap.put(ProviderTableMeta._ID, ProviderTableMeta._ID);
        mGroupsProjectionMap.put(ProviderTableMeta.GROUP, ProviderTableMeta.GROUP);
        mGroupsProjectionMap.put(ProviderTableMeta.GROUP_ADMIN,
                ProviderTableMeta.GROUP_ADMIN);
    }
    static {
        mFriendsProjectionMap = new HashMap<String, String>();
        mFriendsProjectionMap.put(ProviderTableMeta._ID, ProviderTableMeta._ID);
        mFriendsProjectionMap.put(ProviderTableMeta.FRIEND, ProviderTableMeta.FRIEND);
    }
    
    private static final UriMatcher mUriMatcher;
    static {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(ProviderMeta.AUTHORITY, null, ROOT_DIRECTORY);
        mUriMatcher.addURI(ProviderMeta.AUTHORITY, "file/", SINGLE_FILE);
        mUriMatcher.addURI(ProviderMeta.AUTHORITY, "file/#", SINGLE_FILE);
        mUriMatcher.addURI(ProviderMeta.AUTHORITY, "dir/#", DIRECTORY);
        mUriMatcher.addURI(ProviderMeta.AUTHORITY, "sharedwithme/", SHARED_WITH_ME);
        mUriMatcher.addURI(ProviderMeta.AUTHORITY, "sharedbyme/", SHARED_BY_ME);
        mUriMatcher.addURI(ProviderMeta.AUTHORITY, "groups/", GROUP);
        mUriMatcher.addURI(ProviderMeta.AUTHORITY, "friends/", FRIEND);
        
    }

    
    @Override
    public boolean onCreate() {
        mDbHelper = new DataBaseHelper(getContext());
        return true;
    }
    
    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int count = 0;      
        switch (mUriMatcher.match(uri)) {
        case SINGLE_FILE:
            count = db.delete(ProviderTableMeta.FILES_TABLE_NAME,
                    ProviderTableMeta._ID
                            + "="
                            + uri.getPathSegments().get(1)
                            + (!TextUtils.isEmpty(where) ? " AND (" + where
                                    + ")" : ""), whereArgs);
            break;
        case ROOT_DIRECTORY:
            count = db.delete(ProviderTableMeta.FILES_TABLE_NAME, where, whereArgs);
            break;
        case SHARED_WITH_ME: 
            //the file will be deleted by matching where and where args (name, owner, location)
            count = db.delete(ProviderTableMeta.SHARED_WITH_ME_TABLE_NAME, where, whereArgs);
            break;
        case SHARED_BY_ME: 
            //the file will be deleted by matching where and where args (name, owner, location)
            count = db.delete(ProviderTableMeta.SHARED_BY_ME_TABLE_NAME, where, whereArgs);
            break;
        case GROUP: 
            //the file will be deleted by matching where and where args (name, owner, location)
            count = db.delete(ProviderTableMeta.GROUPS_TABLE_NAME, where, whereArgs);
            break;
        case FRIEND: 
            //the file will be deleted by matching where and where args (name, owner, location)
            count = db.delete(ProviderTableMeta.FRIENDS_TABLE_NAME, where, whereArgs);
            break;
        default:
            throw new IllegalArgumentException("Unknown uri: " + uri.toString());
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
    //Only used in Uploader to check for file type.
    @Override
    public String getType(Uri uri) {
        switch (mUriMatcher.match(uri)) {
        case ROOT_DIRECTORY:
            return ProviderTableMeta.CONTENT_TYPE;
        case SINGLE_FILE:
            return ProviderTableMeta.CONTENT_TYPE_ITEM;
        default:
            throw new IllegalArgumentException("Unknown Uri id."
                    + uri.toString());
        }
    }

    @Override
    
    //seems like for FILE db it is only used in FileDataStorageManager with insert.(CONTENT_URI_FILE)
    //or batch operations to the root folder
    public Uri insert(Uri uri, ContentValues values) {
        
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long rowId;
        Uri insertedFileUri = null;
        switch (mUriMatcher.match(uri)){ 
        
        //FILES TABLE
        case ROOT_DIRECTORY: //comes from batch operation; just continue to the next case
        case SINGLE_FILE:
            
            rowId = db.insert(ProviderTableMeta.FILES_TABLE_NAME, null, values);
            if (rowId > 0) { //-1 on error
                insertedFileUri = ContentUris.withAppendedId(   //return an uri"content://" + AUTHORITY + "/file/ + rowID"
                        ProviderTableMeta.CONTENT_URI_FILE, rowId);
                
                //Notification
                getContext().getContentResolver().notifyChange(insertedFileUri,
                        null);
            }
            break;
            
        //SHARE TABLE
        case SHARED_WITH_ME:
            rowId = db.insert(ProviderTableMeta.SHARED_WITH_ME_TABLE_NAME, null, values);
            if (rowId > 0) {
                insertedFileUri = ContentUris.withAppendedId(   //return an uri"content://" + AUTHORITY + "/shared/ + rowID"
                        ProviderTableMeta.CONTENT_URI_SHARED_WITH_ME, rowId);
            } //return row id else return null
            break;
        case SHARED_BY_ME:
            rowId = db.insert(ProviderTableMeta.SHARED_BY_ME_TABLE_NAME, null, values);
            if (rowId > 0) {
                insertedFileUri = ContentUris.withAppendedId(   //return an uri"content://" + AUTHORITY + "/shared/ + rowID"
                        ProviderTableMeta.CONTENT_URI_SHARED_BY_ME, rowId);
            } //return row id else return null
            break;
        case GROUP:
            rowId = db.insert(ProviderTableMeta.GROUPS_TABLE_NAME, null, values);
            if (rowId > 0) {
                insertedFileUri = ContentUris.withAppendedId(   //return an uri"content://" + AUTHORITY + "/shared/ + rowID"
                        ProviderTableMeta.CONTENT_URI_GROUPS, rowId);
            } //return row id else return null
            break;
        case FRIEND:
            rowId = db.insert(ProviderTableMeta.FRIENDS_TABLE_NAME, null, values);
            if (rowId > 0) {
                insertedFileUri = ContentUris.withAppendedId(   //return an uri"content://" + AUTHORITY + "/shared/ + rowID"
                        ProviderTableMeta.CONTENT_URI_FRIENDS, rowId);
            } //return row id else return null
            break;
        default:
            //throw new SQLException("ERROR " + uri); 
            throw new IllegalArgumentException("Unknown uri id: " + uri);
        }
        return insertedFileUri;
    }

    

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, //projections are not used and are set in this method
            String[] selectionArgs, String sortOrder) {
        
        SQLiteQueryBuilder sqlQuery = new SQLiteQueryBuilder();
        String order;
        

        switch (mUriMatcher.match(uri)) {
        case ROOT_DIRECTORY://add nothing, move to the query
            sqlQuery.setTables(ProviderTableMeta.FILES_TABLE_NAME);
            sqlQuery.setProjectionMap(mFilesProjectionMap);
            if (TextUtils.isEmpty(sortOrder)) {
                order = ProviderTableMeta.FILE_DEFAULT_SORT_ORDER;
            } else {
                order = sortOrder;
            }
            break;
        case DIRECTORY://query all the files in this folder
            sqlQuery.setTables(ProviderTableMeta.FILES_TABLE_NAME);
            sqlQuery.setProjectionMap(mFilesProjectionMap);
            sqlQuery.appendWhere(ProviderTableMeta.FILE_PARENT + "="
                    + uri.getPathSegments().get(1));
            if (TextUtils.isEmpty(sortOrder)) {
                order = ProviderTableMeta.FILE_DEFAULT_SORT_ORDER;
            } else {
                order = sortOrder;
            }
            break;
        case SINGLE_FILE://query single file
            sqlQuery.setTables(ProviderTableMeta.FILES_TABLE_NAME);
            sqlQuery.setProjectionMap(mFilesProjectionMap);
            if (uri.getPathSegments().size() > 1) {
                sqlQuery.appendWhere(ProviderTableMeta._ID + "="
                        + uri.getPathSegments().get(1)); 
            }
            if (TextUtils.isEmpty(sortOrder)) {
                order = ProviderTableMeta.FILE_DEFAULT_SORT_ORDER;
            } else {
                order = sortOrder;
            }
            break;
        case SHARED_WITH_ME://query shared file
            sqlQuery.setTables(ProviderTableMeta.SHARED_WITH_ME_TABLE_NAME);
            sqlQuery.setProjectionMap(mSharedWithMeProjectionMap);
            //do  nothing, everything is in the selection.
            if (TextUtils.isEmpty(sortOrder)) {
                order = ProviderTableMeta.SHAREDWM_FILE_DEFAULT_SORT_ORDER;
            } else {
                order = sortOrder;
            }
            break;
        case SHARED_BY_ME://query shared file
            sqlQuery.setTables(ProviderTableMeta.SHARED_BY_ME_TABLE_NAME);
            sqlQuery.setProjectionMap(mSharedByMeProjectionMap);
            //do  nothing, everything is in the selection.
            if (TextUtils.isEmpty(sortOrder)) {
                order = ProviderTableMeta.SHAREDBM_FILE_DEFAULT_SORT_ORDER;
            } else {
                order = sortOrder;
            }
            break;
        case GROUP://query shared file
            sqlQuery.setTables(ProviderTableMeta.GROUPS_TABLE_NAME);
            sqlQuery.setProjectionMap(mGroupsProjectionMap);
            //do  nothing, everything is in the selection.
            if (TextUtils.isEmpty(sortOrder)) {
                order = ProviderTableMeta.GROUP_DEFAULT_SORT_ORDER;
            } else {
                order = sortOrder;
            }
            break;
        case FRIEND://query shared file
            sqlQuery.setTables(ProviderTableMeta.FRIENDS_TABLE_NAME);
            sqlQuery.setProjectionMap(mFriendsProjectionMap);
            //do  nothing, everything is in the selection.
            if (TextUtils.isEmpty(sortOrder)) {
                order = ProviderTableMeta.FRIEND_DEFAULT_SORT_ORDER;
            } else {
                order = sortOrder;
            }
            break;
        default:
            throw new IllegalArgumentException("Unknown uri id: " + uri);
        }
     
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        // DB case_sensitive
        db.execSQL("PRAGMA case_sensitive_like = true");  //operator LIKE is case sensitive now
        
        Cursor c = sqlQuery.query(db, projection, selection, selectionArgs,
                null, null, order);
        //Notiification Part
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        SQLiteDatabase db  = mDbHelper.getWritableDatabase();
        int retVal = 0;
        switch (mUriMatcher.match(uri)) {
        case ROOT_DIRECTORY:
        case DIRECTORY:
        case SINGLE_FILE:
                retVal = db.update(
                ProviderTableMeta.FILES_TABLE_NAME, values, selection, selectionArgs);
                break;
        case SHARED_WITH_ME:
                retVal = db.update(
                ProviderTableMeta.SHARED_WITH_ME_TABLE_NAME, values, selection, selectionArgs);
                break;
        case SHARED_BY_ME:
            retVal = db.update(
            ProviderTableMeta.SHARED_BY_ME_TABLE_NAME, values, selection, selectionArgs);
            break;
        case GROUP:
                retVal = db.update(
                ProviderTableMeta.GROUPS_TABLE_NAME, values, selection, selectionArgs);
                break;
        case FRIEND:
                retVal = db.update(
                ProviderTableMeta.FRIENDS_TABLE_NAME, values, selection, selectionArgs);
                break;
        default:
                retVal = 0;
        }
        return retVal;
    }

    /**
     * 
     * Creates database with multiple tables in it.
     * Each method performs actions on multiple tables.
     *
     */
    
    class DataBaseHelper extends SQLiteOpenHelper {
     
        public DataBaseHelper(Context context) {
            
            super(context, ProviderMeta.OC_DB, null, ProviderMeta.DB_VERSION); //creates  general database
            
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            
            // files table
            Log_OC.d(TAG, "Entering in onCreate");
            db.execSQL("CREATE TABLE "
                    + ProviderTableMeta.FILES_TABLE_NAME + "("
                    + ProviderTableMeta._ID + " INTEGER PRIMARY KEY, "
                    + ProviderTableMeta.FILE_NAME + " TEXT, "
                    + ProviderTableMeta.FILE_PATH + " TEXT, "
                    + ProviderTableMeta.FILE_PARENT + " INTEGER, "
                    + ProviderTableMeta.FILE_CREATION + " INTEGER, "
                    + ProviderTableMeta.FILE_MODIFIED + " INTEGER, "
                    + ProviderTableMeta.FILE_CONTENT_TYPE + " TEXT, "
                    + ProviderTableMeta.FILE_CONTENT_LENGTH + " INTEGER, "
                    + ProviderTableMeta.FILE_STORAGE_PATH + " TEXT, "
                    + ProviderTableMeta.FILE_ACCOUNT_OWNER + " TEXT, "
                    + ProviderTableMeta.FILE_LAST_SYNC_DATE + " INTEGER, "
                    + ProviderTableMeta.FILE_SERVER_ID + " INTEGER, "                
                    + ProviderTableMeta.FILE_KEEP_IN_SYNC + " INTEGER, "
                    + ProviderTableMeta.FILE_LAST_SYNC_DATE_FOR_DATA + " INTEGER, "
                    + ProviderTableMeta.FILE_MODIFIED_AT_LAST_SYNC_FOR_DATA + " INTEGER );"
                    );
            
            //share with me table
            db.execSQL("CREATE TABLE " 
                    + ProviderTableMeta.SHARED_WITH_ME_TABLE_NAME + "("
                    + ProviderTableMeta.SHAREDWM_FILE_SERVER_ID + " INTEGER PRIMARY KEY, "
                    + ProviderTableMeta.SHAREDWM_OWNER_LOCATION + " TEXT, "
                    + ProviderTableMeta.SHAREDWM_FILE_NAME + " TEXT );"     //path which is not really important            
                    );
            //shared by me table
            db.execSQL("CREATE TABLE " 
                    + ProviderTableMeta.SHARED_BY_ME_TABLE_NAME + "("
                    + ProviderTableMeta.SHAREDBM_FILE_SERVER_ID + " INTEGER PRIMARY KEY, "
                    + ProviderTableMeta.SHAREDBM_USER_LOCATION + " TEXT ); "               
                    );
            
            // groups table 
            db.execSQL("CREATE TABLE " 
                    + ProviderTableMeta.GROUPS_TABLE_NAME + "("
                    + ProviderTableMeta._ID + " INTEGER PRIMARY KEY, " //for array adapter
                    + ProviderTableMeta.GROUP + " TEXT, "
                    + ProviderTableMeta.GROUP_ADMIN + " INTEGER ); "                 
                    );
            
          
            // friends table
            db.execSQL("CREATE TABLE " 
                    + ProviderTableMeta.FRIENDS_TABLE_NAME + "("
                    + ProviderTableMeta._ID + " INTEGER PRIMARY KEY, " //for array adapter
                    + ProviderTableMeta.FRIEND + " TEXT ); "                 
                    );
            
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log_OC.i("SQL", "Entering in onUpgrade");
            
            //upgrade for files table
            boolean upgraded = false; 
            if (oldVersion == 1 && newVersion >= 2) {
                Log_OC.i("SQL", "Entering in the #1 ADD in onUpgrade");
                db.execSQL("ALTER TABLE " + ProviderTableMeta.FILES_TABLE_NAME +
                           " ADD COLUMN " + ProviderTableMeta.FILE_KEEP_IN_SYNC  + " INTEGER " +
                           " DEFAULT 0");
                upgraded = true;
            }
            if (oldVersion < 3 && newVersion >= 3) {
                Log_OC.i("SQL", "Entering in the #2 ADD in onUpgrade");
                db.beginTransaction();
                try {
                    db.execSQL("ALTER TABLE " + ProviderTableMeta.FILES_TABLE_NAME +
                               " ADD COLUMN " + ProviderTableMeta.FILE_LAST_SYNC_DATE_FOR_DATA  + " INTEGER " +
                               " DEFAULT 0");
                    
                    // assume there are not local changes pending to upload
                    db.execSQL("UPDATE " + ProviderTableMeta.FILES_TABLE_NAME + 
                            " SET " + ProviderTableMeta.FILE_LAST_SYNC_DATE_FOR_DATA + " = " + System.currentTimeMillis() + 
                            " WHERE " + ProviderTableMeta.FILE_STORAGE_PATH + " IS NOT NULL");
                 
                    upgraded = true;
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }
            if (oldVersion < 4 && newVersion >= 4) {
                Log_OC.i("SQL", "Entering in the #3 ADD in onUpgrade");
                db.beginTransaction();
                try {
                    db .execSQL("ALTER TABLE " + ProviderTableMeta.FILES_TABLE_NAME +
                           " ADD COLUMN " + ProviderTableMeta.FILE_MODIFIED_AT_LAST_SYNC_FOR_DATA  + " INTEGER " +
                           " DEFAULT 0");
                
                    db.execSQL("UPDATE " + ProviderTableMeta.FILES_TABLE_NAME + 
                           " SET " + ProviderTableMeta.FILE_MODIFIED_AT_LAST_SYNC_FOR_DATA + " = " + ProviderTableMeta.FILE_MODIFIED + 
                           " WHERE " + ProviderTableMeta.FILE_STORAGE_PATH + " IS NOT NULL");
                
                    upgraded = true;
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }
            if (!upgraded)
                Log_OC.i("SQL", "OUT of the ADD in onUpgrade; oldVersion == " + oldVersion + ", newVersion == " + newVersion);
        }
        //nothing to be done for other tables
    }

}
