package com.example.smoogle;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

public class TextRecognitionViewModel extends ViewModel {
    private static final String TAG = "TextRecognitionVM";
    private final MutableLiveData<String> recognizedText = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isProcessing = new MutableLiveData<>();
    private final TextRecognizer textRecognizer;

    public TextRecognitionViewModel() {
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
    }

    public LiveData<String> getRecognizedText() {
        return recognizedText;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsProcessing() {
        return isProcessing;
    }

    public void processImage(InputImage image) {
        isProcessing.setValue(true);
        textRecognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    recognizedText.setValue(visionText.getText());
                    isProcessing.setValue(false);
                })
                .addOnFailureListener(e -> {
                    errorMessage.setValue(e.getMessage());
                    isProcessing.setValue(false);
                });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        textRecognizer.close();
    }
}