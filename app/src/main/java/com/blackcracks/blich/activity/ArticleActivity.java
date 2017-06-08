package com.blackcracks.blich.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.blackcracks.blich.R;
import com.blackcracks.blich.util.Constants;

public class ArticleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        Intent intent = getIntent();
        if (intent == null) {
            throw new IllegalArgumentException("Intent can't be null");
        }

        String bodyText = intent.getStringExtra(Constants.IntentConstants.EXTRA_ARTICLE_BODY);
        TextView body = (TextView) findViewById(R.id.article_body);
        body.setText(bodyText);
    }
}
