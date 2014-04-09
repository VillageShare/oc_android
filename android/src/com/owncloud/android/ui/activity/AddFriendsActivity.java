/*
 * VillageShare
 * 
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
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.owncloud.android.R;
import com.owncloud.android.authentication.AccountAuthenticator;
import com.owncloud.android.authentication.AccountUtils;
/**
 * 
 * @author Smruthi Manjunath
 *
 */
public class AddFriendsActivity extends Activity implements OnClickListener, OnItemSelectedListener {

    EditText friendName;
    Button Add;
    String accountname;
    Object location;
    TextView textView;
    TextView tv;
    ListView listView;
    Spinner s1;
    JSONArray jary;
    String url;
    String username;
    friendArrayAdapter adapter;

    ArrayList<String> friendNames;
    String TAG = "AddFriendsActivity";

    @Override
    public void onCreate(Bundle SavedInstanceState) {

        super.onCreate(SavedInstanceState);
        setContentView(R.layout.add_friendstab);

        listView = (ListView) findViewById(R.id.listview);
        friendNames = new ArrayList<String>();
        adapter = new friendArrayAdapter(this, R.layout.removeyourfriends, friendNames);
        listView.setAdapter(adapter);
        AccountManager am = AccountManager.get(this);
        Account account = AccountUtils.getCurrentOwnCloudAccount(this);
        String[] url1 = (am.getUserData(account, AccountAuthenticator.KEY_OC_BASE_URL)).split("/");
        url = url1[2];
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

                        jary = obj1.getJSONArray("sentFriendshipRequests");
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
                                    TextView frndTxt = (TextView) findViewById(R.id.defaultadd);
                                    frndTxt.setText("You have not added any friends");
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    TextView frndTxt = (TextView) findViewById(R.id.defaultadd);
                                    frndTxt.setVisibility(View.INVISIBLE);
                                }
                            });
                        }

                    } else {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(AddFriendsActivity.this,
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
        Add = (Button) findViewById(R.id.btn1);
        friendName = (EditText) findViewById(R.id.edttext1);
        s1 = (Spinner) findViewById(R.id.spinner_addfriends);
        s1.setOnItemSelectedListener(this);
        Add.setOnClickListener(this);
    }

    protected void notifyDataChanged() {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        String val1 = friendName.getText().toString();
        if (val1.equals("")) {
            Toast.makeText(AddFriendsActivity.this, "Please enter a friends name", Toast.LENGTH_LONG).show();
        } else {
            final String val = val1 + "@" + location;
            final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("CURRENTUSER", username));
            params.add(new BasicNameValuePair("REQUESTEDFRIEND", val));

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    HttpPost post = new HttpPost("http://" + url + "/owncloud/index.php/apps/friends/friendrequest");
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
                            
                            Boolean jary = obj.getBoolean("success");
                            if(jary) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    adapter.add(val);
                                    friendName.setText("");
                                    Toast.makeText(AddFriendsActivity.this,
                                            "You requested " + val + " to add as friend", Toast.LENGTH_SHORT).show();
                                }
                            });
                            } else {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        adapter.add(val);
                                        friendName.setText("");
                                        Toast.makeText(AddFriendsActivity.this,
                                                "Unable to add " + val + " as a friend", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } else {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(
                                            AddFriendsActivity.this,
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
    }

    public void handler1(View v) {
        final int position = listView.getPositionForView((View) v.getParent());
        final String str = ((TextView) ((View) v.getParent()).findViewById(R.id.yourfrndtxt)).getText().toString();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                HttpPost post = new HttpPost("http://" + url + "/owncloud/index.php/apps/friends/removefriendrequest");
                HttpEntity entity;

                final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("CURRENTUSER", username));
                params.add(new BasicNameValuePair("SENTORRECEIVED", "sent"));
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
                        
                        Boolean jary = obj.getBoolean("success");
                        if(jary) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                String s = Integer.toString(position);
                                adapter.remove(s);
                                friendNames.remove(position);
                                Toast.makeText(AddFriendsActivity.this, "You removed friend successfully",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                        } else {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(AddFriendsActivity.this, "Unable to remove friend, try again later.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } 
                    else {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(AddFriendsActivity.this,"Sorry unable to remove friend, check internet connection and try after sometime",
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

    private class friendArrayAdapter extends ArrayAdapter<String> {

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
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        location = parent.getItemAtPosition(position);

    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {

    }
}