/*
 * Village Share
 */
package com.owncloud.android.ui.activity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.owncloud.android.R;


/**
 * @author Smruthi Manjunath
 *
 */
public class FacebookFriendSelectDisplay extends Activity{
    
    PopupWindow popupWindow; 
    String TAG = "FacebookFriendSelectDisplay";
    facebookfriendArrayAdapter facebookfriendadapter;
    Button bt;
    String friendArray;
    ListView listView;
    ArrayList<String> friendList = new ArrayList<String>();
    JSONArray friendListToPost;
    static int k = 0;
    Toast toast;
    JSONArray jary;
    ConcurrentHashMap<String,String> temp = new ConcurrentHashMap<String,String>();
    
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.selectfacebookfriend);
        
        friendListToPost = new JSONArray();

        listView = (ListView)findViewById(R.id.friendlist1);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
 
        Log.i(TAG,"heeeeeeeeeeeeeeeeeeeeeeeeeeeeeere");
        
        Intent i = getIntent();
        if(getIntent().hasExtra("friendName")== true){
            Log.d(TAG,"passed the arraylist");
        }
       
        friendArray = i.getStringExtra("friendName");
        
        try {
            jary = new JSONArray(friendArray);
            JSONObject obj;
            for(int j =0;j<jary.length();j++){
                obj = jary.getJSONObject(j);
                friendList.add(obj.optString("name"));
                
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
       
        Log.d(TAG,"herrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr "+friendList.size());
        if(friendList.size()!=0){
        facebookfriendadapter = new facebookfriendArrayAdapter(this,R.layout.friendrow,friendList,0);
        
        listView.setAdapter(facebookfriendadapter);
        
        
      
        }
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        finish();
    }
    public void onDone(View v){
        Log.d(TAG+"wekjw",friendListToPost.length()+" ");
        
        try {
                Log.d("Before friendlist ",friendListToPost.length()+" ");
                for(int i = 0;i < jary.length();i++){
                if(temp.containsKey(jary.getJSONObject(i).optString("id")))
                    
                    friendListToPost.put(jary.getJSONObject(i));
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
        Log.d(TAG,friendListToPost.length()+" ");
        for(int i = 0;i<friendListToPost.length();i++){
            try {
                
                Log.d("Printign array contents before pushing to the server ",friendListToPost.getJSONObject(i).toString());
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        
        Log.w("friendlist ",friendListToPost.length()+" ");
        PushToServerAsync pushdata = new PushToServerAsync();
        JSONArray jaryPush = new JSONArray();
        jaryPush.put(friendListToPost);
        //pushdata.execute(jaryPush);
        onDestroy();
        
    }

    public void SelectAll(View v){
        Log.d(TAG,"Selected All");
        
        listView = (ListView)findViewById(R.id.friendlist1);
        if(friendList.size()!=0){
        facebookfriendadapter = new facebookfriendArrayAdapter(this,R.layout.friendrow,friendList,1);
        listView.setAdapter(facebookfriendadapter);
        }
        facebookfriendadapter.notifyDataSetChanged();
        for(int i = 0;i<jary.length();i++){
            try {
                temp.put(jary.getJSONObject(i).optString("id"),jary.getJSONObject(i).optString("name"));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        //friendListToPost.put(jary);
            
    }
    
    public void DeselectAll(View v){
        Log.d(TAG,"DeSelected All");
        
        listView = (ListView)findViewById(R.id.friendlist1);
        if(friendList.size()!=0){
        facebookfriendadapter = new facebookfriendArrayAdapter(this,R.layout.friendrow,friendList,0);
        listView.setAdapter(facebookfriendadapter);
        for(int i = 0;i<jary.length();i++){
            try {
                temp.remove(jary.getJSONObject(i).optString("id"));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        }
        
        temp.clear();
        
    }
    public void onCheckboxClicked(View v){
        final int position =listView.getPositionForView((View) v.getParent());
        friendRowholder holder;
        boolean chk1 = ((CheckBox)((View)v.getParent()).findViewById(R.id.isSelected)).isChecked();
        Log.i(TAG,chk1+" ");
        if(chk1==true){
            Log.d("chek bxxx","was checked");
            ((CheckBox)((View)v.getParent()).findViewById(R.id.isSelected)).setChecked(true);
        } else{
            ((CheckBox)((View)v.getParent()).findViewById(R.id.isSelected)).setChecked(false);
        }
      
            try {
                if(temp.containsKey(jary.getJSONObject(position).optString("id"))){
                    Log.d(TAG,"Removing the entry "+jary.getJSONObject(position).optString("name"));
                    temp.remove(jary.getJSONObject(position).optString("id"));
                } else {
                   
                    temp.put(jary.getJSONObject(position).optString("id"),jary.getJSONObject(position).optString("name"));
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
    }
    private class facebookfriendArrayAdapter extends ArrayAdapter<String>{
        
        int i=0;
        List<String> Objects;
        Context context;
        int layoutResourceId;
        friendRowholder holder;
        private int j;
        //static int counteri=0;
        public facebookfriendArrayAdapter(Context context,int layoutResourceId,List<String> Objects, int j){
            super(context,layoutResourceId,Objects);
            this.context = context;
            this.layoutResourceId = layoutResourceId;
            this.Objects = Objects;
            this.j = j;
        }
        
        @Override
        public View getView(int position, View convertView,ViewGroup parent){
            View row = convertView;
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId,parent,false);
            
            holder = new friendRowholder();
            
            
            holder.frndPos = Objects.get(position);
            holder.checkbox = (CheckBox)row.findViewById(R.id.isSelected);
            holder.frndNametxt = (TextView)row.findViewById(R.id.friend_row);
            
            
            if(row.getTag()==null){
            row.setTag(holder);
           
            Log.i(TAG,"here in ii");
            String text = friendList.get(position);
            
            holder.frndNametxt.setText(text);
            
            
            }
            
            if(j==1){
                holder.checkbox.setChecked(true);
                return row;
            }
            else{
                holder.checkbox.setChecked(false);
                return row;
            }
           
        }

    }

    public class friendRowholder{
        String frndPos;
        TextView frndNametxt;
        CheckBox checkbox;
        
        
    }
    
    
}