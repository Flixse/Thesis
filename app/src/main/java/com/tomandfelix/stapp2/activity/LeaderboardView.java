package com.tomandfelix.stapp2.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.tomandfelix.stapp2.R;
import com.tomandfelix.stapp2.persistency.DatabaseHelper;
import com.tomandfelix.stapp2.persistency.Profile;
import com.tomandfelix.stapp2.persistency.ServerHelper;

import java.util.ArrayList;

/**
 * Created by Tom on 17/11/2014.
 * This is the class that will display the leaderboard
 */
public class LeaderboardView extends DrawerActivity {
    private Profile mProfile;
    private ListView leaderboardList;
    private LeaderboardListAdapter adapter;
    private ArrayList<Profile> list;
    private ShowcaseView mShowcaseView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_leaderboard);
        super.onCreate(savedInstanceState);
        mProfile = DatabaseHelper.getInstance().getOwner();
        leaderboardList = (ListView) findViewById(R.id.leaderboard_list);
        getLeaderboard();
        tutorialShowCase();
    }

    private void getLeaderboard() {
        if(ServerHelper.getInstance().checkInternetConnection()) {
            ServerHelper.getInstance().getLeaderboardById(DatabaseHelper.getInstance().getOwnerId(),
                    new ServerHelper.ResponseFunc<ArrayList<Profile>>() {
                        @Override
                        public void onResponse(ArrayList<Profile> response) {
                            list = response;
                            setupList();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            askForPassword();
                        }
                    }, false);
        }else{
            Toast.makeText(getApplicationContext(),getString(R.string.leaderboard_no_internet_connection), Toast.LENGTH_SHORT).show();
        }
    }

    private void tutorialShowCase(){
        ServerHelper.getInstance().isTutorialOfViewOn(mProfile.getId(), LEADERBOARD_VIEW_TUTORIAL, new ServerHelper.ResponseFunc<Boolean>() {
            @Override
            public void onResponse(Boolean response) {
                if(response) {
                    mShowcaseView = new ShowcaseView.Builder(LeaderboardView.this)
                            .setStyle(R.style.CustomShowcaseTheme2)
                            .setContentTitle(getString(R.string.tutorial_leaderboard_view_title))
                            .setContentText(getString(R.string.tutorial_leaderboard_view))
                            .build();
                    mShowcaseView.setButtonText(getString(R.string.tutorial_close));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(LeaderboardView.this, R.string.tutorial_error, Toast.LENGTH_LONG).show();
            }
        });

    }


    private void askForPassword() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(getString(R.string.leaderboard_error)).setTitle(getString(R.string.leaderboard_error_title));
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
                                            getLeaderboard();
                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError volleyError) {
                                            if (volleyError.getMessage().equals("wrong")) {
                                                askForPassword();
                                            }
                                        }
                                    });
                        }else{
                            Toast.makeText(getApplicationContext(),getString(R.string.leaderboard_no_internet_connection_on_error),Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        };
        alert.setPositiveButton(getString(R.string.confirm), listener);
        alert.setNegativeButton(getString(R.string.cancel), listener);
        alert.show();
    }

    private void setupList() {
        adapter = new LeaderboardListAdapter(LeaderboardView.this, R.layout.list_item_leaderboard, list);
        View header = getLayoutInflater().inflate(R.layout.list_head_foot_leaderboard, leaderboardList, false);
        TextView head = (TextView) header.findViewById(R.id.head_foot_text);
        head.setText(getString(R.string.leaderboard_load_higher_ranks));
        leaderboardList.addHeaderView(header);
        View footer = getLayoutInflater().inflate(R.layout.list_head_foot_leaderboard, leaderboardList, false);
        TextView foot = (TextView) footer.findViewById(R.id.head_foot_text);
        foot.setText(getString(R.string.leaderboard_load_lower_ranks));
        leaderboardList.addFooterView(footer);
        leaderboardList.setAdapter(adapter);
        leaderboardList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(ServerHelper.getInstance().checkInternetConnection()) {
                    int startRank;
                    int endRank;
                    if (position == 0 && (startRank = list.get(0).getRank()) != 1) {
                        ServerHelper.getInstance().getLeaderboardByRank(startRank - 2,
                                new ServerHelper.ResponseFunc<ArrayList<Profile>>() {
                                    @Override
                                    public void onResponse(ArrayList<Profile> response) {
                                        list.addAll(0, response);
                                        ((ArrayAdapter) ((HeaderViewListAdapter) leaderboardList.getAdapter()).getWrappedAdapter()).notifyDataSetChanged();
                                    }
                                }, null, false);
                    } else if (position == list.size() + 1 && (endRank = list.get(list.size() - 1).getRank()) % 10 == 0) {
                        ServerHelper.getInstance().getLeaderboardByRank(endRank + 1,
                                new ServerHelper.ResponseFunc<ArrayList<Profile>>() {
                                    @Override
                                    public void onResponse(ArrayList<Profile> response) {
                                        list.addAll(response);
                                        ((ArrayAdapter) ((HeaderViewListAdapter) leaderboardList.getAdapter()).getWrappedAdapter()).notifyDataSetChanged();
                                    }
                                }, null, false);
                    } else if (position > 0 && position <= list.size()) {
                        int destId = list.get(position - 1).getId();
                        if (destId == DatabaseHelper.getInstance().getOwnerId()) {
                            loadActivity(PROFILE);
                        } else {
                            Intent intent = new Intent(LeaderboardView.this, StrangerView.class);
                            intent.putExtra("strangerId", destId);
                            startActivity(intent);
                            overridePendingTransition(R.anim.enter_right, R.anim.leave_left);
                        }
                    }
                }else{
                    Toast.makeText(getApplicationContext(),getString(R.string.leaderboard_no_internet_connection), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private class LeaderboardListAdapter extends ArrayAdapter<Profile> {
        private int normalColor = getResources().getColor(R.color.secondaryText);
        private int accentColor = getResources().getColor(R.color.accentColor);
        private int itemLayoutId;

        public LeaderboardListAdapter(Context context, int itemLayoutId, ArrayList<Profile> data) {
            super(context, itemLayoutId, data);
            this.itemLayoutId = itemLayoutId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(itemLayoutId, parent, false);
            }
            Profile p = getItem(position);

            if(p != null) {
                TextView rank = (TextView) convertView.findViewById(R.id.leaderboard_rank);
                ImageView avatar = (ImageView) convertView.findViewById(R.id.leaderboard_avatar);
                TextView username = (TextView) convertView.findViewById(R.id.leaderboard_username);
                TextView experience = (TextView) convertView.findViewById(R.id.leaderboard_experience);


                int avatarID = getResources().getIdentifier("avatar_" + p.getAvatar() +"_128", "drawable", getPackageName());
                if(p.getId() == mProfile.getId()){
                    rank.setTextColor(accentColor);
                    username.setTextColor(accentColor);
                    experience.setTextColor(accentColor);
                } else {
                    rank.setTextColor(normalColor);
                    username.setTextColor(normalColor);
                    experience.setTextColor(normalColor);
                }
                if(rank != null) {rank.setText(Integer.toString(p.getRank()));}
                if(avatar != null) {avatar.setImageResource(avatarID);}
                if(username != null) {username.setText(p.getUsername());}
                if(experience != null) {experience.setText(Integer.toString(p.getExperience()));}
            }
            return convertView;
        }
    }
}
