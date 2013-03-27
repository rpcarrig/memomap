package com.rpcarrig.memomapa;

import java.util.ArrayList;

import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ViewMemoListFragment extends ListFragment {
	private final static String CLASS = "ViewMemoListFragment";
	
	MemoAdapter memoAdapter;
	
	public ViewMemoListFragment() { }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
			Bundle savedInstanceState) {
		Log.d(CLASS, "onCreateView");
		ArrayList<Memo> memoArray = DbHandler.getInstance(MemoMap.getInstance()).getAllMemos();		
		memoAdapter = new MemoAdapter(MemoMap.getInstance(), 0, memoArray, null);
		setListAdapter(memoAdapter);
		
		View view = inflater.inflate(R.layout.fragment_viewmemolist, container, false);
		return view;
	}
	
	@Override
	public void onStart(){
		Log.d(CLASS, "onStart");
		super.onStart();
	}
	
	public void update(){
		memoAdapter.notifyDataSetChanged();
	}
}