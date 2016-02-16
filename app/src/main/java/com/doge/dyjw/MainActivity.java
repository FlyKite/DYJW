package com.doge.dyjw;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.doge.dyjw.jiaowu.Jiaowu;
import com.doge.dyjw.news.DownloadService;
import com.doge.dyjw.util.Log;
import com.doge.dyjw.util.Update;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends ActionBarActivity {

    private Update up;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initDirectory();
        setUpToolbar();
        setUpDrawerList();
        setUpUser();
        setUpBroadcastReceiver();
        if (savedInstanceState == null) {
            changeFragment(0);
        }
        up = new Update(this);
        up.startUpdate();
    }

    public static final String SDCARD = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String DIR = SDCARD + "/DYJW/";
    public static final String IMAGE_DIR = DIR + "images";
    public static final String DOWNLOAD_DIR = DIR + "download";
    public static final String APK_DIR = DIR + "apk";
    public static final String LOG_DIR = DIR + "log";
    private void initDirectory() {
        File dir = new File(DIR);
        File imgdir = new File(IMAGE_DIR);
        File dldir = new File(DOWNLOAD_DIR);
        File apkdir = new File(APK_DIR);
        File logdir = new File(LOG_DIR);
        if (!dir.exists()) dir.mkdir();
        if (!imgdir.exists()) imgdir.mkdir();
        if (!dldir.exists()) dldir.mkdir();
        if (!apkdir.exists()) apkdir.mkdir();
        if (!logdir.exists()) logdir.mkdir();
    }

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;

    private void setUpToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.text_white));
        setSupportActionBar(toolbar);
        drawerLayout = ((DrawerLayout) findViewById(R.id.drawer_layout));
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        toggle = new ActionBarDrawerToggle(this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        toggle.setDrawerIndicatorEnabled(true);
        drawerLayout.setDrawerListener(toggle);
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion < Build.VERSION_CODES.KITKAT){
            findViewById(R.id.status_bar).setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        toggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        toggle.onConfigurationChanged(newConfig);
    }

    private ListView mNavList;

    private void setUpDrawerList() {
        mNavList = ((ListView) findViewById(R.id.drawer_list));
        String[] array = getResources().getStringArray(R.array.nav_list);
        ArrayList<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < array.length; i++) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("text", array[i]);
            switch (i) {
                case 0:
                    map.put("icon", R.drawable.course_blue);
                    break;
                case 1:
                    map.put("icon", R.drawable.marcket_blue);
                    break;
                case 2:
                    map.put("icon", R.drawable.jiaowu_blue);
                    break;
                case 3:
                    map.put("icon", R.drawable.news_blue);
                    break;
                case 4:
                    map.put("icon", R.drawable.notice_blue);
                    break;
            }
            list.add(map);
        }
        SimpleAdapter adapter = new SimpleAdapter(this, list, R.layout.drawer_list_item,
                new String[]{"icon", "text"},
                new int[]{R.id.icon, R.id.text});
        adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if ((data instanceof Drawable))
                    ((ImageView) view).setImageResource(Integer.parseInt(data + ""));
                return false;
            }
        });
        mNavList.setAdapter(adapter);
        mNavList.setItemChecked(0, true);
        mNavList.setOnItemClickListener(new DrawerListener());
    }

    class DrawerListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position == 1) {
                Toast.makeText(MainActivity.this, "黎明湖畔暂未开放，敬请期待", Toast.LENGTH_LONG).show();
                return;
            }
            drawerLayout.closeDrawer(findViewById(R.id.navigation_drawer));
            changeFragment(position);
            downloadItem.setVisible(position > 2);
            menu.getItem(2).setVisible(position == 2);
        }
    }

    private Button login;
    private Button logout;
    private TextView userWelcome;
    private void setUpUser() {
        userWelcome = (TextView) findViewById(R.id.user_welcome);
        login = (Button) findViewById(R.id.login);
        logout = (Button) findViewById(R.id.logout);
        String name = getSharedPreferences("account", MODE_PRIVATE).getString("jw_name", "");
        login.setOnClickListener(new LoginListener());
        logout.setOnClickListener(new LogoutListener());
        if (name.length() > 0) {
            userWelcome.setText(name);
            login.setVisibility(View.GONE);
            logout.setVisibility(View.VISIBLE);
        } else {
            userWelcome.setText(R.string.user_welcome);
            login.setVisibility(View.VISIBLE);
            logout.setVisibility(View.GONE);
        }
    }

    private void setUpBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(getString(R.string.broadcast_login));
        registerReceiver(receiver, filter);
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(getString(R.string.broadcast_login)) && intent.getStringExtra("msg").equals("login")) {
                String name = getSharedPreferences("account", MODE_PRIVATE).getString("jw_name", "");
                if (name.length() > 0) {
                    userWelcome.setText(name);
                    login.setVisibility(View.GONE);
                    logout.setVisibility(View.VISIBLE);
                } else {
                    userWelcome.setText(R.string.user_welcome);
                    login.setVisibility(View.VISIBLE);
                    logout.setVisibility(View.GONE);
                }
            }
        }
    };

    //登录，教务系统--------------------------------------------------------------------------------
    private class LoginListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            drawerLayout.closeDrawer(findViewById(R.id.navigation_drawer));
            changeFragment(2);
        }
    }

    //注销，退出登录--------------------------------------------------------------------------------
    private class LogoutListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.logout)
                    .setMessage(R.string.logout_confirm)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            getSharedPreferences("account", MODE_PRIVATE).edit().clear().commit();
                            userWelcome.setText(R.string.user_welcome);
                            login.setVisibility(View.VISIBLE);
                            logout.setVisibility(View.GONE);
                            Jiaowu jw = ((MainApplication)getApplicationContext()).getJiaowu();
                            jw.setJsessionId("");
                            jw.setAccount("", "");
                            jw.setName("");
                            Intent intent = new Intent(getString(R.string.broadcast_logout));
                            intent.putExtra("msg", "logout");
                            sendBroadcast(intent);
                        }
                    })
                    .setNegativeButton(R.string.cancel, null).show();
        }
    }

    //切换当前页面内容------------------------------------------------------------------------------
    private void changeFragment(int position) {
        mNavList.setItemChecked(position, true);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, HolderFragment.newInstance(position + 1))
                .commit();
        toolbar.setTitle(getTitle(position + 1));
    }

    //获取当前按页面标题----------------------------------------------------------------------------
    public String getTitle(int number) {
        switch (number) {
            case 1:
                return getString(R.string.title_section1);
            case 2:
                return getString(R.string.title_section2);
            case 3:
                return getString(R.string.title_section3);
            case 4:
                return getString(R.string.title_section4);
            case 5:
                return getString(R.string.title_section5);
            default:
                return getString(R.string.app_name);
        }
    }

    MenuItem downloadItem;
    Menu menu;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        menu.add(0, 2, 0, R.string.menu_about);
        menu.add(0, 3, 0, getString(R.string.check_update));
        menu.add(0, 4, 0, getString(R.string.open_debug)).setVisible(false);
        menu.add(0, 5, 0, getString(R.string.report_suggest));
        downloadItem = menu.add(1, 6, 0, "Download");
        downloadItem.setIcon(R.drawable.download);
        downloadItem.setVisible(false);
        MenuItemCompat.setShowAsAction(downloadItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
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
            case 2:
                intent.putExtra("titleId", R.string.menu_about);
                startActivity(intent);
                break;
            case 3:
                getSharedPreferences("settings", MODE_PRIVATE)
                        .edit()
                        .putLong("check_date", 0)
                        .commit();
                up.startUpdateByUser();
                break;
            case 4:
                final Log log = ((MainApplication) getApplicationContext()).getLog();
                if (log.isDebug()) {
                    if (!log.isPosting()) {
                        new AlertDialog.Builder(this)
                                .setTitle(R.string.post_debug)
                                .setMessage(R.string.post_debug_notice)
                                .setPositiveButton(R.string.yes,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                new PostLogTask(item).execute();
                                            }
                                        })
                                .setNegativeButton(R.string.no, null).show();
                        break;
                    }
                    Toast.makeText(this, R.string.postting_debug, Toast.LENGTH_LONG).show();
                    break;
                }
                new AlertDialog.Builder(this)
                        .setTitle(R.string.open_debug)
                        .setMessage(R.string.open_debug_notice)
                        .setPositiveButton(R.string.yes,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        log.setDebug(true);
                                        item.setTitle(R.string.post_debug);
                                    }
                                })
                        .setNegativeButton(R.string.no, null).show();
                break;
            case 5:
                intent.putExtra("titleId", R.string.report_suggest);
                startActivity(intent);
                break;
            case 6:
                intent.putExtra("titleId", R.string.download_manager);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    class PostLogTask extends AsyncTask<Void, Integer, String> {
        Log log = ((MainApplication) getApplicationContext()).getLog();
        MenuItem item;
        boolean postPassword = false;
        public PostLogTask(MenuItem item) {
            this.item = item;
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.post_debug)
                    .setMessage(R.string.post_password)
                    .setPositiveButton(R.string.yes,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    postPassword = true;
                                }
                            })
                    .setNegativeButton(R.string.no, null).show();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(MainActivity.this, R.string.postting_debug, Toast.LENGTH_LONG).show();
        }

        @Override
        protected String doInBackground(Void... arg0) {
            return log.postLog(postPassword);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.contains("true")) {
                Toast.makeText(MainActivity.this, result.replace("true", ""), Toast.LENGTH_LONG).show();
                item.setTitle(R.string.open_debug);
                log.clearLog();
            } else {
                Toast.makeText(MainActivity.this, result.replace("false", ""), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        Intent intent = new Intent(this, DownloadService.class);
        stopService(intent);
    }

    private long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (System.currentTimeMillis() - exitTime > 2500) {
                Toast.makeText(this, getText(R.string.back_press), Toast.LENGTH_LONG).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
            }
        }
        return false;
    }

}
