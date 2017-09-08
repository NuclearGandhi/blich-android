package com.blackcracks.blich.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackcracks.blich.GlideApp;
import com.blackcracks.blich.R;
import com.blackcracks.blich.util.Constants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArticleActivity extends AppCompatActivity {

    private static final String BLICH_BASE_URL = "https://www.blich.co.il";

    private static final String URL_IMAGE_REGEX =
            "((http[s]?|ftp):\\/)?\\/?([^:\\/\\s]+)" +
                    "((\\/\\w+)*\\/)([\\w\\-\\.]+[^#?\\s]+)(.*)?(#[\\w\\-]+)?(?:png|jpg)";

    private static final String URL_REGEX =
            "^((http[s]?|ftp):\\/)?\\/?([^:\\/\\s]+)" +
                    "((\\/\\w+)*\\/)([\\w\\-\\.]+[^#?\\s]+)(.*)?(#[\\w\\-]+)?$";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        Intent intent = getIntent();
        if (intent == null) {
            throw new IllegalArgumentException("Intent can't be null");
        }

        String bodyText = intent.getStringExtra(Constants.IntentConstants.EXTRA_ARTICLE_BODY);
        TextView body = findViewById(R.id.article_body);
        body.setText(bodyText);

        String titleText = intent.getStringExtra(Constants.IntentConstants.EXTRA_ARTICLE_TITLE);
        TextView title = findViewById((R.id.article_title));
        title.setText(Html.fromHtml(titleText));

        ImageView imageView = findViewById(R.id.article_image);

        new FindUrlTask(bodyText, body, imageView).execute();
    }

    private class FindUrlTask extends AsyncTask<Void, Void, Boolean[]> {

        String mBody = "";

        TextView mBodyView;
        ImageView mImageView;

        String mImageUrl = "";

        FindUrlTask(String body, TextView bodyView, ImageView imageView) {

            mBody = body;
            mBodyView = bodyView;
            mImageView = imageView;
        }

        @Override
        protected Boolean[] doInBackground(Void... params) {

            Boolean[] didFind = new Boolean[2];
            didFind[0] = false;
            didFind[1] = false;

            //Extract url from body (if it exists)
            Pattern imagePattern = Pattern.compile(URL_IMAGE_REGEX);
            Matcher imageMatcher = imagePattern.matcher(mBody);

            if (imageMatcher.find()) {
                didFind[0] = true;
                mImageUrl = imageMatcher.group(0);
            }

            Pattern urlPattern = Pattern.compile(URL_REGEX);
            Matcher urlMatcher = urlPattern.matcher(mBody);

            if (urlMatcher.find()) didFind[1] = true;

            return didFind;
        }

        @Override
        protected void onPostExecute(Boolean[] didFind) {
            super.onPostExecute(didFind);
            if (didFind[0]) {
                mImageView.setVisibility(View.VISIBLE);
                if (mImageUrl.contains("/sites/")) {
                    mImageUrl = BLICH_BASE_URL + mImageUrl;
                }

                GlideApp.with(getBaseContext())
                        .load(mImageUrl)
                        .placeholder(R.color.almost_black)
                        .fitCenter()
                        .into(mImageView);


                mBody = mBody.replaceAll(URL_IMAGE_REGEX, "");
            } else if (didFind[1]) {
                mBody = mBody.replaceAll(URL_REGEX, "");
            }

            if (mBody.equals("")) mBodyView.setVisibility(View.GONE);
            else {
                mBodyView.setVisibility(View.VISIBLE);
                mBodyView.setText(Html.fromHtml(mBody));
            }
        }
    }
}
