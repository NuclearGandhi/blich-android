package com.blackcracks.blich.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blackcracks.blich.R;

public class NewsFragment extends BlichBaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_news;
    }

    @Override
    protected int getFragmentTitle() {
        return R.string.drawer_news_title;
    }

    @Override
    protected int getMenuResource() {
        return R.menu.fragment_news;
    }

}
