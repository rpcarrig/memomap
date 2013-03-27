package com.rpcarrig.memomapa;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class NewMemoBodyFragment extends Fragment {
	private final String CLASS = "NewMemoBodyFragment";
	
	private TextView memoBody;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
			Bundle savedInstanceState) {
		Log.d(CLASS, "onCreateView");
		return inflater.inflate(R.layout.fragment_newmemobody, container, false);
	}
	
	@Override
	public void onStart(){
		memoBody = (TextView) getView().findViewById(R.id.newmemo_body);
		super.onStart();
	}
	
	public String getBody(){ return memoBody.getText().toString(); }
}
