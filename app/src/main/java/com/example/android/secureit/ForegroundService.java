package com.example.android.secureit;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationBuilderWithBuilderAccessor;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;

public class ForegroundService extends Service {

    @RequiresApi(api = Build.VERSION_CODES.O)
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Secureit");



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(ForegroundService.this, "System Activated", Toast.LENGTH_SHORT).show();

        databaseReference.addValueEventListener(ListenerClass.getValueEventListener());

        final String CHANNEL_ID = "foreground_service_id";
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_ID,
                NotificationManager.IMPORTANCE_HIGH
        );
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_baseline_security_24);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
        Notification.Builder notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentText("Secureit Now Runs in Background")
                .setContentTitle("Service is enabled")
                .setLargeIcon(largeIcon);

        startForeground(100, notification.build());
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
