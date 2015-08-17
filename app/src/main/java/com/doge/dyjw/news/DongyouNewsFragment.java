package com.doge.dyjw.news;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.doge.dyjw.HolderFragment;
import com.doge.dyjw.R;

import java.util.Timer;
import java.util.TimerTask;

public class DongyouNewsFragment extends HolderFragment {
    private int currIndex = 0;
    private ImageView cursor;
    private ViewPager mViewPager;
    private View rootView;
    private LinearLayout tabContainer;
    private HorizontalScrollView tabScrollView;

    public static final int NEWS = 111;
    public static final int JWC = 222;

    private int which;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_news_list, container, false);
        which = getArguments().getInt("which");
        bindService();
        initNewsTabs();
        initViewPager();
        return rootView;
    }

    private DownloadService downloadService;
    private void bindService() {
        Intent intent = new Intent(getActivity(), DownloadService.class);
        getActivity().startService(intent);
        Log.d("bindService", "bind1");
        getActivity().bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }
    private ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            Log.d("bindService", "bind2");
            if (downloadService == null) {
                downloadService = ((DownloadService.DownloadBinder) binder).getService();
                Log.d("bindService", "bind3");
            }
        }

        public void onServiceDisconnected(ComponentName arg0) {
            downloadService = null;
        }
    };

    private void initNewsTabs() {
        String[] tabTitle = null;
        if(which == NEWS) {
            tabTitle = getResources().getStringArray(R.array.news_tab);
        } else {
            tabTitle = getResources().getStringArray(R.array.notice_tab);
        }
        tabContainer = (LinearLayout) rootView.findViewById(R.id.tab_container);
        cursor = (ImageView) rootView.findViewById(R.id.cursor);
        tabScrollView = (HorizontalScrollView) rootView.findViewById(R.id.tab_scrollView);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int padding = (int) TypedValue.applyDimension(1, 16.0f, displayMetrics);
        float textSize = TypedValue.applyDimension(0, 14.0f, displayMetrics);
        int count = tabTitle.length;
        ((LinearLayout) rootView.findViewById(R.id.cursor_container)).setWeightSum(count);
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                1.0f);
        int i = 0;
        while (i < count) {
            TextView tv = new TextView(getActivity());
            tv.setLayoutParams(params);
            tv.setPadding(padding, padding, padding, padding);
            tv.setTextSize(textSize);
            tv.setTextColor(getResources().getColor(i == 0 ? R.color.text_white : R.color.blue_selected));
            tv.setText(tabTitle[i]);
            tv.setOnClickListener(new NewsTabClickListener(i));
            tabContainer.addView(tv);
            i++;
        }
    }

    private class NewsTabClickListener implements OnClickListener {
        private int position;

        public NewsTabClickListener(int position) {
            this.position = position;
        }

        public void onClick(View v) {
            mViewPager.setCurrentItem(position);
        }
    }

    private void initViewPager() {
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());
        mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new PageListener());
        mSectionsPagerAdapter.notifyDataSetChanged();
        System.out.println("initViewPager");
    }

    private int scrollTime = 0;
    private Timer scrollAnimation;
    class PageListener implements OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }

        @Override
        public void onPageSelected(int position) {
            Animation animation = new TranslateAnimation((float) (currIndex * cursor.getWidth()), (float) (cursor.getWidth() * position), 0f, 0f);
            ((TextView) tabContainer.getChildAt(currIndex)).setTextColor(getResources().getColor(R.color.blue_selected));
            animation.setFillAfter(true);
            animation.setDuration(200);
            cursor.startAnimation(animation);
            if (scrollAnimation != null) {
                scrollAnimation.cancel();
                scrollTime = 0;
            }
            scrollAnimation = new Timer(true);
            scrollAnimation.schedule(new ScrollTask(currIndex, position), 0, 10);
            currIndex = position;
            ((TextView) tabContainer.getChildAt(currIndex)).setTextColor(getResources().getColor(R.color.text_white));
        }
    }

    private class ScrollTask extends TimerTask {
        private int newPosition;
        private int oldPosition;
        public ScrollTask(int oldPosition, int newPosition) {
            this.oldPosition = oldPosition;
            this.newPosition = newPosition;
        }

        public void run() {
            if (scrollTime != 200) {
                int to = (int) ((oldPosition - 0.5) * cursor.getWidth() +
                        (scrollTime * (newPosition - oldPosition) * cursor.getWidth()) / 200);
                Message message = new Message();
                message.what = to;
                scrollHandler.sendMessage(message);
                scrollTime += 10;
                return;
            }
            scrollAnimation.cancel();
            scrollTime = 0;
        }
    }

    private Handler scrollHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            tabScrollView.scrollTo(msg.what, 0);
            return false;
        }
    });

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public Fragment getItem(int position) {
            if(which == NEWS) {
                String[] url = getResources().getStringArray(R.array.news_url);
                return NewsListFragment.newInstance(position, url[position]);
            } else {
                String[] url = getResources().getStringArray(R.array.notice_url);
                return NewsListFragment.newInstance(position, url[position]);
            }
        }

        public int getCount() {
            return tabContainer.getChildCount();
        }
    }

    public void onDetach() {
        super.onDetach();
        NewsListFragment.releaseFragments();
        getActivity().unbindService(conn);
//        try {
//            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
//            childFragmentManager.setAccessible(true);
//            childFragmentManager.set(this, null);
//        } catch (NoSuchFieldException | IllegalAccessException e) {
//            e.printStackTrace();
//        }
    }
}
