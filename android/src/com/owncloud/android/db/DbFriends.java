/**
 * VillageShare Project
 */
package com.owncloud.android.db;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.owncloud.android.Log_OC;

/**
 * @author Smruthi Manjunath
 *
 */
public class DbFriends {
    private SQLiteDatabase mDB;
    private OpenerHelper mHelper;
    private final String mDatabaseName = "ownCloudfriends";
    private final int mDatabaseVersion = 3;

    private final String TABLE_YOUR_FRIENDS = "your_friends";
    private final String TABLE_ACCEPT_FRIENDS = "received_requests";
    String[] accept_friends_cloumns = {"_ID","friendrequestfrom"};
    String[] your_friends_cloumns = {"_ID","friend"};
    public DbFriends(Context context) {
        mHelper = new OpenerHelper(context);
        mDB = mHelper.getWritableDatabase();
    }

    public void close() {
        mDB.close();
    }

    public boolean putNewFriendRequest(String friendAccountName, String account) {
        ContentValues cv = new ContentValues();
        cv.put("friendRequestFrom", friendAccountName);
        cv.put("account", account);
        long result = mDB.insert(TABLE_ACCEPT_FRIENDS, null, cv);
        Log_OC.d(TABLE_ACCEPT_FRIENDS, "putNewFriendRequest returns with: " + result + " for friend: " + friendAccountName);
        return result != -1;
    }

    public List<String> updateFriendRequestStatus(ArrayList<String> friendRequests,String account) {
        ContentValues cv = new ContentValues();
        Cursor cursor = mDB.query(TABLE_ACCEPT_FRIENDS, accept_friends_cloumns,"account=?",new String[]{account},null,null,null);
        cursor.moveToFirst();
        Set<String> presentInDatabase = new HashSet<String>();
        List<String> sendNotificationList = new ArrayList<String>();
        while (!cursor.isAfterLast()) {
          presentInDatabase.add(cursor.getString(1));
          cursor.moveToNext();
        }
        
        for(int i = 0;i<friendRequests.size();i++) {
            if(!presentInDatabase.contains(friendRequests.get(i))) {
                putNewFriendRequest(friendRequests.get(i), account);
                sendNotificationList.add(friendRequests.get(i));
            }
        }
       for(String s : presentInDatabase) {
           if(!friendRequests.contains(s)) {
                mDB.delete(TABLE_ACCEPT_FRIENDS, "friendrequestfrom = ?", new String[] {s});
           }
       }
       
       return sendNotificationList;
    }

    public boolean putNewFriends(String friendAccountName, String account) {
        ContentValues cv = new ContentValues();
        cv.put("friend", friendAccountName);
        cv.put("account", account);
        long result = mDB.insert(TABLE_YOUR_FRIENDS, null, cv);
        Log_OC.d(TABLE_YOUR_FRIENDS, "putNewFriendRequest returns with: " + result + " for friend: " + friendAccountName);
        return result != -1;
    }

    public ArrayList<String> getFriendList(String account) {
        Cursor cursor = mDB.query(TABLE_YOUR_FRIENDS, your_friends_cloumns,"account=?",new String[]{account},null,null,null);
        cursor.moveToFirst();
        ArrayList<String> sendFriendList = new ArrayList<String>();
        while (!cursor.isAfterLast()) {
          sendFriendList.add(cursor.getString(1));
          cursor.moveToNext();
        }
        return sendFriendList;
    }
    public List<String> updateFriendStatus(ArrayList<String> friendRequests,String account) {
        ContentValues cv = new ContentValues();
        Cursor cursor = mDB.query(TABLE_YOUR_FRIENDS, your_friends_cloumns,null,null,null,null,null);
        cursor.moveToFirst();
        Set<String> presentInDatabase = new HashSet<String>();
        List<String> sendNotificationList = new ArrayList<String>();
        while (!cursor.isAfterLast()) {
          presentInDatabase.add(cursor.getString(1));
          cursor.moveToNext();
        }
        
        for(int i = 0;i<friendRequests.size();i++) {
            if(!presentInDatabase.contains(friendRequests.get(i))) {
                putNewFriends(friendRequests.get(i), account);
                sendNotificationList.add(friendRequests.get(i));
            }
        }
       for(String s : presentInDatabase) {
           if(!friendRequests.contains(s)) {
                mDB.delete(TABLE_YOUR_FRIENDS, "friend = ?", new String[] {s});
           }
       }
       
       return sendNotificationList;
    }

    public void clearFiles() {
        mDB.delete(TABLE_ACCEPT_FRIENDS, null, null);
        mDB.delete(TABLE_YOUR_FRIENDS, null, null);
    }

    
    private class OpenerHelper extends SQLiteOpenHelper {
        public OpenerHelper(Context context) {
            super(context, mDatabaseName, null, mDatabaseVersion);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_YOUR_FRIENDS + " (" + " _id INTEGER PRIMARY KEY, "+" friend TEXT, " + " account TEXT);");
            db.execSQL("CREATE TABLE " + TABLE_ACCEPT_FRIENDS + " (" + " _id INTEGER PRIMARY KEY, "+" friendrequestfrom TEXT, " + " account TEXT);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            
        }
    }
}

