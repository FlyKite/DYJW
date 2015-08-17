package com.doge.dyjw.news;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.doge.dyjw.HolderFragment;
import com.doge.dyjw.MainActivity;
import com.doge.dyjw.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 政 on 2015/7/21.
 */
public class DownloadFragment extends HolderFragment {
    private View rootView;
    private RecyclerView downloadList;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(savedInstanceState != null) return rootView;
        rootView = inflater.inflate(R.layout.fragment_download, container, false);
        return rootView;
    }

    List<Map<String, Object>> list = new ArrayList<>();
    DownloadAdapter adapter;
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findView();
        // improve performance if you know that changes in content
        // do not change the size of the RecyclerView
        downloadList.setHasFixedSize(true);
        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        downloadList.setLayoutManager(layoutManager);
        // specify an adapter (see also next example)
        bindService();
        setUpBroadcastReceiver();
    }

    private void findView() {
        downloadList = (RecyclerView) rootView.findViewById(R.id.download_list);
    }

    private DownloadService downloadService;
    private void bindService() {
        Intent intent = new Intent(getActivity(), DownloadService.class);
        getActivity().startService(intent);
        Log.v("bindService", "bind1");
        getActivity().bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }
    private ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            Log.v("bindService", "bind2");
            if (downloadService == null) {
                downloadService = ((DownloadService.DownloadBinder) binder).getService();
                getList();
                setAdapter();
                Log.v("bindService", "bind3");
            }
        }

        public void onServiceDisconnected(ComponentName arg0) {
            downloadService = null;
        }
    };

    public void getList() {
        File downloadFolder = new File(MainActivity.DOWNLOAD_DIR);
        if(downloadFolder.exists()) {
            File[] files = downloadFolder.listFiles();
            for(File file : files) {
                if(file.isFile()) {
                    String filename = file.getName();
                    HashMap<String, Object> map = new HashMap<>();
                    if(filename.endsWith(".tfp")) {
                        filename = filename.substring(0, filename.length() - 4);
                        if(downloadService.isDownloading(filename)) {
                            map.put("status", "started");
                        } else {
                            map.put("status", "stoped");
                        }
                    } else if(!file.getName().endsWith(".tmp")) {
                        map.put("status", "finished");
                    } else {
                        continue;
                    }
                    map.put("filename", filename);
                    map.put("time", file.lastModified());
                    list.add(map);
                }
            }
        }
        Collections.sort(list, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> lhs, Map<String, Object> rhs) {
                if (Long.parseLong(lhs.get("time") + "") > Long.parseLong(rhs.get("time") + "")) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });
    }

    private void setAdapter() {
        adapter = new DownloadAdapter(list, downloadService);
        downloadList.setAdapter(adapter);
    }

    private void setUpBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(getString(R.string.broadcast_download));
        getActivity().registerReceiver(receiver, filter);
    }

    public BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(getString(R.string.broadcast_download))) {
                String filename = intent.getStringExtra("filename");
                String msg = intent.getStringExtra("msg");
                switch(msg) {
                    case "finish":
                        for(Map<String, Object> map : list) {
                            if(map.get("filename").equals(filename)) {
                                map.put("status", "finished");
                                adapter.notifyDataSetChanged();
                            }
                        }
                        break;
                    case "error":
                    case "updateProgress":
                        String speed = intent.getStringExtra("speed");
                        for(Map<String, Object> map : list) {
                            if(map.get("filename").equals(filename)) {
                                map.put("status", "started");
                                map.put("speed", speed);
                                if(msg.equals("error")) {
                                    map.put("status", "error");
                                    Log.d("download", "error:" + speed);
                                }
                                adapter.notifyDataSetChanged();
                            }
                        }
                        break;
                    case "stop":
                        for(Map<String, Object> map : list) {
                            if(map.get("filename").equals(filename)) {
                                map.put("status", "stoped");
                                adapter.notifyDataSetChanged();
                            }
                        }
                        break;
                }
            }
        }
    };

    public void onDestroy() {
        getActivity().unbindService(conn);
        getActivity().unregisterReceiver(receiver);
        super.onDestroy();
    }
}
