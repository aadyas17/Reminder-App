package com.example.n1;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_NOTIFICATION_PERMISSION = 1;

    private ArrayList<String> tasks = new ArrayList<>();
    private ArrayList<String> taskTimes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create notification channel
        createNotificationChannel();

        // Check and request notification permission if needed
        if (!checkNotificationPermission()) {
            requestNotificationPermission();
        }

        // Initialize UI components
        EditText taskInput = findViewById(R.id.taskInput);
        TimePicker timePicker = findViewById(R.id.timePicker);
        Button setReminderButton = findViewById(R.id.setReminderButton);
        TextView taskListTextView = findViewById(R.id.taskListTextView);

        // Ensure TimePicker uses 24-hour format
        timePicker.setIs24HourView(false);

        setReminderButton.setOnClickListener(view -> {
            String task = taskInput.getText().toString().trim();
            if (task.isEmpty()) {
                taskListTextView.setText("Please enter a task.");
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

            // Check if the task already exists and update the time
            boolean taskUpdated = false;
            for (int i = 0; i < tasks.size(); i++) {
                if (tasks.get(i).equals(task)) {
                    taskTimes.set(i, String.format("%02d:%02d", hour, minute));  // Update time
                    taskUpdated = true;
                    break;
                }
            }

            // If it's a new task, add it to the lists
            if (!taskUpdated) {
                tasks.add(task);
                taskTimes.add(String.format("%02d:%02d", hour, minute));
            }

            // Schedule the main notification and the reminder 5 minutes before
            scheduleNotification(task, calendar.getTimeInMillis(), 0); // Main reminder
            scheduleNotification(task, calendar.getTimeInMillis() - (5 * 60 * 1000), 5); // 5 minutes before

            // Show confirmation
            Toast.makeText(this, "Reminder set for task: " + task, Toast.LENGTH_SHORT).show();

            // Clear task input field after setting the reminder
            taskInput.setText("");

            // Update the task list display
            updateTaskListDisplay(taskListTextView);
        });
    }

    private void updateTaskListDisplay(TextView taskListTextView) {
        if (tasks.isEmpty()) {
            taskListTextView.setText("No tasks saved.");
        } else {
            StringBuilder taskList = new StringBuilder();
            for (int i = 0; i < tasks.size(); i++) {
                taskList.append("Task: ").append(tasks.get(i))
                        .append(" at ").append(taskTimes.get(i)).append("\n");
            }
            taskListTextView.setText(taskList.toString());
        }
    }

    private void scheduleNotification(String task, long triggerAtMillis, int notificationId) {
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("task", task);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                notificationId, // Unique ID for each notification
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
            );
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

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
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
