package com.tomandfelix.stapp2.activity;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.tomandfelix.stapp2.R;
import com.tomandfelix.stapp2.persistency.Profile;
import com.tomandfelix.stapp2.persistency.ServerHelper;

import java.util.ArrayList;


public class StrangerView extends ServiceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_stranger_profile);
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        int strangerId = getIntent().getIntExtra("strangerId", -1);
        if(ServerHelper.getInstance().checkInternetConnection()) {
            ServerHelper.getInstance().getOtherProfile(strangerId,
                    new ServerHelper.ResponseFunc<Profile>() {
                        @Override
                        public void onResponse(Profile response) {
                            updateVisual(response);
                        }
                    }, null, false); //TODO make this a meaningful errorListener
        }else{
            Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void updateVisual(final Profile profile) {
        getSupportActionBar().setTitle(profile.getUsername());
        TextView rank = (TextView) findViewById(R.id.stranger_rank);
        TextView username = (TextView) findViewById(R.id.stranger_username);
        TextView experience = (TextView) findViewById(R.id.stranger_xp);
        final TextView progress = (TextView) findViewById(R.id.stranger_progress);
        ImageView avatar = (ImageView) findViewById(R.id.stranger_avatar);

        int avatarID = getResources().getIdentifier("avatar_" + profile.getAvatar() + "_512", "drawable", getPackageName());

        rank.setText(Integer.toString(profile.getRank()));
        username.setText(profile.getUsername());
        experience.setText(Integer.toString(profile.getExperience()));
        avatar.setImageResource(avatarID);
        if(ServerHelper.getInstance().checkInternetConnection()) {
            ServerHelper.getInstance().getProgressOfOther(profile.getId(), new ServerHelper.ResponseFunc<Double>() {
                @Override
                public void onResponse(Double response) {
                    Log.d("StrangerView", response + "");
                    if (response > 0) {
                        float roundedFloat = (float) Math.round(response);
                        progress.setText(profile.getUsername() + " has achieved an average daily score of " + roundedFloat + "% in the past two weeks.");
                    } else {
                        progress.setText(profile.getUsername() + " has not been active in the past 2 weeks.");
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Log.e("VolleyError", "getProgressOfOther");
                }
            });
        }else{
            Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT).show();
        }
    }
}
