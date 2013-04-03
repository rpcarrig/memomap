/**
 * 
 */

package com.rpcarrig.memomapa;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ListFragment;
import android.app.ProgressDialog;
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
import android.widget.ListView;

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

	private static Context sContext;
	private static LatLng sLatLongLocation;
	private static GoogleMap sGoogleMap;
	private Location mLocation;
	private Fragments.MemoList mMemoListFragment;
	private LocationManager mLocationManager = null;

	boolean circlesExist	 = false,
			listExists		 = false,
			serviceBound	 = false,
			canGetLocation	 = false,
			isGpsEnabled	 = false,
			isNetworkEnabled = false;

	ArrayList<Memo> mMemoArray;
	GoogleMap mGoogleMap;
	ListView mListView;
	MemoAdapter mMemoAdapter;
	ProgressDialog mDialog;
	Runnable drawMemoMarkers = new Runnable(){
		public void run(){
			for(Memo m : DataHandler.getInstance(MemoMap
					.getInstance()).getAllMemos()){
				Circle circ = sGoogleMap.addCircle(new CircleOptions()
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
				m.setMarker(sGoogleMap.addMarker(markOpt));
			}
			circlesExist = true;
		}
	};
	SharedPreferences mSharedPreferences;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

		setContentView(R.layout.activity_memomap);		

		mSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(MemoMap.getInstance());
		
		mDialog = new ProgressDialog(this);
		mDialog.setTitle(R.string.waiting);
		mDialog.setCancelable(false);
		if(mLocation == null) mDialog.show();
		
		int mMapType = Integer.parseInt(mSharedPreferences.getString("prefMapType", "2"));
		mGoogleMap = ((MapFragment)getFragmentManager()
				.findFragmentById(R.id.memo_map)).getMap();
		mGoogleMap.setMapType(mMapType);
		mGoogleMap.setMyLocationEnabled(true);
		
		mListView = (ListView) findViewById(R.id.memo_list);
		//mMemoArray = DataHandler.getInstance(MemoMap.getInstance()).getAllClosestMemos(location);
		//mMemoAdapter = new MemoAdapter(MemoMap.getInstance(), 0, memoArray, location);
		
		sContext = MemoMap.getInstance();

		Intent serviceIntent = new Intent(this, MemoMapService.class);
		startGps();
		startService(serviceIntent);
	}

	@Override
	public void onStart(){ 
		super.onStart();
	}

	@Override
	public void onResume(){	
		//if(!circlesExist) new Thread(drawMemoMarkers).run();
		super.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    //No call for super(). Bug on API Level > 11.
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
		if(mDialog.isShowing()) mDialog.cancel();
		location = getFreshLocation();
		/**
		if (!listExists){
			addMemoList();
			listExists = true;
		}*/
		LatLng mLatLongLocation = new LatLng(location.getLatitude(), location.getLongitude());
		mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLongLocation, 17));
	}

	@Override
	public boolean onOptionsItemSelected( MenuItem item ){
		Log.d(TAG, "onOptionsItemSelected");
	    switch(item.getItemId()){
	    	case R.id.menu_resetview:
	    		resetView();
	    		return true;
		    case R.id.menu_creatememo:
		    	createMemo();
		    	return true;
		    case R.id.menu_settings:
		    	showSettings();
		    	return true;
	    	default:
	    		return super.onOptionsItemSelected(item);
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
	private void addMemoList(){ 
		mMemoListFragment = new Fragments.MemoList();
		double[] coords = {sLatLongLocation.latitude, sLatLongLocation.longitude};
		Bundle bundle = new Bundle();
		bundle.putDoubleArray("location", coords);
		mMemoListFragment.setArguments(bundle);

		getFragmentManager().beginTransaction()
			//.replace(R.id.fragment_bottomcontainer, mMemoListFragment, "memoList")
			.commit();
	}
	
	private void createMemo(){
		double latitude  = sLatLongLocation.latitude,
			   longitude = sLatLongLocation.longitude;

		//Intent createMemo = new Intent(MemoMapActivity.this, CreateMemoActivity.class);
		Intent createMemo = new Intent(this, CreateMemoFlipperActivity.class);
		
		Bundle bundle = new Bundle();

		bundle.putDouble("lat", latitude);
		bundle.putDouble("lon", longitude);
		createMemo.putExtras(bundle);

		startActivity(createMemo);	
	}

	public Location getFreshLocation(){
		Log.d(TAG, "getFreshLocation");
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
				if (mLocationManager != null){
					mLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
					if(mLocation != null){
						latitude = mLocation.getLatitude();
						longitude = mLocation.getLongitude();
						sLatLongLocation = new LatLng(latitude, longitude);
					}
				}
			}

			// If the GPS is also available, get coordinates using GPS services.
			if(isGpsEnabled){
				Log.d("geoLocation", "GPS is enabled.");
				if (mLocationManager != null){
					mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
					if(mLocation != null){
						latitude = mLocation.getLatitude();
						longitude = mLocation.getLongitude();
						sLatLongLocation = new LatLng(latitude, longitude);
					}
				}
			}
		}
		return mLocation;
	}

	private void resetView(){
		Log.d(TAG, "resetView");

		if(mLocation != null){
			sLatLongLocation = new LatLng(
					mLocation.getLatitude(), 
					mLocation.getLongitude());
			sGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sLatLongLocation, 12), 1000, null);
		}
	}

	private void showSettings(){
    	Intent settingsIntent = new Intent(this, SettingsActivity.class);
    	startActivity(settingsIntent);
	}

	private void startGps() {
		try{
			mLocationManager	= (LocationManager)this.getSystemService(LOCATION_SERVICE);
			isGpsEnabled		= mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			isNetworkEnabled	= mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_MS_BETWEEN_UPDATES, MIN_DISTANCE_CHANGE, this);	

			mLocation = getFreshLocation();
		}
		catch (Exception e){
			e.printStackTrace();
			Log.e(NOTIFICATION_SERVICE, "GPS Start error");
		}
	}

	private static void viewMemoOnMap(long id){
		Log.d(TAG, "viewMemo (id: " + id + ")");
		Memo m = DataHandler.getInstance(MemoMap.getInstance())
				.getAllClosestMemos(sLatLongLocation).get((int)id);

		if(m != null){
			if (m.getMarker() != null) m.getMarker().showInfoWindow();
			sGoogleMap.animateCamera(CameraUpdateFactory
				.newLatLngZoom(m.getLatLong(), 17), 2500, null);
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
				getListView().setAdapter(memoAdapter);
				
				super.onStart();
			}

			private void showModifyDialog(final long id){
				final Memo m = memoArray.get((int) id);

				AlertDialog.Builder modifyDialogBuilder = new AlertDialog.Builder(sContext)
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
								Intent editIntent = new Intent(MemoMapActivity.sContext, EditMemoActivity.class);
								editIntent.putExtra("id", m.getId());
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
	}
}