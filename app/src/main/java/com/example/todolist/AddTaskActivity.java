package com.example.todolist;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.Calendar;

public class AddTaskActivity extends AppCompatActivity {

    private EditText etTaskName, etTaskDescription;
    private ImageView ivTaskImage;
    private TextView tvReminderDateTime;
    private Button btnSelectImage, btnSaveTask;

    private Uri selectedImageUri = null;
    private String reminderDateTime = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // Initialize UI components
        etTaskName = findViewById(R.id.etTaskName);
        etTaskDescription = findViewById(R.id.etTaskDescription);
        ivTaskImage = findViewById(R.id.ivTaskImage);
        tvReminderDateTime = findViewById(R.id.tvReminderDateTime);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnSaveTask = findViewById(R.id.btnSaveTask);

        // Back navigation
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set image selection button
        btnSelectImage.setOnClickListener(v -> openImageSelector());

        // Set reminder selection
        tvReminderDateTime.setOnClickListener(v -> openDateTimePicker());

        // Set save button
        btnSaveTask.setOnClickListener(v -> saveTask());
    }

    private void openImageSelector() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 100);
    }

    private void openDateTimePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                            (view1, hourOfDay, minute) -> {
                                reminderDateTime = dayOfMonth + "/" + (month + 1) + "/" + year + " " + hourOfDay + ":" + minute;
                                tvReminderDateTime.setText(reminderDateTime);
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true);
                    timePickerDialog.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                ivTaskImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveTask() {
        String name = etTaskName.getText().toString();
        String description = etTaskDescription.getText().toString();

        if (name.isEmpty() || description.isEmpty() || reminderDateTime == null) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate a unique ID for the task (use SQLite in a real app)
//        int taskId = (int) (System.currentTimeMillis() / 1000);
        TodoDatabaseHelper dbHelper = new TodoDatabaseHelper(this);

        long taskId = dbHelper.insertTask(
                etTaskName.getText().toString(),
                etTaskDescription.getText().toString(),
                selectedImageUri != null ? selectedImageUri.toString() : null,
                reminderDateTime
        );

        if (taskId != -1) {
            Toast.makeText(this, "Task Added Successfully!", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity
        } else {
            Toast.makeText(this, "Error Adding Task!", Toast.LENGTH_SHORT).show();
        }
        // Go back to the previous screen
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
