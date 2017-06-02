package com.blackcracks.blich.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blackcracks.blich.R;
import com.blackcracks.blich.data.BlichContract.NewsEntry;

public class NewsAdapter extends CursorRecyclerViewAdapter<NewsAdapter.ViewHolder> {

    private Context mContext;

    public NewsAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.news_item, null);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
        String title = cursor.getString(cursor.getColumnIndex(NewsEntry.COL_TITLE));
        viewHolder.title.setText(Html.fromHtml(title));

        String body = cursor.getString(cursor.getColumnIndex(NewsEntry.COL_BODY));
        viewHolder.body.setText(Html.fromHtml(body));

        String date = cursor.getString(cursor.getColumnIndex(NewsEntry.COL_DATE));
        viewHolder.date.setText("נכתב לפני - " + date + ", ");

        String author = cursor.getString(cursor.getColumnIndex(NewsEntry.COL_AUTHOR));
        viewHolder.author.setText("על ידי " + author);
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
