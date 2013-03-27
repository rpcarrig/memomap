package com.rpcarrig.memomapa;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.model.LatLng;

public class MemoMapService extends Service implements LocationListener {
	private static final String CLASS = "MemoMapService";
	
	boolean canGetLocation	 = false,
			hasDatabase		 = false,	
			isGpsEnabled	 = false,
			isNetworkEnabled = false;
	static double latitude,
				  longitude;
	int	counter	= 0,
		mId		= 0;

	private Location location = null;
	private LocationManager locationManager = null;
	private NotificationCompat.Builder noteBuilder = null;
	private NotificationManager noteManager = null;		
	
	private final IBinder binder = new GpsBinder();
	
	private static final long
		MIN_DISTANCE_CHANGE		= 10,
		MIN_MS_BETWEEN_UPDATES	= 1000 * 60 * 1;
	
	/**
	 * 
	 */
	public MemoMapService(){
		Log.d(CLASS, "MemoMapService (constructor)");
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		Log.d(CLASS, "onBind");
		return binder; }

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(CLASS, "onCreate");

		noteManager = (NotificationManager)
				getSystemService(Context.NOTIFICATION_SERVICE);
		
		startGps();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(CLASS, "onDestroy");
		noteManager.cancel(0);
		
		if(locationManager != null){
			locationManager.removeUpdates(MemoMapService.this);
		}
	}
	
	@Override
	public void onLocationChanged(Location arg0) {
		Log.d(CLASS, "onLocationChanged");
		if (locationManager != null){
			location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if(location != null){
				latitude = location.getLatitude();
				longitude = location.getLongitude();
			}
		}
		updateNote("Location updated. MemoMap is searching...");
		
		float[] results = { -1, -1, -1 };
		ArrayList<Memo> memoList = DbHandler.getInstance(this).getAllMemos();
		for (Memo m : memoList) {
			Location.distanceBetween(latitude, longitude, m.getLatitude(), 
					m.getLongitude(), results);
			if (results[0] <= m.getRadius())
				updateNote(m.getMemoBody(), m.getMemoTitle());
		}
	}
	
	@Override
	public void onProviderDisabled(String arg0) { }
	@Override
	public void onProviderEnabled(String arg0) { }

	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		Log.d(CLASS, "onStartCommand");
		return Service.START_STICKY;
	}
	
	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) { 
		Log.d(CLASS, "onStatusChanged");
	}
	
	/**
	 * 
	 */	
	public boolean canGetLocation(){
		Log.d(CLASS, "canGetLocation");
		return this.canGetLocation;
	}
	
	public static double distanceTo(LatLng loc){
		Log.d(CLASS, "distanceTo");
		float[] results = {0};
		Location.distanceBetween(latitude, longitude, loc.latitude, loc.longitude, results);
		return results[0];	
	}
	
	public double getLatitude(){
		Log.d(CLASS, "getLatitude");
		if (location != null){ return latitude = location.getLatitude(); }
		else return -1;
	}
	
	public double getLongitude(){
		Log.d(CLASS, "getLongitude");
		if (location != null){ return longitude = location.getLongitude(); }
		else return -1;
	}
		
	public Location getFreshLocation(){
		Log.d(CLASS, "geoLocation");
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
		return location;
	}
	
	public Location getLocation(){ return location; }
	
	public void startGps(){
		Log.d(CLASS, "startGps");
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
		
		Intent intent = new Intent(this, MemoMapActivity.class);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
		noteBuilder =
		        new NotificationCompat.Builder(this)
		        .setContentTitle("MemoMap")
		        .setContentText("Searching for memos...")
		        .setContentIntent(pIntent)
		        //.setOngoing(true)
		        .setSmallIcon(R.drawable.ic_launcher);
	}
	
	public void showSettingsAlert(){
		Log.d(CLASS, "showSettingsAlert");
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		
		alertDialog.setMessage("Your GPS is not available. Please verify that GPS is enabled in your device's settings.");
		alertDialog.setTitle("GPS Error");
		
		alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(intent);
			}
		});
		alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
	}
	
	public void stopGps(){
		Log.d(CLASS, "stopGps");
		locationManager.removeUpdates(this);
	}
	
	public void updateNote(String ticker){
		noteBuilder.setTicker(ticker);
		noteManager.notify(0, noteBuilder.build());		
	}
	
	public void updateNote(String memo, String loc){
		Log.d(CLASS, "updateNote");
		String s = "@" + loc + ": " + memo;
		noteBuilder.setContentText(loc);
		noteBuilder.setContentTitle(memo);
		noteBuilder.setTicker(s);
		noteManager.notify(0, noteBuilder.build());
	}	
	
	public class GpsBinder extends Binder{
		MemoMapService getService(){
			Log.d(CLASS, "getService");
			return MemoMapService.this;
		}
	}
}
