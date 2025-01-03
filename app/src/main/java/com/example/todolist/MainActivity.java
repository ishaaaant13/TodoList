package com.example.todolist;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TodoAdapter taskAdapter;
    private ArrayList<TodoItem> taskList = new ArrayList<>();
    private TodoDatabaseHelper dbHelper;

    // Request codes for startActivityForResult
    private static final int REQUEST_ADD_TASK = 1;
    private static final int REQUEST_EDIT_TASK = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        recyclerView = findViewById(R.id.recyclerView);
        FloatingActionButton fabAddTask = findViewById(R.id.fab);

        // Initialize database helper
        dbHelper = new TodoDatabaseHelper(this);

        // Create the notification channel for reminders
        NotificationHelper.createNotificationChannel(this);

        // Load tasks from database
        loadTasksFromDatabase();

        // Set up RecyclerView & Adapter
        taskAdapter = new TodoAdapter(taskList, this::onTaskClick);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(taskAdapter);

        // Floating Action Button for adding new tasks
        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
            startActivityForResult(intent, REQUEST_ADD_TASK);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload tasks whenever the activity is back in focus
        loadTasksFromDatabase();
        taskAdapter.notifyDataSetChanged();
    }

    /**
     * Retrieve all tasks from the DB into taskList
     */
    private void loadTasksFromDatabase() {
        taskList.clear();
        Cursor cursor = dbHelper.getAllTasks();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_NAME));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_DESCRIPTION));
                String imageUri = cursor.getString(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_IMAGE_URI));
                String reminder = cursor.getString(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_REMINDER));

                taskList.add(new TodoItem(id, name, description, imageUri, reminder));
            } while (cursor.moveToNext());

            cursor.close();
        }
    }

    /**
     * Handle clicks on a task (open detail screen)
     */
    private void onTaskClick(TodoItem task) {
        Intent intent = new Intent(MainActivity.this, TodoDetailActivity.class);
        intent.putExtra("task_id", task.getId());
        intent.putExtra("task_name", task.getTitle());
        intent.putExtra("task_description", task.getDescription());
        intent.putExtra("task_image", task.getImageUri());
        intent.putExtra("task_reminder", task.getDateTime());
        startActivityForResult(intent, REQUEST_EDIT_TASK);
    }

    /**
     * Refresh the list if tasks were added/edited
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && (requestCode == REQUEST_ADD_TASK || requestCode == REQUEST_EDIT_TASK)) {
            loadTasksFromDatabase();
            taskAdapter.notifyDataSetChanged();
            Toast.makeText(this, "Tasks Updated", Toast.LENGTH_SHORT).show();
        }
    }
}
