package com.blackcracks.blich.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class BlichAuthenticatorService extends Service {

    private BlichAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        mAuthenticator = new BlichAuthenticator(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
