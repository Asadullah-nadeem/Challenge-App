package com.example.challengeapp;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tvDaysLeft;
    private TextView tvCountdown;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            updateTime();
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannel();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        tvDaysLeft = findViewById(R.id.tvDaysLeft);
        tvCountdown = findViewById(R.id.tvCountdown);
        
        updateTime();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        handler.post(runnable);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Daily Motivation";
            String description = "Notifications for days left in the year";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("CHALLENGE_APP_CHANNEL", name, importance);
            channel.setDescription(description);
            
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void updateTime() {
        Calendar today = Calendar.getInstance();
        Calendar endOfYear = Calendar.getInstance();
        
        int currentYear = today.get(Calendar.YEAR);
        endOfYear.set(Calendar.YEAR, currentYear);
        endOfYear.set(Calendar.MONTH, Calendar.DECEMBER);
        endOfYear.set(Calendar.DAY_OF_MONTH, 31);
        
        // Ensure time is set to the end of the day for December 31st to get full days left
        endOfYear.set(Calendar.HOUR_OF_DAY, 23);
        endOfYear.set(Calendar.MINUTE, 59);
        endOfYear.set(Calendar.SECOND, 59);
        endOfYear.set(Calendar.MILLISECOND, 999);

        long diffInMillis = endOfYear.getTimeInMillis() - today.getTimeInMillis();
        if (diffInMillis < 0) diffInMillis = 0;
        
        int days = (int) (diffInMillis / (1000 * 60 * 60 * 24));
        long remainder = diffInMillis % (1000 * 60 * 60 * 24);
        int hours = (int) (remainder / (1000 * 60 * 60));
        remainder %= (1000 * 60 * 60);
        int minutes = (int) (remainder / (1000 * 60));
        remainder %= (1000 * 60);
        int seconds = (int) (remainder / 1000);

        tvDaysLeft.setText(String.valueOf(days));
        tvCountdown.setText(String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds));
    }
}
