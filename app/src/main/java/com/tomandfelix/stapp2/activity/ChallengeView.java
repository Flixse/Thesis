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

import com.tomandfelix.stapp2.R;
import com.tomandfelix.stapp2.persistency.Challenge;
import com.tomandfelix.stapp2.tabs.SlidingTabLayout;

public class ChallengeView extends DrawerActivity {
    private SlidingTabLayout tabLayout;
    private ViewPager viewPager;
    private ChallengePagerAdapter adapter;
    private NotificationManager notificationManager;

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
            return position == 0 ? "Challenges" : "Progress";
        }
    }
}