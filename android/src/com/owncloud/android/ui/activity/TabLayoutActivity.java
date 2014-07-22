/*
 * 
 * Village Shaer
 */

package com.owncloud.android.ui.activity;

import android.app.Activity;
import android.app.LocalActivityManager;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TabHost;

import com.actionbarsherlock.app.ActionBar;
import com.owncloud.android.R;
/**
 * 
 * @author Smruthi Manjunath
 *
 */
public class TabLayoutActivity extends TabActivity {

    TabHost mTabHost;
    String TAG = "TabLayoutActivty";

    @Override
    public void onCreate(Bundle SavedInstanceState) {
        super.onCreate(SavedInstanceState);
        setContentView(R.layout.friend_tab);

        // Creating tabs and associating activities
        mTabHost = getTabHost();
        TabHost.TabSpec spec1, spec2, spec3, spec4;
        Intent intent;

        // intent = new Intent(this,YourFriendsActivity.class);
        intent = new Intent(this, YourFriendsActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        spec1 = mTabHost.newTabSpec("YourFriends").setIndicator("Your Friends").setContent(intent);
        mTabHost.addTab(spec1);

        intent = new Intent(this, AddFriendsActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        spec2 = mTabHost.newTabSpec("AddFriends").setIndicator("Add Friends").setContent(intent);
        mTabHost.addTab(spec2);

        intent = new Intent(this, AcceptFriendRequestsActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        spec3 = mTabHost.newTabSpec("AcceptFriendsRequests").setIndicator("Accept Friend Requests").setContent(intent);
        mTabHost.addTab(spec3);

        //uncomment the below lines to include facebook friend import
        intent = new Intent(this, FacebookSync.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        spec4 = mTabHost.newTabSpec("Facebook").setIndicator("Facebook").setContent(intent);
        mTabHost.addTab(spec4); 

        // Adding Refresh and enabling back button
        android.app.ActionBar ab = getActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowTitleEnabled(false);
        ab.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        setProgressBarIndeterminateVisibility(false);

    }

    void callforresumeStart() {
        LocalActivityManager manager = getLocalActivityManager();
        String currentTag = mTabHost.getCurrentTabTag();
        Log.d(TAG + "jeewjqqjpqejpq ", currentTag);
        Class<? extends Activity> currentClass = manager.getCurrentActivity().getClass();
        manager.startActivity(currentTag, new Intent(this, currentClass));
    }

    @Override
    public void onStart() {
        callforresumeStart();
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("onResume", "before calling onresumestart");
        callforresumeStart();
        Log.d("onResume", "Here");
    }

    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {

        android.view.MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.friend_menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {

        boolean retval = true;
        switch (item.getItemId()) {
        case R.id.action_refresh: {

            // callforresumeStart();
            onResume();
            Log.d("reusme ", "pressed buttons");
            break;
        }
        case android.R.id.home: {
            onBackPressed();
            break;
        }

        }
        return retval;
    }
}