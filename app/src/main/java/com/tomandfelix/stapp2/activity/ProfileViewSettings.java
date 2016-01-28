package com.tomandfelix.stapp2.activity;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.tomandfelix.stapp2.R;
import com.tomandfelix.stapp2.persistency.DatabaseHelper;

public class ProfileViewSettings extends ServiceActivity {
    private CheckBox graphCheckBox;
    private CheckBox openChallengeCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_profile_view_settings);
        super.onCreate(savedInstanceState);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        graphCheckBox = (CheckBox) findViewById(R.id.profile_settings_graph_checkbox);
        openChallengeCheckBox = (CheckBox) findViewById(R.id.profile_settings_open_challenges_checkbox);
        setChecked();
        graphCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                DatabaseHelper.getInstance().setProfileActivityGraphSettings(b);
            }
        });

        openChallengeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                DatabaseHelper.getInstance().setProfileActivityOpenChallengeSettings(b);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setChecked();

    }

    public void setChecked(){
        if(DatabaseHelper.getInstance().getProfileActivityGrapSetting() == 1){
            graphCheckBox.setChecked(true);
        }
        if(DatabaseHelper.getInstance().getProfileActivityOpenChallengeSetting() == 1){
            openChallengeCheckBox.setChecked(true);
        }
    }
}
