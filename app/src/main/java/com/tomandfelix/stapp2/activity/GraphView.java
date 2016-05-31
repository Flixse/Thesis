package com.tomandfelix.stapp2.activity;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.tomandfelix.stapp2.R;
import com.tomandfelix.stapp2.persistency.DatabaseHelper;
import com.tomandfelix.stapp2.persistency.Profile;
import com.tomandfelix.stapp2.persistency.ServerHelper;
import com.tomandfelix.stapp2.tabs.SlidingTabLayout;

public class GraphView extends DrawerActivity {
    private SlidingTabLayout tabLayout;
    private ViewPager viewPager;
    private GraphPagerAdapter adapter;
    private ShowcaseView mShowcaseView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_graph);
        super.onCreate(savedInstanceState);

        adapter = new GraphPagerAdapter(getFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.graph_pager);
        viewPager.setAdapter(adapter);
        tabLayout = (SlidingTabLayout) findViewById(R.id.tabs_bar);
        tabLayout.setViewPager(viewPager);
        tabLayout.setSelectedIndicatorColors(R.color.primaryColor);
        tutorialShowCase();
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

    private class GraphPagerAdapter extends FragmentPagerAdapter {
        public GraphPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int index) {
            switch (index) {
                case 0:
                    return new GraphDayFragment();
                case 1:
                    return new GraphWeekFragment();
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
            return position == 0 ? (DatabaseHelper.getInstance().dayStarted() == null ? getString(R.string.graph_yesterday_or_older) : getString(R.string.graph_today)) : getString(R.string.graph_past_two_weeks);
        }
    }

    private void tutorialShowCase(){
        Profile profile = DatabaseHelper.getInstance().getOwner();
        ServerHelper.getInstance().isTutorialOfViewOn(profile.getId(), GRAPH_VIEW_TUTORIAL, new ServerHelper.ResponseFunc<Boolean>() {
            @Override
            public void onResponse(Boolean response) {
                if(response) {
                    mShowcaseView = new ShowcaseView.Builder(GraphView.this)
                            .setStyle(R.style.CustomShowcaseTheme2)
                            .setContentTitle(getString(R.string.tutorial_graph_view_title))
                            .setContentText(getString(R.string.tutorial_graph_view))
                            .build();
                    mShowcaseView.setButtonText(getString(R.string.tutorial_close));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(GraphView.this, R.string.tutorial_error, Toast.LENGTH_LONG).show();
            }
        });

    }

}