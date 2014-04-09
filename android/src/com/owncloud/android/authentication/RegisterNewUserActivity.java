package com.owncloud.android.authentication;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import android.util.Log;
import com.owncloud.android.R;
import com.owncloud.android.operations.OwnCloudServerCheckOperation;
import com.owncloud.android.utils.OwnCloudVersion;

public class RegisterNewUserActivity extends Activity implements OnFocusChangeListener{
    
    
    
    private EditText mHostUrlInput;
    private EditText userName;
    
    private Spinner locationSpinner;
    private EditText password1;
    private EditText password2;
    private Button registerUser;
    private Button mRefreshButton;
    private String mAuthMessageText;
    private int mAuthMessageVisibility, mServerStatusText, mServerStatusIcon;
    private boolean mServerIsChecked, mServerIsValid, mIsSslConn;
    private int mAuthStatusText, mAuthStatusIcon;    
    private TextView mAuthStatusLayout;
    
    String hostUrl;
    
    private OwnCloudServerCheckOperation mOcServerChkOperation;
    private final Handler mHandler = new Handler();
    private Thread mOperationThread;
    
    private OwnCloudVersion mDiscoveredVersion;
    private boolean mHostUrlInputEnabled;
    
    AuthenticatorActivity a1 = new AuthenticatorActivity();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
        setContentView(R.layout.register_newuser);
        mHostUrlInput = (EditText)findViewById(R.id.hostUrlInput);
        userName = (EditText)findViewById(R.id.register_newuser);
        locationSpinner = (Spinner)findViewById(R.id.spinner1);
        password1= (EditText)findViewById(R.id.password1);
        password2 = (EditText)findViewById(R.id.password2);
        registerUser = (Button) findViewById(R.id.create_newuser);
        mRefreshButton = (Button) findViewById(R.id.centeredRefreshButton);
        
    
        
    }
    
    
    public void onClickCreateNewUser(View view) {
        
        hostUrl = mHostUrlInput.getText().toString().trim();
        String username = userName.getText().toString().trim();
        String location = locationSpinner.getSelectedItem().toString();
        String passwordVa = password1.getText().toString().trim();
        String passwordva2 = password2.getText().toString().trim();
        if(hostUrl == null || hostUrl.equals(""))
            a1.callonRefresh(hostUrl);
        if(!passwordVa.equals(passwordva2))
        {
            Toast.makeText(getApplicationContext(), "Passwords do not match, please reenter", Toast.LENGTH_SHORT).show();
            password1.setText("");
            password2.setText("");
        } else {
        username = username + "@" + location;
        JSONObject obj1 = new JSONObject();
        final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("regname", username));
        params.add(new BasicNameValuePair("password1",passwordVa));
        params.add(new BasicNameValuePair("password2",passwordva2));
        //Log.d("sdn object ",params.toString());

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                
               HttpPost post = new HttpPost("http://"+hostUrl+"/index.php/apps/friends/friendrequest");
               
               HttpEntity entity;
               try {
                   entity = new UrlEncodedFormEntity(params,"utf-8");
                   HttpClient client = new DefaultHttpClient();
                   post.setEntity(entity);
                   HttpResponse response = client.execute(post);
                   //Log.d(TAG,"Fetching friend list from server");
                   
                   if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                        Log.d("RegisterNewUserActivity ",HttpStatus.SC_OK+" ");
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Account created", Toast.LENGTH_SHORT).show();
                            }
                        }); 
                    }
                        
                    
               } catch (UnsupportedEncodingException e) {
                   // TODO Auto-generated catch block
                   e.printStackTrace();
               } /*catch (ClientProtocolException e) {
                   // TODO Auto-generated catch block
                   e.printStackTrace();
               }*/ catch (IOException e) {
                   // TODO Auto-generated catch block
                   e.printStackTrace();
               } 
               }
            };
            new Thread(runnable).start();
        }
    }
    
    private void onUrlInputFocusLost(TextView hostInput) {
       /* if (!hostUrl.equals(normalizeUrl(mHostUrlInput.getText().toString()))) {
            a1.onRefreshClick(hostInput);
        } else {
            registerUser.setEnabled(mServerIsValid);
            if (!mServerIsValid) {
                showRefreshButton();
            }
        }*/
    }

    private void onRefreshClick(View view) {
        a1.onRefreshClick(view);
    }
/*    private void checkOcServer() {
        String uri = trimUrlWebdav(mHostUrlInput.getText().toString().trim());
        
        if (!mHostUrlInputEnabled){
            uri = getString(R.string.server_url);
        }
        
        mServerIsValid = false;
        mServerIsChecked = false;
        registerUser.setEnabled(false);
        mDiscoveredVersion = null;
        //hideRefreshButton();
        if (uri.length() != 0) {
            mServerStatusText = R.string.auth_testing_connection;
            mServerStatusIcon = R.drawable.progress_small;
            showServerStatus();
            mOcServerChkOperation = new  OwnCloudServerCheckOperation(uri, this);
            WebdavClient client = OwnCloudClientUtils.createOwnCloudClient(Uri.parse(uri), this, true);
            mOperationThread = mOcServerChkOperation.execute(client, this, mHandler);
        } else {
            mServerStatusText = 0;
            mServerStatusIcon = 0;
            showServerStatus();
        }
    }
    
    private String trimUrlWebdav(String url){       
        if(url.toLowerCase().endsWith(AccountUtils.WEBDAV_PATH_4_0)){
            url = url.substring(0, url.length() - AccountUtils.WEBDAV_PATH_4_0.length());             
        } else if(url.toLowerCase().endsWith(AccountUtils.WEBDAV_PATH_2_0)){
            url = url.substring(0, url.length() - AccountUtils.WEBDAV_PATH_2_0.length());             
        } else if (url.toLowerCase().endsWith(AccountUtils.WEBDAV_PATH_1_2)){
            url = url.substring(0, url.length() - AccountUtils.WEBDAV_PATH_1_2.length());             
        } 
        return (url != null ? url : "");
    }
    private void showServerStatus() {
        TextView tv = (TextView) findViewById(R.id.server_status_text);

        if (mServerStatusIcon == 0 && mServerStatusText == 0) {
            tv.setVisibility(View.INVISIBLE);

        } else {
            tv.setText(mServerStatusText);
            tv.setCompoundDrawablesWithIntrinsicBounds(mServerStatusIcon, 0, 0, 0);
            tv.setVisibility(View.VISIBLE);
        }

    }


    @Override
    public void onRemoteOperationFinish(RemoteOperation operation, RemoteOperationResult result) {
       
    }
    //private void onOcServerCheckFinish(OwnCloudServerCheckOperation operation, RemoteOperationResult result) {
*/


    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        // TODO Auto-generated method stub
        if (v.getId() == R.id.hostUrlInput) {   
            if (!hasFocus) {
                onUrlInputFocusLost((TextView) v);
            }
            else {
                hideRefreshButton();
            }

        }
    }
    
    private void showRefreshButton() {
        mRefreshButton.setVisibility(View.VISIBLE);
    }

    private void hideRefreshButton() {
        mRefreshButton.setVisibility(View.GONE);
    }
}
