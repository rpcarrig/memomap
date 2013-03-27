/**
 * 
 */

package com.rpcarrig.memomapa;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.rpcarrig.memomapa.MemoMapService.GpsBinder;

public class MemoMapActivity extends Activity implements OnSeekBarChangeListener{
	private final String TAG = "MemoMapActivity";
	
	private ArrayList<Memo> memoArray;
	private GoogleMap googleMap;
	
	private MemoMapService memoMapService;
	private LatLng latLongLocation;
	private Location location;
	private Marker marker;
	private NewMemoBodyFragment memoBodyFragment;
	private NewMemoLocFragment memoLocFragment;
	
	static String editBody,
				  receiveAddress;
	boolean gpsBound = false,
			isZoomed = false;
	Double lat,
		   lon;
	
	ServiceConnection serviceConnection = new ServiceConnection(){
		@Override
		public void onServiceConnected(ComponentName name,
				IBinder service){
			Log.d(TAG, "ServiceConnection: onServiceConnected");
			GpsBinder binder = (GpsBinder)service;
			
			memoMapService = binder.getService();
			gpsBound = true;
			
			location = memoMapService.getLocation();
			resetView();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.d(TAG, "ServiceConnection: onServiceDisconnected");
			gpsBound = false;
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_memomap);
		
		if(findViewById(R.id.fragment_topcontainer) != null){
			if(savedInstanceState != null) return;
			getFragmentManager().beginTransaction()
				.add(R.id.fragment_topcontainer, new MapFragment(), "map")
				.commit();
			getFragmentManager().beginTransaction()
				.add(R.id.fragment_bottomcontainer, new ViewMemoListFragment(), "view")
				.commit();
		}
		
		Intent gpsIntent = new Intent(this, MemoMapService.class);
		bindService(gpsIntent, serviceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(TAG, "onCreateOptionsMenu");
		getMenuInflater().inflate(R.menu.activity_memomap, menu);
		return true;
	}
	
	@Override
	public void onDestroy(){
		Log.d(TAG, "onDestroy");
		super.onDestroy();
		if(gpsBound) unbindService(serviceConnection);
	}
	
	@Override
	public boolean onOptionsItemSelected( MenuItem item ){
		Log.d(TAG, "onOptionsItemSelected");
	    switch(item.getItemId()){
	    	case R.id.menu_resetview:
	    		Log.d(TAG, "reset");
	    		resetView();
	    		return true;
		    case R.id.menu_creatememo:
		    	Log.d(TAG, "create");
		    	createMemo();
		    	return true;
		    case R.id.menu_settings:
		    	Log.d(TAG, "settings");
		    	showSettings();
		    	return true;
	    	default:
	    		return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	public void onPause(){
		Log.d(TAG, "onPause");
		super.onPause();
	}
	
	@Override
	public void onResume(){	
		Log.d(TAG, "onResume"); 
		super.onResume();
	}
	
	@Override
	public void onStart(){ 
		Log.d(TAG, "onStart");
		super.onStart();
		MapFragment m = (MapFragment) getFragmentManager().findFragmentByTag("map");
		if(m != null) {
			googleMap = m.getMap();
			googleMap.setMyLocationEnabled(true);
		}	
	}
	
	/**
	 * 
	 */
	public void continueMemo(View view){
		editBody = memoBodyFragment.getBody();
		googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLongLocation, 11));
		marker.setTitle(editBody);
		marker.showInfoWindow();
		getFragmentManager().beginTransaction()
			.replace(R.id.fragment_bottomcontainer, new NewMemoLocFragment(), "memoloc")
			.setCustomAnimations(
				R.animator.slide_in_frombottom, R.animator.slide_out_totop,
				R.animator.slide_in_fromtop, R.animator.slide_out_tobottom)
			.addToBackStack(null)
			.commit();
	}
	
	private void createMemo(){
		Log.d(TAG, "createMemo");
		marker = googleMap.addMarker(new MarkerOptions()
				.position(latLongLocation));
		memoBodyFragment = new NewMemoBodyFragment();
    	getFragmentManager().beginTransaction()
			.replace(R.id.fragment_bottomcontainer, memoBodyFragment, "memobody")
			.setCustomAnimations(
					R.animator.slide_in_frombottom, R.animator.slide_out_totop,
					R.animator.slide_in_fromtop, R.animator.slide_out_tobottom)
			.addToBackStack(null)
			.commit();
	}
	
	public void defaultClick(View view){
		SeekBar seekBar = (SeekBar)findViewById(R.id.seekBar1);
		seekBar.setProgress(75);
	}
	
	private void geocode(String address) throws IOException{
		List<Address> found = new Geocoder(this)
				.getFromLocationName(address, 1);
		final LatLng result = new LatLng(found.get(0).getLatitude(),
				found.get(0).getLongitude());
		
		marker = googleMap.addMarker(new MarkerOptions()
					.position(result).title(address));
		marker.showInfoWindow();
		googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(result, 17));
		
		OnInfoWindowClickListener listener = new OnInfoWindowClickListener(){
			@Override
			public void onInfoWindowClick(Marker marker) {
				createMemo();
			}
		};
		googleMap.setOnInfoWindowClickListener(listener);
	}
	
