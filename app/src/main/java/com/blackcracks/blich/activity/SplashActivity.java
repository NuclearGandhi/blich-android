package com.blackcracks.blich.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.util.ATEUtil;
import com.blackcracks.blich.R;
import com.blackcracks.blich.util.Utilities;

public class SplashActivity extends BaseStatusBarActivity {

    private static final int SPLASH_SCREEN_DURATION = 250;

    private String mATEKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        View rootView = findViewById(R.id.splash_root);

        setAutoStatusBarColor(true);
        setupTheme();

        mATEKey = Utilities.getATEKey(this);

        int primaryColor = Config.primaryColor(this, mATEKey);
        rootView.setBackgroundColor(primaryColor);

        View logoView = findViewById(R.id.logo);
        int logoDrawableId = ATEUtil.isColorLight(primaryColor) ?
                R.drawable.logo_grey :
                R.drawable.logo_white;
        logoView.setBackgroundResource(logoDrawableId);

        ProgressBar loadingView = findViewById(R.id.loading);
        Drawable progressIntermediate = loadingView.getIndeterminateDrawable().mutate();
        progressIntermediate.setTint(Config.accentColor(this, mATEKey));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        },  SPLASH_SCREEN_DURATION);
    }

    private void setupTheme() {
        // Default config
        if (!ATE.config(this, "light_theme").isConfigured(4)) {
            ATE.config(this, "light_theme")
                    .activityTheme(R.style.AppTheme)
                    .primaryColorRes(R.color.defaultLightPrimaryColor)
                    .accentColorRes(R.color.defaultLightAccentColor)
                    .statusBarColor(Color.TRANSPARENT)
                    .lightStatusBarMode(Config.LIGHT_STATUS_BAR_AUTO)
                    .navigationViewSelectedIconRes(R.color.defaultLightAccentColor)
                    .navigationViewSelectedTextRes(R.color.defaultLightAccentColor)
                    .commit();
        }

        if (!ATE.config(this, "dark_theme").isConfigured(4)) {
            ATE.config(this, "dark_theme")
                    .activityTheme(R.style.AppTheme_Dark)
                    .primaryColorRes(R.color.defaultDarkPrimaryColor)
                    .accentColorRes(R.color.defaultDarkAccentColor)
                    .statusBarColor(Color.TRANSPARENT)
                    .lightStatusBarMode(Config.LIGHT_STATUS_BAR_AUTO)
                    .navigationViewSelectedIconRes(R.color.defaultDarkAccentColor)
                    .navigationViewSelectedTextRes(R.color.defaultDarkAccentColor)
                    .commit();
        }
    }
}
