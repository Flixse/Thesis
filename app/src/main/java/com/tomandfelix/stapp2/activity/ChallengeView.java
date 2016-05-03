package com.tomandfelix.stapp2.activity;

/**
 * Created by Flixse on 19/03/2015.
 */

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.tomandfelix.stapp2.R;
import com.tomandfelix.stapp2.persistency.Challenge;
import com.tomandfelix.stapp2.persistency.DatabaseHelper;
import com.tomandfelix.stapp2.persistency.Profile;
import com.tomandfelix.stapp2.persistency.ServerHelper;
import com.tomandfelix.stapp2.tabs.SlidingTabLayout;

public class ChallengeView extends DrawerActivity {
    private SlidingTabLayout tabLayout;
    private ViewPager viewPager;
    private ChallengePagerAdapter adapter;
    private NotificationManager notificationManager;
    private ShowcaseView mShowcaseView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_challenge_list);
        super.onCreate(savedInstanceState);
        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1);
        adapter = new ChallengePagerAdapter(getFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.challenge_pager);
        viewPager.setAdapter(adapter);
        tabLayout = (SlidingTabLayout) findViewById(R.id.tabs_challenge_bar);
        tabLayout.setViewPager(viewPager);
        tabLayout.setSelectedIndicatorColors(R.color.primaryColor);
        tutorialShowCase();
    }

    @Override
    protected void onResume() {
        super.onResume();
        notificationManager.cancel(1);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        tabLayout.setLabelWidth();
        if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
        findViewById(R.id.tabs_bar).setVisibility(View.VISIBLE);
        else
        findViewById(R.id.tabs_bar).setVisibility(View.GONE);
    }

    private void tutorialShowCase(){
        Profile profile = DatabaseHelper.getInstance().getOwner();
        ServerHelper.getInstance().isTutorialOfViewOn(profile.getId(), CHALLENGE_VIEW_TUTORIAL, new ServerHelper.ResponseFunc<Boolean>() {
            @Override
            public void onResponse(Boolean response) {
                if(response) {
                    mShowcaseView = new ShowcaseView.Builder(ChallengeView.this)
                            .setStyle(R.style.CustomShowcaseTheme2)
                            .setContentTitle(getString(R.string.tutorial_challenge_view_title))
                            .setContentText(getString(R.string.tutorial_challenge_view))
                            .build();
                    mShowcaseView.setButtonText(getString(R.string.tutorial_close));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(ChallengeView.this, R.string.tutorial_error, Toast.LENGTH_LONG).show();
            }
        });

    }

    public void onInviteButton(View v) {
        Challenge c = ListChallengesFragment.getExpandedChallenge();
        Log.i("ChallengesList", c.toString());
        Intent intent = new Intent(this, ChallengeLeaderboard.class);
        intent.putExtra("challengeID", c.getId());
        startActivity(intent);
        overridePendingTransition(R.anim.enter_right, R.anim.leave_left);
    }

    private class ChallengePagerAdapter extends FragmentPagerAdapter {
        public ChallengePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int index) {
            switch (index) {
                case 0:
                    return new ListChallengesFragment();
                case 1:
                    return new OpenChallengesFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return position == 0 ? getString(R.string.challenge_view_page_title_list) : getString(R.string.challenge_view_page_title_progress);
        }
    }
}