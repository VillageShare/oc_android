/*
 * 
 * 
 * 
 * VillageShare
 * 
 */
package com.owncloud.android.ui.activity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;
import com.owncloud.android.R;
import com.owncloud.android.authentication.AccountAuthenticator;
import com.owncloud.android.authentication.AccountUtils;
import com.owncloud.android.utils.OwnCloudVersion;

/**
 * 
 * @author Smruthi Manjunath
 *
 */
public class FacebookSync extends Activity implements OnClickListener,DialogInterface.OnClickListener{

    String APP_ID;
    Facebook facebook;
    public static ImageView facebook_sync;
    TextView frnds,add_frnds,pending_request;
    TextView welcome,namefr;
    SharedPreferences sher;
    AsyncFacebookRunner asyncRunner;
    public static String username; // = ((TextView).findViewById(R.id.user_input)).getText().toString().trim();
    public static String url;// = ((TextView)findViewById(R.id.host_URL)).getText().toString().trim();
    public static String id;
    String access_token;
    Long expires;
    String name;
    public static String currentUserId;
    public static Account accountname;
    public static JSONObject MyDetails;
    
    public static Toast toast;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(android.os.Build.VERSION.SDK_INT>9){
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Context gettingApplicationContext = getBaseContext();
        AccountManager am = AccountManager.get(this);
        Account account = AccountUtils.getCurrentOwnCloudAccount(this);
        OwnCloudVersion ocv = new OwnCloudVersion(am.getUserData(account, AccountAuthenticator.KEY_OC_VERSION));
        String[] url1 = (am.getUserData(account, AccountAuthenticator.KEY_OC_BASE_URL)).split("/");
        url = url1[2];
        //accountname = AccountUtils.getCurrentOwnCloudAccount(gettingApplicationContext);
        String vals[] = account.toString().split("[=,]");
        currentUserId = vals[1];
        Log.d("onCreate, FacebookSync",account.toString());
        super.onCreate(savedInstanceState);
        }
        setContentView(R.layout.sync_page);
        
      
        
        
        APP_ID = getString(R.string.APP_ID);
        facebook = new Facebook(APP_ID);
        asyncRunner = new AsyncFacebookRunner(facebook);
        
        
        facebook_sync = (ImageView) findViewById(R.id.setup_sync);
        frnds = (TextView) findViewById(R.id.frnds);
        sher = getPreferences(MODE_PRIVATE);
        access_token = sher.getString("access_token",null);
        expires = sher.getLong("access_expires",0); 
        if(access_token != null){
            Log.d("Tagdew","stored access tokens----------------------------------");
           facebook.setAccessToken(access_token);
        }
        if(expires!=0){
            Log.d("jehdiwef","stored expsdjnfowfneke---------------");
            facebook.setAccessExpires(expires);
        }
        //listener l1 = new listener(this);
        toast = Toast.makeText(this, "FacebookSync sync of friend list was done successfully", Toast.LENGTH_LONG);
        facebook_sync.setOnClickListener(this);
        
    }
    
    @SuppressWarnings("deprecation")
    public void loginfacebook(){
        if(facebook.isSessionValid()){
            JSONObject obj = null;
            URL friends_url = null;
            
            String JsonObject;
            try {
                JsonObject = facebook.request("me");
                obj = Util.parseJson(JsonObject);
                MyDetails = obj;
                id = obj.optString("id");
                name = obj.optString("name");
                Bundle params = new Bundle();
                params.putString("fields", "name,id");
                asyncRunner.request("me/friends",params,"GET",new listener(this), null);
                friends_url = new URL("http://graph.facebook.com/"+id+"/friends?fields=name");
                
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (FacebookError e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JSONException e) {
                // TODO Auto-generated catch bolock
                e.printStackTrace();
            }
            
            
            
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if(facebook.isSessionValid()){
           
                //facebook.logout(this);
                Toast.makeText(FacebookSync.this, "You re logged in", Toast.LENGTH_SHORT).show();
                showDialog(0);
        }
        else {
            
            
            //chkBeforeLogging();
            showDialog(0);
            Toast.makeText(FacebookSync.this, "Before logging in", Toast.LENGTH_SHORT).show();
            
        }
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        facebook.authorizeCallback(requestCode, resultCode, data);
    }

    @Override
    public void onClick(DialogInterface arg0, int arg1) {
        // TODO Auto-generated method stub
        
    }
    
   public Dialog onCreateDialog(int id){
       Dialog dialog = null;
       switch(id){
       case 0: android.app.AlertDialog.Builder builder = new AlertDialog.Builder(this);
       builder.setTitle("Facebook Sync")
       .setCancelable(false)
       .setMessage("The sync will occur with the user currently logged in to Facebook.  If there is no logged in user, you will be prompted to log in.  Please confirm that someone else is not logged into Facebook .  Then press OK to continue.")
       .setPositiveButton("OK",new DialogInterface.OnClickListener(){

     @Override
     public void onClick(DialogInterface dialog, int which) {
         // TODO Auto-generated method stub
         
         if(!facebook.isSessionValid()){
         facebook.authorize(FacebookSync.this, new String[] {"email","read_friendlists"},new DialogListener() {
             
             @Override
             public void onFacebookError(FacebookError e) {
                 // TODO Auto-generated method stub
                 Toast.makeText(FacebookSync.this, "Sorry unable to log you in! Please try later", Toast.LENGTH_SHORT).show();
             }
             
             @Override
             public void onError(DialogError e) {
                 // TODO Auto-generated method stub
                 Toast.makeText(FacebookSync.this, "Sorry Unable to log you in!", Toast.LENGTH_SHORT).show();
             }
             
             @Override
             public void onComplete(Bundle values) {
                 // TODO Auto-generated method stub
                 Editor editor = sher.edit();
                 editor.putString("access_token", facebook.getAccessToken());
                 editor.putLong("access_expires", facebook.getAccessExpires());
                 editor.commit(); 
                 Log.d("qwoqjoqwqowioqwnwoqr",facebook.getAccessToken());
                 Log.d("wjhkfrewjwerhiwrehwerih"," "+facebook.getAccessExpires());
                 Toast.makeText(FacebookSync.this, "Successfully logged in!", Toast.LENGTH_SHORT).show();
                 loginfacebook();
             }
             
             @Override
             public void onCancel() {
                 // TODO Auto-generated method stub
                 Toast.makeText(FacebookSync.this, "Login Cancelled", Toast.LENGTH_SHORT).show();
             }
         });
         }
         loginfacebook();
     }
        
    })
    .setNegativeButton("Cancel",new DialogInterface.OnClickListener(){

     @Override
     public void onClick(DialogInterface dialog, int which) {
         // TODO Auto-generated method stub
         //loginfacebook();
     }
        
    });//.showDialog();
    //Toast.makeText(SyncDialog.this, "Activity will continue",Toast.LENGTH_LONG).show();
    dialog = builder.create();
    //dialog.show();
       }
    return dialog;
      
   }
    
    
    
   
}

class listener implements RequestListener{

    Context context;
    String TAG = "FacebookSync listener";
    ListView listView;
    ArrayList<String> friendNames = new ArrayList<String>();
    PopupWindow popupWindow ; 
    Button btnOpenpop;
    listener(Context context){
        this.context = context;
    }
    @Override
    public void onComplete(String response, Object state) {
        // TODO Auto-generated method stub
         final JSONObject data;
         JSONArray friendsData;
         final Handler handler;
         final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
         
        try {
            data = Util.parseJson(response);
            friendsData = data.getJSONArray("data");
            for(int i = 0;i<friendsData.length();i++){
                JSONObject friends = friendsData.getJSONObject(i);
                friendNames.add(friends.optString("name"));
            }
           
            Log.d(TAG,"heeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
            Intent intent = new Intent(context,FacebookFriendSelectDisplay.class);
            //intent.putParcelableArrayListExtra("friendList", friendNames);
            //intent.putStringArrayListExtra("friendName", friendNames);
            intent.putExtra("friendName", friendsData.toString());
            for(int i = 0 ;i<friendNames.size();i++){
                Log.d(TAG,friendNames.get(i));
            }
            
            
            
            context.startActivity(intent);
            Log.d(TAG,"hoqhfe;;;;;;;;;;;;;;;w;oggqfyccgqffffffff");
            //PushToServerAsync pushdata = new PushToServerAsync();
            //pushdata.execute(friendsData);
           
           // popupWindow.showAsDropDown(FacebookSync.facebook_sync, 20, -5); 
       
            
        } catch (FacebookError e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void onIOException(IOException e, Object state) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onFileNotFoundException(FileNotFoundException e, Object state) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onMalformedURLException(MalformedURLException e, Object state) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onFacebookError(FacebookError e, Object state) {
        // TODO Auto-generated method stub
        
    }
   
}