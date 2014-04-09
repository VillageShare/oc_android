/*
 * 
 * 
 * VillageShare
 * 
 */
package com.owncloud.android.ui.activity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.owncloud.android.R;
import com.owncloud.android.authentication.AccountAuthenticator;
import com.owncloud.android.authentication.AccountUtils;
import com.owncloud.android.db.DbFriends;
import com.owncloud.android.utils.OwnCloudVersion;
/**
 * 
 * @author Smruthi Manjunath
 *
 */
public class AcceptFriendRequestsActivity extends Activity {

    String accountname;
    ListView listView1;
    String username;
    String url;
    friendArrayAdapter friendadapter;
    ArrayList<String> receivedFriendshipRequestArray;
    JSONArray jary1;
    String TAG = "AcceptFriendRequestsActivity";
    
    DbFriends dataSource;
    private NotificationCompat.Builder acceptFriendNotifier;
    NotificationManager notificationManager;

    @Override
    public void onCreate(Bundle SavedInstanceState) {

        super.onCreate(SavedInstanceState);
        dataSource = new DbFriends(this);
        setContentView(R.layout.accept_friendstab);
        AccountManager am = AccountManager.get(this);
        Account account = AccountUtils.getCurrentOwnCloudAccount(this);
        String[] url1 = (am.getUserData(account, AccountAuthenticator.KEY_OC_BASE_URL)).split("/");
        url = url1[2];
        
        acceptFriendNotifier = new NotificationCompat.Builder(this)
        .setContentTitle("Received Friend Request")
        .setSmallIcon(R.drawable.icon);
        
        Intent fileShareIntent = new Intent(this,AcceptFriendRequestsActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, fileShareIntent, 0);
        acceptFriendNotifier.setContentIntent(pIntent);
        acceptFriendNotifier.setAutoCancel(true);
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        
        listView1 = (ListView) findViewById(R.id.listViewAccept);
        receivedFriendshipRequestArray = new ArrayList<String>();
        friendadapter = new friendArrayAdapter(this, R.layout.listacceptremove, receivedFriendshipRequestArray);
        listView1.setAdapter(friendadapter);

        accountname = account.name;
        String accountName[] = accountname.split("@");
        
        if (accountName.length > 2)
            username = accountName[0] + "@" + accountName[1];
        else {
            throw new NullPointerException("Account name not in correct format");
        }
        final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("CURRENTUSER", username));
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
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
                        jary1 = obj1.getJSONArray("receivedFriendshipRequests");
                        runOnUiThread(new Runnable() {
                            public void run() {
                                notifyDataChanged();
                            }
                        });
                        for (int i = 0; i < jary1.length(); i++) {
                            receivedFriendshipRequestArray.add(jary1.getString(i));
                        }
                        List<String> notificationFor = dataSource.updateFriendRequestStatus(receivedFriendshipRequestArray, username);
                        
