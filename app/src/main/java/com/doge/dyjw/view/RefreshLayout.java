package com.doge.dyjw.view;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.doge.dyjw.R;
import com.doge.dyjw.util.Log;

/**
 * Created by 政 on 2015/5/26.
 */
public class RefreshLayout extends SwipeRefreshLayout implements AbsListView.OnScrollListener {
    /**
     * 滑动到最下面时的上拉操作
     */
    private int mTouchSlop;
    /**
     * listview实例
     */
    private ListView mListView;

    /**
     * 上拉监听器, 到了最底部的上拉加载操作
     */
    private OnLoadListener mOnLoadListener;

    /**
     * ListView的加载中footer
     */
    private View mListViewFooter;

    /**
     * 按下时的y坐标
     */
    private int mYDown;
    /**
     * 抬起时的y坐标, 与mYDown一起用于滑动到底部时判断是上拉还是下拉
     */
    private int mLastY;
    /**
     * 是否在加载中 ( 上拉加载更多 )
     */
    private boolean isLoading = false;

    private ProgressBar footerProgress;
    private TextView footerText;
    private boolean isPullingUp = false;
    private int mPullSlop = 300;
    int top = 9999;

    public RefreshLayout(Context context) {
        this(context, null);
    }

    public RefreshLayout(Context context, AttributeSet set) {
        super(context, set);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mListViewFooter = LayoutInflater.from(context).inflate(R.layout.listview_footer, null, false);
        footerProgress = ((ProgressBar) mListViewFooter.findViewById(R.id.pull_to_refresh_load_progress));
        footerText = ((TextView) mListViewFooter.findViewById(R.id.pull_to_refresh_load_more_text));
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        // 初始化ListView对象
        if (mListView == null)
            getListView();
    }

    /**
     * 获取ListView对象
     */
    private void getListView() {
        if (getChildCount() > 0) {
            mListView = ((ListView) findViewById(R.id.list));
            // 设置滚动监听器给ListView, 使得滚动的情况下也可以自动加载
            mListView.setOnScrollListener(this);
        }
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // 按下
                mYDown = (int) event.getRawY();
                break;

            case MotionEvent.ACTION_MOVE:
                // 移动
                mLastY = (int) event.getRawY();
                if (!isRefreshing() && !isLoading && isBottom() && !isPullingUp && mYDown - mLastY >= mTouchSlop) {
                    isPullingUp = true;
                    mListView.addFooterView(mListViewFooter);
                    footerProgress.setVisibility(View.GONE);
                }
                if (!isRefreshing() && !isLoading && isBottom() && isPullUp())
                    footerText.setText(R.string.release_to_load_more);
                if (!isRefreshing() && !isLoading && isBottom() && !isPullUp()) {
                    footerText.setText(R.string.pull_up_to_load_more);
                }
                break;

            case MotionEvent.ACTION_UP:
                // 抬起
                isPullingUp = false;
                if (canLoad()) {
                    loadData();
                    Log.d("RefreshLayout", "loadData");
                } else {
                    mListView.removeFooterView(mListViewFooter);
                }
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    private boolean canLoad() {
        return !isRefreshing() && !isLoading && isBottom() && isPullUp();
    }

    private boolean isBottom() {
        if (mListView != null) {
            ListAdapter adapter = mListView.getAdapter();
            if (adapter != null) {
                int i = mListView.getLastVisiblePosition();
                int j = mListView.getAdapter().getCount() - 1;
                if (i == j) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    private boolean isPullUp() {
        return mYDown - mLastY >= mPullSlop;
    }

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if ((firstVisibleItem == 0) && (mListView.getAdapter() != null)) {
            if (top == 9999)
                top = mListView.getChildAt(0).getTop();
            if (mListView.getChildCount() > 0 && mListView.getChildAt(0).getTop() == top)
                setEnabled(true);
        } else {
            setEnabled(false);
        }
    }

    private void loadData() {
        if (mOnLoadListener != null) {
            setLoading(true);
            mOnLoadListener.onLoad();
        }
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    public void setLoading(boolean paramBoolean) {
        isLoading = paramBoolean;
        if (isLoading) {
            footerProgress.setVisibility(View.VISIBLE);
            footerText.setText(R.string.loading);
            return;
        }
        mListView.removeFooterView(mListViewFooter);
        isPullingUp = false;
        mYDown = 0;
        mLastY = 0;
    }

    public void setOnLoadListener(OnLoadListener paramOnLoadListener) {
        mOnLoadListener = paramOnLoadListener;
    }

    public static abstract interface OnLoadListener {
        public abstract void onLoad();
    }
}
