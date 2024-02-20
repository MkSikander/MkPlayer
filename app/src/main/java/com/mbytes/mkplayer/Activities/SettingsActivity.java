package com.mbytes.mkplayer.Activities;

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
import com.mbytes.mkplayer.Fragments.PlayerSettingFragment;
import com.mbytes.mkplayer.R;

public class SettingsActivity extends AppCompatActivity implements PlayerSettingFragment.FragmentCallback {
    private RelativeLayout settingLayout;

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
        LinearLayout playerSetting = findViewById(R.id.layout_player_setting);
        settingLayout=findViewById(R.id.setting_layout);
        TextView backBtn = findViewById(R.id.heading_setting);
        backBtn.setOnClickListener(view -> finish());
        playerSetting.setOnClickListener(view -> {
            settingLayout.setVisibility(View.GONE);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main,new PlayerSettingFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }
    @Override
    public void onFragmentRemoved() {
        settingLayout.setVisibility(View.VISIBLE);
    }
}