                        if(notificationFor.size() != 0) {
                            for(int i = 0;i<notificationFor.size();i++) {
                                 acceptFriendNotifier.setContentText(notificationFor.get(i)+ " has sent you a friend request");
                            }
                        }
                        dataSource.close();
                        if (jary1.length() == 0) {

                            runOnUiThread(new Runnable() {
                                public void run() {
                                    TextView frndTxt = (TextView) findViewById(R.id.defaultaccept);
                                    frndTxt.setText("You have no pending friend requests");
                                }
                            });
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(AcceptFriendRequestsActivity.this,
                                        "Sorry unable to add friend, check internet connection and try after sometime",
                                        Toast.LENGTH_LONG).show();

                            }
                        });
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        };
        new Thread(runnable).start();
    }

    protected void notifyDataChanged() {
        friendadapter.notifyDataSetChanged();
    }

    void display() {
        runOnUiThread(new Runnable() {
            public void run() {
                friendadapter.addAll(receivedFriendshipRequestArray);
            }
        });
    }

    public void handler1(View v) {
        final int position = listView1.getPositionForView((View) v.getParent());
        final String str = ((TextView) ((View) v.getParent()).findViewById(R.id.acceptfrndtxt)).getText().toString();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                HttpPost post = new HttpPost("http://" + url + "/owncloud/index.php/apps/friends/acceptfriendrequest");
                HttpEntity entity;

                final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("CURRENTUSER", username));
                params.add(new BasicNameValuePair("ACCEPTFRIEND", str));
                try {
                    entity = new UrlEncodedFormEntity(params, "utf-8");
                    HttpClient client = new DefaultHttpClient();
                    post.setEntity(entity);
                    HttpResponse response = client.execute(post);
                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

                        runOnUiThread(new Runnable() {
                            public void run() {
                                String s = Integer.toString(position);
                                receivedFriendshipRequestArray.remove(position);
                                friendadapter.remove(s);
                                notifyDataChanged();
                                Toast.makeText(AcceptFriendRequestsActivity.this,
                                        "You accepted friend request successfully", Toast.LENGTH_SHORT).show();
                            }
                        });

                    } else {
                        Toast.makeText(AcceptFriendRequestsActivity.this,
                                "Sorry unable to add friend, check internet connection and try after sometime",
                                Toast.LENGTH_LONG).show();
                    }
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        };
        new Thread(runnable).start();
    }

    public void handler2(View v) {
        final int position = listView1.getPositionForView((View) v.getParent());
        final String str = ((TextView) ((View) v.getParent()).findViewById(R.id.acceptfrndtxt)).getText().toString();
        Log.d("handler2 ", str);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                HttpPost post = new HttpPost("http://" + url + "/owncloud/index.php/apps/friends/removefriendrequest");
                HttpEntity entity;

                final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("CURRENTUSER", username));
                params.add(new BasicNameValuePair("SENTORRECEIVED", "received"));
                params.add(new BasicNameValuePair("FRIEND", str));
                try {
                    entity = new UrlEncodedFormEntity(params, "utf-8");
                    HttpClient client = new DefaultHttpClient();
                    post.setEntity(entity);
                    HttpResponse response = client.execute(post);
                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

                        runOnUiThread(new Runnable() {
                            public void run() {
                                String s = Integer.toString(position);
                                receivedFriendshipRequestArray.remove(position);
                                friendadapter.remove(s);
                                notifyDataChanged();
                                Toast.makeText(AcceptFriendRequestsActivity.this,
                                        "You removed friend request successfully", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }

                    else {
                        // Toast.makeText(AddFriendsActivity.this,"Sorry unable to add friend, check internet connection and try after sometime",
                        // Toast.LENGTH_LONG).show();
                        Log.d("in re", " could not remove");
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        };
        new Thread(runnable).start();
    }

    private class friendArrayAdapter extends ArrayAdapter<String> {

        int i = 0;
        List<String> Objects;
        Context context;
        int layoutResourceId;
        friendRowholder holder;

        public friendArrayAdapter(Context context, int layoutResourceId, List<String> Objects) {
            super(context, layoutResourceId, Objects);
            this.context = context;
            this.layoutResourceId = layoutResourceId;
            this.Objects = Objects;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            Log.d("getView", " in getvi");
            holder = new friendRowholder();
            holder.frndPos = Objects.get(position);
            holder.acceptfrndButton = (Button) findViewById(R.id.acceptbtn);
            holder.removefrndButton = (Button) findViewById(R.id.removebtn);
            holder.frndtxt = (TextView) row.findViewById(R.id.acceptfrndtxt);

            if (row.getTag() == null) {
                row.setTag(holder);
                String text = receivedFriendshipRequestArray.get(position);
                Log.d("ine pos", text);
                holder.frndtxt.setText(text);
                return row;
            } else {
                holder.frndtxt.setText("You have no pending friend requests");
                return null;
            }
        }

        public class friendRowholder {
            String frndPos;
            TextView frndtxt;
            Button acceptfrndButton;
            Button removefrndButton;

        }

    }

}