package com.example.todolist;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todolist.AddTaskActivity;
import com.example.todolist.R;
import com.example.todolist.TodoAdapter;
import com.example.todolist.TodoDatabaseHelper;
import com.example.todolist.TodoDetailActivity;
import com.example.todolist.TodoItem;
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

        recyclerView = findViewById(R.id.recyclerView);
        FloatingActionButton fabAddTask = findViewById(R.id.fab);

        // Initialize database helper
        dbHelper = new TodoDatabaseHelper(this);

        // Load tasks from the database
        loadTasksFromDatabase();

        // Set up RecyclerView
        taskAdapter = new TodoAdapter(taskList, this::onTaskClick);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(taskAdapter);

        // Floating Action Button to add a new task
        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
            startActivityForResult(intent, 1); // Request code 1 for adding tasks
        });
    }

    // Load tasks from the database
    private void loadTasksFromDatabase() {
        taskList.clear(); // Clear the list before loading new data
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

    // Handle task click
    private void onTaskClick(TodoItem task) {
        Intent intent = new Intent(MainActivity.this, TodoDetailActivity.class);
        intent.putExtra("task_id", task.getId());
        intent.putExtra("task_name", task.getTitle());
        intent.putExtra("task_description", task.getDescription());
        intent.putExtra("task_image", task.getImageUri());
        intent.putExtra("task_reminder", task.getDateTime());
        startActivityForResult(intent, 2); // Request code 2 for task detail
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == 1 || requestCode == 2) {
                // Reload tasks after adding/updating/deleting
                loadTasksFromDatabase();
                taskAdapter.notifyDataSetChanged();
                Toast.makeText(this, "Tasks Updated", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
