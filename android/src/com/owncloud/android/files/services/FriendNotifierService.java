/**
 * VillageShare project
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
/**
 * 
 * @author Smruthi Manjunath
 *
 */
public class FriendNotifierService extends IntentService {
    public FriendNotifierService() {
        super("FriendNotifierService");
    }
    Account account;
    String username;
    String url;
    ArrayList<String> receivedFriendshipRequestArray;
    
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
        receivedFriendshipRequestArray = new ArrayList<String>();
        
        acceptFriendNotifier = new NotificationCompat.Builder(this)
        .setContentTitle("Received Friend Request")
        .setSmallIcon(R.drawable.icon);
        //.setLargeIcon(R.drawable.friendicon);
        dataSource = new DbFriends(this);
        Intent fileShareIntent = new Intent(this,AcceptFriendRequestsActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, fileShareIntent, 0);
        acceptFriendNotifier.setContentIntent(pIntent);
        acceptFriendNotifier.setAutoCancel(true);
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        
        final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("CURRENTUSER", username));
        HttpPost post = new HttpPost("http://" + url + "/owncloud/index.php/apps/friends/getfriendrequest");
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
                JSONArray jary1 = obj1.getJSONArray("receivedFriendshipRequests");
                
                for (int i = 0; i < jary1.length(); i++) {
                    receivedFriendshipRequestArray.add(jary1.getString(i));
                    Log.d("receivedfriend request ",jary1.getString(i));
                }
                List<String> notificationFor = dataSource.updateFriendRequestStatus(receivedFriendshipRequestArray, username);
                Log.d("************************************** ",notificationFor.size()+" "+notificationFor.get(0));
                if(notificationFor.size() != 0) {
                    for(int i = 0;i<notificationFor.size();i++) {
                        Log.d("notific receivedfriend request ",notificationFor.get(i));
                         acceptFriendNotifier.setContentText(notificationFor.get(i)+ " has sent you a friend request");
                         notificationManager.notify(0,acceptFriendNotifier.build());
                    }
                    dataSource.close();
                }
            }
        } catch(Exception e) {
            
        }
        }
    }
}
