package com.example.smoogle;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class SettingsActivity extends AppCompatActivity {
    private SwitchCompat themeSwitch;
    private SharedPreferences prefs;
    private Spinner languageSpinner;
    private Button backButton;
    private SharedPreferences sharedPreferences;
    private boolean settingsChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Применяем текущую тему перед созданием интерфейса
        applyCurrentTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);

        initViews();
        loadSettings();
        setupListeners();
        themeSwitch.setOnCheckedChangeListener((button, isChecked) -> {
            prefs.edit()
                    .putBoolean("isDarkTheme", isChecked)
                    .apply();
        });
    }

    private void applyCurrentTheme() {
        boolean isDarkTheme = getSharedPreferences("AppSettings", MODE_PRIVATE)
                .getBoolean("isDarkTheme", false);
        setTheme(isDarkTheme ? R.style.AppTheme_Dark : R.style.AppTheme_Light);
    }

    private void initViews() {
        themeSwitch = findViewById(R.id.switchTheme);
        languageSpinner = findViewById(R.id.spinnerLanguage);
        backButton = findViewById(R.id.backButton);
        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
    }

    private void loadSettings() {
        // Загрузка текущих настроек
        themeSwitch.setChecked(sharedPreferences.getBoolean("isDarkTheme", false));
        languageSpinner.setSelection(
                sharedPreferences.getString("language", "en").equals("ru") ? 0 : 1
        );
    }

    private void setupListeners() {
        // Переключение темы
        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit()
                    .putBoolean("isDarkTheme", isChecked)
                    .apply();
            settingsChanged = true;
        });

        // Выбор языка
        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String lang = position == 0 ? "ru" : "en";
                sharedPreferences.edit()
                        .putString("language", lang)
                        .apply();
                settingsChanged = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Кнопка возврата
        backButton.setOnClickListener(v -> handleBackPressed());
    }

    private void handleBackPressed() {
        if (settingsChanged) {
            setResult(RESULT_OK);
        }
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_OK); // Сообщаем MainActivity об изменениях
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Для плавного перехода
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}