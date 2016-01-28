package com.tomandfelix.stapp2.activity;

import android.app.Activity;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;

import com.tomandfelix.stapp2.R;
import com.tomandfelix.stapp2.persistency.DatabaseHelper;


/**
 * Created by Tom on 3/11/2014.
 * This class will create the proper fragment for the viewpager in FragmentViewer
 */
public class FragmentProvider extends Fragment {
    private int position;
    private OnFragmentInteractionListener mListener;
    private TypedArray avatars;

    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(String message);
    }

    static FragmentProvider init(int position) {
        FragmentProvider prov = new FragmentProvider();
        Bundle args = new Bundle();
        args.putInt("pos", position);
        prov.setArguments(args);
        return prov;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments() != null ? getArguments().getInt("pos") : 1;
    }

    @Override
    public void onAttach (Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e){
            throw new ClassCastException(activity.toString() + " must implememnt OnFragementInteractionListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View layoutView;
        switch(position) {
            case 0:
                layoutView = inflater.inflate(R.layout.fragment_login, container, false);
                ((EditText) layoutView.findViewById(R.id.login_username)).setText(DatabaseHelper.getInstance().getLastEnteredUsername());
                break;
            case 2:
                layoutView = inflater.inflate(R.layout.fragment_register, container, false);
                avatars = getResources().obtainTypedArray(R.array.avatars);
                Log.i("register", Integer.toString(avatars.length()));
                GridView avatarGridView = (GridView) layoutView.findViewById(R.id.new_avatar_grid);
                AvatarGridAdapter avatarGridAdapter = new AvatarGridAdapter(getActivity(), R.layout.grid_item_avatar, avatars);
                avatarGridView.setAdapter(avatarGridAdapter);
                layoutView.findViewById(R.id.new_avatar).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(layoutView.findViewById(R.id.new_avatar_grid).getVisibility() == View.GONE) {
                            layoutView.findViewById(R.id.new_avatar_grid).setVisibility(View.VISIBLE);
                        } else {
                            layoutView.findViewById(R.id.new_avatar_grid).setVisibility(View.GONE);
                        }
                    }
                });
                avatarGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        layoutView.findViewById(R.id.new_avatar).callOnClick();
                        mListener.onFragmentInteraction(getResources().getStringArray(R.array.avatar_names)[position]);
                    }
                });
                break;
            case 1:default:
                layoutView = inflater.inflate(R.layout.fragment_start, container, false);
                break;
        }
        return layoutView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(avatars != null) avatars.recycle();
    }
}
