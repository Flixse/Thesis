package com.tomandfelix.stapp2.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.gc.materialdesign.views.ButtonRectangle;
import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.tomandfelix.stapp2.R;
import com.tomandfelix.stapp2.application.StApp;
import com.tomandfelix.stapp2.graphtools.GraphParser;
import com.tomandfelix.stapp2.persistency.ChallengeStatus;
import com.tomandfelix.stapp2.persistency.DatabaseHelper;
import com.tomandfelix.stapp2.persistency.LiveChallenge;
import com.tomandfelix.stapp2.persistency.Profile;
import com.tomandfelix.stapp2.persistency.ServerHelper;
import com.tomandfelix.stapp2.persistency.Tip;
import com.tomandfelix.stapp2.service.ShimmerService;
import com.tomandfelix.stapp2.tools.Logging;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Created by Tom on 17/11/2014.
 * The activity that shows you your own profile
 */
public class ProfileView extends DrawerActivity {
    private View toOpenChallengeDivider;
    private View graphBehaviourDivider;
    private static ImageView statusIcon;
    private Handler loggingMessageHandler = new ProfileHandler(this);
    private Handler dailyDataHandler;
    private TextView username;
    private TextView rank;
    private TextView experience;
    private TextView todayExperience;
    private TextView openChallengesAmount;
    private TextView graphDailyProgress;
    private TextView tips;
    private ImageView avatar;
    private ButtonRectangle pauseButton;
    private ButtonRectangle startStopButton;
    private LinearLayout openChallenges;
    private LinearLayout graphBehaviour;
    private ProgressBarCircularIndeterminate connecting;
    private static final String PAUSE = "Pause";
    private static final String RESUME = "Resume";
    private static final String START = "Start";
    private static final String STOP = "Stop";
    private int ordre = 0;
    private Runnable updateXP = new Runnable() {
        @Override
        public void run() {
            app.commandService(ShimmerService.XP_REQUEST);
            loggingMessageHandler.postDelayed(this, 1000);
        }
    };
    private Runnable updateDailyData = new Runnable() {
        public void run() {
            if(dailydata != null){
                long roundedDailyData = Math.round(dailydata.getTotalScore());
                if(dailydata.getTotalScore() > 90) {
                    graphDailyProgress.setTextColor(getResources().getColor(R.color.green));
                    graphDailyProgress.setText(getString(R.string.profile_day_behaviour, roundedDailyData));
                }else if (dailydata.getTotalScore() < 75) {
                    graphDailyProgress.setTextColor(getResources().getColor(R.color.red));
                    Log.d("daily score", dailydata.getTotalScore() + "");
                    graphDailyProgress.setText(getString(R.string.profile_day_behaviour, roundedDailyData));
                }else {
                    graphDailyProgress.setTextColor(getResources().getColor(R.color.orange));
                    graphDailyProgress.setText(getString(R.string.profile_day_behaviour, roundedDailyData));
                }
            }else{
                dailyDataHandler.postDelayed(this, 1000 * 60);
            }
        }
    };
    private GraphParser.DailyGraphData dailydata;
    private LinearLayout profileStatus;
    private RelativeLayout profileRank;
    private ShowcaseView mShowcaseView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("onCreate", "ProfileView");
        setContentView(R.layout.activity_profile);
        super.onCreate(savedInstanceState);

