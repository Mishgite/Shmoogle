package com.example.smoogle;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.example.textrecognizer.R;
import com.google.mlkit.vision.common.InputImage;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 100;
    private TextRecognitionViewModel viewModel;
    private ImageView imagePreview;
    private TextView resultText;

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
        Button selectButton = findViewById(R.id.select_button);

        selectButton.setOnClickListener(v -> openImagePicker());
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(TextRecognitionViewModel.class);

        viewModel.getRecognizedText().observe(this, text ->
                resultText.setText(text != null ? text : "No text found"));

        viewModel.getErrorMessage().observe(this, error ->
                resultText.setText(error != null ? "Error: " + error : ""));
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST &&
                resultCode == RESULT_OK &&
                data != null &&
                data.getData() != null) {

            Uri imageUri = data.getData();
            processSelectedImage(imageUri);
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
            resultText.setText("Error loading image: " + e.getMessage());
        }
    }
}