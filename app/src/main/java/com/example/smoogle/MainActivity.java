package com.example.smoogle;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.google.mlkit.vision.common.InputImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "CameraDebug";
    private static final int PICK_IMAGE_REQUEST = 100;
    private static final int CAMERA_REQUEST = 200;
    private static final int CAMERA_PERMISSION_REQUEST = 300;
    private static final int STORAGE_PERMISSION_REQUEST = 201;  // Уникальный код для запроса разрешения
    private static final int REQUEST_CODE_SETTINGS = 1001;
    private boolean isReturningFromSettings = false;
    private static final int REQUEST_EDIT_TEXT = 1002;


    private TextRecognitionViewModel viewModel;
    private ImageView imagePreview;
    private TextView resultText;
    private ProgressBar progressBar;
    private String currentPhotoPath;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applySettings();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeViews();
        setupViewModel(); // Добавить эту строку
    }

    private void initializeViews() {
        try {
            imagePreview = findViewById(R.id.image_view);
            resultText = findViewById(R.id.editTextResults); // Правильный ID
            progressBar = findViewById(R.id.progress_bar);
            Button editButton = findViewById(R.id.edit_button); // Добавьте кнопку в XML
            editButton.setOnClickListener(v -> openEditActivity());
            Button selectButton = findViewById(R.id.select_button);
            Button cameraButton = findViewById(R.id.camera_button);
            Button settingsButton = findViewById(R.id.settings_button);
            editButton.setOnClickListener(v -> {
                if (resultText.getText() != null) {
                    openEditActivity();
                } else {
                    Toast.makeText(this, "Нет текста для редактирования", Toast.LENGTH_SHORT).show();
                }
            });
            // Проверка на null
            if (selectButton == null || cameraButton == null || settingsButton == null) {
                throw new RuntimeException("One or more buttons not found!");
            }

            // Обработчики кликов остаются без изменений
            selectButton.setOnClickListener(v -> checkStoragePermission());
            cameraButton.setOnClickListener(v -> checkCameraPermission());
            settingsButton.setOnClickListener(v -> openSettings());

        } catch (Exception e) {
            Log.e(TAG, "View initialization failed", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    private void openEditActivity() {
        if (resultText.getText() == null) {
            Toast.makeText(this, "Текст отсутствует", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, EditTextActivity.class);
        intent.putExtra("text", resultText.getText().toString());
        startActivityForResult(intent, REQUEST_EDIT_TEXT);
    }
    // Загрузка настроек темы
    private void applySettings() {
        // Загрузка настроек темы
        boolean isDarkTheme = getSharedPreferences("AppSettings", MODE_PRIVATE)
                .getBoolean("isDarkTheme", false);
        SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);

        setTheme(isDarkTheme ? R.style.AppTheme_Dark : R.style.AppTheme_Light);

        // Загрузка настроек языка
        String lang = prefs.getString("language", "en");
        updateLocale(lang);

    }

    private void updateLocale(String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);

        Resources res = getResources();
        Configuration config = new Configuration(res.getConfiguration());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale);
            getApplicationContext().createConfigurationContext(config);
        } else {
            config.locale = locale;
        }

        res.updateConfiguration(config, res.getDisplayMetrics());
    }
    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SETTINGS);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(TextRecognitionViewModel.class);

        viewModel.getRecognizedText().observe(this, text -> {
            resultText.setText(text != null ? text : "No text found");
            progressBar.setVisibility(View.GONE);
        });

        viewModel.getErrorMessage().observe(this, error -> {
            resultText.setText(error != null ? "Error: " + error : "");
            progressBar.setVisibility(View.GONE);
        });

        viewModel.getIsProcessing().observe(this, isProcessing -> {
            progressBar.setVisibility(isProcessing ? View.VISIBLE : View.GONE);
            findViewById(R.id.select_button).setEnabled(!isProcessing);
            findViewById(R.id.camera_button).setEnabled(!isProcessing);
        });
    }
    private void showEditDialog() {
        String currentText = resultText.getText().toString();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Text");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setLines(5);
        input.setMaxLines(10);
        input.setVerticalScrollBarEnabled(true);
        input.setText(currentText);
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newText = input.getText().toString();
            resultText.setText(newText);
            // При необходимости обновить ViewModel:
            viewModel.updateRecognizedText(newText);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST
            );
        } else {
            startCamera();
        }
    }

    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            openImagePicker();
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_REQUEST
                );
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void openImagePicker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Для Android 13+ разрешение не требуется
            startImagePicker();
        } else {
            // Проверка разрешения для старых версий
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                startImagePicker();
            } else {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_REQUEST
                );
            }
        }
    }

    private void startImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    private void startCamera() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = createImageFile();
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(
                        this,
                        "com.example.smoogle.fileprovider",
                        photoFile
                );
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST);
            }
        }
    }

    private File createImageFile() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            if (storageDir != null && !storageDir.exists()) {
                if (!storageDir.mkdirs()) {
                    Log.e(TAG, "Failed to create directory");
                    return null;
                }
            }

            File image = File.createTempFile(
                    imageFileName,
                    ".jpg",
                    storageDir
            );
            currentPhotoPath = image.getAbsolutePath();
            Log.d(TAG, "File created: " + currentPhotoPath);
            return image;
        } catch (IOException e) {
            Log.e(TAG, "File creation error: " + e.getMessage());
            Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: " + requestCode + "/" + resultCode);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
                Uri imageUri = data.getData();
                processSelectedImage(imageUri);
            } else if (requestCode == CAMERA_REQUEST) {
                if (currentPhotoPath != null) {
                    File file = new File(currentPhotoPath);
                    Log.d(TAG, "File exists: " + file.exists());
                    Uri imageUri = FileProvider.getUriForFile(
                            this,
                            "com.example.smoogle.fileprovider",
                            file
                    );
                    Log.d(TAG, "URI: " + imageUri);
                    processSelectedImage(imageUri);
                }
            }
            if (requestCode == REQUEST_CODE_SETTINGS) {
                // Полный перезапуск приложения
                Intent restartIntent = new Intent(this, MainActivity.class);
                restartIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(restartIntent);
                finish();
                recreate();
        }
            else if (requestCode == REQUEST_EDIT_TEXT) { // Добавьте этот блок
                // Получение отредактированного текста
                if (data != null) {
                    String editedText = data.getStringExtra("editedText");
                    resultText.setText(editedText);
                    viewModel.updateRecognizedText(editedText);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isReturningFromSettings) {
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            isReturningFromSettings = false;
        }
    }
    private void processSelectedImage(Uri imageUri) {
        try {
            Glide.with(this)
                    .load(imageUri)
                    .into(imagePreview);

            InputImage image = InputImage.fromFilePath(this, imageUri);
            viewModel.processImage(image);
        } catch (IOException e) {
            Log.e(TAG, "Image processing error: " + e.getMessage());
            resultText.setText("Error loading image");
        }
    }
}