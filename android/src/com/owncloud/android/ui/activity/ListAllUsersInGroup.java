/**
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

import com.owncloud.android.R;
import com.owncloud.android.authentication.AccountAuthenticator;
import com.owncloud.android.authentication.AccountUtils;
import com.owncloud.android.db.DbFriends;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;           

import android.widget.TextView;
import android.widget.Toast;

/**0:
 * @author smruthi
 *
 */
public class ListAllUsersInGroup extends Activity implements OnClickListener{

    Button addUserToGroup;
    EditText memberName;
    ImageView deleteUser;
    ListView memberListView;
    ArrayList<String> memberNames;
    MemberArrayAdapter adapter;
    String accountname;
    String username;
    String url;
    Account account;
    String TAG = "AddFriendsActivity";
    String groupName;
    private enum groupOperation { 
        ADD_TO_GROUP("2"),REMOVE_FROM_GROUPS("3"),USERS_IN_GROUP("5");
        private String operationGroup;
    
    private groupOperation(String s) {
        operationGroup = s;
    }           

 
    public String getGroupOperation() {
        return operationGroup;
    }
 };
    @Override
    public void onCreate(Bundle SavedInstanceState) {
        super.onCreate(SavedInstanceState);
        setContentView(R.layout.listallusers);
        groupName = getIntent().getExtras().getString("groupName");
        Log.d("TAG",groupName);
        memberListView = (ListView) findViewById(R.id.listusersview);
        memberNames = new ArrayList<String>();
        adapter = new MemberArrayAdapter(this, R.layout.member_editdelete, memberNames);
        memberListView.setAdapter(adapter);
        addUserToGroup = (Button) findViewById(R.id.AddUserBtn);
        memberName = (EditText) findViewById(R.id.memberName);
        account = AccountUtils.getCurrentOwnCloudAccount(this);
        accountname = account.name;
        String accountName[] = accountname.split("@");
        if (accountName.length > 2) {
            username = accountName[0] + "@" + accountName[1];
            url = accountName[2];
        }
        else {
            throw new NullPointerException("Account name not in correct format");
        }
        
            final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("operation",groupOperation.USERS_IN_GROUP.getGroupOperation()));
            params.add(new BasicNameValuePair("GID",groupName));
            params.add(new BasicNameValuePair("UID", " "));
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    HttpPost post = new HttpPost("http://" + url + "/owncloud/androidcreategroups.php");
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

