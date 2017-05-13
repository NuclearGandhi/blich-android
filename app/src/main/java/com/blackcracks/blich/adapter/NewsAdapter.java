package com.blackcracks.blich.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blackcracks.blich.data.BlichContract.NewsEntry;
import com.blackcracks.blich.R;

public class NewsAdapter extends CursorRecyclerViewAdapter<NewsAdapter.ViewHolder> {

    private Context mContext;

    public NewsAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.news_item, parent);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
        viewHolder.title.setText(cursor.getString(cursor.getColumnIndex(NewsEntry.COL_TITLE)));
        viewHolder.body.setText(cursor.getString(cursor.getColumnIndex(NewsEntry.COL_BODY)));
        viewHolder.date.setText(cursor.getString(cursor.getColumnIndex("נכתב ב - " + NewsEntry.COL_DATE + " ")));
        viewHolder.author.setText(cursor.getString(cursor.getColumnIndex("על ידי " + NewsEntry.COL_AUTHOR)));
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView body;
        TextView date;
        TextView author;

        ViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.news_title);
            body = (TextView) view.findViewById(R.id.news_body);
            date = (TextView) view.findViewById(R.id.news_date);
            author = (TextView) view.findViewById(R.id.news_author);
        }
    }
}
