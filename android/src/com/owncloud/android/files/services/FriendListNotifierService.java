/**
 * VillageShare Project
 */
package com.owncloud.android.files.services;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.accounts.Account;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.owncloud.android.R;
import com.owncloud.android.authentication.AccountUtils;
import com.owncloud.android.db.DbFriends;
import com.owncloud.android.ui.activity.AcceptFriendRequestsActivity;
import com.owncloud.android.ui.activity.YourFriendsActivity;
/**
 * 
 * @author Smruthi Manjunath
 *
 */
public class FriendListNotifierService extends IntentService{
    public FriendListNotifierService() {
        super("FriendListNotifierService");
    }
    Account account;
    String username;
    String url;
    ArrayList<String> friendNames;
    
    DbFriends dataSource;
    private NotificationCompat.Builder acceptFriendNotifier;
    NotificationManager notificationManager;
    @Override
    protected void onHandleIntent(Intent arg0) {
        
        account = AccountUtils.getCurrentOwnCloudAccount(getApplicationContext());
        if(account != null) {
        String[] accountUrl = account.name.split("@");
        if(accountUrl.length > 2) {
            username = accountUrl[0]+"@"+accountUrl[1];
            url = accountUrl[2];
        }
        friendNames = new ArrayList<String>();
        
        acceptFriendNotifier = new NotificationCompat.Builder(this)
        .setContentTitle("Friend Notification")
        .setSmallIcon(R.drawable.icon);
        //.setLargeIcon(R.drawable.friendicon);
        dataSource = new DbFriends(this);
        Intent fileShareIntent = new Intent(this,YourFriendsActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, fileShareIntent, 0);
        acceptFriendNotifier.setContentIntent(pIntent);
        acceptFriendNotifier.setAutoCancel(true);
        acceptFriendNotifier.setOnlyAlertOnce(true);
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        
        final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("CURRENTUSER", username));
        HttpPost post = new HttpPost("http://" + url + "/owncloud/index.php/apps/friends/friendlist");
        HttpEntity entity;
        try {
            entity = new UrlEncodedFormEntity(params, "utf-8");
            HttpClient client = new DefaultHttpClient();
            post.setEntity(entity);
            HttpResponse response = client.execute(post);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entityresponse = response.getEntity();
                String jsonentity = EntityUtils.toString(entityresponse);
                JSONObject obj = new JSONObject(jsonentity);
                JSONObject obj1 = (JSONObject) obj.get("data");

                JSONArray jary = obj1.getJSONArray("friendships");
                
                for (int i = 0; i < jary.length(); i++) {
                    friendNames.add(jary.getString(i));
                    Log.d("friendn ",friendNames.get(i));
                }
                List<String> notificationFor = dataSource.updateFriendStatus(friendNames, username);
                Log.d("************************************** ",notificationFor.size()+" "+notificationFor.get(0));
                if(notificationFor.size() != 0) {
                    for(int i = 0;i<notificationFor.size();i++) {
                        Log.d("notific receivedfriend request ",notificationFor.get(i));
                         acceptFriendNotifier.setContentText(notificationFor.get(i)+ " and You are friends now!");
                         notificationManager.notify(i,acceptFriendNotifier.build());
                    }
                    dataSource.close();
                }
            }
        } catch(Exception e) {
            
        }
        }
    }
}
