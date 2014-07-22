/*
 * VillageShare Project
 */
package com.owncloud.android.ui.activity;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.owncloud.android.R;
import com.owncloud.android.files.services.FriendListNotifierService;
import com.owncloud.android.files.services.FriendNotifierService;
import com.owncloud.android.files.services.instantDownloadSharedFilesService;
/**
 * 
 * @author Smruthi Manjunath
 *
 */
public class InitialPageActivity extends Activity {
    String TAG = InitialPageActivity.class.getName();
    Toast toast;
    Button viewSharedFilesButton;
    Button viewMyFilesButton;
    Button ownCloudFilesButton;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        Log.d(TAG, "in initial page");
        setContentView(R.layout.optiontoviewfiles);
        viewSharedFilesButton = (Button) findViewById(R.id.displayFiles);
        viewMyFilesButton = (Button) findViewById(R.id.displaymyfiles);
        ownCloudFilesButton = (Button) findViewById(R.id.owncloudFiles);

        ownCloudFilesButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable()) {
                    finish();
                } else {
                    Toast.makeText(InitialPageActivity.this, "Please connect to the Internet", Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });
        
        final Intent intent1 = new Intent(this, DisplayFilesOfflineActivity.class);
        viewSharedFilesButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                intent1.putExtra("folder", "shared");
                intent1.putExtra("isShared", true);
                startActivity(intent1);
            }
        });
        viewMyFilesButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                intent1.putExtra("folder", "");
                intent1.putExtra("isShared", false);
                startActivity(intent1);
            }
        });

        Intent intent = new Intent(this, instantDownloadSharedFilesService.class);
        AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);
        Calendar cal = Calendar.getInstance();
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 5 * 60 * 1000, pintent);

        Intent intentfriend = new Intent(this, FriendNotifierService.class);
        alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        PendingIntent pintentfriend = PendingIntent.getService(this, 0, intentfriend, 0);
        cal = Calendar.getInstance();
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 5 * 60 * 1000, pintentfriend);
        
        Intent intentfriendList = new Intent(this, FriendListNotifierService.class);
        alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        PendingIntent pintentfriendlist = PendingIntent.getService(this, 0, intentfriendList, 0);
        cal = Calendar.getInstance();
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 5 * 60 * 1000, pintentfriendlist);
        Log.d(TAG, "Service started");
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo == null ? false : true;
    }

    @Override
    public void onBackPressed() {

    }

}
