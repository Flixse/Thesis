package com.tomandfelix.stapp2.activity;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.tomandfelix.stapp2.R;


/**
 * Created by Tom on 15/12/2014.
 * This class is an adapter for a gridview, it places
 */
public class AvatarGridAdapter extends BaseAdapter {
    private TypedArray data;
    private int itemLayoutId;
    Context context;

    public AvatarGridAdapter(Context context, int itemLayoutId, TypedArray data) {
        super();
        this.context = context;
        this.data = data;
        this.itemLayoutId = itemLayoutId;
    }

    @Override
    public int getCount() {
        return data.length();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(itemLayoutId, parent, false);
        }

        if(getItem(position) != null) {
            ImageView avatar = (ImageView) convertView.findViewById(R.id.avatar_list_image);
            if(avatar != null) {avatar.setImageDrawable(data.getDrawable(position));}
        }
        return convertView;
    }
}
