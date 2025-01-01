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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        recyclerView = findViewById(R.id.recyclerView);
        FloatingActionButton fabAddTask = findViewById(R.id.fab);

        // Initialize database helper
        dbHelper = new TodoDatabaseHelper(this);

        // Create notification channel
        NotificationHelper.createNotificationChannel(this);

        // Load tasks from database
        loadTasksFromDatabase();

        // Set up RecyclerView
        taskAdapter = new TodoAdapter(taskList, this::onTaskClick);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(taskAdapter);

        // Floating Action Button for adding new tasks
        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
            startActivityForResult(intent, 1);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasksFromDatabase();
        taskAdapter.notifyDataSetChanged();
    }

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

    private void onTaskClick(TodoItem task) {
        Intent intent = new Intent(MainActivity.this, TodoDetailActivity.class);
        intent.putExtra("task_id", task.getId());
        intent.putExtra("task_name", task.getTitle());
        intent.putExtra("task_description", task.getDescription());
        intent.putExtra("task_image", task.getImageUri());
        intent.putExtra("task_reminder", task.getDateTime());
        startActivityForResult(intent, 2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && (requestCode == 1 || requestCode == 2)) {
            loadTasksFromDatabase();
            taskAdapter.notifyDataSetChanged();
            Toast.makeText(this, "Tasks Updated", Toast.LENGTH_SHORT).show();
        }
    }
}
