package com.tomandfelix.stapp2.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.tomandfelix.stapp2.R;
import com.tomandfelix.stapp2.persistency.DatabaseHelper;
import com.tomandfelix.stapp2.persistency.Profile;
import com.tomandfelix.stapp2.persistency.ServerHelper;

public class SettingsView extends DrawerActivity {
    private Profile mProfile;
    private ListView settingsList;
    private SettingsAdapter adapter;
    private Setting[] settings;
    private ShowcaseView mShowcaseView;
    private int ordre = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_settings);
        super.onCreate(savedInstanceState);

        mProfile = DatabaseHelper.getInstance().getOwner();

        settings = new Setting[7];
        int freq = DatabaseHelper.getInstance().getUploadFrequency() / 60000;
        settings[0] = new Setting(getString(R.string.settings_view_upload_frequency), freq + (freq == 1 ? getString(R.string.settings_view_upload_frequency_minute) :getString(R.string.settings_view_upload_frequency_minutes)));
        settings[1] = new Setting(getString(R.string.settings_view_sensor), DatabaseHelper.getInstance().getSensor());
        settings[2] = new Setting(getString(R.string.settings_view_profile_tab_settings),getString(R.string.settings_view_profile_tab_settings_description));
        settings[3] = new Setting(getString(R.string.settings_view_account_settings), DatabaseHelper.getInstance().getOwner().getUsername());
        settings[4] = new Setting(getString(R.string.settings_view_mobile_date),getString(R.string.settings_view_mobile_date_description), DatabaseHelper.getInstance().uploadOn3G(),
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        DatabaseHelper.getInstance().setUploadOn3G(isChecked);
                    }
                });
        settings[5] = new Setting(getString(R.string.settings_view_notifications), null, DatabaseHelper.getInstance().getNotification(),
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        DatabaseHelper.getInstance().setNotification(isChecked);
                    }
                });
        settings[6] = new Setting(getString(R.string.settings_view_logout), null);

        settingsList = (ListView) findViewById(R.id.settings_list);
        adapter = new SettingsAdapter(this, R.layout.list_item_settings, settings);
        settingsList.setAdapter(adapter);
        settingsList.setOnItemClickListener(new SettingsListener());
        tutorialShowCase();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case 1:
                String address = data.getExtras().getString("address");
                DatabaseHelper.getInstance().setSensor(address);
                settings[1].subTitle = address;
                adapter.notifyDataSetChanged();
                break;
            case 2:
                mProfile = DatabaseHelper.getInstance().getOwner();
                settings[2].subTitle = mProfile.getUsername();
                getNavigationDrawerAdapter().notifyDataSetChanged();
                adapter.notifyDataSetChanged();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void tutorialShowCase(){
        ServerHelper.getInstance().isTutorialOfViewOn(mProfile.getId(), SETTINGS_VIEW_TUTORIAL, new ServerHelper.ResponseFunc<Boolean>() {
            @Override
            public void onResponse(Boolean response) {
                if(response) {
                    mShowcaseView = new ShowcaseView.Builder(SettingsView.this)
                            .setStyle(R.style.CustomShowcaseTheme2)
                            .setContentTitle(getString(R.string.tutorial_settings_view_title))
                            .setContentText(getString(R.string.tutorial_settings_view))
                            .setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    switch (ordre) {
                                        case 0:
                                            changeTutorialShowcaseView(new ViewTarget(settingsList.getChildAt(0).findViewById(R.id.setting_title)), getString(R.string.tutorial_settings_view_upload_frequency_title), getString(R.string.tutorial_settings_view_upload_frequency));
                                            ordre++;
                                            break;
                                        case 1:
                                            changeTutorialShowcaseView(new ViewTarget(settingsList.getChildAt(1).findViewById(R.id.setting_title)), getString(R.string.tutorial_settings_view_sensor_title), getString(R.string.tutorial_settings_view_sensor));
                                            ordre++;
                                            break;
                                        case 2:
                                            changeTutorialShowcaseView(new ViewTarget(settingsList.getChildAt(2).findViewById(R.id.setting_title)), getString(R.string.tutorial_settings_view_personalisation_title), getString(R.string.tutorial_settings_view_personalisation));
                                            ordre++;
                                            break;
                                        case 3:
                                            changeTutorialShowcaseView(new ViewTarget(settingsList.getChildAt(3).findViewById(R.id.setting_title)), getString(R.string.tutorial_settings_view_account_settings_title), getString(R.string.tutorial_settings_view_account_settings));
                                            ordre++;
                                            break;
                                        case 4:
                                            changeTutorialShowcaseView(new ViewTarget(settingsList.getChildAt(4).findViewById(R.id.setting_title)), getString(R.string.tutorial_settings_view_mobile_data_title), getString(R.string.tutorial_settings_view_mobile_data));
                                            ordre++;
                                            break;
                                        case 5:
                                            changeTutorialShowcaseView(new ViewTarget(settingsList.getChildAt(5).findViewById(R.id.setting_title)), getString(R.string.tutorial_settings_view_notifications_title), getString(R.string.tutorial_settings_view_notifications));
                                            ordre++;
                                            break;
                                        case 6:
                                            changeTutorialShowcaseView(new ViewTarget(settingsList.getChildAt(6).findViewById(R.id.setting_title)), getString(R.string.tutorial_settings_view_log_off_title), getString(R.string.tutorial_settings_view_log_off));
                                            ordre++;
                                            break;
                                        case 7:
                                            mShowcaseView.hide();
                                            break;
                                        default:
                                            break;
                                    }

                                }
                            })
                            .build();
                    mShowcaseView.setButtonText(getString(R.string.tutorial_next));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(SettingsView.this, R.string.tutorial_error, Toast.LENGTH_LONG).show();
            }
        });

    }

    private void changeTutorialShowcaseView(ViewTarget viewTarget, String contentTitle, String contentText){
        mShowcaseView.setTarget(viewTarget);
        mShowcaseView.setContentTitle(contentTitle);
        mShowcaseView.setContentText(contentText);
    }

    private class Setting {
        private String title;
        private String subTitle;
        private boolean checked;
        private CompoundButton.OnCheckedChangeListener listener;

        private Setting(String title, String subTitle, boolean checked, CompoundButton.OnCheckedChangeListener listener) {
            this.title = title;
            this.subTitle = subTitle;
            this.checked = checked;
            this.listener = listener;
        }

        public Setting(String title, String subTitle) {
            this(title, subTitle, false, null);
        }
    }

    private class SettingsAdapter extends ArrayAdapter<Setting> {
        private int resourceId;

        public SettingsAdapter(Context context, int resource, Setting[] objects) {
            super(context, resource, objects);
            this.resourceId = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = getLayoutInflater().inflate(resourceId, parent, false);
            }

            final Setting s = getItem(position);

            if(s != null) {
                ((TextView) convertView.findViewById(R.id.setting_title)).setText(s.title);
                TextView subTitle = (TextView) convertView.findViewById(R.id.settings_subtitle);
                if(s.subTitle != null && !s.subTitle.equals("")) {
                    subTitle.setText(s.subTitle);
                    subTitle.setVisibility(View.VISIBLE);
                } else {
                    subTitle.setVisibility(View.GONE);
                }
                if(s.listener != null) {
                    CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.settings_checkBox);
                    checkBox.setVisibility(View.VISIBLE);
                    checkBox.setChecked(s.checked);
                    checkBox.setOnCheckedChangeListener(s.listener);
                } else {
                    convertView.findViewById(R.id.settings_checkBox).setVisibility(View.INVISIBLE);
                }
            }
            return convertView;
        }

        @Override
        public boolean isEnabled(int position) {
            return getItem(position).listener == null;
        }
    }

    private class SettingsListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch(position) {
                case 0:
                    AlertDialog.Builder alert = new AlertDialog.Builder(SettingsView.this);
                    alert.setMessage(getString(R.string.settings_view_upload_frequency_message)).setTitle(getString(R.string.settings_view_upload_frequency));
                    final EditText input = new EditText(SettingsView.this);
                    input.setInputType(InputType.TYPE_CLASS_NUMBER);
                    alert.setView(input);
                    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    int newUploadFreq = Integer.parseInt(input.getText().toString());
                                    if (newUploadFreq > 0) {
                                        DatabaseHelper.getInstance().setUploadFrequency(newUploadFreq * 60000);
                                        settings[0].subTitle = newUploadFreq + (newUploadFreq == 1 ? getString(R.string.settings_view_upload_frequency_minute) : getString(R.string.settings_view_upload_frequency_minutes));
                                        adapter.notifyDataSetChanged();
                                    }
                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                    break;
                            }
                        }
                    };
                    alert.setPositiveButton(getString(R.string.cancel), listener);
                    alert.setNegativeButton(getString(R.string.confirm), listener);
                    alert.show();
                    break;
                case 1:
                    Intent sensorIntent = new Intent(SettingsView.this, SensorSelection.class);
                    startActivityForResult(sensorIntent, 1);
                    break;
                case 2:
                    Intent profileViewIntent = new Intent(SettingsView.this, ProfileViewSettings.class);
                    startActivityForResult(profileViewIntent,2);
                    break;
                case 3:
                    Intent accountIntent = new Intent(SettingsView.this, AccountSettings.class);
                    startActivityForResult(accountIntent, 3);
                    break;
                case 6:
                    DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(ServerHelper.getInstance().checkInternetConnection()) {
                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        if (ServerHelper.getInstance().checkInternetConnection()) {
                                            ServerHelper.getInstance().logout();
                                        }
                                        DatabaseHelper.getInstance().setToken("");
                                        index = INITIAL;
                                        Intent intent = new Intent(SettingsView.this, FragmentViewer.class);
                                        startActivity(intent);
                                        overridePendingTransition(R.anim.enter_bottom, R.anim.leave_top);
                                        finish();
                                        break;
                                }
                            }else{
                                Toast.makeText(getApplicationContext(), getString(R.string.settings_view_logout_failed), Toast.LENGTH_SHORT).show();
                            }
                        }
                    };
                    if(ServerHelper.getInstance().checkInternetConnection()) {
                        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(SettingsView.this);
                        alertDialog.setMessage(getString(R.string.settings_view_logout_message));
                        alertDialog.setPositiveButton(getString(R.string.settings_view_logout), dialogListener);
                        alertDialog.setNegativeButton(getString(R.string.cancel), dialogListener);
                        alertDialog.show();
                    }else{
                        Toast.makeText(getApplicationContext(), getString(R.string.settings_view_logout_failed), Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    }
}