                            JSONArray jary = obj.getJSONArray("usersinGrup");
                            memberNames.clear();
                            for (int i = 0; i < jary.length(); i++) {
                                memberNames.add(jary.getString(i));

                                   Log.d(TAG, jary.getString(i));
                            }
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    adapter.setNotifyOnChange(true);
                                }
                            });
                            if (jary.length() == 0) {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        TextView frndTxt = (TextView) findViewById(R.id.defaultGroup);
                                        frndTxt.setText("You have not added any friends");
                                    }
                                });
                            } 

                                  } else {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                   Toast.makeText(ListAllUsersInGroup.this,
                                            "Sorry unable to create group, check internet connection and try after sometime",
                                            Toast.LENGTH_LONG).show();
                                }
                            });

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
            addUserToGroup.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    
                    final String val = memberName.getText().toString();
                    //Account account = AccountUtils.getCurrentOwnCloudAccount(List);
                    accountname = account.name;
                    String accountName[] = accountname.split("@");
                    if (accountName.length > 2)
                        username = accountName[0] + "@" + accountName[1];
                    else {
                        throw new NullPointerException("Account name not in correct format");
                    }
                    if (val.equals("")) {
                        Toast.makeText(ListAllUsersInGroup.this, "Please enter a group name", Toast.LENGTH_LONG).show();
                    } else {
                        final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
                        params.add(new BasicNameValuePair("operation",groupOperation.ADD_TO_GROUP.getGroupOperation()));
                        params.add(new BasicNameValuePair("UID", val));
                        params.add(new BasicNameValuePair("GID", groupName));

                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                HttpPost post = new HttpPost("http://" + url + "/owncloud/androidcreategroups.php");
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
                                        Boolean shareSuccess = obj.getBoolean("addToGroup");
                                        if(shareSuccess == true) {
                                        runOnUiThread(new Runnable() {
                                            public void run() {
                                                adapter.add(val);
                                                memberName.setText("");
                                                Toast.makeText(ListAllUsersInGroup.this,
                                                        "You added " + val + " to the group", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        } else {
                                            runOnUiThread(new Runnable() {
                                                public void run() {
                                                    Toast.makeText(
                                                            ListAllUsersInGroup.this,
                                                            "Sorry unable to create groups",
                                                            Toast.LENGTH_LONG).show();
                                                }
                                            });
                                        }
                                    } else {
                                        runOnUiThread(new Runnable() {
                                            public void run() {
                                                Toast.makeText(
                                                        ListAllUsersInGroup.this,
                                                        "Sorry unable to create group, check internet connection and try after sometime",
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        });
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
                    
                }
            });
            deleteUser = (ImageView)findViewById(R.id.deletememberbtn1);
            
    }
    
    public void onDeleteMember(View v) {
        final int position = memberListView.getPositionForView((View) v.getParent());
        final String userToRemove = ((TextView) ((View) v.getParent()).findViewById(R.id.yourmembertxt)).getText().toString();
        final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("operation", groupOperation.REMOVE_FROM_GROUPS.getGroupOperation()));
        params.add(new BasicNameValuePair("UID", userToRemove));
        params.add(new BasicNameValuePair("GID", groupName));

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                HttpPost post = new HttpPost("http://" + url + "/owncloud/androidcreategroups.php");
                HttpEntity entity;
                try {
                    entity = new UrlEncodedFormEntity(params, "utf-8");
                    HttpClient client = new DefaultHttpClient();
                    post.setEntity(entity);
                    HttpResponse response = client.execute(post);
                    Log.d(TAG, "Deleting group");

                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        HttpEntity entityresponse = response.getEntity();
                        String jsonentity = EntityUtils.toString(entityresponse);
                        JSONObject obj = new JSONObject(jsonentity);
                        Boolean groupDelete = obj.getBoolean("removeFromGroups");
                        if(groupDelete == true) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(ListAllUsersInGroup.this,
                                        "You have deleted " + userToRemove + " group "+groupName, Toast.LENGTH_SHORT).show();
                            }
                        });
                        } else {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(
                                            ListAllUsersInGroup.this,
                                            "Sorry unable to delete user from group",
                                            Toast.LENGTH_LONG).show();            

                                }
                            });
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(
                                        ListAllUsersInGroup.this,
                                        "Sorry unable to delete user from the group, check internet connection and try after sometime",
                                        Toast.LENGTH_LONG).show();            

                            }
                        });
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    
                } catch (IOException e) { // TODO Auto-generated method stub
                    
                    e.printStackTrace();
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        };
        new Thread(runnable).start();
    }
    
    private class MemberArrayAdapter extends ArrayAdapter<String> {

        List<String> Objects;
        Context context;
        int layoutResourceId;
        friendRowholder holder;

        public MemberArrayAdapter(Context context, int layoutResourceId, List<String> Objects) {
            super(context, layoutResourceId, Objects);
            this.context = context;
            this.layoutResourceId = layoutResourceId;
            this.Objects = Objects;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View row = convertView;
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new friendRowholder();
            holder.frndPos = Objects.get(position);
           // holder.editgroupButton = (Button)findViewById(R.id.editgroupbtn1);
            holder.deletememberButton = (ImageView) findViewById(R.id.deletememberbtn1);
            holder.membertxt = (TextView) row.findViewById(R.id.yourmembertxt);
            //DeleteGroup = (ImageView)findViewById(R.id.deletegroupbtn1);
            
            
            if (row.getTag() == null) {
                row.setTag(holder);
                String text = memberNames.get(position);
                holder.membertxt.setText(text);
                return row;
            } else {
                return null;
            }
            
           
           
        }
        public class friendRowholder {
            String frndPos;
            TextView membertxt;
            ImageView deletememberButton;
        }
    }


    @Override
    public void onClick(View arg0) {
        // TODO Auto-generated method stub
        
    }
    
}

