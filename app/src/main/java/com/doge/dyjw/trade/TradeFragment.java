package com.doge.dyjw.trade;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.doge.dyjw.ContainerActivity;
import com.doge.dyjw.HolderFragment;
import com.doge.dyjw.R;
import com.doge.dyjw.util.Log;
import com.doge.dyjw.view.RefreshLayout;
import com.doge.dyjw.view.TradeImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Created by 政 on 2015/9/4.
 */
public class TradeFragment extends HolderFragment {

    private View rootView;
    private ListView itemListView;
    private Button pubButton;
    private RefreshLayout swipeRefreshLayout;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_trade, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findView();
        initRefreshLayout();
        getItems();
        pubButton.setOnClickListener(new PubItemListener());
    }

    class PubItemListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), ContainerActivity.class);
            intent.putExtra("titleId", R.string.pub_item);
            startActivity(intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            itemListView.setAdapter(adapter);
        } else {
            loadData();
        }
    }

    private List<Map<String, Object>> itemList = null;
    private SimpleAdapter adapter = null;
    private int page = 1;
    private void initRefreshLayout() {
        swipeRefreshLayout.setColorSchemeResources(R.color.blue_main);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {
                itemList = null;
                page = 1;
                Log.v("RefreshItem", "上拉刷新");
                new ItemListTask().execute();
            }
        });
        swipeRefreshLayout.setOnLoadListener(new RefreshLayout.OnLoadListener() {
            public void onLoad() {
                page++;
                Log.v("LoadItem", "下拉加载");
                new ItemListTask().execute();
            }
        });
    }

    public void loadData() {
        if (itemList == null) {
            new ItemListTask().execute();
            swipeRefreshLayout.setRefreshing(true);
        }
    }

    private void findView() {
        swipeRefreshLayout = (RefreshLayout) rootView.findViewById(R.id.swipe_container);
        itemListView = (ListView) rootView.findViewById(R.id.list);
        pubButton = (Button) rootView.findViewById(R.id.pub_item);
    }

    private ItemListTask task;
    private void getItems() {
        task = new ItemListTask();
        task.execute();
    }

    class ItemListTask extends AsyncTask<Void, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            Trade trade = new Trade(getActivity());
            try {
                List<Map<String, Object>> list = trade.getItems(page);
                if (itemList == null) {
                    itemList = list;
                } else if (page != 1) {
                    itemList.addAll(list);
                    return list.size() == 0 ? false : true;
                }
                return list != null;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            swipeRefreshLayout.setRefreshing(false);
            swipeRefreshLayout.setLoading(false);
            if (result) {
                setList();
            } else {
                if (itemList != null) {
                    showMessage(getString(R.string.no_more));
                } else {
                    showMessage(getString(R.string.get_failed));
                }
            }
        }
    }

    private void setList() {
        Log.v("SetList", "设置列表");
        if (adapter != null) {
            adapter.notifyDataSetChanged();
            return;
        }
        adapter = new SimpleAdapter(getActivity(), itemList, R.layout.list_item_trade_item,
                new String[]{"id", "nickname", "title", "price", "description",
                        "pubtime", "images"},
                new int[]{R.id.id, R.id.username, R.id.title, R.id.price,
                        R.id.description, R.id.lasttime, R.id.images});
        adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if (view instanceof GridView) {
                    if (((GridView)view).getAdapter() == null) {
                        StringTokenizer stk = new StringTokenizer(data.toString(), ",");
                        if (stk.countTokens() == 0) return true;
                        ArrayList<Map<String, String>> list = new ArrayList<>();
                        while (stk.hasMoreTokens()) {
                            HashMap<String, String> map = new HashMap<>();
                            map.put("imageName", stk.nextToken());
                            list.add(map);
                        }
                        SimpleAdapter imageAdapter = new SimpleAdapter(getActivity(), list, R.layout.grid_image,
                                new String[]{"imageName"}, new int[]{R.id.image});
                        imageAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
                            @Override
                            public boolean setViewValue(View view, Object data, String textRepresentation) {
                                if (view instanceof TradeImageView) {
                                    ((TradeImageView) view).setImageName((String) data);
                                    return true;
                                }
                                return false;
                            }
                        });
                        ((GridView) view).setAdapter(imageAdapter);
                    }
                    return true;
                }
                return false;
            }
        });
        itemListView.setAdapter(adapter);
//        itemListView.setOnItemClickListener(null);
    }

    private void showMessage(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }
}