	public void openMemo(Memo memo){
		View view = getLayoutInflater().inflate(R.layout.dialog_viewmemo,
				null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(view);
		final TextView memoText = (TextView) view.findViewById(R.id.textView2),
			memoBody = (TextView) view.findViewById(R.id.textView1),
			memoLat = (TextView) view.findViewById(R.id.memoLat),
			memoLong = (TextView) view.findViewById(R.id.memoLong);
		memoBody.setText(memo.getMemoBody());
		memoText.setText(memo.getMemoTitle());
		memoLong.setText(Double.valueOf(memo.getLongitude()).toString());
		memoLat.setText(Double.valueOf(memo.getLatitude()).toString());
		builder
			.setPositiveButton(R.string.edit, new DialogInterface
					.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, 
								int which) {
							Log.d(TAG, "onClick");
						}
					})
			.setNeutralButton(R.string.delete_memo, new DialogInterface
					.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						
						}
					})
			.setNegativeButton(R.string.cancel, new DialogInterface
					.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, 
								int which) {
						}
					});
		builder.create().show();		
	}
	
	private void resetView(){
		Log.d(TAG, "resetView");
		if(location != null){
			latLongLocation = new LatLng(
					location.getLatitude(), 
					location.getLongitude());
			googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLongLocation, 12));
		}
	}
	
	public void saveMemoClick(View view){
		CheckBox fave = (CheckBox)view.findViewById(R.id.checkbox);
		TextView text = (TextView)view.findViewById(R.id.newMemoLocation);
		
		String memoBody = editBody,
				memoLoc = memoLocFragment.getMemoLoc();
		int radius = memoLocFragment.getRadius();
		if(fave.isChecked());
		
		Memo m = new Memo(memoLoc, memoBody, lat, lon, radius);
		DbHandler.getInstance(MemoMap.getInstance()).addMemo(m);
	}
	
	private void searchByAddress(){
		View view = getLayoutInflater().inflate(R.layout.dialog_searchaddr,
				null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(view);
		final EditText userInput = (EditText) 
				view.findViewById(R.id.address_entry);
		builder
			.setPositiveButton(R.string.search, new DialogInterface
					.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, 
								int which) {
							Log.d(TAG, "onClick");
							receiveAddress = userInput.getText().toString();
							try {
								geocode(receiveAddress);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					})
			.setNegativeButton(R.string.cancel, new DialogInterface
					.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, 
								int which) {
						}
					});
		builder.create().show();
	}
	
	private void showSettings(){
    	getFragmentManager().beginTransaction()
			.setCustomAnimations(
					R.animator.slide_in_fromleft, R.animator.slide_out_toright,
					R.animator.slide_in_fromright, R.animator.slide_out_toleft)
			.replace(R.id.fragment_topcontainer, new SettingsFragment())
			.addToBackStack(null)
			.commit();
	}
	
	private void viewMemoOnMap(long id, GoogleMap map){
		Log.d(TAG, "viewMemo (id: " + id + ")");
		
		Memo memo = memoArray.get((int)id);
		//memo.getMarkerOptions().showInfoWindow();
		map.animateCamera(CameraUpdateFactory
				.newLatLngZoom(memo.getLatLong(), 17));
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		Log.d(TAG, "onProgressChanged");
		
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}
}