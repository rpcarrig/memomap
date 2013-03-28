/**
 * 
 */

package com.rpcarrig.memomapa;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.rpcarrig.memomapa.MemoMapService.GpsBinder;

public class MemoMapActivity extends Activity implements LocationListener, OnSeekBarChangeListener {
	private static final String TAG = "MemoMapActivity";
	private static final long MIN_DISTANCE_CHANGE	 = 10,
							  MIN_MS_BETWEEN_UPDATES = 1000 * 60 * 1;
	
	private MemoMapService memoMapService;
	private LatLng latLongLocation;
	private static ArrayList<Memo> memoList;
	private static Circle circle;
	private static GoogleMap googleMap;
	private static Location lastLocation,
							location;
	private LocationManager locationManager = null;
	private static Marker marker;
	private static Memo tempMemo;
	private static String receiveAddress;

	boolean gpsBound         = false,
			canGetLocation	 = false,
			isGpsEnabled	 = false,
			isNetworkEnabled = false;
	Double latitude,
		   longitude;
	MemoListFragment memoListFragment = null;
	Runnable drawMemoMarkers = new Runnable(){
		public void run(){
			for(Memo m : DataHandler.getInstance(MemoMap
					.getInstance()).getAllMemos()){
				circle = googleMap.addCircle(m.getCircleOptions());
				marker = googleMap.addMarker(m.getMarkerOptions());
				m.setMarker(marker);
			}
		}
	};
	/**
	ServiceConnection serviceConnection = new ServiceConnection(){
		@Override
		public void onServiceConnected(ComponentName name,
				IBinder service){
			Log.d(TAG, "ServiceConnection: " + name);
			GpsBinder binder = (GpsBinder)service;
			
			memoMapService = binder.getService();
			gpsBound = true;
			
			location = memoMapService.getFreshLocation();
			latLongLocation = new LatLng(location.getLatitude(), location.getLongitude());
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.d(TAG, "ServiceConnection: onServiceDisconnected");
			gpsBound = false;
		}
	};
	*/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_memomap);
		
		//Intent gpsIntent = new Intent(this, MemoMapService.class);
		//bindService(gpsIntent, serviceConnection, Context.BIND_AUTO_CREATE);
		
		if(findViewById(R.id.fragment_topcontainer) != null){
			MapFragment mapFragment = new MapFragment();
			getFragmentManager().beginTransaction()
				.add(R.id.fragment_topcontainer, mapFragment, "map")
				.commit();
		}
		
		startGps();
				
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
		
		if(memoListFragment == null){
			memoListFragment = new MemoListFragment();
			double[] coords = {location.getLatitude(), location.getLongitude()};
			Bundle bundle = new Bundle();
			bundle.putDoubleArray("location", coords);
			memoListFragment.setArguments(bundle);
			
			
			
			if(findViewById(R.id.fragment_bottomcontainer) != null){
				getFragmentManager().beginTransaction()
					.add(R.id.fragment_bottomcontainer, memoListFragment, "memoList")
					.commit();
			}
		}
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
		//if(gpsBound) unbindService(serviceConnection);
	}
	
	@Override
	public void onLocationChanged(Location location) {
		Log.d(TAG, "onLocationChanged");
		if (locationManager != null){
			location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if(location != null){
				latitude = location.getLatitude();
				longitude = location.getLongitude();
			}
		}
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
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		Log.d(TAG, "onProgressChanged");		
	}

	@Override
	public void onProviderDisabled(String provider) { }

	@Override
	public void onProviderEnabled(String provider) { }
	
	@Override
	public void onResume(){	
		Log.d(TAG, "onResume"); 
		super.onResume();
	}
	
	@Override
	public void onStart(){ 
		Log.d(TAG, "onStart");
		super.onStart();
		
		googleMap = ((MapFragment)getFragmentManager().findFragmentByTag("map")).getMap();
		googleMap.setMyLocationEnabled(true);
		
		resetView();
		new Thread(drawMemoMarkers).run();
	}
	
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) { }
	
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) { }
	
	/**
	 * 
	 */
	public void continueMemo(View view){
		EditText editText = (EditText)findViewById(R.id.newmemo_body);
		if(editText != null) tempMemo.setMemoBody(editText.getText().toString());
		else Log.d(TAG, "Can't find EditText.");
		
		googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLongLocation, 11));
		marker.setTitle(tempMemo.getMemoBody());
		marker.showInfoWindow();
		
		
		getFragmentManager().beginTransaction()
			.setCustomAnimations(
				R.animator.slide_in_frombottom, R.animator.slide_out_totop,
				R.animator.slide_in_fromtop, R.animator.slide_out_tobottom)
			.addToBackStack(null)
			.replace(R.id.fragment_bottomcontainer, new MemoLocFragment())
			.commit();
	}
	
	private void createMemo(){
		Log.d(TAG, "createMemo");
		tempMemo = new Memo();
		
		marker = googleMap.addMarker(new MarkerOptions()
				.position(latLongLocation));
		
    	getFragmentManager().beginTransaction()
    		.setCustomAnimations(
				R.animator.slide_in_fromleft, R.animator.slide_out_toright,
				R.animator.slide_in_fromright, R.animator.slide_out_toleft)
			.addToBackStack(null)
			.replace(R.id.fragment_bottomcontainer, new MemoBodyFragment())
			.commit();
	}
	
	public void defaultClick(View view){
		SeekBar seekBar = (SeekBar)findViewById(R.id.seekBar1);
		seekBar.setOnSeekBarChangeListener(this);
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
	
	public Location getFreshLocation(){
		lastLocation = location;
		
		Log.d(TAG, "geoLocation");
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
					}
				}
			}
		}
		
		if(isBetterLocation(location, lastLocation)) return location;
		else return lastLocation;
	}
	
	public void openMemo(Memo memo){}
	
	private void resetView(){
		Log.d(TAG, "resetView");
		if(circle != null) circle.remove();
		if(marker != null) marker.remove();
		if(location != null){
			latLongLocation = new LatLng(
					location.getLatitude(), 
					location.getLongitude());
			googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLongLocation, 12));
		}
		//if(memoListFragment.getListView().isClickable()){
			//memoListFragment.getListView().setOnItemClickListener(memoItemClick);
		//}
	}
	
	public void saveMemoClick(View view){
		CheckBox fave = (CheckBox)findViewById(R.id.checkbox);
		SeekBar seekBar = (SeekBar)findViewById(R.id.seekBar1);
		TextView text = (TextView)findViewById(R.id.newMemoLocation);
		if(text != null) tempMemo.setMemoTitle(text.getText().toString());
		else Log.d(TAG, "Can't find memo location.");
		if(fave != null) { boolean b = fave.isChecked(); }
		else Log.d(TAG, "Can't find fave box.");
		if(seekBar != null) { tempMemo.setRadius(seekBar.getProgress()); }
		tempMemo.setLatitude(location.getLatitude());
		tempMemo.setLongitude(location.getLongitude());
		
		DataHandler.getInstance(MemoMap.getInstance()).addMemo(tempMemo);

		getFragmentManager().popBackStack(getFragmentManager().getBackStackEntryCount(), 0);
    	getFragmentManager().beginTransaction()
			.setCustomAnimations(
					R.animator.slide_in_fromright, R.animator.slide_out_toleft,
					R.animator.slide_in_fromleft, R.animator.slide_out_toright)
			.replace(R.id.fragment_bottomcontainer, new MemoListFragment(), "memoList")
			.commit();
    	
    	ArrayList<Memo> memoArray = DataHandler.getInstance(MemoMap.getInstance()).getAllMemos();		
		MemoAdapter memoAdapter = new MemoAdapter(MemoMap.getInstance(), 0, 
				memoArray, latLongLocation);
    	((MemoListFragment)getFragmentManager().findFragmentByTag("memoList"))
    		.setListAdapter(memoAdapter);
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
	
	private static void viewMemoOnMap(long id){
		Log.d(TAG, "viewMemo (id: " + id + ")");
		int i = (int)id;
		Memo m = memoList.get(i);
		m.getMarker().showInfoWindow();
		googleMap.animateCamera(CameraUpdateFactory
				.newLatLngZoom(m.getLatLong(), 17));
	}
	
	/**
	 * 
	 *
	 *
	 */	
	
	public static class MemoBodyFragment extends Fragment {
		private final String TAG = "NewMemoBodyFragment";
		public MemoBodyFragment(){ }
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, 
				Bundle savedInstanceState) {
			Log.d(TAG, "onCreateView");
			return inflater.inflate(R.layout.fragment_newmemobody, container, false);
		}
	}
	
	public static class MemoLocFragment extends Fragment{
		final String CLASS = "MemoLocFragment";
		public MemoLocFragment(){ }

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, 
				Bundle savedInstanceState) {
			Log.d(CLASS, "onCreateView");
			return inflater.inflate(R.layout.fragment_newmemoloc, container, false);
		}		
	}
	
	public static class OpenMemoFragment extends Fragment{
		final String CLASS = "OpenMemoFragment";
		public OpenMemoFragment(){ }
		
		@Override 
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			Log.d(CLASS, "onCreateView");
			return inflater.inflate(R.layout.dialog_viewmemo, container, false);
		}
	}
	
	/** Determines whether one Location reading is better than the current Location fix
	  * @param location  The new Location that you want to evaluate
	  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	  */
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
		Log.d(TAG, "isBetterLocation");
		Log.d(TAG, "Comparing (" + location.getLongitude() + ", " + location.getLatitude() + ")");
		
		final int TWO_MINUTES = 1000 * 60 * 2;
	    if (currentBestLocation == null) {
	        // A new location is always better than no location
	        return true;
	    }
	    else Log.d(TAG, "With (" + currentBestLocation.getLongitude() + ", " + currentBestLocation.getLatitude() + ")");

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
	    boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
	            currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}
}
