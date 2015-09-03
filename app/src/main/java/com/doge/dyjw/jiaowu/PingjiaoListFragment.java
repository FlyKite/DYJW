package com.doge.dyjw.jiaowu;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.doge.dyjw.ContainerActivity;
import com.doge.dyjw.MainApplication;
import com.doge.dyjw.R;

import java.util.List;
import java.util.Map;

public class PingjiaoListFragment extends Fragment {
	
	private ListView pjList;
	
	private ProgressDialog progressDialog;
	
	String xnxq;
	String pjpc;
	String pjfl;
	String pjkc;
	
	public PingjiaoListFragment() {
        Bundle bundle = getArguments();
		xnxq = bundle.getString("xnxq");
		pjpc = bundle.getString("pjpc");
		pjfl = bundle.getString("pjfl");
		pjkc = bundle.getString("pjkc");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_blank, container, false);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		LinearLayout main = (LinearLayout)getActivity().findViewById(R.id.frag_container);
		pjList = new ListView(getActivity());
		pjList.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		main.addView(pjList);
		progressDialog = new ProgressDialog(getActivity(), ProgressDialog.THEME_HOLO_LIGHT);
		progressDialog.setMessage(getString(R.string.loading));
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setOnCancelListener(new BackListener());
		getList();
	}
	
	GetListTask task;
	private void getList() {
		task = new GetListTask();
		task.execute();
	}
	
	class BackListener implements OnCancelListener {
		@Override
		public void onCancel(DialogInterface arg0) {
			task.cancel(true);
			getActivity().finish();
		}
	}

	List<Map<String, Object>> list;
	class GetListTask extends AsyncTask<Void, Integer, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog.show();
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			Jiaowu jw = ((MainApplication)getActivity().getApplicationContext()).getJiaowu();
			list = jw.getPingjiaoList(xnxq, pjpc, pjfl, pjkc);
			if(list != null) {
				return true;
			}
			return false;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			progressDialog.hide();
			if(result) {
				setList();
			} else {
				Toast.makeText(getActivity(), getString(R.string.load_failed), Toast.LENGTH_LONG).show();
			}
		}
		
	}
	
	private void setList() {
		SimpleAdapter adapter = new SimpleAdapter(getActivity(), list, R.layout.list_item_tri_link,
				new String[]{"title", "url", "top", "bottom"},
				new int[]{R.id.title, R.id.url, R.id.top, R.id.bottom});
		pjList.setAdapter(adapter);
		pjList.setOnItemClickListener(new PingjiaoListener());
	}
	
	class PingjiaoListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> arg0, View v, int arg2,
				long arg3) {
			TextView url = (TextView)v.findViewById(R.id.url);
//			FragmentTransaction transaction = getFragmentManager().beginTransaction();
//			transaction.addToBackStack(null);
//			Fragment pjInfoFrag = new PingjiaoInfoFragment(url.getText().toString());
//			transaction.replace(R.id.container, pjInfoFrag).commit();
            Intent intent = new Intent();
            intent.setClass(getActivity(), ContainerActivity.class);
            intent.putExtra("titleId", R.string.jxpj);
            intent.putExtra("which", "PingjiaoInfo");
            intent.putExtra("url", url.getText().toString());
            startActivity(intent);
		}
	}
	
	@Override
	public void onDestroy() {
		progressDialog.dismiss();
//		getFragmentManager().beginTransaction().remove(this).commit();
		super.onDestroy();
	}
}
