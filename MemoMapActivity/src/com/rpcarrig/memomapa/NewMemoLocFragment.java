package com.rpcarrig.memomapa;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class NewMemoLocFragment extends Fragment implements OnSeekBarChangeListener {
	private final static String CLASS = "ContentFragment";
	private final static int DEFAULT_PROGRESS = 75;
	
	private CheckBox fave;
	private SeekBar seek;
	private TextView memo;
	
	public NewMemoLocFragment() { }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
			Bundle savedInstanceState) {
		Log.d(CLASS, "onCreateView");
		return inflater.inflate(R.layout.fragment_newmemoloc, container, false);
	}	
	
	@Override
	public void onPause(){
		
	}
	
	@Override
	public void onStart(){
		Log.d(CLASS, "onStart");

		seek = (SeekBar)getView().findViewById(R.id.seekBar1);
		seek.setProgress(DEFAULT_PROGRESS);
		seek.setOnSeekBarChangeListener(this);
		super.onStart();
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		TextView text = (TextView)getView().findViewById(R.id.TextView02);
		text.setText(new String((progress + 25) + " m"));
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) { }

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) { }
	
	public int getRadius(){
		return (seek.getProgress() + 25);
	}
	
	public boolean getSaveFave(){
		return fave.isChecked();
	}
	
	public String getMemoLoc(){
		return memo.getText().toString();
	}
	
}