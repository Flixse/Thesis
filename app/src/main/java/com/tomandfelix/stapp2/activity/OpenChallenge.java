package com.tomandfelix.stapp2.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.gc.materialdesign.views.ButtonRectangle;
import com.gc.materialdesign.views.ProgressBarDeterminate;
import com.tomandfelix.stapp2.R;
import com.tomandfelix.stapp2.application.StApp;
import com.tomandfelix.stapp2.persistency.ChallengeStatus;
import com.tomandfelix.stapp2.persistency.DatabaseHelper;
import com.tomandfelix.stapp2.persistency.LiveChallenge;
import com.tomandfelix.stapp2.persistency.Profile;
import com.tomandfelix.stapp2.persistency.ServerHelper;
import com.tomandfelix.stapp2.service.ShimmerService;
import com.tomandfelix.stapp2.tools.Logging;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

public class OpenChallenge extends ServiceActivity {
    private static OpenChallengeHandler openHandler = null;
    private ListView openChallengeList;
    private OpenChallengeListAdapter adapter;
    private ArrayList<Profile> mProfileList = new ArrayList<>();
    private LiveChallenge challenge;
    private View buttons;
    private ButtonRectangle negativeButton;
    private ButtonRectangle positiveButton;
    private ButtonRectangle stopButton;
    private ProgressBarDeterminate progress;
    private TextView resultView;
    private int state;
    private static boolean timerRunning = false;
    private Date startTime;
    public static final int MSG_REFRESH = 1;
    private static final String START = "Start";
    private static final String WAITING = "Waiting";
    private static final String ACCEPT = "Accept";
    private static final String DECLINE = "Decline";
    private static final String DISMISS = "Dismiss";
    private static final String PLS_CONNECT = "Connect a sensor first";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_open_challenge);
        super.onCreate(savedInstanceState);

        openChallengeList = (ListView) findViewById(R.id.open_challenge_list_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        buttons = findViewById(R.id.open_challenge_buttons);
        negativeButton = (ButtonRectangle) findViewById(R.id.open_challenge_negative_btn);
        positiveButton = (ButtonRectangle) findViewById(R.id.open_challenge_positive_btn);
        stopButton = (ButtonRectangle) findViewById(R.id.open_challenge_stop);
        progress = (ProgressBarDeterminate) findViewById(R.id.open_challenge_progress);
        resultView = (TextView) findViewById(R.id.open_challenge_result);

        Intent intent = getIntent();
        String challengeIndex = intent.getStringExtra("challenge_unique_index");
        challenge = StApp.challenges.get(challengeIndex);
        getSupportActionBar().setTitle(challenge.getChallenge().getName());
        TextView challengeDescription = (TextView) findViewById(R.id.open_challenge_description);
        challengeDescription.setText(challenge.getChallenge().getDescription());
        if(ServerHelper.getInstance().checkInternetConnection()) {
            ServerHelper.getInstance().getProfilesByIds(challenge.getOpponents(), new ServerHelper.ResponseFunc<ArrayList<Profile>>() {
                @Override
                public void onResponse(ArrayList<Profile> response) {
                    mProfileList = response;
                    updateProfileViews();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Log.e("OpenChallenge", volleyError.getMessage());
                }
            }, false);
         } else {
            StApp.makeToast("Unable to get opponent information, no internet connection");
        }
    }

    public static Handler getHandler() {
        return openHandler;
    }

    @Override
    protected void onResume() {
        super.onResume();
        openHandler = new OpenChallengeHandler(this);
        StApp.setHandler(openHandler);
        updateChallengeViews();
        app.commandService(ShimmerService.REQUEST_STATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        StApp.setHandler(null);
        openHandler.removeCallbacksAndMessages(null);
        openHandler = null;
        timerRunning = false;
    }

    private void updateChallengeViews() {
        switch(challenge.getMyStatus()) {
            case NOT_ACCEPTED:
                buttons.setVisibility(View.VISIBLE);
                progress.setVisibility(View.INVISIBLE);
                negativeButton.setVisibility(View.VISIBLE);
                negativeButton.setText(DECLINE);
                positiveButton.setVisibility(View.VISIBLE);
                positiveButton.setText(ACCEPT);
                positiveButton.setEnabled(true);
                break;
            case ACCEPTED:
                if(challenge.hasEveryoneAccepted()) {
                    if(Arrays.asList(Logging.STATE_CONNECTED, Logging.STATE_SIT, Logging.STATE_STAND, Logging.STATE_OVERTIME).contains(state)) {
                        buttons.setVisibility(View.VISIBLE);
                        progress.setVisibility(View.INVISIBLE);
                        negativeButton.setVisibility(View.GONE);
                        positiveButton.setVisibility(View.VISIBLE);
                        positiveButton.setText(START);
                        positiveButton.setEnabled(true);
                    } else {
                        buttons.setVisibility(View.VISIBLE);
                        progress.setVisibility(View.INVISIBLE);
                        negativeButton.setVisibility(View.GONE);
                        positiveButton.setVisibility(View.VISIBLE);
                        positiveButton.setText(PLS_CONNECT);
                        positiveButton.setEnabled(false);
                    }
                } else {
                    buttons.setVisibility(View.VISIBLE);
                    progress.setVisibility(View.INVISIBLE);
                    negativeButton.setVisibility(View.GONE);
                    positiveButton.setVisibility(View.VISIBLE);
                    positiveButton.setText(WAITING);
                    positiveButton.setEnabled(false);
                }
                break;
            case STARTED:
                stopButton.setVisibility(View.VISIBLE);
                buttons.setVisibility(View.INVISIBLE);
                if(challenge.getChallenge().showProgress()) {
                    progress.setVisibility(View.VISIBLE);
                    progress.setMin(0);
                    progress.setMax(challenge.getChallenge().getDuration() * 60);
                    if (startTime == null)
                        startTime = DatabaseHelper.stringToDate(challenge.getMyStatusData().split("\\|")[0]);
                    progress.setProgress((int) ((new Date().getTime() - startTime.getTime()) / 1000));
                    if (!timerRunning) {
                        timerRunning = true;
                        startTimer();
                    }
                } else {
                    progress.setVisibility(View.INVISIBLE);
                }
                break;
            case DONE:
            case STOP:
                buttons.setVisibility(View.INVISIBLE);
                progress.setVisibility(View.INVISIBLE);
                break;
            case SCORED:
                stopButton.setVisibility(View.INVISIBLE);
                buttons.setVisibility(View.VISIBLE);
                progress.setVisibility(View.INVISIBLE);
                negativeButton.setVisibility(View.GONE);
                positiveButton.setVisibility(View.VISIBLE);
                positiveButton.setText(DISMISS);
                positiveButton.setEnabled(true);
                break;
        }
        if(challenge.getStatusMessage() != null) {
            resultView.setVisibility(View.VISIBLE);
            resultView.setText(challenge.getStatusMessage());
        } else {
            resultView.setVisibility(View.INVISIBLE);
        }
        if(challenge.getChallenge().showOpponentStatusIcons()) {
            int i = 0;
            for(Map.Entry<Integer, ChallengeStatus> e : challenge.getOpponentStatus().entrySet()) {
                View row = openChallengeList.getChildAt(i);
                if(row == null) {
                    return;
                } else if(e.getValue().getData() == null) {
                    row.findViewById(R.id.open_challenge_opponent_status_icon).setVisibility(View.INVISIBLE);
                } else {
                    ((ImageView) row.findViewById(R.id.open_challenge_opponent_status_icon)).setImageResource(Boolean.parseBoolean(e.getValue().getData()) ? R.drawable.icon_stand : R.drawable.icon_sit_red);
                    row.findViewById(R.id.open_challenge_opponent_status_icon).setVisibility(View.VISIBLE);
                }
                i++;
            }
        }
    }

    private void updateState(int state) {
        this.state = state;
        updateChallengeViews();
    }

    private void startTimer() {
        openHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                progress.setProgress((int) ((new Date().getTime() - startTime.getTime()) / 1000));
                if (progress.getProgress() < challenge.getChallenge().getDuration() * 60) {
                    startTimer();
                } else {
                    timerRunning = false;
                }
            }
        }, 1000);
    }

    public void onPositiveButton(View v) {
        switch(challenge.getMyStatus()) {
            case ACCEPTED:
                if(challenge.hasEveryoneAccepted()) {
                    challenge.start();
                }
                break;
            case NOT_ACCEPTED:
                challenge.accept();
                break;
            case SCORED:
                StApp.challenges.remove(challenge.getUniqueId());
                DatabaseHelper.getInstance().removeLC(challenge.getUniqueId());
                challenge.removeCallbacksAndMessages(null);
                finish();
                break;
        }
        updateChallengeViews();
    }

    public void onNegativeButton(View v) {
        switch(challenge.getMyStatus()) {
            case NOT_ACCEPTED:
                challenge.decline();
                StApp.challenges.remove(challenge.getUniqueId());
                DatabaseHelper.getInstance().removeLC(challenge.getUniqueId());
                challenge.removeCallbacksAndMessages(null);
                finish();
                break;

        }
        updateChallengeViews();
    }

    public void onStopButton(View v){
        Log.d("in stop","true");
        challenge.decline();
        StApp.challenges.remove(challenge.getUniqueId());
        DatabaseHelper.getInstance().removeLC(challenge.getUniqueId());
        challenge.removeCallbacksAndMessages(null);
        finish();
    }

    private void updateProfileViews() {
        adapter = new OpenChallengeListAdapter(OpenChallenge.this, R.layout.list_item_open_challenge, mProfileList);
        openChallengeList.setAdapter(adapter);
    }

    private class OpenChallengeListAdapter extends ArrayAdapter<Profile> {
        private int normalColor = getResources().getColor(R.color.secondaryText);
        private ArrayList<Profile> data;
        private int itemLayoutId;

        public OpenChallengeListAdapter(Context context, int itemLayoutId, ArrayList<Profile> data) {
            super(context, itemLayoutId, data);
            this.data = data;
            this.itemLayoutId = itemLayoutId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(itemLayoutId, parent, false);
            }
            Profile p = data.get(position);

            if(p != null) {
                TextView rank = (TextView) convertView.findViewById(R.id.open_challenge_rank);
                ImageView avatar = (ImageView) convertView.findViewById(R.id.open_challenge_avatar);
                TextView username = (TextView) convertView.findViewById(R.id.open_challenge_username);


                int avatarID = getResources().getIdentifier("avatar_" + p.getAvatar() +"_128", "drawable", getPackageName());

                if(rank != null) {rank.setText(Integer.toString(p.getRank()));}
                if(avatar != null) {avatar.setImageResource(avatarID);}
                if(username != null) {
                    username.setTextColor(normalColor);
                    username.setText(p.getUsername());
                }
            }
            return convertView;
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }
    }

    private static class OpenChallengeHandler extends Handler {
        private WeakReference<OpenChallenge> oc;

        public OpenChallengeHandler(OpenChallenge oc) {
            this.oc = new WeakReference<>(oc);
        }
        @Override
        public void handleMessage(Message msg) {
            if(oc.get() != null) {
                if (msg.what == MSG_REFRESH) {
                    oc.get().updateChallengeViews();
                } else {
                    oc.get().updateState(msg.what);
                }
            }
        }
    }
}