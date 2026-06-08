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
import android.content.SharedPreferences;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import org.json.JSONArray;
import org.json.JSONObject;
import android.util.Log;

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

        SharedPreferences prefs = getSharedPreferences("ChallengeAppPrefs", MODE_PRIVATE);
        int cachedStreak = prefs.getInt("currentStreak", 0);
        int cachedContributions = prefs.getInt("todayContributions", 0);
        
        TextView tvCurrentStreak = findViewById(R.id.tvCurrentStreak);
        tvCurrentStreak.setText("GitHub Current Streak: " + cachedStreak + " 🔥");
        
        TextView tvTodayContributions = findViewById(R.id.tvTodayContributions);
        tvTodayContributions.setText("Today's Contributions: " + cachedContributions);

        fetchGitHubStreak();
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

    private void fetchGitHubStreak() {
        GitHubDataFetcher.fetchContributionData(this, new GitHubDataFetcher.GitHubDataListener() {
            @Override
            public void onSuccess(int streak, int todayContributions) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    TextView tvCurrentStreak = findViewById(R.id.tvCurrentStreak);
                    tvCurrentStreak.setText("GitHub Current Streak: " + streak + " 🔥");
                    
                    TextView tvTodayContributions = findViewById(R.id.tvTodayContributions);
                    tvTodayContributions.setText("Today's Contributions: " + todayContributions);
                    
                    updateWidgetStreak();
                });
            }

            @Override
            public void onError(Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    TextView tvCurrentStreak = findViewById(R.id.tvCurrentStreak);
                    if (tvCurrentStreak.getText().toString().contains("0")) {
                        tvCurrentStreak.setText("GitHub Current Streak: Error");
                    }
                });
            }
        });
    }

    private void updateWidgetStreak() {
        Intent intent = new Intent(this, DaysLeftWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), DaysLeftWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }
}
