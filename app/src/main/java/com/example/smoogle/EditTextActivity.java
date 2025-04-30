package com.example.smoogle;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class EditTextActivity extends AppCompatActivity {

    private TextInputEditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applySettings();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_text);

        editText = findViewById(R.id.editText);
        Button btnCopy = findViewById(R.id.btnCopy);
        Button btnSearch = findViewById(R.id.btnSearch);
        Button btnBack = findViewById(R.id.btnBack);

        // Получение текста из MainActivity
        String text = getIntent().getStringExtra("text");
        if (text != null) editText.setText(text);

        // Копирование текста
        btnCopy.setOnClickListener(v -> copyText());

        // Поиск в интернете
        btnSearch.setOnClickListener(v -> searchText());

        // Возврат с сохранением изменений
        btnBack.setOnClickListener(v -> returnToMain());
    }

    private void copyText() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied Text", editText.getText());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Текст скопирован", Toast.LENGTH_SHORT).show();
    }

    private void searchText() {
        String query = editText.getText().toString().trim();
        if (!query.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://www.google.com/search?q=" + Uri.encode(query)));
            startActivity(intent);
        }
    }

    private void returnToMain() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("editedText", editText.getText().toString());
        setResult(RESULT_OK, resultIntent);
        finish();
    }
    private void applySettings() {
        boolean isDarkTheme = getSharedPreferences("AppSettings", MODE_PRIVATE)
                .getBoolean("isDarkTheme", false);
        setTheme(isDarkTheme ? R.style.AppTheme_Dark : R.style.AppTheme_Light);
    }
}
