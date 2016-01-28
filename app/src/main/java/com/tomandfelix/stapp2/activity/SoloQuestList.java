package com.tomandfelix.stapp2.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
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
import com.tomandfelix.stapp2.persistency.Solo;
import com.tomandfelix.stapp2.persistency.SoloList;

import java.util.List;

public class SoloQuestList extends DrawerActivity {
    private Profile mProfile;
    SoloQuestListAdapter soloAdapter;
    AdapterView.OnItemClickListener listener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_quest_list);
        super.onCreate(savedInstanceState);

        mProfile = DatabaseHelper.getInstance().getOwner();

        ListView questList = (ListView) findViewById(R.id.quest_list);
        soloAdapter = new SoloQuestListAdapter(this, R.layout.list_item_solo_quest, SoloList.getList());
        questList.setAdapter(soloAdapter);
        listener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(SoloQuestList.this, OpenSoloQuest.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent.putExtra("position", position);
                startActivity(intent);
            }
        };
        questList.setOnItemClickListener(listener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        soloAdapter.notifyDataSetChanged();
    }

    private class SoloQuestListAdapter extends ArrayAdapter<Solo> {
        private int itemLayoutId;

        public SoloQuestListAdapter(Context context, int itemLayoutId, List<Solo> data) {
            super(context, itemLayoutId, data);
            this.itemLayoutId = itemLayoutId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(itemLayoutId, parent, false);
            }
            Solo s = getItem(position);
            if(s != null) {
                ImageView difficulty = (ImageView) convertView.findViewById(R.id.solo_list_difficulty);
                TextView name = (TextView) convertView.findViewById(R.id.solo_list_name);
                TextView xp = (TextView) convertView.findViewById(R.id.solo_list_xp);
                TextView xpNeeded = (TextView) convertView.findViewById(R.id.solo_list_xp_needed);
                View xpImage = convertView.findViewById(R.id.solo_list_xp_needed_icon);
                if(s.getXpNeeded() <= mProfile.getExperience()) {
                    if(s.getHandler() == null) {
                        switch (s.getDifficulty()) {
                            case EASY:
                                difficulty.setImageResource(R.drawable.circle_green);
                                break;
                            case MEDIUM:
                                difficulty.setImageResource(R.drawable.circle_orange);
                                break;
                            case HARD:
                                difficulty.setImageResource(R.drawable.circle_red);
                                break;
                        }
                    } else {
                        difficulty.setImageResource(R.drawable.circle_blue);
                    }
                    xpImage.setVisibility(View.INVISIBLE);
                    xpNeeded.setVisibility(View.INVISIBLE);
                }else{
                    xpImage.setVisibility(View.VISIBLE);
                    xpNeeded.setText(s.getXpNeeded() - mProfile.getExperience() + " xp needed");
                    xpNeeded.setVisibility(View.VISIBLE);
                    difficulty.setImageResource(R.drawable.circle_grey);
                }
                name.setText(s.getName());
                xp.setText(Integer.toString(s.getxp()));
            }
            return convertView;
        }

        @Override
        public boolean isEnabled(int position) {
            return getItem(position).getXpNeeded() <= mProfile.getExperience();
        }
    }
}