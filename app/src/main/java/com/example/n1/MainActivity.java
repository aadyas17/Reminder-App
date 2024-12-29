package com.example.n1;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_NOTIFICATION_PERMISSION = 1;
    private static final String TASKS_PREFS = "tasksPrefs";
    private static final String TASK_LIST_KEY = "taskList";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create notification channel first
        createNotificationChannel();

        // Check for notification permission
        if (!checkNotificationPermission()) {
            requestNotificationPermission();
        }

        // Initialize UI components
        EditText taskInput = findViewById(R.id.taskInput);
        TimePicker timePicker = findViewById(R.id.timePicker);
        Button setReminderButton = findViewById(R.id.setReminderButton);
        Button viewTasksButton = findViewById(R.id.viewTasksButton);

        // Ensure TimePicker uses 24-hour format
        timePicker.setIs24HourView(false);

        setReminderButton.setOnClickListener(view -> {
            if (!checkNotificationPermission()) {
                requestNotificationPermission();
                return;
            }

            String task = taskInput.getText().toString().trim();
            if (task.isEmpty()) {
                Toast.makeText(this, "Please enter a task", Toast.LENGTH_SHORT).show();
                return;
            }

            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            // Save the task
            saveTask(task);

            // Schedule the main notification and the reminder 5 minutes before
            scheduleNotification(task, calendar.getTimeInMillis(), 0); // Main reminder
            scheduleNotification(task, calendar.getTimeInMillis(), -5 * 60 * 1000); // 5 minutes before
        });

        // View saved tasks
        viewTasksButton.setOnClickListener(view -> {
            String savedTasks = getSavedTasks();
            if (savedTasks.isEmpty()) {
                Toast.makeText(this, "No tasks saved!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, savedTasks, Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    REQUEST_NOTIFICATION_PERMISSION
            );
        }
    }

    private void scheduleNotification(String task, long triggerAtMillis, long advanceMillis) {
        long notificationTime = triggerAtMillis + advanceMillis;

        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("task", task);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                (int) System.currentTimeMillis(), // Unique ID for each notification
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    notificationTime,
                    pendingIntent
            );
            if (advanceMillis == 0) {
                Toast.makeText(this, "Reminder set!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "taskReminderChannel",
                    "Task Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Channel for task reminders");
            channel.enableLights(true);
            channel.enableVibration(true);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void saveTask(String task) {
        SharedPreferences sharedPreferences = getSharedPreferences(TASKS_PREFS, MODE_PRIVATE);
        String savedTasks = sharedPreferences.getString(TASK_LIST_KEY, "");
        savedTasks += task + "\n"; // Add the new task
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TASK_LIST_KEY, savedTasks);
        editor.apply();
    }

    private String getSavedTasks() {
        SharedPreferences sharedPreferences = getSharedPreferences(TASKS_PREFS, MODE_PRIVATE);
        return sharedPreferences.getString(TASK_LIST_KEY, "");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}