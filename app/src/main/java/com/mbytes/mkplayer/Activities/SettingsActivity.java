package com.mbytes.mkplayer.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.mbytes.mkplayer.SettingsHelperActivities.AppearanceActivity;
import com.mbytes.mkplayer.SettingsHelperActivities.PlayerSettingsActivity;
import com.mbytes.mkplayer.R;
import com.mbytes.mkplayer.Utils.Preferences;

public class SettingsActivity extends AppCompatActivity  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Preferences preferences = new Preferences(this);
        LinearLayout playerSetting = findViewById(R.id.layout_player_setting);
        LinearLayout appearanceSetting=findViewById(R.id.layout_appearance_setting);
        RelativeLayout settingLayout = findViewById(R.id.setting_layout);
        TextView backBtn = findViewById(R.id.heading_setting);
        if (preferences.getBoolean("isFragOpen")){
            settingLayout.setVisibility(View.GONE);
        }
        backBtn.setOnClickListener(view -> finish());
        playerSetting.setOnClickListener(view -> {
            Intent intent=new Intent(SettingsActivity.this,PlayerSettingsActivity.class);
            startActivity(intent);
        });

        appearanceSetting.setOnClickListener(view -> {
            Intent intent=new Intent(SettingsActivity.this, AppearanceActivity.class);
            startActivity(intent);
        });


    }


}