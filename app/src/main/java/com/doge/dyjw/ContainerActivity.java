package com.doge.dyjw;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.doge.dyjw.jiaowu.BukaoFragmeng;
import com.doge.dyjw.jiaowu.ChengjiFragment;
import com.doge.dyjw.jiaowu.ChongxiuFragment;
import com.doge.dyjw.jiaowu.JihuaFragment;
import com.doge.dyjw.jiaowu.KebiaoFragment;
import com.doge.dyjw.jiaowu.PingjiaoFragment;
import com.doge.dyjw.jiaowu.PingjiaoInfoFragment;
import com.doge.dyjw.jiaowu.PingjiaoListFragment;
import com.doge.dyjw.news.DownloadFragment;
import com.doge.dyjw.news.NewsFragment;
import com.doge.dyjw.trade.PubItemFragment;

public class ContainerActivity extends ActionBarActivity {

    private int titleId;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            titleId = intent.getIntExtra("titleId", 0);
            initToolbar();
            initFragment(titleId, intent);
        }
        Uri uri = getIntent().getData();
        System.out.println(uri);
    }

    private void initToolbar() {
        CharSequence title = getString(titleId);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(title);
        mToolbar.setTitleTextColor(getResources().getColor(R.color.text_white));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new ClickListener());
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion < Build.VERSION_CODES.KITKAT){
            findViewById(R.id.status_bar).setVisibility(View.GONE);
        }
    }

    class ClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            finish();
        }
    }

    private void initFragment(int titleId, Intent intent) {
        Fragment fragment = null;
        Bundle bundle = new Bundle();
        switch (titleId) {
            case R.string.menu_about:
                fragment = new AboutFragment();
                break;
            case R.string.cjcx:
                fragment = new ChengjiFragment();
                break;
            case R.string.grkb:
                fragment = new KebiaoFragment();
                break;
            case R.string.cxbm:
                fragment = new ChongxiuFragment();
                break;
            case R.string.bkbm:
                fragment = new BukaoFragmeng();
                break;
            case R.string.jxjh:
                fragment = new JihuaFragment();
                break;
            case R.string.jxpj:
                String which = intent.getStringExtra("which");
                which = which == null ? "" : which;
                switch (which) {
                    case "PingjiaoList":
                        bundle.putString("xnxq", intent.getStringExtra("xnxq"));
                        bundle.putString("xnxq", intent.getStringExtra("pjpc"));
                        bundle.putString("xnxq", intent.getStringExtra("pjfl"));
                        bundle.putString("xnxq", intent.getStringExtra("pjkc"));
                        fragment = new PingjiaoListFragment();
                        fragment.setArguments(bundle);
                        break;
                    case "PingjiaoInfo":
                        bundle.putString("url", intent.getStringExtra("url"));
                        fragment = new PingjiaoInfoFragment();
                        fragment.setArguments(bundle);
                        break;
                    default:
                        fragment = new PingjiaoFragment();
                }
                break;
            case R.string.news :
            case R.string.notice:
                fragment = new NewsFragment();
                break;
            case R.string.download_manager:
                fragment = new DownloadFragment();
                break;
            case R.string.report_suggest:
                fragment = new SuggestFragment();
                break;
            case R.string.pub_item:
                fragment = new PubItemFragment();
                break;
        }
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).commit();
        }
    }

    MenuItem downloadItem;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(titleId == R.string.news || titleId == R.string.notice) {
            downloadItem = menu.add(1, 5, 0, "Download");
            downloadItem.setIcon(R.drawable.download);
            MenuItemCompat.setShowAsAction(downloadItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Intent intent = new Intent();
        intent.setClass(this, ContainerActivity.class);
        switch (id) {
            case 5:
                intent.putExtra("titleId", R.string.download_manager);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
