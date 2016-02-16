package com.doge.dyjw.jiaowu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.doge.dyjw.ContainerActivity;
import com.doge.dyjw.MainApplication;
import com.doge.dyjw.R;
import com.doge.dyjw.view.VerifyDialog;
import com.doge.dyjw.view.VerifyDialog.LoginCallback;

public class JiaowuPanelFragment extends Fragment {
    private Button button_cjcx;
    private Button button_grkb;
    private Button button_cxbm;
    private Button button_bkbm;
    private Button button_jxjh;
    private Button button_jxpj;
    private LayoutInflater inflater;
    private View rootView;
    private TextView welcome;
    private Jiaowu jw;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        rootView = inflater.inflate(R.layout.fragment_jiaowu_panel, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findView();
        jw = ((MainApplication)getActivity().getApplicationContext()).getJiaowu();
        welcome.setText(jw.getName() + getString(R.string.welcome));
        OnClickListener l = new ButtonListener();
        button_cjcx.setOnClickListener(l);
        button_grkb.setOnClickListener(l);
        button_jxjh.setOnClickListener(l);
        button_cxbm.setOnClickListener(l);
        button_bkbm.setOnClickListener(l);
        button_jxpj.setOnClickListener(l);
//        button_jxpj.setVisibility(View.GONE);
        setUpBroadcastReceiver();
    }

    private void findView() {
        welcome = (TextView) rootView.findViewById(R.id.welcome);
        button_cjcx = (Button) rootView.findViewById(R.id.button_cjcx);
        button_grkb = (Button) rootView.findViewById(R.id.button_grkb);
        button_cxbm = (Button) rootView.findViewById(R.id.button_cxbm);
        button_bkbm = (Button) rootView.findViewById(R.id.button_bkbm);
        button_jxjh = (Button) rootView.findViewById(R.id.button_jxjh);
        button_jxpj = (Button) rootView.findViewById(R.id.button_jxpj);
    }

    private void setUpBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(getString(R.string.broadcast_logout));
        getActivity().registerReceiver(receiver, filter);
    }

    public BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(getString(R.string.broadcast_logout)) &&
                    intent.getStringExtra("msg").equals("logout")) {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                Fragment loginPanelFrag = new JiaowuSystemFragment();
                transaction.remove(JiaowuPanelFragment.this);
                transaction.add(R.id.container, loginPanelFrag).commit();
            }
        }
    };

    class ButtonListener implements OnClickListener {
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setClass(getActivity(), ContainerActivity.class);
            switch (v.getId()) {
                case R.id.button_cjcx:
                    intent.putExtra("titleId", R.string.cjcx);
                    break;
                case R.id.button_grkb:
                    intent.putExtra("titleId", R.string.grkb);
                    break;
                case R.id.button_cxbm:
                    intent.putExtra("titleId", R.string.cxbm);
                    break;
                case R.id.button_bkbm:
                    intent.putExtra("titleId", R.string.bkbm);
                    break;
                case R.id.button_jxjh:
                    intent.putExtra("titleId", R.string.jxjh);
                    break;
                case R.id.button_jxpj:
                    intent.putExtra("titleId", R.string.jxpj);
                    break;
            }
            SharedPreferences sp = getActivity().getSharedPreferences("account", 0);
            if (System.currentTimeMillis() - sp.getLong("jw_lasttime", 0) > 1200000) {
                System.out.println("请输入验证码");
                new VerifyDialog(getActivity(), inflater, new OnVerifyListener(intent)).show();
                return;
            }
            sp.edit().putLong("jw_lasttime", System.currentTimeMillis()).commit();
            startActivity(intent);
        }
    }

    public class OnVerifyListener implements LoginCallback {
        private Intent intent;

        public OnVerifyListener(Intent intent) {
            this.intent = intent;
        }

        public void success() {
            startActivity(intent);
        }

        public void fail() {
            new VerifyDialog(getActivity(), inflater, this).show();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        getActivity().unregisterReceiver(receiver);
    }
}
