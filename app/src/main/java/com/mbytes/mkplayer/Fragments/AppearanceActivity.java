package com.mbytes.mkplayer.Fragments;

import android.content.DialogInterface;
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
    private MaterialSwitch themeSwitch,contrastSwitch,dynamicSwitch;
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
        themeLayout=findViewById(R.id.theme_layout);
        dynamicSwitch=findViewById(R.id.dynamic_switch);
        themeSwitch=findViewById(R.id.dark_switch);
        contrastSwitch=findViewById(R.id.contrast_switch);
        preferences=new Preferences(this);
        themeSwitch.setChecked(preferences.getDarkTheme());
        TextView themeText=findViewById(R.id.theme_text);
        if (preferences.getDarkTheme()){
            themeText.setText("On");
        }
        else {
            themeText.setText("Off");
        }
    }
    private void onSwitchAndClick(){
        int selectedTheme=preferences.getInt("sele_theme");
        themeSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            preferences.setDarkTheme(b);
            new Handler().postDelayed(()->setTheme(b),200);
        });
        contrastSwitch.setOnCheckedChangeListener((compoundButton, b) -> preferences.setContrast(b));
        dynamicSwitch.setOnCheckedChangeListener((compoundButton, b) -> preferences.setDynamicTheme(b));
        themeLayout.setOnClickListener(view -> {
            MaterialAlertDialogBuilder builder=new MaterialAlertDialogBuilder(this);
            builder.setTitle("Choose Default Theme");
            builder.setSingleChoiceItems(themeMode, selectedTheme, (dialogInterface, i) -> {
                    if(i==0){
                        preferences.setDefaultTheme(true);
                        preferences.setDarkTheme(false);
                        preferences.setInt("sele_theme",i);
                        new Handler().postDelayed(()-> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),200);

                    }
                    if(i==1){
                        preferences.setDefaultTheme(false);
                        preferences.setDarkTheme(false);
                        preferences.setInt("sele_theme",i);
                        new Handler().postDelayed(()-> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO),200);


                    }
                    if(i==2){
                        preferences.setDefaultTheme(false);
                        preferences.setDarkTheme(true);
                        preferences.setInt("sele_theme",i);
                        new Handler().postDelayed(()-> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES),200);

                    }
                    dialogInterface.dismiss();

            });
            builder.setOnCancelListener(dialogInterface -> {
            });
            builder.show();
        });
    }
    private void setTheme(boolean b) {
        if (b){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else if (!preferences.getDarkTheme()&&!preferences.getDefaultTheme()){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}