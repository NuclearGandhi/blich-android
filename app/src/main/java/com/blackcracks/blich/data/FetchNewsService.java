package com.blackcracks.blich.data;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.support.annotation.IntDef;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.blackcracks.blich.data.BlichContract.NewsEntry;
import com.blackcracks.blich.sync.BlichSyncAdapter;
import com.blackcracks.blich.util.Constants;
import com.blackcracks.blich.util.Constants.IntentConstants;
import com.blackcracks.blich.util.Utilities;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Retention;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import static java.lang.annotation.RetentionPolicy.SOURCE;


public class FetchNewsService extends IntentService {

    private static final String TAG = FetchNewsService.class.getSimpleName();

    @Retention(SOURCE)
    @IntDef({CATEGORY_GENERAL, CATEGORY_TET, CATEGORY_YUD, CATEGORY_YA, CATEGORY_YB})
    public @interface NewsCategory {}

    public static final int CATEGORY_GENERAL = 0;
    public static final int CATEGORY_TET = 1;
    public static final int CATEGORY_YUD = 2;
    public static final int CATEGORY_YA = 3;
    public static final int CATEGORY_YB = 4;

    private static final String NEWS_GENERAL_URL = "https://blich.co.il/xml_blich_news";
    private static final String NEWS_TET_URL = "https://blich.co.il/xml_tet_news";
    private static final String NEWS_YUD_URL = "https://blich.co.il/xml_yud_news";
    private static final String NEWS_YA_URL = "https://blich.co.il/xml_ya_news";
    private static final String NEWS_YB_URL = "https://blich.co.il/xml_yb_news";

    private static final String TAG_ARTICLE = "node";
    private static final String TAG_TITLE = "title";
    private static final String TAG_BODY = "body";
    private static final String TAG_AUTHOR = "quot";
    private static final String TAG_POST_DATE = "Postdate";

    public FetchNewsService() {
        super("FetchNewsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        @NewsCategory int category = intent.getIntExtra(Constants.IntentConstants.EXTRA_NEWS_CATEGORY, CATEGORY_GENERAL);

        if (Utilities.News.getIsFetchingForCategory(getBaseContext(), category)) return;

        //Set isFetching to true;
        Utilities.News.setIsFetchingForCategory(getBaseContext(), category, true);

        int status = fetchNews(category);
        Intent broadcast = new Intent(Utilities.News.getActionForCategory(category));
        broadcast.putExtra(IntentConstants.EXTRA_FETCH_STATUS, status);
        LocalBroadcastManager.getInstance(getApplicationContext())
                .sendBroadcast(broadcast);

        //Set isFetching to false
        Utilities.News.setIsFetchingForCategory(getBaseContext(), category, false);
    }

    private @BlichSyncAdapter.FetchStatus int fetchNews(@NewsCategory int category) {
        BufferedReader reader = null;
        String html = null;

        String SOURCE_URL = "";
        switch(category) {
            case CATEGORY_GENERAL: {
                SOURCE_URL = NEWS_GENERAL_URL;
                break;
            }
            case CATEGORY_TET: {
                SOURCE_URL = NEWS_TET_URL;
                break;
            }
            case CATEGORY_YUD: {
                SOURCE_URL = NEWS_YUD_URL;
                break;
            }
            case CATEGORY_YA: {
                SOURCE_URL = NEWS_YA_URL;
                break;
            }
            case CATEGORY_YB: {
                SOURCE_URL = NEWS_YB_URL;
                break;
            }
        }

        try {
            /*
            get the html
             */
            URL url = new URL(SOURCE_URL);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setConnectTimeout(5000);
            urlConnection.setDoOutput(true);

            reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            html = builder.toString();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (html == null) return BlichSyncAdapter.FETCH_STATUS_UNSUCCESSFUL;

        Document document = Jsoup.parse(html);
        if (document == null) return BlichSyncAdapter.FETCH_STATUS_UNSUCCESSFUL;
        Elements news = document.getElementsByTag(TAG_ARTICLE);

        List<ContentValues> contentValuesList = new ArrayList<>();

        if (news == null) return BlichSyncAdapter.FETCH_STATUS_UNSUCCESSFUL;
        for (Element article : news) {
            String title = article.getElementsByTag(TAG_TITLE).get(0).text();
            String body = article.getElementsByTag(TAG_BODY).get(0).text();
            String author = article.getElementsByTag(TAG_AUTHOR).get(0).text();
            String postDate = article.getElementsByTag(TAG_POST_DATE).get(0).text();

            ContentValues values = new ContentValues();
            values.put(NewsEntry.COL_CATEGORY, category);
            values.put(NewsEntry.COL_TITLE, title);
            values.put(NewsEntry.COL_BODY, body);
            values.put(NewsEntry.COL_AUTHOR, author);
            values.put(NewsEntry.COL_DATE, postDate);

            contentValuesList.add(values);
        }

        getBaseContext().getContentResolver().bulkInsert(
                NewsEntry.CONTENT_URI,
                contentValuesList.toArray(new ContentValues[contentValuesList.size()]));

        return BlichSyncAdapter.FETCH_STATUS_SUCCESSFUL;
    }
}
