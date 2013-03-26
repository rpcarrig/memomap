package com.rpcarrig.memomapa;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ContentFragment extends Fragment {
	private final static String CLASS = "ContentFragment";
	
	public ContentFragment() { }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
			Bundle savedInstanceState) {
		Log.d(CLASS, "onCreateView");
		return inflater.inflate(R.layout.canvas, container, false);
	}	
	
	@Override
	public void onStart(){
		Log.d(CLASS, "onStart");
		super.onStart();
	}
}