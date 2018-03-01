/*
 * Copyright (C) Ido Fang Bentov - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ido Fang Bentov <dodobentov@gmail.com>, 2017
 */

package com.blackcracks.blich.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;

import com.afollestad.appthemeengine.Config;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blackcracks.blich.R;
import com.blackcracks.blich.util.Utilities;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Aidan Follestad (afollestad)
 */
public class ChangelogDialog extends DialogFragment {

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View customView;

        try {
            customView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_changelog, null);
        } catch (InflateException e) {
            e.printStackTrace();
            return new MaterialDialog.Builder(getContext())
                    .title(android.R.string.dialog_alert_title)
                    .content("This device doesn't support web view, which is necessary to view the change log. It is missing a system component.")
                    .positiveText(android.R.string.ok)
                    .build();
        }
        MaterialDialog dialog = new MaterialDialog.Builder(getContext())
                .title(R.string.dialog_changelog_title)
                .customView(customView, true)
                .positiveText(android.R.string.ok)
                .build();

        final WebView webView = customView.findViewById(R.id.web_view);
        try {
            // Load from phonograph-changelog.html in the assets folder
            StringBuilder buf = new StringBuilder();
            InputStream html = getActivity().getAssets().open("change_log.html");
            BufferedReader in = new BufferedReader(new InputStreamReader(html, "UTF-8"));
            String str;
            while ((str = in.readLine()) != null)
                buf.append(str);
            in.close();

            // Inject color values for WebView body background and links
            String ateKey = Utilities.getATEKey(getContext());
            String darkDialogHex = colorToHex(ContextCompat.getColor(getContext(), R.color.grey_800));
            String lightDialogHex = colorToHex(Color.WHITE);

            final String backgroundColor = ateKey.equals("dark_theme") ? darkDialogHex : lightDialogHex;
            final String textColor = colorToHex(Config.textColorPrimary(getContext(), ateKey));
            webView.loadData(buf.toString()
                    .replace("{#background-color}", backgroundColor)
                    .replace("{#text-color}", textColor)
                    , "text/html", "UTF-8");
        } catch (Throwable e) {
            webView.loadData("<h1>Unable to load</h1><p>" + e.getLocalizedMessage() + "</p>", "text/html", "UTF-8");
        }

        dialog.getTitleView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        return dialog;
    }

    private static String colorToHex(int color) {
        return Integer.toHexString(color).substring(2);
    }
}