        dailyDataHandler = new Handler();
        graphBehaviourDivider = findViewById(R.id.profile_graph_behaviour_divider);
        toOpenChallengeDivider = findViewById(R.id.profile_to_open_challenges_divider);
        username = (TextView) findViewById(R.id.profile_username);
        rank = (TextView) findViewById(R.id.profile_rank);
        experience = (TextView) findViewById(R.id.profile_xp);
        todayExperience = (TextView) findViewById(R.id.profile_xp_today);
        openChallengesAmount = (TextView) findViewById(R.id.profile_open_challenges_amount);
        graphDailyProgress = (TextView) findViewById(R.id.profile_graph_behaviour);
        tips = (TextView) findViewById(R.id.profile_tips_textview);
        avatar = (ImageView) findViewById(R.id.profile_avatar);
        pauseButton = (ButtonRectangle) findViewById(R.id.profile_pause_button);
        startStopButton = (ButtonRectangle) findViewById(R.id.profile_start_stop_button);
        connecting = (ProgressBarCircularIndeterminate) findViewById(R.id.profile_progress);
        openChallenges = (LinearLayout) findViewById(R.id.profile_open_challenges);
        graphBehaviour = (LinearLayout) findViewById(R.id.profile_graph_behaviour_layout);
        profileStatus = (LinearLayout) findViewById(R.id.profile_status);
        profileRank = (RelativeLayout) findViewById(R.id.rank_layout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        Profile profile = getIntent().getParcelableExtra("profile");
        if (profile != null) {
            DatabaseHelper.getInstance().updateProfile(profile);
            updateVisual();
        } else {
            updateVisual();
            if(ServerHelper.getInstance().checkInternetConnection()) {
                ServerHelper.getInstance().getProfile(new ServerHelper.ResponseFunc<Profile>() {
                    @Override
                    public void onResponse(Profile response) {
                        if (response != null) {
                            DatabaseHelper.getInstance().updateProfile(response);

                            updateVisual();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        askForPassword();
                    }
                }, true);
            }else{
                Toast.makeText(getApplicationContext(),getString(R.string.profile_out_of_date), Toast.LENGTH_SHORT).show();
            }
        }

        statusIcon = (ImageView) findViewById(R.id.profile_status_icon);
        updateProfileViaSettingsView();
        updateToOpenChallengeView();
        dailydata = GraphParser.formatDailyData(DatabaseHelper.getInstance().getTodaysLogs(), DatabaseHelper.getInstance().getTodaysConnectionLogs());

        tutorialShowCase();
    }

    private void tutorialShowCase(){
        final Profile profile = DatabaseHelper.getInstance().getOwner();
        ServerHelper.getInstance().isTutorialOfViewOn(profile.getId(), PROFILE_VIEW_TUTORIAL, new ServerHelper.ResponseFunc<Boolean>() {
            @Override
            public void onResponse(Boolean response) {
                if(response){
                    mShowcaseView = new ShowcaseView.Builder(ProfileView.this)
                            .setStyle(R.style.CustomShowcaseTheme2)
                            .setContentTitle(getString(R.string.tutorial_profile_view_title))
                            .setContentText(getString(R.string.tutorial_profile_view_one))
                            .setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    switch(ordre){
                                        case 0:
                                            changeTutorialShowcaseView(new ViewTarget(profileRank),getString(R.string.tutorial_profile_view_title), getString(R.string.tutorial_profile_view_two));
                                            ordre++;
                                            break;
                                        case 1:
                                            changeTutorialShowcaseView(new ViewTarget(profileRank),getString(R.string.tutorial_profile_view_ranking_title), getString(R.string.tutorial_profile_view_ranking_one));
                                            ordre++;
                                            break;
                                        case 2:
                                            changeTutorialShowcaseView(new ViewTarget(profileRank),getString(R.string.tutorial_profile_view_ranking_title), getString(R.string.tutorial_profile_view_ranking_two));
                                            ordre++;
                                            break;
                                        case 3:
                                            changeTutorialShowcaseView(new ViewTarget(profileStatus),getString(R.string.tutorial_profile_view_sensor_title), getString(R.string.tutorial_profile_view_sensor_one));
                                            ordre++;
                                            break;
                                        case 4:
                                            changeTutorialShowcaseView(new ViewTarget(profileStatus),getString(R.string.tutorial_profile_view_sensor_title), getString(R.string.tutorial_profile_view_sensor_two));
                                            ordre++;
                                            break;
                                        case 5:
                                            changeTutorialShowcaseView(new ViewTarget(profileStatus),getString(R.string.tutorial_profile_view_sensor_title), getString(R.string.tutorial_profile_view_sensor_three));
                                            ordre++;
                                            break;
                                        case 6:
                                            changeTutorialShowcaseView(new ViewTarget(profileStatus),getString(R.string.tutorial_profile_view_sensor_title), getString(R.string.tutorial_profile_view_sensor_four));
                                            ordre++;
                                            break;
                                        case 7:
                                            changeTutorialShowcaseView(new ViewTarget(profileStatus),getString(R.string.tutorial_profile_view_sensor_title), getString(R.string.tutorial_profile_view_sensor_five));
                                            ordre++;
                                            break;
                                        case 8:
                                            changeTutorialShowcaseView(new ViewTarget(profileStatus),getString(R.string.tutorial_profile_view_sensor_title), getString(R.string.tutorial_profile_view_sensor_six));
                                            ordre++;
                                            break;
                                        case 9:
                                            changeTutorialShowcaseView(new ViewTarget(profileStatus),getString(R.string.tutorial_profile_view_sensor_title), getString(R.string.tutorial_profile_view_sensor_seven));
                                            ordre++;
                                            break;
                                        case 10:
                                            changeTutorialShowcaseView(new ViewTarget(tips),getString(R.string.tutorial_profile_view_tips_title), getString(R.string.tutorial_profile_view_tips));
                                            ordre++;
                                            break;
                                        case 11:
                                            try {
                                                Field field = Toolbar.class.getDeclaredField("mNavButtonView");
                                                field.setAccessible(true);
                                                View navigationView = (View) field.get(toolbar);
                                                changeTutorialShowcaseView(new ViewTarget(navigationView),getString(R.string.tutorial_home_button_title), getString(R.string.tutorial_home_button));
                                            } catch (NoSuchFieldException e) {
                                                e.printStackTrace();
                                            } catch (IllegalAccessException e) {
                                                e.printStackTrace();
                                            }
                                            mShowcaseView.setButtonText(getString(R.string.tutorial_close));
                                            ordre++;
                                            break;
                                        case 12:
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
                Toast.makeText(ProfileView.this, R.string.tutorial_error, Toast.LENGTH_LONG).show();
            }
        });

    }

    private void changeTutorialShowcaseView(ViewTarget viewTarget, String contentTitle, String contentText){
        mShowcaseView.setTarget(viewTarget);
        mShowcaseView.setContentTitle(contentTitle);
        mShowcaseView.setContentText(contentText);
    }

    private void askForPassword() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(R.string.profile_error).setTitle(R.string.profile_error_title);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setTransformationMethod(PasswordTransformationMethod.getInstance());
        alert.setView(input);
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        if(ServerHelper.getInstance().checkInternetConnection()) {
                            String password = input.getText().toString();
                            ServerHelper.getInstance().login(DatabaseHelper.getInstance().getOwner().getUsername(), password,
                                    new ServerHelper.ResponseFunc<Profile>() {
                                        @Override
                                        public void onResponse(Profile response) {
                                            DatabaseHelper.getInstance().updateProfile(response);
                                            updateVisual();
                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError volleyError) {
                                            if(volleyError.getMessage() != null) {
                                                if (volleyError.getMessage().equals("wrong")) {
                                                    askForPassword();
                                                }
                                            }
                                        }
                                    });
                        }else{
                            Toast.makeText(getApplicationContext(), R.string.profile_no_internet_error, Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        };
        alert.setPositiveButton(R.string.confirm, listener);
        alert.setNegativeButton(R.string.cancel, listener);
        alert.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //dailydata = GraphParser.formatDailyData(DatabaseHelper.getInstance().getTodaysLogs(), DatabaseHelper.getInstance().getTodaysConnectionLogs());
        StApp.setHandler(loggingMessageHandler);
        app.commandService(ShimmerService.REQUEST_STATE);
        loggingMessageHandler.post(updateXP);
        dailyDataHandler.post(updateDailyData);
        updateProfileViaSettingsView();
        updateToOpenChallengeView();
        updateTipsTextview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        StApp.setHandler(null);
        loggingMessageHandler.removeCallbacks(updateXP);
        dailyDataHandler.removeCallbacks(updateDailyData);
    }

    private void updateVisual() {
        final Profile profile = DatabaseHelper.getInstance().getOwner();
        getSupportActionBar().setTitle(profile.getFirstName() + " " + profile.getLastName());
        rank.setText(Integer.toString(profile.getRank()));
        experience.setText(Integer.toString(profile.getExperience()));
        username.setText(profile.getUsername());
        int avatarID = getResources().getIdentifier("avatar_" + profile.getAvatar() + "_512", "drawable", getPackageName());
        avatar.setImageResource(avatarID);
    }

    private void updateXPToday(int xpToday) {
        todayExperience.setText("+" + xpToday + " Today");
    }

    public void onPauseResume(View view) {
        if(pauseButton.getText().equals(PAUSE)) {
            app.commandService(ShimmerService.PAUSE);
        } else {
            String sensor = DatabaseHelper.getInstance().getSensor();
            if (sensor != null && !sensor.equals("")) {
                app.commandServiceConnect(sensor);
            }
        }
    }

    public void onStartStop(View view) {
        if(startStopButton.getText().equals(START)) {
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBTIntent, 1);
            } else if(DatabaseHelper.getInstance().getSensor() == null || DatabaseHelper.getInstance().getSensor().equals("")) {
                createSensorDialog().show();
            } else {
                app.commandService(ShimmerService.LOG_START_DAY);
                String sensor = DatabaseHelper.getInstance().getSensor();
                app.commandServiceConnect(sensor);
            }
        } else {
            app.commandService(ShimmerService.END_DAY);
        }
    }

    public void onGoToOpenChallenges(View v){
        Intent intent = new Intent(this, ChallengeView.class);
        StApp.setHandler(null);
        loggingMessageHandler.removeCallbacks(updateXP);
        startActivity(intent);
    }

    public void onGoToGraphs(View v){
        Intent intent = new Intent(this, GraphView.class);
        StApp.setHandler(null);
        loggingMessageHandler.removeCallbacks(updateXP);
        startActivity(intent);
    }

    private void updateState(int state) {
        switch(state) {
            case Logging.STATE_CONNECTING:
                Log.d("StateZ connecting!","?");
                statusIcon.setVisibility(View.GONE);
                connecting.setVisibility(View.VISIBLE);
                pauseButton.setVisibility(View.VISIBLE);
                pauseButton.setText(PAUSE);
                startStopButton.setText(STOP);
                break;
            case Logging.STATE_DISCONNECTED:
                Log.d("StateZ disconnected!","?");
                statusIcon.setVisibility(View.VISIBLE);
                statusIcon.setImageResource(R.drawable.icon_disconnected);
                connecting.setVisibility(View.GONE);
                pauseButton.setVisibility(View.VISIBLE);
                pauseButton.setText(RESUME);
                startStopButton.setText(STOP);
                break;
            case Logging.STATE_DAY_STOPPED:
                Log.d("StateZ state day stoppe","?");
                statusIcon.setVisibility(View.VISIBLE);
                statusIcon.setImageResource(R.drawable.icon_no_day_started);
                connecting.setVisibility(View.GONE);
                pauseButton.setVisibility(View.GONE);
                startStopButton.setText(START);
                updateVisual();
                break;
            case Logging.STATE_CONNECTED:
            case Logging.STATE_SIT:
                Log.d("StateZ connected+ sit!","?");
                statusIcon.setVisibility(View.VISIBLE);
                statusIcon.setImageResource(R.drawable.icon_sit_green);
                connecting.setVisibility(View.GONE);
                pauseButton.setVisibility(View.VISIBLE);
                pauseButton.setText(PAUSE);
                startStopButton.setText(STOP);
                break;
            case Logging.STATE_STAND:
                Log.d("StateZ stand!","?");
                statusIcon.setVisibility(View.VISIBLE);
                statusIcon.setImageResource(R.drawable.icon_stand);
                connecting.setVisibility(View.GONE);
                pauseButton.setVisibility(View.VISIBLE);
                pauseButton.setText(PAUSE);
                startStopButton.setText(STOP);
                break;
            case Logging.STATE_OVERTIME:
                Log.d("StateZ overtime!","?");
                statusIcon.setVisibility(View.VISIBLE);
                statusIcon.setImageResource(R.drawable.icon_sit_red);
                connecting.setVisibility(View.GONE);
                pauseButton.setVisibility(View.VISIBLE);
                pauseButton.setText(PAUSE);
                startStopButton.setText(STOP);
                break;
        }
        findViewById(R.id.profile_status).setVisibility(View.VISIBLE);
    }

    public void updateToOpenChallengeView(){
        int countChallenges = 0;
        for(Map.Entry<String, LiveChallenge> entry : StApp.challenges.entrySet()){
            if(entry.getValue().getMyStatus() == ChallengeStatus.Status.NOT_ACCEPTED || entry.getValue().getMyStatus() == ChallengeStatus.Status.ACCEPTED){
                countChallenges++;
            }
        }
        if(countChallenges>0){
            //openChallenges.setVisibility(View.VISIBLE);
            //toOpenChallengeDivider.setVisibility(View.VISIBLE);
            if(countChallenges ==1){
                openChallengesAmount.setText("1 open challenge!");
            }else{
                openChallengesAmount.setText("More than one open challenge!");
            }
        }else{
            openChallenges.setVisibility(View.INVISIBLE);
            toOpenChallengeDivider.setVisibility(View.INVISIBLE);
        }
    }

    public void updateProfileViaSettingsView(){
        if(DatabaseHelper.getInstance().getProfileActivityGrapSetting() == 1){
            graphBehaviour.setVisibility(View.VISIBLE);
            graphBehaviourDivider.setVisibility(View.VISIBLE);
        }else{
            graphBehaviour.setVisibility(View.INVISIBLE);
            graphBehaviour.setVisibility(View.INVISIBLE);
        }
        if(DatabaseHelper.getInstance().getProfileActivityOpenChallengeSetting() == 1){
            openChallenges.setVisibility(View.VISIBLE);
            toOpenChallengeDivider.setVisibility(View.VISIBLE);
        }else{
            openChallenges.setVisibility(View.INVISIBLE);
            toOpenChallengeDivider.setVisibility(View.INVISIBLE);
        }
    }

    public void updateTipsTextview(){
        final Profile profile = DatabaseHelper.getInstance().getOwner();
        int languageId = 0;
        switch (Locale.getDefault().getLanguage()) {
            case "nl":
                languageId = 1;
                break;
            case "fr":
                languageId = 2;
                break;
            case "en":
                languageId = 0;
                break;
            default :
                languageId = 0;
        }
        ServerHelper.getInstance().getTipByProfileIdLanguageId(profile.getId(), languageId, new ServerHelper.ResponseFunc<Tip>() {
            @Override
            public void onResponse(final Tip response) {
                tips.setText(response.getText());
                ServerHelper.getInstance().incrementTipsByProfileIdLanguageId(profile.getId(), response.getTipsId(), new ServerHelper.ResponseFunc<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d("response", response.getString("error"));
                        }catch(JSONException e){
                            e.printStackTrace();
                        }
                    }
                } ,new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        if(!ServerHelper.getInstance().checkInternetConnection()) {
                            //askForPassword();
                        }
                    }
                });
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                askForPassword();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                onStartStop(null);
            } else {
                Toast.makeText(this, "Bluetooth not enabled\nExiting...", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private Dialog createSensorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.profile_pick_sensor)).setTitle(getString(R.string.profile_pick_sensor_title));
        ListView listView = new ListView(this);
        builder.setView(listView);

        final ArrayList<String> deviceNames = new ArrayList<>();
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter != null) {
            //Get paired devices and add their address to the list
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice d : pairedDevices) {
                    if (d.getName().contains("RN42")) {
                        deviceNames.add(d.getAddress());
                    }
                }
            } else {
                deviceNames.add("No devices Found");
            }
        }

        //Populate the listView
        ArrayAdapter deviceNamesAdapter = new ArrayAdapter<>(this, R.layout.list_item_devices, deviceNames);
        listView.setAdapter(deviceNamesAdapter);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        final Dialog result =  builder.create();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                DatabaseHelper.getInstance().setSensor(deviceNames.get(position));
                result.dismiss();
                onStartStop(null);
            }
        });
        return result;
    }

    private static class ProfileHandler extends Handler {
        private final WeakReference<ProfileView> mProfileView;

        public ProfileHandler(ProfileView aProfileView) {
            mProfileView = new WeakReference<>(aProfileView);
        }

        @Override
        public void handleMessage(Message msg) {
            if(mProfileView.get() != null) {
                if(msg.what == 100) {
                    mProfileView.get().updateXPToday(msg.arg1);
                } else {
                    mProfileView.get().updateState(msg.what);
                }
            }
        }
    }
}