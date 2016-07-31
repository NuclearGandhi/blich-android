package com.blackcracks.blich.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.DrawableRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackcracks.blich.R;

public class DrawerListAdapter extends BaseAdapter {

    private Context mContext;
    private String[] mTitles;
    private int[] mIcons;

    public DrawerListAdapter(Context context, String[] titles, @DrawableRes int[] icons) {
        mContext = context;
        mTitles = titles;
        mIcons = icons;
    }

    @Override
    public int getCount() {
        return mTitles.length;
    }

    @Override
    public Object getItem(int i) {
        return mTitles[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        @SuppressLint("ViewHolder")
        View root = LayoutInflater.from(mContext).inflate(R.layout.drawer_item, null);

        ImageView icon = (ImageView) root.findViewById(R.id.icon);
        TextView title = (TextView) root.findViewById(R.id.title);

        icon.setImageResource(mIcons[i]);
        title.setText(mTitles[i]);

        return root;
    }
}
