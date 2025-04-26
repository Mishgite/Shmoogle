package com.example.smoogle;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "CameraDebug";
    private static final int PICK_IMAGE_REQUEST = 100;
    private static final int CAMERA_REQUEST = 200;
    private static final int CAMERA_PERMISSION_REQUEST = 300;
    private static final int STORAGE_PERMISSION_REQUEST = 201;  // Уникальный код для запроса разрешения

    private TextRecognitionViewModel viewModel;
    private ImageView imagePreview;
    private TextView resultText;
    private ProgressBar progressBar;
    private String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupViewModel();
    }

    private void initializeViews() {
        imagePreview = findViewById(R.id.image_view);
        resultText = findViewById(R.id.result_text);
        progressBar = findViewById(R.id.progress_bar);
        Button selectButton = findViewById(R.id.select_button);
        Button cameraButton = findViewById(R.id.camera_button);

        selectButton.setOnClickListener(v -> openImagePicker());
        cameraButton.setOnClickListener(v -> checkCameraPermission());
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