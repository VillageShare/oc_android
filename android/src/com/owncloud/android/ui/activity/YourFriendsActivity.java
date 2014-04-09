/*
 * 
 * 
 * VillageShare
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
import android.content.Context;
import android.os.Bundle;
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

public class YourFriendsActivity extends Activity {

    Account accountname;
    ListView listView;
    friendlistArrayAdapter adapter;
    static ArrayList<String> friendNames;
    JSONArray jary;
    String username;
    String url;
    String TAG = "YourFriedsActivty";

    @Override
    public void onCreate(Bundle SavedInstanceState) {

        super.onCreate(SavedInstanceState);
        setContentView(R.layout.your_friendstab);

        listView = (ListView) findViewById(R.id.yourlistview);
        friendNames = new ArrayList<String>();
        adapter = new friendlistArrayAdapter(this, R.layout.removeyourfriends, friendNames);
        listView.setAdapter(adapter);
        AccountManager am = AccountManager.get(this);
        Account account = AccountUtils.getCurrentOwnCloudAccount(this);
        String[] url1 = (am.getUserData(account, AccountAuthenticator.KEY_OC_BASE_URL)).split("/");
        url = url1[2];
        String[] accountName = account.name.split("@");
        //String username = null;
        if (accountName.length > 2) {
            username = accountName[0] + "@" + accountName[1];
        } else {
            
            throw new NullPointerException();
        }
        final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("CURRENTUSER", username));

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                HttpPost post = new HttpPost("http://" + url + "/owncloud/index.php/apps/friends/friendlist");

                HttpEntity entity;
                try {
                    entity = new UrlEncodedFormEntity(params, "utf-8");
                    HttpClient client = new DefaultHttpClient();
                    post.setEntity(entity);
                    HttpResponse response = client.execute(post);
                    Log.d(TAG, "Fetching friend list from server");

                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        HttpEntity entityresponse = response.getEntity();
                        String jsonentity = EntityUtils.toString(entityresponse);
                        JSONObject obj = new JSONObject(jsonentity);
                        JSONObject obj1 = (JSONObject) obj.get("data");

                        jary = obj1.getJSONArray("friendships");
                        friendNames.clear();
                        for (int i = 0; i < jary.length(); i++) {
                            friendNames.add(jary.getString(i));
                        }

                        runOnUiThread(new Runnable() {
                            public void run() {
                                notifyDataChanged();
                            }
                        });
                        if (jary.length() == 0) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    TextView frndTxt = (TextView) findViewById(R.id.defaultyourfriends);
                                    frndTxt.setText("You have no friends");
                                }
                            });
                        }

                    } else {
                        Toast.makeText(YourFriendsActivity.this,
                                "Sorry unable to get data, check internet connection and try after sometime",
                                Toast.LENGTH_LONG).show();
                        Log.d("Return", "unable to get data");
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
        adapter.notifyDataSetChanged();
    }

    protected void notifyDataChanged() {
        adapter.notifyDataSetChanged();
    }

    void display() {
        runOnUiThread(new Runnable() {
            public void run() {
                adapter.addAll(friendNames);
            }
        });

    }

    protected void onPostExecute(ArrayList<String> items) {
        adapter.notifyDataSetChanged();
    }

    public void handler1(View v) {
        final int position = listView.getPositionForView((View) v.getParent());
        final String str = ((TextView) ((View) v.getParent()).findViewById(R.id.yourfrndtxt)).getText().toString();
        
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                HttpPost post = new HttpPost("http://" + url + "/owncloud/index.php/apps/friends/removefriend");
                HttpEntity entity;

                final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("CURRENTUSER", username));
                params.add(new BasicNameValuePair("FRIEND", str));
                try {
                    entity = new UrlEncodedFormEntity(params, "utf-8");
                    HttpClient client = new DefaultHttpClient();
                    post.setEntity(entity);
                    HttpResponse response = client.execute(post);
                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        HttpEntity entityresponse = response.getEntity();
                        String jsonentity = EntityUtils.toString(entityresponse);
                        JSONObject obj = new JSONObject(jsonentity);
                        
                        String jary = obj.getString("success");
                        if(jary.equals("true")) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                String s = Integer.toString(position);

                                adapter.remove(s);
                                friendNames.remove(position);
                                Toast.makeText(YourFriendsActivity.this, "You removed friend successfully",
                                        Toast.LENGTH_SHORT).show();
                                adapter.notifyDataSetChanged();

                            }
                        });

                    } else if(jary.equals("false")) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(YourFriendsActivity.this, "Could not remove friendship",
                                        Toast.LENGTH_SHORT).show();
                                adapter.notifyDataSetChanged();

                            }
                        });

                    } else if(jary.equals("FRIENDSHIP_NOT_FOUND")) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(YourFriendsActivity.this, "Unable to remove friendship. Invalid request.",
                                        Toast.LENGTH_SHORT).show();
                                adapter.notifyDataSetChanged();

                            }
                        });

                    }
                    }

                    else {
                        Toast.makeText(YourFriendsActivity.this,
                                "Sorry unable to add friend, check internet connection and try after sometime",
                                Toast.LENGTH_LONG).show();
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        };
        new Thread(runnable).start();
    }

    private class friendlistArrayAdapter extends ArrayAdapter<String> {

        int i = 0;
        List<String> Objects;
        Context context;
        int layoutResourceId;
        friendRowholder holder;

        public friendlistArrayAdapter(Context context, int layoutResourceId, List<String> Objects) {
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
            holder = new friendRowholder();
            holder.frndPos = Objects.get(position);
            holder.removefrndButton = (Button) findViewById(R.id.removebtn1);
            holder.frndtxt = (TextView) row.findViewById(R.id.yourfrndtxt);
            if (row.getTag() == null) {
                row.setTag(holder);
                String text = friendNames.get(position);
                holder.frndtxt.setText(text);
                return row;
            } else {
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