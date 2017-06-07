package com.blackcracks.blich.adapter;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackcracks.blich.R;
import com.blackcracks.blich.data.BlichContract.NewsEntry;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewsAdapter extends CursorRecyclerViewAdapter<NewsAdapter.ViewHolder> {

    private static final String URL_IMAGE_REGEX =
                    "((http[s]?|ftp):\\/)?\\/?([^:\\/\\s]+)" +
                    "((\\/\\w+)*\\/)([\\w\\-\\.]+[^#?\\s]+)(.*)?(#[\\w\\-]+)?(?:png|jpg)";

    private static final String URL_REGEX =
                    "^((http[s]?|ftp):\\/)?\\/?([^:\\/\\s]+)" +
                    "((\\/\\w+)*\\/)([\\w\\-\\.]+[^#?\\s]+)(.*)?(#[\\w\\-]+)?$";

    private Context mContext;

    public NewsAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.news_item, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
        String title = cursor.getString(cursor.getColumnIndex(NewsEntry.COL_TITLE));
        viewHolder.title.setText(Html.fromHtml(title));

        String body = cursor.getString(cursor.getColumnIndex(NewsEntry.COL_BODY));

        if (viewHolder.image.getVisibility() != View.GONE)
            viewHolder.image.setVisibility(View.GONE);

        new FindUrlTask(body, viewHolder).execute();

        String date = cursor.getString(cursor.getColumnIndex(NewsEntry.COL_DATE));

        String author = cursor.getString(cursor.getColumnIndex(NewsEntry.COL_AUTHOR));
        viewHolder.citation.setText("נכתב לפני - " + date + ", " + "על ידי " + author);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView body;
        ImageView image;
        TextView citation;

        ViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.news_title);
            body = (TextView) view.findViewById(R.id.news_body);
            image = (ImageView) view.findViewById(R.id.news_image);
            citation = (TextView) view.findViewById(R.id.news_citation);
        }
    }

    private class FindUrlTask extends AsyncTask<Void, Void, Boolean[]> {

        String mBody = "";
        ViewHolder mViewHolder;

        public FindUrlTask(String body, ViewHolder viewHolder) {

            mBody = body;

            mViewHolder = viewHolder;
            if (mViewHolder == null) {
                throw new IllegalArgumentException(
                        "The given parameters can't be null"
                );
            }
        }

        @Override
        protected Boolean[] doInBackground(Void... params) {

            Boolean[] didFind = new Boolean[2];
            didFind[0] = false;
            didFind[1] = false;

            //Extract url from body (if it exists)
            Pattern imagePattern = Pattern.compile(URL_IMAGE_REGEX);
            Matcher imageMatcher = imagePattern.matcher(mBody);

            if (imageMatcher.find()) didFind[0] = true;

            Pattern urlPattern = Pattern.compile(URL_REGEX);
            Matcher urlMatcher = urlPattern.matcher(mBody);

            if (urlMatcher.find()) didFind[1] = true;

            return didFind;
        }

        @Override
        protected void onPostExecute(Boolean[] didFind) {
            super.onPostExecute(didFind);
            if (didFind[0]) {
                mViewHolder.image.setVisibility(View.VISIBLE);
                mViewHolder.image.setImageDrawable(
                        ContextCompat.getDrawable(mContext, R.drawable.ic_image_24dp)
                );
                mBody = mBody.replaceAll(URL_IMAGE_REGEX, "");
            } else if (didFind[1]) {
                mBody = mBody.replaceAll(URL_REGEX, "");
            }

            if (mBody.equals("")) mViewHolder.body.setVisibility(View.GONE);
            else {
                mViewHolder.body.setVisibility(View.VISIBLE);
                mViewHolder.body.setText(Html.fromHtml(mBody));
            }
        }
    }
}
