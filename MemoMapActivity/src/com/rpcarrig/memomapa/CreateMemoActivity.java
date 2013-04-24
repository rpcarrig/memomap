/**
 * This activity provides an interface for the creation of new Memos into the database.
 * 
 * @author Ryan P. Carrigan
 * @version 2.10 18 April 2013
 */

package com.rpcarrig.memomapa;

import java.io.IOException;

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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class CreateMemoActivity extends Activity implements OnSeekBarChangeListener {
	boolean     mCanGoBack  = false,
				mPublicMemo = false;
	Circle      mCircle;
	CheckBox    mCheckBox,
				mCheckPublic;
	GoogleMap   mGoogleMap;
	int         mRadius;
	LatLng      mLatLongLocation;
	Marker      mMarker;
	String      mMemoBody, 
				mLocName;
	EditText    mBodyText, 
				mLocNameText;
	ViewFlipper mViewFlipper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
		setContentView(R.layout.activity_creatememo_flip);
		
        SharedPreferences mSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(MemoMap.getInstance());
	        
		Bundle bundle = getIntent().getExtras();
		mLocName = bundle.getString("loc");
		mLatLongLocation = new LatLng(bundle.getDouble("lat"), bundle.getDouble("lon"));
		
		mGoogleMap = ((MapFragment)getFragmentManager()
				.findFragmentById(R.id.create_map)).getMap();
		mGoogleMap.setMapType(Integer.parseInt(mSharedPreferences.getString("prefMapType", "2")));
		mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLatLongLocation, 17));
	    
		mMarker = mGoogleMap.addMarker(new MarkerOptions().position(mLatLongLocation)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin4_yellow)));
		mRadius = 100;
		mViewFlipper = (ViewFlipper) findViewById(R.id.viewflipper1);
		mViewFlipper.setFlipInterval(2500);
		mViewFlipper.setInAnimation(MemoMap.getInstance(), android.R.anim.slide_in_left);
		mViewFlipper.setOutAnimation(MemoMap.getInstance(), android.R.anim.slide_out_right);
	}
	
	/* Sets the seek bar on-change listener. */
	@Override
	protected void onStart(){
		super.onStart();
		((SeekBar) findViewById(R.id.seekBar1)).setOnSeekBarChangeListener(this);
	}
	
	/* Allows the back button to be used to flip the view back. */
	@Override
	public void onBackPressed(){
		if(mCanGoBack){
			mCanGoBack = false;
			mCircle.remove();
			mGoogleMap.animateCamera(CameraUpdateFactory.zoomIn());
			mMarker = mGoogleMap.addMarker(new MarkerOptions().position(mLatLongLocation)
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.pin4_yellow)));
			mViewFlipper.showPrevious();
		}
		else finish();
	}
	
	/* Adjusts the radius circle when the seek bar changes. */
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		mCircle.setRadius((((SeekBar)findViewById(R.id.seekBar1)).getProgress() + 25));
		((TextView) findViewById(R.id.TextView02)).setText((progress + 25) + " m");
	}
	
	/* Changes the circle's color when the seekbar is touched. */
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		mCircle.setFillColor(0x000000);
		mCircle.setStrokeColor(0xFF000000);
		mCircle.setStrokeWidth(4);
	}
	
	/* Adjusts the circle and the zoom level after the radius has been adjusted. */
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
	
	/* Closes the activity when the Cancel button is pressed. */
	public void cancelMemo(View view){
		finish();
	}
	
	/* Sets parameters when the continue button is pressed. */
	public void continueMemo(View view){
		mCanGoBack = true;
		mBodyText = (EditText) findViewById(R.id.newmemo_body);
		mCheckPublic = (CheckBox) findViewById(R.id.checkPublic);
		if(mCheckPublic.isChecked()) {
			mMarker = mGoogleMap.addMarker(new MarkerOptions().position(mLatLongLocation)
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin4_green)));
			mPublicMemo = true;
		}
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
		
		mLocNameText = (EditText)findViewById(R.id.newMemoLocation);
		mLocNameText.setText(mLocName);
	}

	/* Returns the radius to the default progress. */
	public void defaultClick(View view){
		((SeekBar) findViewById(R.id.seekBar1)).setProgress(75);
		mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mMarker.getPosition(), 16));
	}
	
	/* Saves the memo to the database. */
	public void saveMemoClick(View view) throws IOException{
		mLocName = mLocNameText.getText().toString();
		if(mLocName.contentEquals("")) mLocName = "[" + mLatLongLocation.longitude + 
				", " + mLatLongLocation.latitude + "]";
		
		mCircle.setRadius(mRadius);
		mRadius = ((SeekBar)findViewById(R.id.seekBar1)).getProgress();
		
		final Memo newMemo = new Memo(mLocName, mMemoBody, mLatLongLocation.latitude,
				mLatLongLocation.longitude, mRadius, DataHandler.getAndroidId());
		mCheckBox = (CheckBox)findViewById(R.id.checkbox);
		if(mCheckBox.isChecked()){
			Favorite fave = new Favorite(mLocName, mLocName, mLatLongLocation.latitude,
					mLatLongLocation.longitude);
			DataHandler.getInstance(MemoMap.getInstance()).addFave(fave);
		}
		
		if(mPublicMemo){
			new Thread( new Runnable(){
				public void run(){
					ServerHandler.upload(newMemo);
				}
			}).start();
		}
		else DataHandler.getInstance(MemoMap.getInstance()).addMemo(newMemo);
		finish();
	}
}
