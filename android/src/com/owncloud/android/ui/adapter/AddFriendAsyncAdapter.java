/**
 * 
 */
package com.owncloud.android.ui.adapter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

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

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

/**
 * @author smruthi
 *
 */
public class AddFriendAsyncAdapter extends AsyncTask<String, Integer , ArrayList<String>> {

    ArrayAdapter<String> adapter;
    ArrayList<String> sharefriendList;
    String TAG = "TryingAsync";
    AutoCompleteTextView textview;
    @Override
    protected ArrayList<String> doInBackground(String... urls) {
        // TODO Auto-generated method stub
        String url = urls[2];
        String username = urls[1];
        ArrayList<String> sharefriendList = new ArrayList<String>();
        Log.d(" Seriously :(",url);
        HttpPost post = new HttpPost("http://"+url+"/owncloud/index.php/apps/friends/removefriendrequest");
        final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("CURRENTUSER", username));
        HttpEntity entity;
     try {
         entity = new UrlEncodedFormEntity(params,"utf-8");
         HttpClient client = new DefaultHttpClient();
         post.setEntity(entity);
         HttpResponse response = client.execute(post);
         String friendArray[] = null;
         //Log.d(TAG,"Fetching friend list from server");
         
         if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
             HttpEntity entityresponse = response.getEntity();
             String jsonentity = EntityUtils.toString(entityresponse);
             JSONObject obj = new JSONObject(jsonentity);
             JSONObject obj1 = (JSONObject) obj.get("data");
             
             JSONArray jary = obj1.getJSONArray("friendships");
             //sharefriendList.clear();
             for(int i = 0; i<jary.length();i++){
             sharefriendList.add(jary.getString(i));
                 Log.d("TAG",jary.getString(i));
             }
                 
         }
        
     } catch (UnsupportedEncodingException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
     } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
     } catch (JSONException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
     }
     
     
        //return null;
     return sharefriendList;
    }
    
   
    @Override
    protected void onPostExecute(ArrayList<String> result) {
        // TODO Auto-generated method stub
        super.onPostExecute(result);
        Log.d("Tired :(",result.get(0));
    }
  
}