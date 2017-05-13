package com.blackcracks.blich.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blackcracks.blich.R;

public class NewsAdapter extends CursorRecyclerViewAdapter<NewsAdapter.ViewHolder> {

    private Context mContext;

    public NewsAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public TextView title;
        public TextView body;
        public TextView date;
        public TextView author;

        public ViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.news_title);
            body = (TextView) view.findViewById(R.id.news_body);
            date = (TextView) view.findViewById(R.id.news_date);
            author = (TextView) view.findViewById(R.id.news_author);
        }
    }
}
