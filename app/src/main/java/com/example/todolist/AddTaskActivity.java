package com.example.todolist;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
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
    private long reminderTimeInMillis = 0;

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

        // Enable back navigation
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set image selection button
        btnSelectImage.setOnClickListener(v -> openImageSelector());

        // Set reminder date and time selection
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
                            (timeView, hourOfDay, minute) -> {
                                Calendar reminderCalendar = Calendar.getInstance();
                                reminderCalendar.set(year, month, dayOfMonth, hourOfDay, minute, 0);
                                reminderTimeInMillis = reminderCalendar.getTimeInMillis();
                                String reminderDateTime = dayOfMonth + "/" + (month + 1) + "/" + year + " " + hourOfDay + ":" + minute;
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
        String name = etTaskName.getText().toString().trim();
        String description = etTaskDescription.getText().toString().trim();

        if (name.isEmpty() || description.isEmpty() || reminderTimeInMillis == 0) {
            Toast.makeText(this, "Please fill all fields and set a reminder", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save task to database
        TodoDatabaseHelper dbHelper = new TodoDatabaseHelper(this);
        long taskId = dbHelper.insertTask(
                name,
                description,
                selectedImageUri != null ? selectedImageUri.toString() : null,
                String.valueOf(reminderTimeInMillis) // Save reminder time as a string
        );

        if (taskId != -1) {
            Toast.makeText(this, "Task Added Successfully!", Toast.LENGTH_SHORT).show();
            scheduleNotification(reminderTimeInMillis, (int) taskId, name);
            finish(); // Close the activity
        } else {
            Toast.makeText(this, "Error Adding Task!", Toast.LENGTH_SHORT).show();
        }
    }

    private void scheduleNotification(long timeInMillis, int taskId, String taskName) {
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("task_id", taskId);
        intent.putExtra("task_name", taskName);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                taskId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
