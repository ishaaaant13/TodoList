package com.example.todolist;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class TodoDetailActivity extends AppCompatActivity {

    private ImageView ivTaskImage;
    private TextView tvTaskName, tvTaskDescription, tvTaskReminder;
    private Button btnModifyTask, btnDeleteTask;
    private TodoDatabaseHelper dbHelper;
    private int taskId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_detail);

        // Initialize UI components
        ivTaskImage = findViewById(R.id.ivTaskImage);
        tvTaskName = findViewById(R.id.tvTaskName);
        tvTaskDescription = findViewById(R.id.tvTaskDescription);
        tvTaskReminder = findViewById(R.id.tvTaskReminder);
        btnModifyTask = findViewById(R.id.btnModifyTask);
        btnDeleteTask = findViewById(R.id.btnDeleteTask);

        // Initialize database helper
        dbHelper = new TodoDatabaseHelper(this);

        // Get task details from intent
        Intent intent = getIntent();
        taskId = intent.getIntExtra("task_id", -1);
        String taskName = intent.getStringExtra("task_name");
        String taskDescription = intent.getStringExtra("task_description");
        String taskImage = intent.getStringExtra("task_image");
        String taskReminder = intent.getStringExtra("task_reminder");

        // Populate the UI with task details
        tvTaskName.setText(taskName);
        tvTaskDescription.setText(taskDescription);
        tvTaskReminder.setText(taskReminder);

        if (taskImage != null && !taskImage.isEmpty()) {
            Glide.with(this).load(taskImage).into(ivTaskImage);
        } else {
            ivTaskImage.setImageResource(R.drawable.placeholder_image);
        }

        // Modify task button listener
        btnModifyTask.setOnClickListener(v -> {
            Intent modifyIntent = new Intent(TodoDetailActivity.this, AddTaskActivity.class);
            modifyIntent.putExtra("task_id", taskId);
            modifyIntent.putExtra("task_name", taskName);
            modifyIntent.putExtra("task_description", taskDescription);
            modifyIntent.putExtra("task_image", taskImage);
            modifyIntent.putExtra("task_reminder", taskReminder);
            startActivity(modifyIntent);
            finish();
        });

        // Delete task button listener
        btnDeleteTask.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    // Show confirmation dialog before deleting the task
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Task");
        builder.setMessage("Are you sure you want to delete this task?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteTask();
            }
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }

    // Delete task from the database
    private void deleteTask() {
        int isDeleted = dbHelper.deleteTask(taskId);
        if (isDeleted == 1) {
            Toast.makeText(this, "Task deleted successfully", Toast.LENGTH_SHORT).show();
            finish(); // Go back to the previous screen
        } else {
            Toast.makeText(this, "Failed to delete task", Toast.LENGTH_SHORT).show();
        }
    }
}
