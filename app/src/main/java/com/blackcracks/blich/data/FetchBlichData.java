package com.blackcracks.blich.data;

import android.os.AsyncTask;
import android.support.annotation.CallSuper;

import java.util.ArrayList;
import java.util.List;

public abstract class FetchBlichData<Params, Progress> extends AsyncTask<Params, Progress, Boolean> {

    private List<OnFetchFinishListener> mFetchFinishListeners = new ArrayList<>();

    @CallSuper
    @Override
    protected void onPostExecute(Boolean result) {
        for (OnFetchFinishListener listener : mFetchFinishListeners) {
            listener.onFetchFinished(result);
        }
    }

    public FetchBlichData<Params, Progress> addOnFetchFinishListener(OnFetchFinishListener listener) {
        mFetchFinishListeners.add(listener);
        return this;
    }

    public interface OnFetchFinishListener {
        void onFetchFinished(boolean isSuccessful);
    }
}
