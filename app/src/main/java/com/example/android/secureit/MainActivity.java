package com.example.android.secureit;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private ImageAdapter mAdapter;
    private ProgressBar mProgressCircle;
    private DatabaseReference mDatabaseRef;
    private DatabaseReference secureitRef;
    private List<Upload> mUploads;
    private ValueEventListener valueEventListenerForImages;
    private ValueEventListener valueEventListenerForDetection;


//    private ForegroundService foregroundService;
//    private ValueEventListener valueEventListenerForDetection = new ValueEventListener() {
//        @Override
//        public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//        }
//
//        @Override
//        public void onCancelled(@NonNull DatabaseError error) {
//
//        }
//    };
    private FirebaseDatabase globalFirebaseDatabase = FirebaseDatabase.getInstance();

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        clearData(null);

        ImageView emptyView = findViewById(R.id.emptyView);
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mProgressCircle = findViewById(R.id.progress_circle);
        mUploads = new ArrayList<>();

        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Uploads");
//        Toast.makeText(this, "get instace OK", Toast.LENGTH_SHORT).show();




        valueEventListenerForImages = mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                mUploads.clear();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Upload upload = postSnapshot.getValue(Upload.class);
                    mUploads.add(upload);
                }

                mAdapter = new ImageAdapter(getBaseContext(), mUploads);

                if(mAdapter.getItemCount() == 0){
                        emptyView.setImageResource(R.drawable.empty);
                        emptyView.setVisibility(View.VISIBLE);
                }
                else emptyView.setVisibility(View.INVISIBLE);
                mRecyclerView.setAdapter(mAdapter);
                mProgressCircle.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getBaseContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                mProgressCircle.setVisibility(View.INVISIBLE);
            }
        });


//        if (!foregroundServiceRunning()) {
//            Intent serviceIntent = new Intent(this, ForegroundService.class);
//            startForegroundService(serviceIntent);
//        }
        secureitRef = FirebaseDatabase.getInstance().getReference("Secureit");


        valueEventListenerForDetection = secureitRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> childrenData = dataSnapshot.getChildren();
                Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    int unique_number_for_multiple_notification = 1;
                    for (DataSnapshot dataSnapshot1 : childrenData) {
                        if (dataSnapshot1.getValue(Integer.class) == 1) {
                            unique_number_for_multiple_notification += 1;
                            final String CHANNEL_ID = "Alert";
                            NotificationChannel channel = new NotificationChannel(
                                    CHANNEL_ID,
                                    CHANNEL_ID,
                                    NotificationManager.IMPORTANCE_LOW
                            );
                            getSystemService(NotificationManager.class).createNotificationChannel(channel);
                            Intent notifyIntent = new Intent(getBaseContext(), MainActivity.class);
                            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            PendingIntent notifyPendingIntent = PendingIntent.getActivity(
                                    getBaseContext(), 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
                            );

                            Notification.Builder notification = new Notification.Builder(getBaseContext(), CHANNEL_ID)
                                    .setContentText(dataSnapshot1.getKey().toUpperCase() + " detected")
                                    .setContentTitle("System Detects Unusual Activity")
                                    .setSmallIcon(R.drawable.ic_launcher_background)
                                    .setPriority(Notification.PRIORITY_HIGH)
                                    .setAutoCancel(true)
                                    .setSmallIcon(R.drawable.ic_baseline_add_alert_24)
                                    .setContentIntent(notifyPendingIntent);

//                            notification.build().defaults = Notification.DEFAULT_SOUND;
                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getBaseContext());
                            notificationManager.notify(unique_number_for_multiple_notification, notification.build());
                        }
                    }
                    if (unique_number_for_multiple_notification > 1) {
                        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), alarmSound);
                        if(!r.isPlaying())
                            r.play();
                    }
                }


                @Override
                public void onCancelled(DatabaseError error) {
                    Log.w("tag", "Failed to read value.", error.toException());
                }
            });

        if(ListenerClass.getValueEventListener() == null) ListenerClass.setValueEventListener(valueEventListenerForDetection);

        if (!foregroundServiceRunning()) {
            Intent serviceIntent = new Intent(this, ForegroundService.class);
            startForegroundService(serviceIntent);
        }
    }



        public boolean foregroundServiceRunning () {
            ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
                if (ForegroundService.class.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
            return false;
        }

        public void clearData(View view){
            FirebaseDatabase database1 = FirebaseDatabase.getInstance();
            DatabaseReference myRef1 = database1.getReference("Secureit");
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("fire", 0);
            hashMap.put("gun", 0);
            hashMap.put("motion" , 0);
            myRef1.updateChildren(hashMap);
        }
        public void clearUploads(){
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Uploads");
            databaseReference.removeValue();
        }

        public void clearAll(View view){
            clearData(null);
            clearUploads();
        }


    @Override
    protected void onDestroy() {

        secureitRef.removeEventListener(valueEventListenerForDetection);
        mDatabaseRef.removeEventListener(valueEventListenerForImages);
        super.onDestroy();
    }
}