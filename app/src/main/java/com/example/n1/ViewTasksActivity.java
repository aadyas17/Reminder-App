package com.example.n1;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;


public class ViewTasksActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_tasks);

        TextView savedTasksTextView = findViewById(R.id.txtSavedTasks);

        // Get the saved tasks from SharedPreferences
        String savedTasks = getSavedTasks();

        // If there are saved tasks, display them; otherwise, show a message
        if (savedTasks.isEmpty()) {
            savedTasksTextView.setText("No tasks saved!");
        } else {
            savedTasksTextView.setText(savedTasks);
        }
    }

    private String getSavedTasks() {
        // Retrieve the saved tasks from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("tasksPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("taskList", "");
    }
}
