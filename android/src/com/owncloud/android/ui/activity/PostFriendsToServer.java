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
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;
/**
 * 
 * @author Smruthi Manjunath
 *
 */
public class PostFriendsToServer extends AsyncTask<Long, Integer, JSONArray>{

    private static final int TIMEOUT_MILLISEC = 10;

    protected void onPostExecute(JSONArray json1) {
        //showDialog("Downloaded " + result + " bytes");
        for(int i=0;i<json1.length();i++){
            try {
                Log.d("tayhsd g ", json1.getJSONObject(i).toString());
                //doInBackground(json1);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            }
        
    }

    void func1(JSONArray json1){
        for(int i=0;i<json1.length();i++){
            try {
                Log.d("tayhsd g ", json1.getJSONObject(i).toString());
                //doInBackground(json1);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            }
    }
    @Override
    protected JSONArray doInBackground(Long...l ) {
        // TODO Auto-generated method stub
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost("http://128.111.52.151/owncloud/index.php");
        final String PARAM_USERNobj1AME="Username";
        final String PARAM_INTERESTS="interests";
        String url = FacebookSync.url;
        String username = "Smruthi Manjunath";
        //StringEntity se = new StringEntity(jsonArray.toString(),HTTP.UTF_8);
       List<NameValuePair> ne = new ArrayList<NameValuePair>();
       List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
       //NameValuePair pair1 = new NameValuePair("PARAMS", "sjndwoenow");
      // ne.add(new NameValuePair("Friendlist",jobj.toString()));
      // ne.add(pair1);
        try {
            //StringEntity entity = new StringEntity(ne.toString(),HTTP.UTF_8);
            //HttpEntity entity = new UrlEncodedFormEntity(ne, "utf-8");
            
            JSONObject obj1 = new JSONObject();
            //JSONArray friendsData = interests.getJSONArray("data");
            //obj1.putOpt("Username", "Smruthi Manjunath");
            final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("USERNAME", username));
            params.add(new BasicNameValuePair("INTERESTS", obj1.toString()));
            /*for(int i=0;i<interests.length;i++){
            Log.d("tayhsd giaejrpwqjrpqjwr[ ", interests.getJSONObject(i).toString());
            } */
            HttpEntity entity =  new UrlEncodedFormEntity(params);
            //final HttpPost post = new HttpPost(UPDATE_INTERESTS_URI);
            post.setEntity(entity);
            HttpResponse response = client.execute(post);
            
            /*post.setHeader("Content-type", "application/json");
            post.setHeader("Accept", "application/json");
        JSONObject obj = new JSONObject();
        obj.put("username", "abcd");
        obj.put("password", "1234");
            post.setEntity(new StringEntity(obj.toString(), "UTF-8"));
            HttpResponse response = client.execute(post);  */
            
            
            /*nameValuePairs.add(new BasicNameValuePair("param1","EKOEWK"));
            nameValuePairs.add(new BasicNameValuePair("param2","KMWEKMR"));
            post.setEntity(new UrlEncodedFormEntity(nameValuePairs)); 
            //post.setEntity(new UrlEncodedFormEntity(ne, "utf-8"));
            //post.setEntity(entity);
            //post.setEntity(new UrlEncodedFormEntity((List<? extends org.apache.http.NameValuePair>) ne));
            HttpResponse response = client.execute(post);*/
            
            Log.d("Http esponse"," "+response.getStatusLine().toString());
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
        return null;
    }


}