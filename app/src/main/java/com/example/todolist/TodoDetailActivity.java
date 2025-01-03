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

        // Initialize DB helper
        dbHelper = new TodoDatabaseHelper(this);

        // Get task details from Intent
        Intent intent = getIntent();
        taskId = intent.getIntExtra("task_id", -1);
        String taskName = intent.getStringExtra("task_name");
        String taskDescription = intent.getStringExtra("task_description");
        String taskImage = intent.getStringExtra("task_image");
        String taskReminder = intent.getStringExtra("task_reminder");

        // Populate UI
        tvTaskName.setText(taskName);
        tvTaskDescription.setText(taskDescription);
        tvTaskReminder.setText(taskReminder);

        // Use Glide to load image
        if (taskImage != null && !taskImage.isEmpty()) {
            Glide.with(this)
                    .load(taskImage)
                    .placeholder(R.drawable.placeholder_image)  // optional placeholder
                    .into(ivTaskImage);
        } else {
            ivTaskImage.setImageResource(R.drawable.placeholder_image);
        }

        // Modify button
        btnModifyTask.setOnClickListener(v -> {
            // Currently, you pass all these extras but we also need the "task_id" to update, not insert
            Intent modifyIntent = new Intent(TodoDetailActivity.this, AddTaskActivity.class);
            modifyIntent.putExtra("task_id", taskId);
            modifyIntent.putExtra("task_name", taskName);
            modifyIntent.putExtra("task_description", taskDescription);
            modifyIntent.putExtra("task_image", taskImage);
            modifyIntent.putExtra("task_reminder", taskReminder);
            startActivity(modifyIntent);

            // You might want to do startActivityForResult(...) if you want a callback
            // or just finish() here, depending on your flow:
            finish();
        });


        // Delete button
        btnDeleteTask.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    /**
     * Confirmation dialog to delete a task
     */
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

    /**
     * Actually delete the task from DB
     */
    private void deleteTask() {
        int isDeleted = dbHelper.deleteTask(taskId);
        if (isDeleted == 1) {
            Toast.makeText(this, "Task deleted successfully", Toast.LENGTH_SHORT).show();
            finish(); // go back to previous screen
        } else {
            Toast.makeText(this, "Failed to delete task", Toast.LENGTH_SHORT).show();
        }
    }
}
