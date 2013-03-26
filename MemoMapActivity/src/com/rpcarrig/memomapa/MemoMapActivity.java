/**
 * 
 */

package com.rpcarrig.memomapa;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.EditText;
import android.widget.ListView;
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

public class MemoMapActivity extends Activity{
	private final String CLASS = "MemoMapActivity";
	
	private ArrayList<Memo> memoArray;
	private GoogleMap mapFragment;
	private MemoAdapter memoAdapter;
	private MemoMapService memoMapService;
	private ListView listView;
	private Location location;
	private Marker marker;
	static String editTitle, 
	  			  editBody, 
				  editLat, 
				  editLon,
				  editRad,
				  receiveAddress;
	boolean gpsBound = false,
			isZoomed = false;
	Double lat,
		   lon;
	ServiceConnection serviceConnection;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(CLASS, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_memomap);
				
		listView = (ListView) findViewById(R.id.memolistview);
		bindService();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(CLASS, "onCreateOptionsMenu");
		getMenuInflater().inflate(R.menu.activity_memomap, menu);
		return true;
	}
	
	@Override
	public void onDestroy(){
		Log.d(CLASS, "onDestroy");
		super.onDestroy();
		if(serviceConnection != null) unbindService(serviceConnection);
	}
	
	@Override
	public boolean onOptionsItemSelected( MenuItem item ){
		Log.d(CLASS, "onOptionsItemSelected");
	    switch(item.getItemId()){
	    	case R.id.menu_resetview:
	    		resetView();
	    		return true;
	    	
	    	case R.id.menu_choosefave:
	    		//chooseFave();
	    		return true;
		    case R.id.menu_createhere:
		    	createMemo(new LatLng(lat, lon));
		    	return true;
		    case R.id.menu_searchaddr:
		    	searchByAddress();
		    	return true;
		    case R.id.menu_settings:
		    	//showSettings();
		    	return true;
	    	default:
	    		return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	public void onPause(){ Log.d(CLASS, "onPause");	super.onPause(); }
	
	@Override
	public void onResume(){	
		Log.d(CLASS, "onResume"); 
		super.onResume();
	}
	
	@Override
	public void onStart(){ 
		Log.d(CLASS, "onStart");
		super.onStart();
		
		DbHandler dbHandler = DbHandler.getInstance(MemoMap.getInstance());
		memoArray = dbHandler.getAllMemos();
	}
	
	/**
	 * 
	 */
	private void bindService(){
		Intent gpsIntent = new Intent(this, MemoMapService.class);
		serviceConnection = new ServiceConnection(){
			@Override
			public void onServiceConnected(ComponentName name,
					IBinder service){
				Log.d(CLASS, "ServiceConnection: onServiceConnected");
				GpsBinder binder = (GpsBinder)service;
				memoMapService = binder.getService();
				gpsBound = true;
				
				location = memoMapService.geoLocation();
				if(location != null){
					showMap(location);
					if(listView != null){
						renderList(memoArray);
					}
				}
	}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.d(CLASS, "ServiceConnection: onServiceDisconnected");
				gpsBound = false;
			}
		};
		bindService(gpsIntent, serviceConnection, Context.BIND_AUTO_CREATE);
	}

	private void chooseFave(){
		Log.d(CLASS, "chooseFave");
		startActivity(new Intent(this, ViewFavoritesActivity.class));
	}
	
	private void createMemo(LatLng loc){
		Log.d(CLASS, "createMemo");
		Intent faveIntent = new Intent(this, CreateMemoActivity.class);
		Bundle extras = new Bundle();
		extras.putDouble("lat", loc.latitude);
		extras.putDouble("lon", loc.longitude);
		faveIntent.putExtras(extras);
		startActivity(faveIntent);
	}
	
	private void geocode(String address) throws IOException{
		List<Address> found = new Geocoder(this)
				.getFromLocationName(address, 1);
		final LatLng result = new LatLng(found.get(0).getLatitude(),
				found.get(0).getLongitude());
		
		marker = mapFragment.addMarker(new MarkerOptions()
					.position(result)
					.title(address));
		marker.showInfoWindow();
		mapFragment.animateCamera(CameraUpdateFactory
				.newLatLngZoom(result, 17));
		
		OnInfoWindowClickListener listener = new OnInfoWindowClickListener(){
			@Override
			public void onInfoWindowClick(Marker marker) {
				createMemo(result);
			}
		};
		mapFragment.setOnInfoWindowClickListener(listener);
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
							Log.d(CLASS, "onClick");
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
	
	private void renderList(ArrayList<Memo> memos){
		Log.d(CLASS, "renderList");
		if(memos != null) { 		
			memoAdapter = new MemoAdapter(this, 0, memoArray, location, 
					getFragmentManager());
			listView.setAdapter(memoAdapter);
			OnItemClickListener listen = new OnItemClickListener() {
				  @Override
				  public void onItemClick(AdapterView<?> parent,
						  View view, int position, long id) {
					  viewMemoOnMap(id, mapFragment);
				  }
				};
			listView.setOnItemClickListener(listen);
		}
	}
	
	private void resetView(){
		Log.d(CLASS, "resetView");
		if(marker != null) marker.setVisible(false);
		if(location != null){
			LatLng latlong = new LatLng(
					location.getLatitude(), 
					location.getLongitude());
			mapFragment.animateCamera(CameraUpdateFactory.newLatLngZoom(latlong, 12));
		}
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
							Log.d(CLASS, "onClick");
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
	
	private void showMap(Location location){
		LatLng latlong = new LatLng(
				location.getLatitude(), 
				location.getLongitude());

		mapFragment = ((MapFragment)getFragmentManager()
				.findFragmentById(R.id.top_fragment)).getMap();
		mapFragment.animateCamera(CameraUpdateFactory
				.newLatLngZoom(latlong, 12));
		mapFragment.setMyLocationEnabled(true);
		
		OnInfoWindowClickListener listener = new OnInfoWindowClickListener(){
			@Override
			public void onInfoWindowClick(Marker marker) {
				Memo memo = new Memo();
				for(Memo m : memoArray){
					if (m.getMarker().getId().equals(marker.getId()))
						memo = m;
				}
				openMemo(memo);
			}
		};
		
		mapFragment
			.setOnInfoWindowClickListener(listener);
	}
	
	private void showSettings(){
    	getFragmentManager().beginTransaction()
		.setCustomAnimations(
				R.animator.slide_in_fromright,
				R.animator.slide_out_toright,
				
				R.animator.slide_in_fromright,
				R.animator.slide_out_toright)
		.replace(R.id.main_fragments_layout, 
				new SettingsFragment())
    	.addToBackStack(null)
    	.commit();
	}
	
	private void viewMemoOnMap(long id, GoogleMap map){
		Log.d(CLASS, "viewMemo (id: " + id + ")");
		
		Memo memo = memoArray.get((int)id);
		memo.getMarker().showInfoWindow();
		map.animateCamera(CameraUpdateFactory
				.newLatLngZoom(memo.getLatLong(), 17));
	}
}