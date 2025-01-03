package com.example.todolist;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

public class AddTaskActivity extends AppCompatActivity {

    private static final int REQUEST_LEGACY_PICK = 200;
    private static final int REQUEST_LEGACY_PERMISSION = 201;

    // Fields
    private EditText etTaskName, etTaskDescription;
    private ImageView ivTaskImage;
    private TextView tvReminderDateTime;
    private Button btnSelectImage, btnSaveTask;

    /**
     * This will hold the ID of the task if we are in "edit" mode.
     * If it stays -1, we are in "create" mode.
     */
    private int mTaskId = -1;

    /**
     * We'll store the final "owned" URI for the task's image here.
     * If the user doesn't pick a new image, we keep the old one.
     */
    private Uri selectedImageUri = null;

    private long reminderTimeInMillis = 0;

    /**
     * For Android 13+ Photo Picker
     */
    private final ActivityResultLauncher<PickVisualMediaRequest> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.PickVisualMedia(),
                    uri -> {
                        if (uri != null) {
                            Uri localUri = copyUriToAppStorage(uri);
                            if (localUri != null) {
                                selectedImageUri = localUri;
                                Glide.with(this)
                                        .load(localUri)
                                        .placeholder(R.drawable.placeholder_image)
                                        .into(ivTaskImage);
                            } else {
                                Toast.makeText(this, "Failed to copy image locally", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // Initialize UI
        etTaskName = findViewById(R.id.etTaskName);
        etTaskDescription = findViewById(R.id.etTaskDescription);
        ivTaskImage = findViewById(R.id.ivTaskImage);
        tvReminderDateTime = findViewById(R.id.tvReminderDateTime);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnSaveTask = findViewById(R.id.btnSaveTask);

        // Action bar back arrow
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Check if we came here in "edit" mode or "create" mode
        Intent intent = getIntent();
        mTaskId = intent.getIntExtra("task_id", -1);

        if (mTaskId != -1) {
            // We are editing an existing task
            // Let's get the existing fields from intent
            String taskName = intent.getStringExtra("task_name");
            String taskDescription = intent.getStringExtra("task_description");
            String taskImage = intent.getStringExtra("task_image");
            String taskReminder = intent.getStringExtra("task_reminder");

            // Populate the UI
            etTaskName.setText(taskName);
            etTaskDescription.setText(taskDescription);

            // Convert reminder to long if not null
            if (taskReminder != null && !taskReminder.isEmpty()) {
                try {
                    reminderTimeInMillis = Long.parseLong(taskReminder);
                    // You might want to format it back to dd/MM/yyyy HH:mm
                    tvReminderDateTime.setText(formatReminderTime(reminderTimeInMillis));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            if (taskImage != null && !taskImage.isEmpty()) {
                // We store it in selectedImageUri so if the user doesn't pick a new image, we keep this
                selectedImageUri = Uri.parse(taskImage);
                Glide.with(this)
                        .load(selectedImageUri)
                        .placeholder(R.drawable.placeholder_image)
                        .into(ivTaskImage);
            }
        }

        // Select image button
        btnSelectImage.setOnClickListener(v -> selectImage());

        // Set reminder date/time
        tvReminderDateTime.setOnClickListener(v -> openDateTimePicker());

        // Save task button
        btnSaveTask.setOnClickListener(v -> saveTask());
    }

    /**
     * Decide whether to use the Photo Picker (Android 13+) or fallback to ACTION_PICK for older versions.
     */
    private void selectImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PickVisualMediaRequest pickMediaRequest = new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build();
            imagePickerLauncher.launch(pickMediaRequest);
        } else {
            checkLegacyPermissionAndPick();
        }
    }

    private void checkLegacyPermissionAndPick() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_LEGACY_PERMISSION
            );
        } else {
            launchLegacyPicker();
        }
    }

    private void launchLegacyPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_LEGACY_PICK);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LEGACY_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchLegacyPicker();
            } else {
                Toast.makeText(this, "Permission denied. Cannot select image.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_LEGACY_PICK && resultCode == RESULT_OK && data != null) {
            Uri ephemeralUri = data.getData();
            if (ephemeralUri != null) {
                // Copy ephemeral URI to private storage
                Uri localUri = copyUriToAppStorage(ephemeralUri);
                if (localUri != null) {
                    selectedImageUri = localUri;
                    Glide.with(this)
                            .load(localUri)
                            .placeholder(R.drawable.placeholder_image)
                            .into(ivTaskImage);
                } else {
                    Toast.makeText(this, "Failed to copy image locally", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * Copy ephemeral or external Uri into app's private storage, returning a permanent file Uri.
     */
    private Uri copyUriToAppStorage(Uri sourceUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(sourceUri);
            if (inputStream == null) return null;

            File outFile = new File(getFilesDir(), "picked_" + System.currentTimeMillis() + ".jpg");
            OutputStream outputStream = new FileOutputStream(outFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            outputStream.close();
            // Return a Uri we own
            return Uri.fromFile(outFile);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Opens a DatePicker, then TimePicker to get the reminder time.
     */
    private void openDateTimePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                            this,
                            (timeView, hourOfDay, minute) -> {
                                Calendar reminderCalendar = Calendar.getInstance();
                                reminderCalendar.set(year, month, dayOfMonth, hourOfDay, minute, 0);
                                reminderTimeInMillis = reminderCalendar.getTimeInMillis();

                                tvReminderDateTime.setText(formatReminderTime(reminderTimeInMillis));
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                    );
                    timePickerDialog.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    /**
     * Save or update the task, depending on whether mTaskId == -1.
     */
    private void saveTask() {
        String name = etTaskName.getText().toString().trim();
        String description = etTaskDescription.getText().toString().trim();

        if (name.isEmpty() || description.isEmpty() || reminderTimeInMillis == 0) {
            Toast.makeText(this, "Please fill all fields and set a reminder", Toast.LENGTH_SHORT).show();
            return;
        }

        TodoDatabaseHelper dbHelper = new TodoDatabaseHelper(this);
        String imageUriString = (selectedImageUri != null) ? selectedImageUri.toString() : null;

        if (mTaskId == -1) {
            // CREATE / INSERT
            long newId = dbHelper.insertTask(
                    name,
                    description,
                    imageUriString,
                    String.valueOf(reminderTimeInMillis)
            );
            if (newId != -1) {
                Toast.makeText(this, "Task Added Successfully!", Toast.LENGTH_SHORT).show();
                scheduleNotification(reminderTimeInMillis, (int) newId, name);
                finish();
            } else {
                Toast.makeText(this, "Error Adding Task!", Toast.LENGTH_SHORT).show();
            }
        } else {
            // EDIT / UPDATE
            int rowsAffected = dbHelper.updateTask(
                    mTaskId,
                    name,
                    description,
                    imageUriString,
                    String.valueOf(reminderTimeInMillis)
            );
            if (rowsAffected == 1) {
                Toast.makeText(this, "Task Updated Successfully!", Toast.LENGTH_SHORT).show();
                // Possibly re-schedule the notification with the same taskId
                scheduleNotification(reminderTimeInMillis, mTaskId, name);
                finish();
            } else {
                Toast.makeText(this, "Error Updating Task!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * (Optional) Format the reminderTimeInMillis to dd/MM/yyyy HH:mm
     */
    private String formatReminderTime(long timeInMillis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timeInMillis);

        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH) + 1;
        int year = c.get(Calendar.YEAR);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        return String.format("%02d/%02d/%04d %02d:%02d", day, month, year, hour, minute);
    }

    /**
     * Schedule a notification for the given time, with the given taskId and taskName.
     */
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
