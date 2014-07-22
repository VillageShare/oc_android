/**
 * VillageShare project
 * 
 */
package com.owncloud.android.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.owncloud.android.Log_OC;

/**
 * @author Smruthi Manjunath
 *
 */
public class DbShareFile {
    private SQLiteDatabase mDB;
    private OpenerHelper mHelper;
    private final String mDatabaseName = "ownCloudshares";
    private final int mDatabaseVersion = 3;

    private final String TABLE_YOUR_SHARES = "shared_files";
    String[] fileSharedWith = {"sharedWith"};
    String[] filesSharer = {"ownerId,fileName"};
    String[] fileSharesPresent = {"ownerId","sharedWith","fileName"};
    String[] filesSharedBeforeInsert = {"ownerId","sharedWith","filename","fileRemotePath"};
    String[] filesSharedBeforeInsertList = {"ownerId","filename","sharedWith"};

    public DbShareFile(Context context) {
        mHelper = new OpenerHelper(context);
        mDB = mHelper.getWritableDatabase();
    }

    public void close() {
        mDB.close();
    }

    public boolean putNewShares(String fileName,String fileRemotePath,String ownerAccountId, String shareWithAccountId) {
        ContentValues cv = new ContentValues();
        cv.put("ownerId", ownerAccountId );
        cv.put("sharedWith", shareWithAccountId);
        cv.put("fileName", fileName);
        cv.put("fileRemotePath", fileRemotePath);
        Set<String> presentInDatabase = new HashSet<String>();
        Cursor cursor = mDB.query(TABLE_YOUR_SHARES, filesSharedBeforeInsert,null,null,null,null,null);
        if(cursor.moveToFirst()) {
        while (!cursor.isAfterLast()) {
            if( (cursor.getString(0).equals(ownerAccountId) && cursor.getString(1).equals(shareWithAccountId) && cursor.getString(2).equals(fileName) && cursor.getString(3).equals(fileRemotePath)))
                return false;
            cursor.moveToNext();
        }
        }
        long result = mDB.insert(TABLE_YOUR_SHARES, null, cv);
        Log_OC.d(TABLE_YOUR_SHARES, "putNewShare returns with: " + result + " for shares: " + fileName);
        return result != -1;
    }

    public boolean putNewShareList(List<String> sharedList, String accountName) {
        ContentValues cv = new ContentValues();
        long result = -1;
        Cursor cursor = mDB.query(TABLE_YOUR_SHARES, filesSharedBeforeInsertList,null,null,null,null,null);
        cursor.moveToFirst();
        Set<String> presentInDatabase = new HashSet<String>();
        while (!cursor.isAfterLast()) {
            if(cursor.getString(2).equals(accountName))
                presentInDatabase.add(cursor.getString(0)+":"+cursor.getString(1)+":"+cursor.getString(2));
            cursor.moveToNext();
        }
        for(int i = 0;i<sharedList.size();i++) {
            if(!presentInDatabase.contains(sharedList.get(i)+":"+accountName)) {
                String[] shareObj = sharedList.get(i).split(":");
                if(shareObj.length > 1) {
                    cv.put("ownerId", shareObj[0]);
                    cv.put("sharedWith", accountName);
                    cv.put("fileName", shareObj[1]);
                    cv.put("fileRemotePath", "/");
                    result = mDB.insert(TABLE_YOUR_SHARES, null, cv);
                    Log_OC.d(TABLE_YOUR_SHARES, "putNewShareList returns with: " + result + " for shares: " + shareObj[0]);
                }
            }
        }
        return result != -1;


    }
    public List<String> getUsersWithWhomIhaveSharedFile(String fileName, String fileRemotePath, String account,String directionSharing) {
        ContentValues cv = new ContentValues();
        Cursor cursor = mDB.query(TABLE_YOUR_SHARES, fileSharedWith,"ownerId= ? AND fileName=? AND fileRemotePath=?", new String[]{account, fileName, fileRemotePath},null,null,null);
        cursor.moveToFirst();
        List<String> shareList = new ArrayList<String>();
        while (!cursor.isAfterLast()) {
            shareList.add(cursor.getString(0));
            cursor.moveToNext();
        }

        return shareList;
    }

    public Map<String,String> getUsersWhoSharedFilesWithMe(String account) {
        ContentValues cv = new ContentValues();
        Cursor cursor = mDB.query(TABLE_YOUR_SHARES, filesSharer,"sharedWith= ? ", new String[]{account},null,null,null);
        cursor.moveToFirst();
        Map<String,String> shareMap = new HashMap<String,String>();
        while (!cursor.isAfterLast()) {
            shareMap.put(cursor.getString(1).substring(1),cursor.getString(0));
            cursor.moveToNext();
        }

        return shareMap;
    }
    public void clearFiles() {
        mDB.delete(TABLE_YOUR_SHARES, null, null);
    }


    private class OpenerHelper extends SQLiteOpenHelper {
        public OpenerHelper(Context context) {
            super(context, mDatabaseName, null, mDatabaseVersion);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_YOUR_SHARES + " (" + " _id INTEGER PRIMARY KEY, "+" ownerId TEXT, " + " sharedWith TEXT, "+" fileName TEXT, "+" fileRemotePath TEXT);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
