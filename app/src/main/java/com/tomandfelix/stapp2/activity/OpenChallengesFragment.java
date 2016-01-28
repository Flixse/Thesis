package com.tomandfelix.stapp2.activity;

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.tomandfelix.stapp2.R;
import com.tomandfelix.stapp2.application.StApp;
import com.tomandfelix.stapp2.persistency.ChallengeStatus;
import com.tomandfelix.stapp2.persistency.LiveChallenge;
import com.tomandfelix.stapp2.persistency.Quest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Flixse on 19/03/2015.
 */
public class OpenChallengesFragment extends ListFragment {
    private static RequestAdapter requestAdapter;
    private static View boundedView;

    public static boolean hasAdapter() {
        return visible;
    }

    private static boolean visible = false;

    public static ArrayAdapter getAdapter() {
        return requestAdapter;
    }

    public static View getBoundedView() {
        return boundedView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        requestAdapter = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        visible = false;
        boundedView = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        visible = true;
        if(requestAdapter != null) {
            requestAdapter.notifyDataSetChanged();
        }
        boundedView = this.getView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("ListChallengesFragment", "onCreateView");
        View view = new ListView(getActivity());
        container.addView(view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        Log.d("ListChallengesFragment", "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        requestAdapter = new RequestAdapter(getActivity(), R.layout.list_item_challenge, new ArrayList<>(StApp.challenges.values()));

        this.setListAdapter(requestAdapter);
        this.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), OpenChallenge.class);
                intent.putExtra("challenge_unique_index", requestAdapter.getItem(position).getUniqueId());
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.enter_right, R.anim.leave_left);
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("DO NOT CRASH", "MKAY");
    }

    private class RequestAdapter extends ArrayAdapter<LiveChallenge> {
        private List<LiveChallenge> data;
        private int itemLayoutId;

        public RequestAdapter(Context context, int itemLayoutId, List<LiveChallenge> data) {
            super(context, itemLayoutId, data);
            this.data = data;
            this.itemLayoutId = itemLayoutId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(itemLayoutId, parent, false);
            }

            LiveChallenge c = getItem(position);

            if(c != null) {
                ImageView type = (ImageView) convertView.findViewById(R.id.challenge_list_type);
                TextView name = (TextView) convertView.findViewById(R.id.challenge_list_name);
                TextView xp = (TextView) convertView.findViewById(R.id.challenge_list_xp);
                TextView people = (TextView) convertView.findViewById(R.id.challenge_list_people);
                TextView status = (TextView) convertView.findViewById(R.id.challenge_status);

                type.setImageResource(c.getChallenge().getType().equals(Quest.Type.CHALLENGE) ? R.drawable.icon_competition : R.drawable.icon_collaboration);
                name.setText(c.getChallenge().getName());
                xp.setText(Integer.toString(c.getChallenge().getxp()));
                people.setText(Integer.toString(c.getOpponents().length + 1));
                if(c.getMyStatus() == ChallengeStatus.Status.NOT_ACCEPTED){
                    status.setVisibility(View.VISIBLE);
                    status.setText(R.string.profile_status_ready_to_accept);
                }else if (c.getMyStatus() == ChallengeStatus.Status.ACCEPTED && c.hasEveryoneAccepted()){
                    status.setVisibility(View.VISIBLE);
                    status.setText(R.string.profile_status_ready_to_play);
                }else if (c.getMyStatus() == ChallengeStatus.Status.STARTED){
                    status.setVisibility(View.VISIBLE);
                    status.setText(R.string.profile_status_started);
                }else{
                    status.setVisibility(View.INVISIBLE);
                }
            }
            return convertView;
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            if(observer != null) {
                super.unregisterDataSetObserver(observer);
            }
        }

        @Override
        public void notifyDataSetChanged() {
            data.clear();
            data.addAll(StApp.challenges.values());
            super.notifyDataSetChanged();
        }
    }
}
