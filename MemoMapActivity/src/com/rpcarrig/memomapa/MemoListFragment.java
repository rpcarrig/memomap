package com.rpcarrig.memomapa;

import java.util.ArrayList;

import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;

public class MemoListFragment extends ListFragment {
	private final static String TAG = "MemoListFragment";
	
	ArrayList<Memo> memoArray;
	LatLng location;
	
	public MemoListFragment(){ }

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Bundle bundle = this.getArguments();
		if(bundle != null) {
			double d[] = bundle.getDoubleArray("location");
			Log.d(TAG, d[0] + " + " + d[1]);
			location = new LatLng(d[0], d[1]);
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
			
		View view = inflater.inflate(R.layout.fragment_viewmemolist, container,
				false);
		return view;
	}
	
	@Override
	public void onStart(){
		Log.d(TAG, "onStart");
		memoArray = DataHandler.getInstance(MemoMap.getInstance()).getAllClosestMemos(location);
		MemoAdapter memoAdapter = new MemoAdapter(MemoMap.getInstance(), 0,
				memoArray, location);	
		getListView().setAdapter(memoAdapter);
		super.onStart();
	}
}