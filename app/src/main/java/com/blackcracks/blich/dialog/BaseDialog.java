package com.blackcracks.blich.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

public abstract class BaseDialog<B extends BaseDialog.Builder> extends DialogFragment {

    protected B mBuilder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBuilder = onCreateBuilder();

        Bundle args = getArguments();
        if (args == null)
            throw new IllegalArgumentException("Dialog must be created using Builder");
        mBuilder.setArgs(args);
    }

    protected abstract B  onCreateBuilder();

    public abstract static class Builder {

        public Builder() {}

        protected abstract void setArgs(Bundle args);

        public abstract BaseDialog build();
    }
}
