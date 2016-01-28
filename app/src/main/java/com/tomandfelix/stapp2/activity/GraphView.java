package com.tomandfelix.stapp2.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.tomandfelix.stapp2.R;
import com.tomandfelix.stapp2.persistency.DatabaseHelper;
import com.tomandfelix.stapp2.tabs.SlidingTabLayout;

public class GraphView extends DrawerActivity {
    private SlidingTabLayout tabLayout;
    private ViewPager viewPager;
    private GraphPagerAdapter adapter;

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
            return position == 0 ? (DatabaseHelper.getInstance().dayStarted() == null ? "YESTERDAY OR OLDER" : "TODAY") : "Past 2 Weeks";
        }
    }
}