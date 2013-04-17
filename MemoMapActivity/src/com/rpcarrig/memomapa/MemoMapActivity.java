/**
 * 
 */

package com.rpcarrig.memomapa;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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

	boolean mCanGoBack = false, 
			circlesExist	 = false,
			listExists		 = false,
			canGetLocation	 = false,
			isGpsEnabled	 = false,
			isNetworkEnabled = false;

	ArrayList<Memo> mMemoArray;
	GoogleMap mGoogleMap;
	LatLng mLatLong;
	ListView mListView;
	Location mLocation;
	LocationManager mLocManager = null;
	Marker mMarker;
	MemoAdapter mMemoAdapter;
	ProgressDialog mDialog;
	Runnable drawMemoMarkers = new Runnable(){
		public void run(){
			for(Memo m : DataHandler.getInstance(MemoMap.getInstance()).getAllMemos()){
				Circle circ = mGoogleMap.addCircle(new CircleOptions()
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
				m.setMarker(mGoogleMap.addMarker(markOpt));
			}
			circlesExist = true;
		}
	};
	SharedPreferences mSharedPreferences;
	ViewFlipper mViewFlipper;

	@Override
	public void onBackPressed(){
		if(mCanGoBack){
			mCanGoBack = false;
			mViewFlipper.setInAnimation(this, R.anim.slide_in_fromright);
			mViewFlipper.setOutAnimation(this, R.anim.slide_out_toleft);
			mViewFlipper.showPrevious();
			mViewFlipper.setInAnimation(this, R.anim.slide_in_fromleft);
			mViewFlipper.setOutAnimation(this, R.anim.slide_out_toright);
			resetView();
		}
		else finish();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

		setContentView(R.layout.activity_memomap);		

		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(MemoMap.getInstance());

		mDialog = new ProgressDialog(this);
		mDialog.setTitle(R.string.loading);
		mDialog.setMessage("Waiting for location fix...");
		mDialog.setCancelable(false);
		mDialog.show();

		mGoogleMap = ((MapFragment)getFragmentManager().findFragmentById(R.id.memo_map)).getMap();

		int mMapType = Integer.parseInt(mSharedPreferences.getString("prefMapType", "2"));
		mGoogleMap.setMapType(mMapType);
		mGoogleMap.setMyLocationEnabled(true);
		mGoogleMap.setOnMapLongClickListener(new OnMapLongClickListener(){
			@Override
			public void onMapLongClick(LatLng pos) {
				//mGoogleMap.addMarker(new MarkerOptions().position(pos));
				Intent mMapClick = new Intent(MemoMapActivity.this, CreateMemoFlipperActivity.class);
				mMapClick.putExtra("lat", pos.latitude);
				mMapClick.putExtra("lon", pos.longitude);	
				startActivity(mMapClick);
			}
		});

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
				showModifyDialog(m.getId());
				return false;
			}	
		});
		
		mViewFlipper = (ViewFlipper) findViewById(R.id.memo_viewflipper);
		mViewFlipper.setInAnimation(this, R.anim.slide_in_fromleft);
		mViewFlipper.setOutAnimation(this, R.anim.slide_out_toright);

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
		redrawMap();

		if(listExists) repopulateList();

		int mMapType = Integer.parseInt(mSharedPreferences.getString("prefMapType", "2"));
		mGoogleMap.setMapType(mMapType);
		super.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) { }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_memomap, menu);
		return true;
	}

	@Override
	public void onLocationChanged(Location location) {
		Log.d(TAG, "onLocationChanged");
		mDialog.cancel();

		mLocation = location;
		mLatLong = new LatLng(location.getLatitude(), location.getLongitude());
		mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLatLong, 12));

		if (!listExists){
			mMemoArray = 
					DataHandler.getInstance(MemoMap.getInstance()).getAllClosestMemos(mLatLong);
			mMemoAdapter = new MemoAdapter(MemoMap.getInstance(), 0, mMemoArray, mLatLong);
			mListView.setAdapter(mMemoAdapter);
			listExists = true;
		}
	}

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

	private void createMemoHere(){
		double latitude  = mLatLong.latitude,
			   longitude = mLatLong.longitude;

		//Intent createMemo = new Intent(MemoMapActivity.this, CreateMemoActivity.class);
		Intent createMemo = new Intent(this, CreateMemoFlipperActivity.class);

		Bundle bundle = new Bundle();

		bundle.putDouble("lat", latitude);
		bundle.putDouble("lon", longitude);
		createMemo.putExtras(bundle);

		startActivity(createMemo);	
	}

	private void geocode(String address) throws IOException{
		List<Address> mFound = new Geocoder(this)
				.getFromLocationName(address, 2);
		if (mFound.size() != 0) {
			final LatLng mResult = 
					new LatLng(mFound.get(0).getLatitude(), mFound.get(0).getLongitude());
			mMarker = mGoogleMap.addMarker(new MarkerOptions().position(mResult).title(address));
			mMarker.showInfoWindow();
			mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mResult, 15));
		} else {
			Toast.makeText(this, R.string.tryagain, Toast.LENGTH_SHORT).show();
			searchByAddress();
		}
	}

	public Location getFreshLocation(){
		double latitude, longitude;
		if( !isGpsEnabled && !isNetworkEnabled){
			//no GPS or network
			Log.e(CONNECTIVITY_SERVICE, "no GPS or network");
		}
		else{
			this.canGetLocation = true;

			// If the network provider is available, get its location first.
			if(isNetworkEnabled){
				if (mLocManager != null){
					mLocation = 
							mLocManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
					if(mLocation != null){
						latitude = mLocation.getLatitude();
						longitude = mLocation.getLongitude();
						mLatLong = new LatLng(latitude, longitude);
					}
				}
			}

			// If the GPS is also available, get coordinates using GPS services.
			if(isGpsEnabled){
				if (mLocManager != null){
					mLocation = mLocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
					if(mLocation != null){
						latitude = mLocation.getLatitude();
						longitude = mLocation.getLongitude();
						mLatLong = new LatLng(latitude, longitude);
					}
				}
			}
		}
		return mLocation;
	}

	private void redrawMap(){
		mGoogleMap.clear();
		new Thread(drawMemoMarkers).run();
	}

	private void repopulateList(){
		mMemoArray = DataHandler.getInstance(MemoMap.getInstance()).getAllClosestMemos(mLatLong);
		mMemoAdapter.clear();
		mMemoAdapter.addAll(mMemoArray);
		mMemoAdapter.notifyDataSetChanged();
		redrawMap();
	}

	private void resetView(){
		if(mLatLong != null){
			mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLong, 12), 1000, null);
		}
	}

	private void searchByAddress(){
		View view = getLayoutInflater().inflate(R.layout.dialog_searchaddr, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(view);
		final EditText userInput = (EditText)view.findViewById(R.id.address_entry);
		builder.setPositiveButton(R.string.search, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog,int which) {
					String receiveAddress = userInput.getText().toString();
					try {
						geocode(receiveAddress);
					} catch (IOException e) {
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

	private void showSettings(){
    	Intent settingsIntent = new Intent(this, SettingsActivity.class);
    	startActivity(settingsIntent);
	}

	private void startGps() {
		try{
			mLocManager = (LocationManager)this.getSystemService(LOCATION_SERVICE);
			isGpsEnabled = mLocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			isNetworkEnabled = mLocManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			mLocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
					MIN_MS_BETWEEN_UPDATES, MIN_DISTANCE_CHANGE, this);	
			mLocation = getFreshLocation();
		}
		catch (Exception e){
			e.printStackTrace();
			Log.e(NOTIFICATION_SERVICE, "GPS Start error");
		}
	}

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
						mEdit.putExtra("lat", mLatLong.latitude);
						mEdit.putExtra("lon", mLatLong.longitude);	
						startActivity(mEdit);
					}
				})
			.setTitle(m.getLocationName());
		AlertDialog modifyMemo = modifyDialogBuilder.create();
		modifyMemo.show();
	}

	private void viewMemo(long id){
		
		Memo m = DataHandler.getInstance(MemoMap.getInstance())
				.getAllClosestMemos(mLatLong).get((int)id);
		TextView mLocationName = (TextView)findViewById(R.id.view_locname),
			mMemoBody = (TextView)findViewById(R.id.view_body),
			mDateView = (TextView)findViewById(R.id.view_date),
			mLat = (TextView)findViewById(R.id.view_lat),
			mLon = (TextView)findViewById(R.id.view_long),
			mRadius = (TextView)findViewById(R.id.view_radius);

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