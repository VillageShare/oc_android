/*
 * Village Share
 */
package com.owncloud.android.ui.activity;

/**
 * @author Smruthi Manjunath
 *
 */

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
import com.owncloud.android.ui.activity.FacebookSync;
import com.owncloud.android.R;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class PushToServerAsync extends AsyncTask<JSONArray, Integer , Integer> {

    final String PARAM_USERNAME="Username";
    final String PARAM_FRIENDS="friends";
    final String url = FacebookSync.url;
    String username = "Smruthi Manjunath";
    final String TAG = "pushdataToServer";
    HttpResponse response;
    //StringEntity se = new StringEntity(jsonArray.toString(),HTTP.UTF_8);
   List<NameValuePair> ne = new ArrayList<NameValuePair>();
   List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
   HttpClient client = new DefaultHttpClient();
   JSONObject details = new JSONObject(){
      {
       try{
           put("id",FacebookSync.MyDetails.getString("id"));
           put("name",FacebookSync.MyDetails.getString("name"));
       }catch(Exception e){
           e.printStackTrace();
       }
      }
   };

   @Override
   protected void onPreExecute(){
       
   }
    @Override
    protected Integer doInBackground(JSONArray... json1) {
        // TODO Auto-generated method stub
        String url = FacebookSync.url;
        //String username = urls[1];
        final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("USERNAME", FacebookSync.currentUserId));//FacebookSync.accountname.toString()));
        params.add(new BasicNameValuePair("USERFACEBOOK",details.toString()));
        params.add(new BasicNameValuePair("FRIENDS", json1[0].toString()));
        HttpEntity entity;
        
        
        /*for(int i = 0;i<json1[0].length();i++){
            JSONObject jar1;
            try {
                jar1 = json1[0].getJSONObject(i);
                Log.d(TAG,jar1.getString("name"));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
        }*/
     try {
         HttpPost post = new HttpPost("http://"+url+"/owncloud/index.php/apps/friends/android");
         //post.setHeader("Content-type", "application/json");
         //HttpEntity entity;
         //Log.d(TAG,"Fetching friend list from server");
         entity = new UrlEncodedFormEntity(params,"utf-8");
         HttpClient client = new DefaultHttpClient();
         post.setEntity(entity);
         response = client.execute(post);
         
         Log.d("Http esponse"," "+response.toString());
         
         if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
             Log.d(TAG,"dta sent");
         }
         else {
             Log.d(TAG,"dta not sent");
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
    //return null;
     return response.getStatusLine().getStatusCode();
         
    }
    
   
    @Override
    protected void onPostExecute(Integer result) {
        // TODO Auto-generated method stub
        //super.onPostExecute();
        //Log.d("Tired :(",result.get(0));
        if(result == HttpStatus.SC_OK){
        FacebookSync.toast.show();
        }
        
    }
    
        
}