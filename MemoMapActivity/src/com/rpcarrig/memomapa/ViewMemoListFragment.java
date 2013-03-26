package com.rpcarrig.memomapa;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ViewMemoListFragment extends Fragment {
	private final static String CLASS = "ViewMemoListFragment";
	public ViewMemoListFragment() { }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
			Bundle savedInstanceState) {
		Log.d(CLASS, "onCreateView");
		
		View view = inflater.inflate(R.layout.fragment_viewmemolist, container, false);
		return view;
	}	
	
	@Override
	public void onStart(){
		Log.d(CLASS, "onStart");
		super.onStart();
	}
}