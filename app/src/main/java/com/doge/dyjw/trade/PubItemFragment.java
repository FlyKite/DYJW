package com.doge.dyjw.trade;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.doge.dyjw.HolderFragment;
import com.doge.dyjw.R;

/**
 * Created by 政 on 2015/9/16.
 */
public class PubItemFragment extends HolderFragment {
    private View rootView;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_post_item, container, false);
        return rootView;
    }
}
