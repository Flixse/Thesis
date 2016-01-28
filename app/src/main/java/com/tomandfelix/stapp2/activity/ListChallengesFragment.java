package com.tomandfelix.stapp2.activity;

import android.app.ListFragment;
import android.content.Context;
import android.database.DataSetObserver;
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
import com.tomandfelix.stapp2.persistency.Challenge;
import com.tomandfelix.stapp2.persistency.ChallengeList;
import com.tomandfelix.stapp2.persistency.Quest;

import java.util.List;

/**
 * Created by Flixse on 19/03/2015.
 */
public class ListChallengesFragment extends ListFragment {
    private List<Challenge> list;
    private static int expandedIndex = -1;

    public ListChallengesFragment(){
        super();
        list = ChallengeList.getList();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = new ListView(getActivity());
        container.addView(view);
        return view;
    }

    @Override
    public void onDestroy() {
        expandedIndex = -1;
        super.onDestroy();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        ChallengeListAdapter challengeAdapter = new ChallengeListAdapter(getActivity(), R.layout.list_item_challenge, list);
        this.setListAdapter(challengeAdapter);
        this.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (expandedIndex == -1) {
                    expandedIndex = position;
                    view.findViewById(R.id.challenge_list_description).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.challenge_list_button).setVisibility(View.VISIBLE);
                } else if (expandedIndex == position) {
                    expandedIndex = -1;
                    view.findViewById(R.id.challenge_list_description).setVisibility(View.GONE);
                    view.findViewById(R.id.challenge_list_button).setVisibility(View.GONE);
                } else {
                    View expanded = getListView().getChildAt(expandedIndex - getListView().getFirstVisiblePosition());
                    expanded.findViewById(R.id.challenge_list_description).setVisibility(View.GONE);
                    expanded.findViewById(R.id.challenge_list_button).setVisibility(View.GONE);
                    expandedIndex = position;
                    view.findViewById(R.id.challenge_list_description).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.challenge_list_button).setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public static Challenge getExpandedChallenge() {
        return ChallengeList.getChallenge(expandedIndex);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("DO NOT CRASH", "OK");
    }

    private class ChallengeListAdapter extends ArrayAdapter<Challenge> {
        private int itemLayoutId;

        public ChallengeListAdapter(Context context, int itemLayoutId, List<Challenge> data) {
            super(context, itemLayoutId, data);
            this.itemLayoutId = itemLayoutId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(itemLayoutId, parent, false);
            }

            Challenge c = getItem(position);

            if(c != null) {
                ImageView type = (ImageView) convertView.findViewById(R.id.challenge_list_type);
                TextView name = (TextView) convertView.findViewById(R.id.challenge_list_name);
                TextView xp = (TextView) convertView.findViewById(R.id.challenge_list_xp);
                TextView people = (TextView) convertView.findViewById(R.id.challenge_list_people);
                TextView description = (TextView) convertView.findViewById(R.id.challenge_list_description);

                type.setImageResource(c.getType().equals(Quest.Type.CHALLENGE) ? R.drawable.icon_competition : R.drawable.icon_collaboration);
                name.setText(c.getName());
                xp.setText(Integer.toString(c.getxp()));
                people.setText(c.getMinAmount() == c.getMaxAmount() ? Integer.toString(c.getMinAmount()) : c.getMinAmount() + " - " + c.getMaxAmount());
                description.setText(c.getDescription());
            }
            return convertView;
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            if(observer != null) {
                super.unregisterDataSetObserver(observer);
            }
        }
    }
}
