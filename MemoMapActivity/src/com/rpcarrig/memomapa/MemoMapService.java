package com.rpcarrig.memomapa;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Notification;
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
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class MemoMapService extends Service implements LocationListener {
	private static final String TAG = "MemoMapService";
	
	boolean canGetLocation	 = false,
			isGpsEnabled	 = false,
			isNetworkEnabled = false;
	static double latitude,
				  longitude;
	int	counter	= 0,
		mId		= 0;
	AlertDialog dialog;
	private Location location = null,
					 lastLocation = null;
	private LocationManager locationManager = null;
	private Notification.Builder ongoingNote,
								 newMemoNote;
	private NotificationManager noteManager = null;		
	
	private final IBinder binder = new GpsBinder();
	
	private static final long
		MIN_DISTANCE_CHANGE		= 10,
		MIN_MS_BETWEEN_UPDATES	= 1000 * 60 * 1;
	
	/**
	 * 
	 */
	public MemoMapService(){
		Log.d(TAG, "MemoMapService (constructor)");
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "onBind");
		return binder; }

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate");

		noteManager = (NotificationManager)
				getSystemService(Context.NOTIFICATION_SERVICE);
		startGps();
		
		startForeground(1, ongoingNote.getNotification());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");
		noteManager.cancel(0);
		
		if(locationManager != null){
			locationManager.removeUpdates(MemoMapService.this);
		}
	}
	
	@Override
	public void onLocationChanged(Location arg0) {
		Log.d(TAG, "onLocationChanged");
		if (locationManager != null){
			location = locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if(location != null){
				latitude = location.getLatitude();
				longitude = location.getLongitude();
			}
		}
		ongoingNote.setWhen(0);
		ongoingNote.setTicker("Location updated. MemoMap is searching...");
		noteManager.notify(1, ongoingNote.getNotification());	
		
		float[] results = { -1, -1, -1 };
		ArrayList<Memo> memoList = DataHandler.getInstance(this).getAllMemos();
		for (Memo m : memoList) {
			Location.distanceBetween(latitude, longitude, m.getLatitude(), 
					m.getLongitude(), results);
			if (results[0] <= m.getRadius())
				updateNote(m);
		}
	}
	
	@Override
	public void onProviderDisabled(String arg0) { }
	@Override
	public void onProviderEnabled(String arg0) { }

	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		Log.d(TAG, "onStartCommand");
		return Service.START_STICKY;
	}
	
	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) { 
		Log.d(TAG, "onStatusChanged");
	}
	
	/**
	 * 
	 */	
	public boolean canGetLocation(){
		Log.d(TAG, "canGetLocation");
		return this.canGetLocation;
	}
	
	public static double distanceTo(LatLng loc){
		Log.d(TAG, "distanceTo");
		float[] results = {0};
		Location.distanceBetween(latitude, longitude, loc.latitude, loc.longitude, results);
		return results[0];	
	}
	
	public double getLatitude(){
		Log.d(TAG, "getLatitude");
		if (location != null){ return latitude = location.getLatitude(); }
		else return -1;
	}
	
	public double getLongitude(){
		Log.d(TAG, "getLongitude");
		if (location != null){ return longitude = location.getLongitude(); }
		else return -1;
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
	
	public Location getLocation(){ return location;	}
	
	public void startGps(){
		Log.d(TAG, "startGps");
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
		newMemoNote = new Notification.Builder(this);
		ongoingNote =
		        new Notification.Builder(this)
		        .setContentTitle("MemoMap")
		        .setContentText("Searching for memos...")
		        .setContentIntent(pIntent)
		        .setSmallIcon(R.drawable.ic_launcher);
	}
	
	public void showSettingsAlert(){
		Log.d(TAG, "showSettingsAlert");
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
		Log.d(TAG, "stopGps");
		locationManager.removeUpdates(this);
	}
	
	public void updateNote(Memo m){
		Log.d(TAG, "updateNote");
		String s = "@" + m.getLocationName() + ": " + m.getMemoBody();
		Intent intent = new Intent(this, OpenMemoActivity.class);
		intent.putExtra("id", m.getId());
		
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
		
		newMemoNote.setContentIntent(pIntent);
		newMemoNote.setContentText(m.getLocationName());
		newMemoNote.setContentTitle(m.getMemoBody());
		newMemoNote.setLights(0xFFFFFF00, 500, 500);
		newMemoNote.setTicker(s);
		newMemoNote.setVibrate(new long[]{100, 200, 100, 200});
		newMemoNote.setSmallIcon(R.drawable.post_it3b);
		noteManager.notify(0, newMemoNote.getNotification());
	}	
	
	public class GpsBinder extends Binder{
		MemoMapService getService(){
			Log.d(TAG, "getService");
			return MemoMapService.this;
		}
	}
	
	/**
	 * 
	 */
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
