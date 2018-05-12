package com.blackcracks.blich.util;

import android.net.Uri;
import android.support.annotation.StringDef;

import com.blackcracks.blich.BuildConfig;
import com.blackcracks.blich.R;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import timber.log.Timber;

import static java.lang.annotation.RetentionPolicy.SOURCE;

public class ShahafUtils {
    public static final String COMMAND_CLASSES = "classes";
    public static final String COMMAND_SCHEDULE = "schedule";
    public static final String COMMAND_EXAMS = "exams";
    public static final String COMMAND_EVENTS = "events";
    public static final String COMMAND_CHANGES = "changes";
    //BlichData
    private static final String BLICH_BASE_URI =
            "http://blich.iscool.co.il/DesktopModules/IS.TimeTable/ApiHandler.ashx";
    private static final String PARAM_SID = "sid";
    private static final String PARAM_API_KEY = "token";
    private static final String PARAM_COMMAND = "cmd";
    private static final String PARAM_CLASS_ID = "clsid";
    private static final int BLICH_ID = 540211;

    /**
     * Build a URL to Shahaf's servers.
     *
     * @param command a {@link FetchCommand}.
     * @return a {@link URL}.
     */
    public static URL buildUrlFromCommand(@FetchCommand String command) {
        int classValue = PreferenceUtils.getInstance().getInt(R.string.pref_user_class_group_key);

        Uri scheduleUri = Uri.parse(BLICH_BASE_URI).buildUpon()
                .appendQueryParameter(PARAM_SID, String.valueOf(BLICH_ID))
                .appendQueryParameter(PARAM_API_KEY, BuildConfig.ShahafBlichApiKey)
                .appendQueryParameter(PARAM_CLASS_ID, String.valueOf(classValue))
                .appendQueryParameter(PARAM_COMMAND, command)
                .build();

        return buildURLFromUri(scheduleUri);
    }

    /**
     * Build a URI without {@link #PARAM_CLASS_ID} parameter.
     *
     * @param command a {@link FetchCommand}.
     * @return a {@link Uri}.
     */
    @SuppressWarnings("SameParameterValue")
    public static Uri buildBaseUriFromCommand(@FetchCommand String command) {

        return Uri.parse(BLICH_BASE_URI).buildUpon()
                .appendQueryParameter(PARAM_SID, String.valueOf(BLICH_ID))
                .appendQueryParameter(PARAM_API_KEY, BuildConfig.ShahafBlichApiKey)
                .appendQueryParameter(PARAM_COMMAND, command)
                .build();
    }

    /**
     * Convert a URI into a URL.
     *
     * @param uri the {@link Uri} to convert.
     * @return a {@link URL}.
     */
    public static URL buildURLFromUri(Uri uri) {
        try {

            if (BuildConfig.DEBUG) {
                Timber.d("Building URI: %s", uri.toString());
            }
            return new URL(uri.toString());
        } catch (MalformedURLException e) {
            Timber.e(e);
            return null;
        }
    }

    /**
     * Connect to the given url, and return its response.
     *
     * @param url {@link URL} to connect to.
     * @return server response.
     */
    public static String getResponseFromUrl(URL url) throws IOException {
        HttpURLConnection scheduleConnection = (HttpURLConnection) url.openConnection();

        InputStream in = scheduleConnection.getInputStream();

        Scanner scanner = new Scanner(in);
        scanner.useDelimiter("\\A");

        boolean hasInput = scanner.hasNext();
        String response = null;
        if (hasInput) {
            response = scanner.next();
        }
        scanner.close();
        scheduleConnection.disconnect();

        return response;
    }

    @Retention(SOURCE)
    @StringDef({COMMAND_CHANGES, COMMAND_EVENTS, COMMAND_EXAMS, COMMAND_SCHEDULE, COMMAND_CLASSES})
    @interface FetchCommand{}
}
