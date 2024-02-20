package com.mbytes.mkplayer.SettingsHelperActivities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.mbytes.mkplayer.R;
import com.mbytes.mkplayer.Utils.Preferences;

public class AppearanceActivity extends AppCompatActivity {
    private Preferences preferences;
    private LinearLayout themeLayout;
    private int selectedTheme;
    private TextView themeText,backBtn;
    private MaterialSwitch contrastSwitch, dynamicSwitch;
    private final String[] themeMode = {"Follow System", "Light Mode", "Dark Mode"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_appearance);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initViews();
        onSwitchAndClick();
    }

    private void initViews() {
        themeLayout = findViewById(R.id.theme_layout);
        dynamicSwitch = findViewById(R.id.dynamic_switch);
        contrastSwitch = findViewById(R.id.contrast_switch);
        preferences = new Preferences(this);
        themeText = findViewById(R.id.theme_text);
        backBtn=findViewById(R.id.heading_appearance_setting);
        selectedTheme = preferences.getSelectedTheme();
        setThemeText();
    }

    private void onSwitchAndClick() {
        backBtn.setOnClickListener(view -> finish());
        //Contrast Switch
        contrastSwitch.setOnCheckedChangeListener((compoundButton, b) -> preferences.setContrast(b));
        //Dynamic Theme Switch
        dynamicSwitch.setOnCheckedChangeListener((compoundButton, b) -> preferences.setDynamicTheme(b));
        themeLayout.setOnClickListener(view -> {
            selectedTheme = preferences.getSelectedTheme();
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.setTitle("Choose Default Theme");
            builder.setSingleChoiceItems(themeMode, selectedTheme, (dialogInterface, i) -> {
                switch (i) {
                    case 0: {
                        preferences.setDefaultTheme(-1);
                        preferences.setSelectedTheme(0);
                        setThemeText();
                        new Handler().postDelayed(() -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM), 200);
                        break;
                    }
                    case 1: {
                        preferences.setDefaultTheme(1);
                        preferences.setSelectedTheme(1);
                        setThemeText();
                        new Handler().postDelayed(() -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO), 200);
                        break;
                    }
                    case 2: {
                        preferences.setDefaultTheme(2);
                        preferences.setSelectedTheme(2);
                        new Handler().postDelayed(() -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES), 200);
                        break;
                    }
                }
                setThemeText();
                dialogInterface.dismiss();
            });
            builder.setOnCancelListener(dialogInterface -> {
            });
            builder.show();
        });
    }

    @SuppressLint("SetTextI18n")
    private void setThemeText() {
        selectedTheme = preferences.getSelectedTheme();
        switch (selectedTheme) {
            case 0:
                themeText.setText("System Default");
                break;
            case 1:
                themeText.setText("Light");
                break;
            case 2:
                themeText.setText("Dark");
                break;
        }
    }
}