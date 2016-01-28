package com.tomandfelix.stapp2.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.tomandfelix.stapp2.R;
import com.tomandfelix.stapp2.persistency.DatabaseHelper;
import com.tomandfelix.stapp2.persistency.Profile;

/**
 * Created by Tom on 18/11/2014.
 * Base class for every activity that needs a side menu, this class takes care of this menu and the event handling for it
 */
public abstract class DrawerActivity extends ServiceActivity {
    protected String[] menuItems;
    protected final static int PROFILE = 0;
    protected final static int LEADERBOARD = 2;
    protected final static int GRAPHS = 3;
    protected final static int SOLO_QUEST = 4;
    protected final static int CHALLENGE_COOP = 5;
    protected final static int SETTINGS = 7;
    protected final static int INITIAL = PROFILE;
    protected static int index = INITIAL;
    private Profile mProfile;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private ListView leftDrawerList;

    private ArrayAdapter<String> navigationDrawerAdapter;
    private static int backgroundColor;
    private static int selectedBackgroundColor;
    private static int textColor;
    private static int selectedTextColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(this instanceof ProfileView) {
            index = PROFILE;
        } else if(this instanceof LeaderboardView) {
            index = LEADERBOARD;
        } else if(this instanceof GraphView) {
            index = GRAPHS;
        } else if(this instanceof SoloQuestList) {
            index = SOLO_QUEST;
        } else if(this instanceof ChallengeView) {
            index = CHALLENGE_COOP;
        } else if(this instanceof SettingsView) {
            index = SETTINGS;
        }
        mProfile = DatabaseHelper.getInstance().getOwner();
        backgroundColor = getResources().getColor(R.color.background);
        selectedBackgroundColor = getResources().getColor(R.color.drawerSelected);
        textColor = getResources().getColor(R.color.secondaryText);
        selectedTextColor = getResources().getColor(R.color.drawerSelectedText);

        menuItems = getResources().getStringArray(R.array.sideMenu);
        leftDrawerList = (ListView) findViewById(R.id.drawer_list);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        navigationDrawerAdapter = new DrawerAdapter(this, R.layout.list_item_drawer_profile, R.layout.list_item_drawer_divider, R.layout.list_item_drawer_normal, menuItems);
        leftDrawerList.setAdapter(navigationDrawerAdapter);


        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.setDrawerListener(drawerToggle);
        leftDrawerList.setOnItemClickListener(new DrawerListener());
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        if(index != 0) {
            loadActivity(0);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    protected void loadActivity(int newIndex) {
        Class destination;
        switch(newIndex) {
            case PROFILE:
                destination = ProfileView.class;
                break;
            case LEADERBOARD:
                destination = LeaderboardView.class;
                break;
            case GRAPHS:
                destination = GraphView.class;
                break;
            case SOLO_QUEST:
                destination = SoloQuestList.class;
                break;
            case CHALLENGE_COOP:
                destination = ChallengeView.class;
                break;
            case SETTINGS:
                destination = SettingsView.class;
                break;
            default:
                destination = ProfileView.class;
                break;
        }
        Log.i("loadActivity", destination.getName());
        Intent intent = new Intent(this, destination);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }

    protected ArrayAdapter<String> getNavigationDrawerAdapter() {
        return navigationDrawerAdapter;
    }

    private class DrawerListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            drawerLayout.closeDrawers();
            view.setSelected(true);
            if (position != index) {
                loadActivity(position);
            }
        }
    }

    private static void setSelected(View view, TextView text) {
        view.setBackgroundColor(selectedBackgroundColor);
        text.setTextColor(selectedTextColor);
    }

    private static void setUnselected(View view, TextView text) {
        view.setBackgroundColor(backgroundColor);
        text.setTextColor(textColor);
    }

    private class DrawerAdapter extends ArrayAdapter<String> {
        private int profileLayoutId;
        private int dividerLayoutId;
        private int normalLayoutId;
        private final static int PROF = 0;
        private final static int DIV = 1;
        private final static int NORMAL = 2;

        public DrawerAdapter(Context context, int profileLayoutId, int dividerLayoutId, int normalLayoutId, String[] data) {
            super(context, normalLayoutId, data);
            this.profileLayoutId = profileLayoutId;
            this.dividerLayoutId = dividerLayoutId;
            this.normalLayoutId = normalLayoutId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            int result = position == PROFILE ? PROF : (getItem(position).equals("div") ? DIV : NORMAL);
            if(convertView == null || ((int) convertView.getTag()) != position) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                if (result == PROF) {
                    convertView = inflater.inflate(profileLayoutId, parent, false);
                    convertView.setTag(PROF);
                } else if (result == DIV) {
                    convertView = inflater.inflate(dividerLayoutId, parent, false);
                    convertView.setTag(DIV);
                } else {
                    convertView = inflater.inflate(normalLayoutId, parent, false);
                    convertView.setTag(NORMAL);
                }
            }
            switch(result) {
                case PROF:
                    int avatarId = getResources().getIdentifier("avatar_" + mProfile.getAvatar() + "_128", "drawable", getPackageName());
                    ((ImageView) convertView.findViewById(R.id.drawer_avatar)).setImageResource(avatarId);
                    TextView username = (TextView) convertView.findViewById(R.id.drawer_username);
                    username.setText(mProfile.getUsername());
                    if(position == index) {
                        setSelected(convertView, username);
                    } else {
                        setUnselected(convertView, username);
                    }
                    break;
                case NORMAL:
                    TextView name = (TextView) convertView.findViewById(R.id.drawer_item_name);
                    name.setText(getItem(position));
                    if(position == index) {
                        setSelected(convertView, name);
                    } else {
                        setUnselected(convertView, name);
                    }
                    break;
            }
            return convertView;
        }

        @Override
        public boolean isEnabled(int position) {
            return super.isEnabled(position) && !getItem(position).equals("div");
        }

        @Override
        public void notifyDataSetChanged() {
            mProfile = DatabaseHelper.getInstance().getOwner();
            super.notifyDataSetChanged();
        }
    }
}