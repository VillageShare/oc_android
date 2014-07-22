/*
 * VillageShare project
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
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
public class GroupActivity extends Activity implements OnClickListener{

    EditText groupName;
    Button CreateGroup;
    ImageView DeleteGroup;
    String accountname;
    TextView groupNameText;
    TextView tv;
    ListView groupListView;
    Spinner s1;
    JSONArray jary;
    String url;
    String username;
    GroupArrayAdapter adapter;
    private enum groupOperation { 
        CREATE_GROUP("0"),DELETE_GROUP("1"),GET_USERS_GROUP("4");
        private String operationGroup;
    
    private groupOperation(String s) {
        operationGroup = s;
    }
 
    public String getGroupOperation() {
        return operationGroup;
    }
 };
    ArrayList<String> groupNames;
    String TAG = "AddFriendsActivity";

    @Override
    public void onCreate(Bundle SavedInstanceState) {
        super.onCreate(SavedInstanceState);
        setContentView(R.layout.groups);
        groupListView = (ListView) findViewById(R.id.listgroupview);
        groupNames = new ArrayList<String>();
        adapter = new GroupArrayAdapter(this, R.layout.group_editdelete, groupNames);
        groupListView.setAdapter(adapter);
        
        groupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
                final String groupName = groupListView.getItemAtPosition(position).toString();
               Intent  intent = new Intent(GroupActivity.this,ListAllUsersInGroup.class);
                intent.putExtra("groupName", groupName);
                startActivity(intent);
                //Log.d("GroupActivity ","in onclick ");
            }
            
        });
        
        
        
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
        params.add(new BasicNameValuePair("operation",groupOperation.GET_USERS_GROUP.getGroupOperation()));
        params.add(new BasicNameValuePair("GID"," "));
        params.add(new BasicNameValuePair("UID", username));
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
                        JSONObject obj1 = (JSONObject) obj.get("getUserGroups");
                        
                        jary = obj1.names();
                        groupNames.clear();
                        for (int i = 0; i < jary.length(); i++) {
                            groupNames.add(obj1.getString(jary.getString(i)));
                            //Log.d(TAG, jary.getString(i)+" "+obj1.getString(jary.getString(i)));
                        }
                        runOnUiThread(new Runnable() {
                            public void run() {
                                notifyDataChanged();
                            }
                        });
                        if (jary.length() == 0) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    TextView frndTxt = (TextView) findViewById(R.id.defaultGroup);
                                    frndTxt.setText("You have not added any friends");
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    TextView frndTxt = (TextView) findViewById(R.id.defaultGroup);
                                    frndTxt.setVisibility(View.INVISIBLE);
                                }
                            });
                        }

                    } else {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(GroupActivity.this,
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
        CreateGroup= (Button) findViewById(R.id.group);
        groupName = (EditText) findViewById(R.id.edttext1);
        DeleteGroup = (ImageView) findViewById(R.id.deletegroupbtn1);
        CreateGroup.setOnClickListener(this);
        //DeleteGroup.setOnClickListener(this);
        
        
    }

    protected void notifyDataChanged() {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        final String val = groupName.getText().toString();
        Account account = AccountUtils.getCurrentOwnCloudAccount(this);
        accountname = account.name;
        String accountName[] = accountname.split("@");
        if (accountName.length > 2)
            username = accountName[0] + "@" + accountName[1];
        else {
            throw new NullPointerException("Account name not in correct format");
        }
        if (val.equals("")) {
            Toast.makeText(GroupActivity.this, "Please enter a group name", Toast.LENGTH_LONG).show();
        } else {
            final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("operation",groupOperation.CREATE_GROUP.getGroupOperation()));
            params.add(new BasicNameValuePair("UID", username));
            params.add(new BasicNameValuePair("GID", val));

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
                            Boolean shareSuccess =obj.getBoolean("createGroup");
                            if(shareSuccess == true) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    adapter.add(val);
                                    groupName.setText("");
                                    Toast.makeText(GroupActivity.this,
                                            "You have created " + val + " group", Toast.LENGTH_SHORT).show();
                                }
                            });
                            } else {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(
                                                GroupActivity.this,
                                                "Sorry unable to create groups",
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        } else {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(
                                            GroupActivity.this,
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

    public void deleteGroup(View view) {
            
    }
   
    public void onDeleteClick(View v){
            final int position = groupListView.getPositionForView((View) v.getParent());
            final String groupName = ((TextView) ((View) v.getParent()).findViewById(R.id.yourgrouptxt)).getText().toString();
            final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("operation", groupOperation.DELETE_GROUP.getGroupOperation()));
            params.add(new BasicNameValuePair("UID", " "));
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
                            Boolean groupDelete = obj.getBoolean("deleteGroup");
                            if(groupDelete == true) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(GroupActivity.this,
                                            "You have deleted " + groupName + " group", Toast.LENGTH_SHORT).show();
                                }
                            });
                            } else {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(
                                                GroupActivity.this,
                                                "Sorry unable to delete group",
                                                Toast.LENGTH_LONG).show();            

                                    }
                                });
                            }
                        } else {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(
                                            GroupActivity.this,
                                            "Sorry unable to delete group, check internet connection and try after sometime",
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
    
    public void listUsers(View v) {
        
    }
    private class GroupArrayAdapter extends ArrayAdapter<String> {

        List<String> Objects;
        Context context;
        int layoutResourceId;
        friendRowholder holder;

        public GroupArrayAdapter(Context context, int layoutResourceId, List<String> Objects) {
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
            holder.deletegroupButton = (ImageView) findViewById(R.id.deletegroupbtn1);
            holder.frndtxt = (TextView) row.findViewById(R.id.yourgrouptxt);
            //DeleteGroup = (ImageView)findViewById(R.id.deletegroupbtn1);
            
            
            if (row.getTag() == null) {
                row.setTag(holder);
                String text = groupNames.get(position);
                holder.frndtxt.setText(text);
                return row;
            } else {
                return null;
            }
            
           
           
        }
        public class friendRowholder {
            String frndPos;
            TextView frndtxt;
            //Button editgroupButton;
            ImageView deletegroupButton;
        }
    }
    /*@Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        final String groupName = ((TextView) ((View) arg1.getParent()).findViewById(R.id.yourgrouptxt)).getText().toString();
        Intent intent = new Intent(GroupActivity.this,ListAllUsersInGroup.class);
        intent.putExtra("groupName", groupName);
        startActivity(intent);        
    } */
    
    
    
}
