package com.example.n1;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> tasks = new ArrayList<>();
    private ArrayList<String> taskTimes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create notification channel first
        createNotificationChannel();

        // Initialize UI components
        EditText taskInput = findViewById(R.id.taskInput);
        TimePicker timePicker = findViewById(R.id.timePicker);
        Button setReminderButton = findViewById(R.id.setReminderButton);
        Button viewTasksButton = findViewById(R.id.viewTasksButton);
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
            scheduleNotification(task, calendar.getTimeInMillis(), -5 * 60 * 1000); // 5 minutes before

            // Show a toast that the task reminder was set or updated
            Toast.makeText(this, "Reminder updated for task: " + task, Toast.LENGTH_SHORT).show();
        });

        // View saved tasks
        viewTasksButton.setOnClickListener(view -> {
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
        });
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
}
