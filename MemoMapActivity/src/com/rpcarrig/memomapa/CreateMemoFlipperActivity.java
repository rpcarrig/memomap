package com.rpcarrig.memomapa;

import java.io.IOException;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class CreateMemoFlipperActivity extends Activity implements OnSeekBarChangeListener {

	boolean     mCanGoBack = false;
	Circle      mCircle;
	CheckBox    mCheckBox;
	double      mLatitude,
			    mLongitude;
	GoogleMap   mGoogleMap;
	int         mRadius, 
    			mOldProgress;
	LatLng      mLatLongLocation;
	Marker      mMarker;
	SeekBar     mSeekBar;
	String      deviceId, 
				mFaveAddress, 
				mMemoBody, 
				mLocName;
	EditText    mBodyText, 
				mLocNameText;
	ViewFlipper mViewFlipper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
		setContentView(R.layout.activity_creatememo_flip);
		
		ActionBar mActionBar = getActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        SharedPreferences mSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(MemoMap.getInstance());
		int mMapType = Integer.parseInt(mSharedPreferences.getString("prefMapType", "2"));    
	        
		Bundle bundle = getIntent().getExtras();
		mLatitude  = bundle.getDouble("lat");
		mLongitude = bundle.getDouble("lon");
		mLatLongLocation = new LatLng(mLatitude, mLongitude);
		
		mGoogleMap = ((MapFragment)getFragmentManager()
				.findFragmentById(R.id.create_map)).getMap();
		mGoogleMap.setMapType(mMapType);
		mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLatLongLocation, 17));
	    
		mMarker = mGoogleMap.addMarker(new MarkerOptions().position(mLatLongLocation));
		
		mViewFlipper = (ViewFlipper) findViewById(R.id.viewflipper1);
		mViewFlipper.setFlipInterval(2500);
		mViewFlipper.setInAnimation(MemoMap.getInstance(), android.R.anim.slide_in_left);
		mViewFlipper.setOutAnimation(MemoMap.getInstance(), android.R.anim.slide_out_right);
	}
	
	@Override
	protected void onStart(){
		super.onStart();
		mSeekBar = (SeekBar)findViewById(R.id.seekBar1);
		mSeekBar.setOnSeekBarChangeListener(this);
	}
	
	@Override
	public void onBackPressed(){
		if(mCanGoBack){
			mCanGoBack = false;
			mCircle.remove();
			mGoogleMap.animateCamera(CameraUpdateFactory.zoomIn());
			mMarker.hideInfoWindow();
			mViewFlipper.showPrevious();
		}
		else finish();
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		progress = (mSeekBar.getProgress() + 25);
		mCircle.setRadius(progress);
		
		TextView distance = (TextView) findViewById(R.id.TextView02);
		distance.setText((progress) + " m");
		
		mOldProgress = progress;
	}
	
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		mCircle.setFillColor(0x000000);
		mCircle.setStrokeColor(0xFF000000);
		mCircle.setStrokeWidth(4);
	}
	
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		if(mCircle.getRadius() < 250) mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(16));
		if(mCircle.getRadius() > 250) mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
		if(mCircle.getRadius() > 500) mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(14));
		if(mCircle.getRadius() > 750) mGoogleMap
				.animateCamera(CameraUpdateFactory.zoomTo((float) 13.5));
		mCircle.setFillColor(0x25AA0000);
		mCircle.setStrokeColor(0xFFAA0000);
		mCircle.setStrokeWidth(4);
		mRadius = (int) mCircle.getRadius();
	}
	
	public void cancelMemo(View view){
		finish();
	}
	
	public void continueMemo(View view){
		mCanGoBack = true;
		mBodyText = (EditText) findViewById(R.id.newmemo_body);
		mMemoBody = mBodyText.getText().toString();
		if(mBodyText.getText().toString().contentEquals("")) mMemoBody = "New Memo";
		
		mCircle = mGoogleMap.addCircle(new CircleOptions()
			.center(mMarker.getPosition())
			.fillColor(0x25AA0000)
			.strokeColor(0xFFAA0000)
			.strokeWidth(2)
			.radius(100));
		mGoogleMap.animateCamera(CameraUpdateFactory.zoomOut());
		mMarker.setTitle(mMemoBody);
		mMarker.showInfoWindow();
		mViewFlipper.showNext();
	}

	public void defaultClick(View view){
		mSeekBar.setProgress(75);
		mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mMarker.getPosition(), 16));
	}
	
	public void saveMemoClick(View view) throws IOException{
		mLocNameText = (EditText)findViewById(R.id.newMemoLocation);
		mLocName = mLocNameText.getText().toString();
		if(mLocName.contentEquals("")) mLocName = "[" + mLongitude + ", " + mLatitude + "]";
		
		mSeekBar = (SeekBar)findViewById(R.id.seekBar1);
		mCircle.setRadius(mRadius);
		mRadius = mSeekBar.getProgress();
		
		final Memo newMemo = new Memo(mLocName, mMemoBody, mLatitude, mLongitude, mRadius,
									  DataHandler.getAndroidId());
		
		mCheckBox = (CheckBox)findViewById(R.id.checkbox);
		if(mCheckBox.isChecked()){
			//mFaveAddress = "[Address]";
			//Favorite fave = new Favorite(mLocName, mFaveAddress, mLatitude, mLongitude);
			//DataHandler.getInstance(this).addFave(fave);
			new Thread( new Runnable(){
				public void run(){
					ServerHandler.upload(newMemo);
				}
			}).start();
			
		}
		else {
			DataHandler.getInstance(MemoMap.getInstance()).addMemo(newMemo);
		}

		finish();
	}
}
