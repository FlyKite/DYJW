package com.doge.dyjw.news;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.doge.dyjw.R;
import com.doge.dyjw.view.NetImageView;
import com.doge.dyjw.view.TableView;
import com.doge.dyjw.view.UrlTextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class NewsFragment extends Fragment {
    private LinearLayout container;
    private TextView info;
    private News news;
    private ProgressDialog progressDialog;
    private View rootView;
    NetTask task;
    private TextView title;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_news, container, false);
        return rootView;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findView();
        if(savedInstanceState != null) return;
        progressDialog = new ProgressDialog(getActivity(), AlertDialog.THEME_HOLO_LIGHT);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setOnCancelListener(new BackListener());
        bindService();
    }

    private void findView() {
        title = (TextView) rootView.findViewById(R.id.title);
        container = (LinearLayout) rootView.findViewById(R.id.news_container);
        info = (TextView) rootView.findViewById(R.id.info);
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
                Log.v("bindService", "bind3");
                getNews();
            }
        }

        public void onServiceDisconnected(ComponentName arg0) {
            downloadService = null;
        }
    };

    private void getNews() {
        task = new NetTask();
        task.execute();
    }

    class BackListener implements OnCancelListener {
        @Override
        public void onCancel(DialogInterface arg0) {
            task.cancel(true);
            getActivity().finish();
        }
    }

    class NetTask extends AsyncTask<Void, Integer, Boolean> {
        private String url;

        protected void onPreExecute() {
            super.onPreExecute();
            url = getActivity().getIntent().getStringExtra("url");
            progressDialog.show();
        }

        protected Boolean doInBackground(Void... arg0) {
            news = new News();
            try {
                news.getNews(url);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return true;
            }
        }

        protected void onPostExecute(Boolean result) {
            progressDialog.hide();
            if (result) {
                setNews();
            } else {
                Toast.makeText(getActivity(), getString(R.string.load_failed), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setNews() {
        title.setText(news.getTitle());
        info.setText(news.getInfo());
        Document doc = Jsoup.parse(news.getContent());
        Elements els = doc.select("body>div, body>p, body>table");
        for(Element e : els) {
            if(e.toString().contains("<tbody>")) {
                for(Element table : e.select("table")) {
                    container.addView(new TableView(getActivity(), table));
                }
            } else if(!e.toString().contains("<img")) {
                UrlTextView t = new UrlTextView(getActivity());
                t.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
                t.setText(Html.fromHtml(e.toString()), download);
                t.setTextColor(getResources().getColor(R.color.text_black));
                t.setMovementMethod(LinkMovementMethod.getInstance());
                container.addView(t);
            } else {
                NetImageView img = new NetImageView(getActivity(), e.select("img").get(0).attr("src"));
                container.addView(img);
            }
        }
    }

    UrlTextView.OnDownloadListener download = new UrlTextView.OnDownloadListener() {
        @Override
        public boolean download(String url, String filename) {
            return downloadService.download(url, filename);
        }
    };

    public void onDestroy() {
        progressDialog.dismiss();
        getActivity().unbindService(conn);
        super.onDestroy();
    }
}
