/**
 * Provides the main user interface for the MemoMap application.
 * 
 * @author  Ryan P. Carrigan, Drew Markle
 * @version 
 */

package com.rpcarrig.memomapa;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MemoMapActivity extends Activity implements LocationListener {
    private static final String TAG = "MemoMapActivity";
	private static final long MIN_DISTANCE_CHANGE	 = 10,
							  MIN_MS_BETWEEN_UPDATES = 1000 * 10 * 1;
	ArrayList<Memo>		mMemoArray;
	boolean 			mCanGoBack        = false, 
						mCirclesExist     = false,
						mListExists       = false,
						mCanGetLocation   = false,
						mIsGpsEnabled     = false,
						mIsNetworkEnabled = false;
	GoogleMap			mGoogleMap;
	LatLng				mLatLongLocation;
	ListView 			mListView;
	Location 			mLocation;
	LocationManager 	mLocManager = null;
	Marker 				mMarker;
	MemoAdapter 		mMemoAdapter;
	NetworkInfo 		mNetworkInfo;
	ProgressDialog 		mDialog;
	SharedPreferences 	mSharedPreferences;
	ViewFlipper 		mViewFlipper;
	
	Runnable drawMemoMarkers = new Runnable(){
		/* Draws the markers for each memo onto the Google map. */
		public void run(){
			for (Memo m : DataHandler.getInstance(MemoMap.getInstance()).getAllMemos()) {
				Circle circle = mGoogleMap.addCircle(new CircleOptions()
					.center(m.getLatLong())
					.fillColor   (0x25AA0000)
					.strokeColor (0xFFAA0000)
					.strokeWidth (2)
					.radius      (m.getRadius()));
				m.setCircle(circle);

				MarkerOptions markerOptions = new MarkerOptions()
					.icon(BitmapDescriptorFactory.fromResource(determineIcon(m)))
					.position (m.getLatLong())
					.snippet  (m.getLocationName())
					.title    (m.getMemoBody());
				m.setMarker(mGoogleMap.addMarker(markerOptions));
			}
			mCirclesExist = true;
		}
	};
	
	/** Captures the Back button to control ViewFlipper animation. */
	@Override
	public void onBackPressed(){
		if(mCanGoBack){
			mCanGoBack = false;
			mViewFlipper.setInAnimation (this, R.anim.slide_in_fromright);
			mViewFlipper.setOutAnimation(this, R.anim.slide_out_toleft);
			mViewFlipper.showPrevious();
			mViewFlipper.setInAnimation (this, R.anim.slide_in_fromleft);
			mViewFlipper.setOutAnimation(this, R.anim.slide_out_toright);
			resetView();
		}
		else finish();
	}

	/** Runs first in the Activity life cycle */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

		setContentView(R.layout.activity_memomap);
		
		if(!isInternetReachable()){
			//TODO: deal with this
		}
		
		mGoogleMap = ((MapFragment)getFragmentManager().findFragmentById(R.id.memo_map)).getMap();
		
		startGps();
		initDialog();
		getFreshLocation();
		
		Intent serviceIntent = new Intent(this, MemoMapService.class);
		startService(serviceIntent);
	}
	
	@Override
	public void onPause(){
		super.onPause();
	}

	@Override
	public void onStart(){ 
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(MemoMap.getInstance());
		mViewFlipper = (ViewFlipper) findViewById(R.id.memo_viewflipper);
		mViewFlipper.setInAnimation(this, R.anim.slide_in_fromleft);
		mViewFlipper.setOutAnimation(this, R.anim.slide_out_toright);
		
		initList();
		initMap();
		super.onStart();
	}

	/** Runs third in the Activity life cycle. */
	@Override
	public void onResume(){	
		redrawMap();

		if(mListExists) repopulateList();

		int mMapType = Integer.parseInt(mSharedPreferences.getString("prefMapType", "2"));
		mGoogleMap.setMapType(mMapType);
		super.onResume();
	}
	
	/** Runs when the instance state is saved. */
	@Override
	protected void onSaveInstanceState(Bundle outState) { }

	/** Inflates the options menu. */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_memomap, menu);
		return true;
	}

	/** Runs whenever the LocationListener received a location changed broadcast. */
	@Override
	public void onLocationChanged(Location location) {
		Log.d(TAG, "onLocationChanged");
		mDialog.cancel();

		mLocation = location;
		mLatLongLocation = new LatLng(location.getLatitude(), location.getLongitude());
		mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLatLongLocation, 12));

		if (!mListExists){
			mMemoArray = DataHandler.getInstance(MemoMap.getInstance())
							.getAllClosestMemos(mLatLongLocation);
			mMemoAdapter = new MemoAdapter(MemoMap.getInstance(), 0, mMemoArray, mLatLongLocation);
			mListView.setAdapter(mMemoAdapter);
			mListExists = true;
		}
		else repopulateList();
	}

	/** Routes the actions of menu item selections. */
	@Override
	public boolean onOptionsItemSelected( MenuItem item ){
	    switch(item.getItemId()){
	    	case R.id.menu_resetview:
	    		resetView();
	    		return true;
		    case R.id.menu_creatememo:
		    	return true;
		    case R.id.menu_createhere:
		    	createMemoHere();
		    	return true;
		    case R.id.menu_searchaddr:
		    	searchByAddress();
		    	return true;
		    case R.id.menu_choosefave:
		    	chooseFave();
		    	return true;
		    case R.id.menu_settings:
		    	showSettings();
		    	return true;
	    	default:
	    		return super.onOptionsItemSelected(item);
	    }
	}

	/** Called when the GPS provider is disabled by the user. */
	@Override
	public void onProviderDisabled(String provider) { }

	/** Called when the GPS provider is enabled by the user. */
	@Override
	public void onProviderEnabled(String provider) { }

	/* Called when the GPS provider status changes. */
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {}

	/* Checks whether Internet services are reachable. */
	private boolean isInternetReachable(){
		mNetworkInfo = ((ConnectivityManager)getApplicationContext()
				.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		
		if (mNetworkInfo != null && mNetworkInfo.isConnected()) return true;
		else return false;
	}
	
	/* Displays a dialog populated by entries in the fave database. */
	private void chooseFave(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final List<Favorite> favorites =
				DataHandler.getInstance(MemoMap.getInstance()).getAllFaves();
		Log.d(TAG, "Size: " + favorites.size());
		
		if (favorites != null) {
			CharSequence[] results = faveResults(favorites);
			builder.setItems(results, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent mFaveClick = new Intent(MemoMapActivity.this, 
							CreateMemoActivity.class);
					mFaveClick.putExtra("lat", favorites.get(which).getLatitude());
					mFaveClick.putExtra("lon", favorites.get(which).getLongitude());
					mFaveClick.putExtra("loc", favorites.get(which).getFaveName());
					startActivity(mFaveClick);
				}
			});
			builder.setTitle(R.string.favorites);
			builder.create().show();
		}
	}

	/* Called when the user chooses to create a memo at the current location. */
	private void createMemoHere() {
		mLocation = mGoogleMap.getMyLocation();
		if(mLocation != null) {
			
			Intent createMemo = new Intent(this, CreateMemoActivity.class);
			Bundle bundle = new Bundle();
			bundle.putDouble("lat", mLocation.getLatitude());
			bundle.putDouble("lon", mLocation.getLongitude());
			try {
				bundle.putString("loc", geocodeReverse(mLocation));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			createMemo.putExtras(bundle);
	
			startActivity(createMemo);
		}
	}
	
	/* Changes the icon based on whether a memo is public or private. */
	private int determineIcon(Memo m){
		String localId = DataHandler.getAndroidId();
		if(m.isPublic()){
			if(m.getAndroidId().equals(localId)) return R.drawable.pin4_green;
			else return R.drawable.pin4_blue;
		}
		else return R.drawable.pin4_yellow;
	}
	
	/* Translates a list of Favorites into a CharSequence array. */
	private CharSequence[] faveResults(List<Favorite> list){
		CharSequence[] results = new CharSequence[list.size()];
		int i = 0;
		for(Favorite f : list){
			String s = f.getFaveName();
			Log.d(TAG, "Name:" + s);
			results[i] = s;
			i++;
		}
		return results;
	}

	/** Converts a search term into mappable coordinates. */
	private void geocode(String address) throws IOException{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);	
		final List<Address> mFound = new Geocoder(this, Locale.getDefault())
				.getFromLocationName(address, 3);
		if (mFound.size() != 0) {
			CharSequence[] results = geocodeResults(mFound);
			builder.setItems(results, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent mGeoClick = new Intent(MemoMapActivity.this, 
							CreateMemoActivity.class);
					mGeoClick.putExtra("lat", mFound.get(which).getLatitude());
					mGeoClick.putExtra("lon", mFound.get(which).getLongitude());	
					mGeoClick.putExtra("loc", mFound.get(which).getAddressLine(0));
					startActivity(mGeoClick);
				}
			});
			builder.setTitle(address);
			builder.create().show();
		} else {
			Toast.makeText(this, R.string.tryagain, Toast.LENGTH_SHORT).show();
			searchByAddress();
		}
	}
	
	/* Converts map coordinates into an address. */
	private String geocodeReverse(Location loc) throws IOException{
		double lat = loc.getLatitude();
		double lon = loc.getLongitude();
		List<Address> address = new Geocoder(this, Locale.getDefault())
			.getFromLocation(lat, lon, 1);
		return address.get(0).getAddressLine(0);
	}
	
	/* Translates an Address list into a CharSequence array. */
	private CharSequence[] geocodeResults(List<Address> list){
		CharSequence[] results = new CharSequence[list.size()];
		int i = 0;
		for(Address a : list){
			String s = a.getAddressLine(0);
			int j = 1;
			while(a.getAddressLine(j) != null){
				s = s.concat(", " + a.getAddressLine(j));
				j++;
			}
			results[i] = s;
			i++;
		}
		return results;
	}

	/* Sets the Location and LatLongLocation to the last known location. */
	public Location getFreshLocation(){
		if( !mIsGpsEnabled && !mIsNetworkEnabled){
			Log.e(CONNECTIVITY_SERVICE, "no GPS or network");
		}
		else{
			mCanGetLocation = true;

			// If the network provider is available, get its location first.
			if(mIsNetworkEnabled){
				if (mLocManager != null){
					mLocation = mLocManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				}
			}

			// If the GPS is also available, get coordinates using GPS services.
			if(mIsGpsEnabled){
				if (mLocManager != null){
					mLocation = mLocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				}
			}
		}
		return mLocation;
	}
	
	/* Initializes the dialog. */
	private void initDialog(){
		mDialog = new ProgressDialog(this);
		mDialog.setTitle(R.string.loading);
		mDialog.setMessage("Waiting for location fix...");
		mDialog.setCancelable(false);
		mDialog.show();
	}
	
	/* Initializes the ListView. */
	private void initList(){
		mListView = (ListView) findViewById(R.id.memo_list);
		mListView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				mCanGoBack = true;
				mViewFlipper.showNext();
				viewMemo(arg3);
			}
		});
		mListView.setOnItemLongClickListener(new OnItemLongClickListener(){
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Memo m = mMemoArray.get((int)arg3);
				if(!m.isPublic()) showModifyDialog(m.getId());
				return false;
			}	
		});
	}
	
	/* Initializes the Google map. */
	private void initMap(){
		int mMapType = Integer.parseInt(mSharedPreferences.getString("prefMapType", "2"));
		
		mGoogleMap.setMapType(mMapType);
		mGoogleMap.setMyLocationEnabled(true);
		mGoogleMap.setOnMapLongClickListener(new OnMapLongClickListener(){
			@Override
			public void onMapLongClick(LatLng pos) {
				//mGoogleMap.addMarker(new MarkerOptions().position(pos));
				Intent mMapClick = new Intent(MemoMapActivity.this,
											  CreateMemoActivity.class);
				mMapClick.putExtra("lat", pos.latitude);
				mMapClick.putExtra("lon", pos.longitude);	
				startActivity(mMapClick);
			}
		});
	}

	/* Clears the GoogleMap and redraws the markers. */
	private void redrawMap(){
		mGoogleMap.clear();
		new Thread(drawMemoMarkers).run();
	}

	/* Updates mMemoArray with the closest memos then resets mMemoAdapter and redraws map. */
	private void repopulateList(){
		mMemoArray = DataHandler.getInstance(MemoMap.getInstance())
				.getAllClosestMemos(mLatLongLocation);
		mMemoAdapter.clear();
		mMemoAdapter.addAll(mMemoArray);
		mMemoAdapter.notifyDataSetChanged();
		redrawMap();
	}

	/* Resets the camera to the default zoom level and position. */
	private void resetView(){
		mLocation = mGoogleMap.getMyLocation();
		if(mLatLongLocation != null){
			mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLongLocation, 12), 
									 1000, null);
		}
		Toast.makeText(MemoMap.getInstance(), R.string.view_reset, Toast.LENGTH_SHORT).show();
	}

	/* Displays the search dialog to get terms to pass to the geocoder. */
	private void searchByAddress(){
		View view = getLayoutInflater().inflate(R.layout.dialog_searchaddr, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(view);
		final EditText userInput = (EditText)view.findViewById(R.id.address_entry);
		builder.setPositiveButton(R.string.search, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog,int which) {
					String receiveAddress = userInput.getText().toString();
					try { geocode(receiveAddress); }
					catch (IOException e) {
						e.printStackTrace();
					}
				}
			})
			.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {}
			});
		builder.create().show();
	}

	/* Starts the Settings Activity. */
	private void showSettings(){
    	Intent settingsIntent = new Intent(this, SettingsActivity.class);
    	startActivity(settingsIntent);
	}
	
	/* Initializes the GPS services. */
	private void startGps() {
		try{
			mLocManager = (LocationManager)this.getSystemService(LOCATION_SERVICE);
			mIsGpsEnabled = mLocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			mIsNetworkEnabled = mLocManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			mLocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
					MIN_MS_BETWEEN_UPDATES, MIN_DISTANCE_CHANGE, this);	
			mLocation = getFreshLocation();
		}
		catch (Exception e){
			e.printStackTrace();
			Log.e(NOTIFICATION_SERVICE, "GPS Start error");
		}
	}

	/* Displays the dialog for modifying a memo. */
	private void showModifyDialog(int id){
		final Memo m = DataHandler.getInstance(MemoMap.getInstance()).getMemo(id);
		AlertDialog.Builder modifyDialogBuilder = new AlertDialog.Builder(this)
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
						DataHandler.getInstance(MemoMap.getInstance()).deleteMemo(m);
						repopulateList();
					}
				})
				.setPositiveButton("Edit", 
						new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Log.d(TAG, "Edit Memo");
						Intent mEdit = new Intent(MemoMapActivity.this, EditMemoActivity.class);
						mEdit.putExtra("id", m.getId());
						mEdit.putExtra("lat", mLatLongLocation.latitude);
						mEdit.putExtra("lon", mLatLongLocation.longitude);	
						startActivity(mEdit);
					}
				})
			.setTitle(m.getLocationName());
		AlertDialog modifyMemo = modifyDialogBuilder.create();
		modifyMemo.show();
	}

	/* Displays a memo and its fields. */
	private void viewMemo(long id){
		TextView mLocationName = (TextView)findViewById(R.id.view_locname),
			     mMemoBody     = (TextView)findViewById(R.id.view_body),
			     mDateView     = (TextView)findViewById(R.id.view_date),
			     mLat          = (TextView)findViewById(R.id.view_lat),
			     mLon          = (TextView)findViewById(R.id.view_long),
			     mRadius       = (TextView)findViewById(R.id.view_radius);

		Memo m = DataHandler.getInstance(MemoMap.getInstance())
				     .getAllClosestMemos(mLatLongLocation).get((int)id);
		if(m != null){
			if (m.getMarker() != null) m.getMarker().showInfoWindow();
			mGoogleMap.animateCamera(CameraUpdateFactory
					.newLatLngZoom(m.getLatLong(), 17), 2500, null);
			mLocationName.setText(m.getLocationName());
			mMemoBody.setText(m.getMemoBody());
			mDateView.setText(m.getMemoDate());
			mLat.setText("" + m.getLatitude());
			mLon.setText("" + m.getLongitude());
			mRadius.setText("" + m.getRadius());
		}
	}
}