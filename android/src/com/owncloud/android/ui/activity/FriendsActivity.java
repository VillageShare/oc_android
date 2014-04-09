/*
 * 
 * 
 * 
 * VillageShare
 * 
 */
package com.owncloud.android.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.owncloud.android.R;
/**
 * 
 * @author Smruthi Manjunath
 *
 */
public class FriendsActivity extends Activity implements OnClickListener{
    
    @Override
    public void onCreate(Bundle savedInstanceState){
        
        super.onCreate(savedInstanceState);
       
        setContentView(R.layout.sync_page);
        
        ImageView setupsync = (ImageView)findViewById(R.id.setup_sync);
        setupsync.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                call();
            }
            
        });
        //Intent friendsIntent = new Intent(this, FacebookSync.class);
       //startActivity(friendsIntent);
        
    }
    
    void call(){
        Intent friendsIntent = new Intent(this, FacebookSync.class);
        startActivity(friendsIntent);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        
    }
    
}