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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.doge.dyjw.ContainerActivity;
import com.doge.dyjw.MainApplication;
import com.doge.dyjw.R;

import java.util.ArrayList;

public class PingjiaoFragment extends Fragment {

	private Spinner xnxq;
	private Spinner pjpc;
	private Spinner pjfl;
	private Spinner pjkc;
	
	private Button submit;
	
	ProgressDialog progressDialog;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_pingjiao, container, false);
		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		findView();
		progressDialog = new ProgressDialog(getActivity(), ProgressDialog.THEME_HOLO_LIGHT);
		progressDialog.setMessage(getString(R.string.loading));
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setOnCancelListener(new BackListener());
		submit.setOnClickListener(new SubmitListener());
		getInfo();
	}
	
	class SubmitListener implements OnClickListener {
		@Override
		public void onClick(View v) {
//			FragmentTransaction transaction = getFragmentManager().beginTransaction();
			String xnxqStr = ((Option)xnxq.getSelectedItem()).getValue();
			String pjpcStr = ((Option)pjpc.getSelectedItem()).getValue();
			String pjflStr = ((Option)pjfl.getSelectedItem()).getValue();
			String pjkcStr = ((Option)pjkc.getSelectedItem()).getValue();
//			transaction.addToBackStack(null);
//			Fragment pjlistFrag = new PingjiaoListFragment(xnxqStr, pjpcStr, pjflStr, pjkcStr);
//			transaction.replace(R.id.container, pjlistFrag).commit();
            Intent intent = new Intent();
            intent.setClass(getActivity(), ContainerActivity.class);
            intent.putExtra("titleId", R.string.jxpj);
            intent.putExtra("which", "PingjiaoList");
            intent.putExtra("xnxq", xnxqStr);
            intent.putExtra("pjpc", pjpcStr);
            intent.putExtra("pjfl", pjflStr);
            intent.putExtra("pjkc", pjkcStr);
            startActivity(intent);
		}
	}
	
	private void findView() {
		xnxq = (Spinner)getActivity().findViewById(R.id.xnxq);
		pjpc = (Spinner)getActivity().findViewById(R.id.pjpc);
		pjfl = (Spinner)getActivity().findViewById(R.id.pjfl);
		pjkc = (Spinner)getActivity().findViewById(R.id.pjkc);
		submit = (Button)getActivity().findViewById(R.id.submit);
	}
	
	GetInfoTask task;
	private void getInfo() {
		task = new GetInfoTask();
		task.execute();
	}
	
	class BackListener implements OnCancelListener {
		@Override
		public void onCancel(DialogInterface arg0) {
			task.cancel(true);
			getActivity().finish();
		}
	}

	public static class Option {
		private String value;
		private String html;
		private boolean selected = false;
		public Option(String html, String value, boolean selected) {
			this.html = html;
			this.value = value;
			this.selected = selected;
		}
		public String toString() {
			return html;
		}
		public String getValue() {
			return value;
		}
		public boolean isSelected() {
			return selected;
		}
	}

	ArrayList<Option> xnxqList;
	ArrayList<Option> pjpcList;
	ArrayList<Option> pjflList;
	ArrayList<Option> pjkcList;
	class GetInfoTask extends AsyncTask<Void, Integer, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog.show();
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			Jiaowu jw = ((MainApplication)getActivity().getApplicationContext()).getJiaowu();
			ArrayList<ArrayList<Option>> als = jw.getPingjiao();
			if(als != null && als.size() == 4) {
				xnxqList = als.get(0);
				pjpcList = als.get(1);
				pjflList = als.get(2);
				pjkcList = als.get(3);
				return true;
			}
			return false;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			progressDialog.hide();
			if(result) {
				setSpinner();
			} else {
				Toast.makeText(getActivity(), getString(R.string.load_failed), Toast.LENGTH_LONG).show();
			}
		}
		
	}
	
	private void setSpinner() {
		ArrayAdapter<String> xnxqAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, (ArrayList)xnxqList);
		ArrayAdapter<String> pjpcAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, (ArrayList)pjpcList);
		ArrayAdapter<String> pjflAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, (ArrayList)pjflList);
		ArrayAdapter<String> pjkcAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, (ArrayList)pjkcList);
		xnxqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		pjpcAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		pjflAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		pjkcAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    xnxq.setAdapter(xnxqAdapter);
	    pjpc.setAdapter(pjpcAdapter);
	    pjfl.setAdapter(pjflAdapter);
	    pjkc.setAdapter(pjkcAdapter);
	    System.out.println(xnxqList.size() + "-" + pjpcList.size() + "-" + pjflList.size() + "-" + pjkcList.size());
	    for(int i = 0; i < xnxqList.size(); i++) {
	    	Option o = xnxqList.get(i);
	    	if(o.isSelected()) {
	    		xnxq.setSelection(i);
	    	}
	    }
	}

	@Override
	public void onPause() {
		super.onPause();
		progressDialog.dismiss();
	}

}
