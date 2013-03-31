/**
 * 
 */

package com.rpcarrig.memomapa;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MemoMapActivity extends Activity implements LocationListener {
	private static final String TAG = "MemoMapActivity";
	private static final long MIN_DISTANCE_CHANGE	 = 10,
							  MIN_MS_BETWEEN_UPDATES = 1000 * 10 * 1;

	private static Context context;
	private static LatLng latLongLocation;
	private static GoogleMap googleMap;
	private static Location location;
	private MapFragment mapFragment;
	private Fragments.MemoList memoListFragment;
	private LocationManager locationManager = null;
	private MemoMapService memoMapService;
	private SharedPreferences sharedPreferences;

	boolean gpsBound         = false,
			canGetLocation	 = false,
			isGpsEnabled	 = false,
			isNetworkEnabled = false;

	Runnable drawMemoMarkers = new Runnable(){
		public void run(){
			googleMap = ((MapFragment)getFragmentManager().findFragmentByTag("map")).getMap();
			for(Memo m : DataHandler.getInstance(MemoMap
					.getInstance()).getAllMemos()){
				Circle circ = googleMap.addCircle(new CircleOptions()
					.center(m.getLatLong())
					.fillColor(0x25AA0000)
					.strokeColor(0xFFAA0000)
					.strokeWidth(2)
					.radius(m.getRadius()));
				m.setCircle(circ);
				
				MarkerOptions markOpt = new MarkerOptions()
					.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
					.position(m.getLatLong())
					.snippet(m.getLocationName())
					.title(m.getMemoBody());
				m.setMarker(googleMap.addMarker(markOpt));
				Log.d(TAG, m.getMarker().toString());
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
		setContentView(R.layout.activity_memomap);		
		
		context = this;
		
		startGps();
		
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MemoMap.getInstance());
		int mapType = Integer.parseInt(sharedPreferences.getString("prefMapType", "2"));
		mapFragment = MapFragment.newInstance(new GoogleMapOptions()
			.camera(new CameraPosition(latLongLocation, 12, 0, 0))
			.mapType(mapType));
		
		getFragmentManager().beginTransaction()
				.replace(R.id.fragment_topcontainer, mapFragment, "map")
				.commit();
			
		memoListFragment = new Fragments.MemoList();
		//if(location == null){
			location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			double[] coords = {location.getLatitude(), location.getLongitude()};
			Bundle bundle = new Bundle();
			bundle.putDoubleArray("location", coords);
			memoListFragment.setArguments(bundle);
		//}
		
		getFragmentManager().beginTransaction()
			.replace(R.id.fragment_bottomcontainer, memoListFragment, "memoList")
			.commit();
	}

	@Override
	public void onStart(){ 
		Log.d(TAG, "onStart");
		super.onStart();
		
		int mapType = Integer.parseInt(sharedPreferences.getString("prefMapType", "2"));

		googleMap = ((MapFragment)getFragmentManager().findFragmentByTag("map")).getMap();
		googleMap.setMapType(mapType);
		googleMap.setMyLocationEnabled(true);
		
		new Thread(drawMemoMarkers).run();
	}
	
	@Override
	public void onResume(){	
		Log.d(TAG, "onResume"); 
		super.onResume();
	}

	@Override
	public void onPause(){
		Log.d(TAG, "onPause");
		super.onPause();
	}

	@Override
	public void onStop(){
		Log.d(TAG, "onStop");
		super.onDestroy();
	}

	@Override
	public void onDestroy(){
		Log.d(TAG, "onDestroy");
		super.onDestroy();
		//if(gpsBound) unbindService(serviceConnection);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(TAG, "onCreateOptionsMenu");
		getMenuInflater().inflate(R.menu.activity_memomap, menu);
		return true;
	}
	
	@Override
	public void onLocationChanged(Location location) {
		Log.d(TAG, "onLocationChanged");
		if (locationManager != null){
			location = getFreshLocation();
		}
	}
	
	@Override
	public boolean onOptionsItemSelected( MenuItem item ){
		Log.d(TAG, "onOptionsItemSelected");
	    switch(item.getItemId()){
	    	case R.id.menu_resetview:  resetView();
	    							   return true;
		    case R.id.menu_creatememo: createMemo();
		    						   return true;
		    case R.id.menu_settings:   showSettings();
		    						   return true;
	    	default: return super.onOptionsItemSelected(item);
	    }
	}
	

	@Override
	public void onProviderDisabled(String provider) { }

	@Override
	public void onProviderEnabled(String provider) { }
		
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {}
	
	/**
	 * 
	 */
	
	private void createMemo(){
		double latitude  = location.getLatitude(),
			   longitude = location.getLongitude();
		
		Intent createMemo = new Intent(MemoMapActivity.this, CreateMemoActivity.class);
		Bundle bundle = new Bundle();
		
		bundle.putDouble("lat", latitude);
		bundle.putDouble("lon", longitude);
		createMemo.putExtras(bundle);
		
		startActivity(createMemo);
	}
	
	public Location getFreshLocation(){
		Log.d(TAG, "geoLocation");
		double latitude, longitude;
		if( !isGpsEnabled && !isNetworkEnabled){
			//no GPS or network
			Log.e(CONNECTIVITY_SERVICE, "no GPS or network");
		}
		else{
			this.canGetLocation = true;
			
			// If the network provider is available, get its location first.
			if(isNetworkEnabled){
				Log.d("geoLocation", "Network is enabled.");
				if (locationManager != null){
					location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
					if(location != null){
						latitude = location.getLatitude();
						longitude = location.getLongitude();
						latLongLocation = new LatLng(latitude, longitude);
					}
				}
			}
			
			// If the GPS is also available, get coordinates using GPS services.
			if(isGpsEnabled){
				Log.d("geoLocation", "GPS is enabled.");
				if (locationManager != null){
					location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
					if(location != null){
						latitude = location.getLatitude();
						longitude = location.getLongitude();
						latLongLocation = new LatLng(latitude, longitude);
					}
				}
			}
		}
		return location;
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
	
	private void showSettings(){
    	Intent settingsIntent = new Intent(this, SettingsActivity.class);
    	startActivity(settingsIntent);
	}
	
	private void startGps() {
		try{
			locationManager	= (LocationManager)this.getSystemService(LOCATION_SERVICE);
			isGpsEnabled		= locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			isNetworkEnabled	= locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_MS_BETWEEN_UPDATES, MIN_DISTANCE_CHANGE, this);	

			location = getFreshLocation();
		}
		catch (Exception e){
			e.printStackTrace();
			Log.e(NOTIFICATION_SERVICE, "GPS Start error");
		}
	}

	private static void viewMemoOnMap(long id){
		Log.d(TAG, "viewMemo (id: " + id + ")");
		Memo m = DataHandler.getInstance(MemoMap.getInstance())
				.getAllClosestMemos(latLongLocation).get((int)id);
		
		if(m != null){
			if (m.getMarker() != null) m.getMarker().showInfoWindow();
			googleMap.animateCamera(CameraUpdateFactory
				.newLatLngZoom(m.getLatLong(), 17));
		}
	}
	
	/**
	 * 
	 *
	 *
	 */	
	private static class Fragments {
		public static class MemoList extends ListFragment {
			private final static String TAG = "MemoListFragment";
			
			ArrayList<Memo> memoArray;
			LatLng location;
			MemoAdapter memoAdapter;
			OnItemClickListener itemClick = new OnItemClickListener(){
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
						long arg3) {
					viewMemoOnMap(arg3);
				}
			};
			OnItemLongClickListener itemLongClick = new OnItemLongClickListener(){
				@Override
				public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					showModifyDialog(arg3);
					return false;
				}	
			};
			
			public MemoList(){ }

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
				
				return inflater.inflate(R.layout.fragment_viewmemolist, container,
						false);
			}
			
			@Override
			public void onResume(){
				Log.d(TAG, "onResume");
				
				if(getListView().isClickable()){
					getListView().setOnItemClickListener(itemClick);
					getListView().setOnItemLongClickListener(itemLongClick);
				}
				super.onResume();
			}
			
			@Override
			public void onStart(){
				Log.d(TAG, "onStart");
				memoArray = DataHandler.getInstance(MemoMap.getInstance()).getAllClosestMemos(location);
				memoAdapter = new MemoAdapter(MemoMap.getInstance(), 0,
						memoArray, location);
				getListView().setAdapter(memoAdapter);
				super.onStart();
			}
			
			private void showModifyDialog(final long id){
				final Memo m = memoArray.get((int) id);

				AlertDialog.Builder modifyDialogBuilder = new AlertDialog.Builder(context)
						.setMessage(m.getMemoBody())
						.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								Log.d(TAG, "Cancel");
							}
						})
						.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								DataHandler.getInstance(MemoMap.getInstance())
									.deleteMemo(m);
								getActivity().recreate();	
							}
						})
						.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								Log.d(TAG, "Edit Memo");
								Intent editIntent = new Intent(MemoMapActivity.context, EditMemoActivity.class);
								editIntent.putExtra("id", id);
								editIntent.putExtra("lat", location.latitude);
								editIntent.putExtra("lon", location.longitude);	
								startActivity(editIntent);
							}
						})
					.setTitle(m.getLocationName());
				AlertDialog modifyMemo = modifyDialogBuilder.create();
				modifyMemo.show();
			}
		}
		
		public static class OpenMemo extends Fragment{
			final String CLASS = "OpenMemoFragment";
			public OpenMemo(){ }
			
			@Override 
			public View onCreateView(LayoutInflater inflater, ViewGroup container,
					Bundle savedInstanceState) {
				Log.d(CLASS, "onCreateView");
				return inflater.inflate(R.layout.dialog_viewmemo, container, false);
			}
		}
	}
}