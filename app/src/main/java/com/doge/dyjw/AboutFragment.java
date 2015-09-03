package com.doge.dyjw;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AboutFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_about, container, false);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		TextView content = (TextView)getActivity().findViewById(R.id.content);
		content.setText(Html.fromHtml(getString(R.string.about_content) + "<br/><br/>" + getString(R.string.app_name) + getString(R.string.versionName)));
		content.setMovementMethod(LinkMovementMethod.getInstance());
	}
	
}
