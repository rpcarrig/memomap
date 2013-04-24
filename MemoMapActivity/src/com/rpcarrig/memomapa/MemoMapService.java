/**
 * Service that runs in the background, downloading new memos and notifying when nearby.
 * 
 * @author  Ryan P. Carrigan
 * @version 1.80 24 April 2013
 */

package com.rpcarrig.memomapa;

import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class MemoMapService extends Service implements LocationListener {
	private static final String TAG = "MemoMapService";

	boolean canGetLocation = false,
			isGpsEnabled = false,
			isNetworkEnabled = false;
	private Location location;
	private LocationManager locationManager;
	private Notification.Builder ongoingNote;
	private NotificationManager noteManager;

	private final IBinder binder = new MemoMapServiceBinder();

	private static final long
		MIN_DISTANCE_CHANGE	= 10,
		MIN_MS_BETWEEN_UPDATES = 1000 * 60 * 1;

	/* Empty constructor required for binding services. */
	public MemoMapService(){ }

	/* Runs when the service is bound. */
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	/* Starts the running notification. */
	@Override
	public void onCreate() {
		super.onCreate();
		noteManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		startGps();
		startForeground(1, ongoingNote.getNotification());
	}

	/* Cancels the running notification. */
	@Override
	public void onDestroy() {
		super.onDestroy();
		noteManager.cancel(1);
		if(locationManager != null) locationManager.removeUpdates(MemoMapService.this);
	}

	/* Runs whenever the location changes. */
	@Override
	public void onLocationChanged(Location loc) {
		Log.d(TAG, "onLocationChanged");
		location = loc;
		
		ongoingNote.setTicker("Location changed. MemoMap is searching...");
		noteManager.notify(1, ongoingNote.getNotification());	

		float[] results = { -1, -1, -1 };
		ArrayList<Memo> memoList = DataHandler.getInstance(this).getAllMemos();
		int i = 2;
		for (Memo m : memoList) {
			Location.distanceBetween(location.getLatitude(), location.getLongitude(),
									 m.getLatitude(), m.getLongitude(), results);
			if (results[0] <= m.getRadius()) noteManager.notify(i, memoNote(m));
			i++;
		}
		downloadMemos();
	}

	/* Called when the GPS provider is disabled by the user. */
	@Override
	public void onProviderDisabled(String provider) { }

	/* Called when the GPS provider is enabled by the user. */
	@Override
	public void onProviderEnabled(String provider) { }

	/* Called when the GPS provider status changes. */
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {}

	/* Called when the service starts at boot. */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		return Service.START_STICKY;
	}

	/* Updates the location with the last known coordinates. */
	public Location getFreshLocation(){
		Location lastLocation = new Location(location);

		if(!isGpsEnabled && !isNetworkEnabled)Log.e(CONNECTIVITY_SERVICE, "no GPS or network");
		
		else if (locationManager != null) {
			canGetLocation = true;
			if(isNetworkEnabled){
				location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			}
			if(isGpsEnabled){
				location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			}
		}
		if (isBetterLocation(location, lastLocation)) return location;
		else return lastLocation;
	}
	
	/* Initializes the GPS services. */
	public void startGps(){
		try{
			locationManager  = (LocationManager) getSystemService(LOCATION_SERVICE);
			isGpsEnabled     = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 
					MIN_MS_BETWEEN_UPDATES, MIN_DISTANCE_CHANGE, this);	
			location = getFreshLocation();
		}
		catch (Exception e){
			e.printStackTrace();
			Log.e(NOTIFICATION_SERVICE, "GPS Start error");
		}

		Intent intent = new Intent(this, MemoMapActivity.class);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
		ongoingNote = new Notification.Builder(this)
		        .setContentTitle("MemoMap")
		        .setContentText("Looking for memos...")
		        .setContentIntent(pIntent)
		        .setSmallIcon(R.drawable.ic_launcher);
	}
	
	/* Stops listening for location updates. */
	public void stopGps(){
		locationManager.removeUpdates(this);
	}

	/* Returns a notification for a memo. */
	public Notification memoNote(Memo m){
		Intent intent = new Intent(this, OpenMemoActivity.class);
		intent.putExtra("id", m.getId());
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
		
		Notification.Builder newMemoNote = new Notification.Builder(this)
			.setAutoCancel(true)
			.setContentIntent(pIntent)
			.setContentText(m.getLocationName())
			.setContentTitle(m.getMemoBody())
			.setLights(0xFFFFFF00, 500, 500)
			.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
			.setTicker("@" + m.getLocationName() + ": " + m.getMemoBody())
			.setVibrate(new long[]{100, 200, 100, 200})
			.setSmallIcon(R.drawable.post_it3b);
		return newMemoNote.getNotification();
	}
	
	/* Downloads memos from the remote server. */
	public void downloadMemos(){
		new Thread(new Runnable(){
			public void run(){
				ServerHandler.download();
			}
		}).start();
	}

	/* Allows binding of the service. */
	public class MemoMapServiceBinder extends Binder{
		MemoMapService getService(){
			return MemoMapService.this;
		}
	}

	/**
	 * The following two methods were modified from code provided by Google.
	 */
	
	/** Determines whether one Location reading is better than the current Location fix. */
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
		Log.d(TAG, "Comparing (" + location.getLongitude() + ", " + location.getLatitude() + ")");

		final int TWO_MINUTES = 1000 * 60 * 2;
	    if (currentBestLocation == null) return true;
	    
	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
	    boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) return true;
	    else if (isSignificantlyOlder) return false;

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
	            currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) return true;
	    else if (isNewer && !isLessAccurate) return true;
	    else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) return true;
	    else return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) return provider2 == null;
	    return provider1.equals(provider2);
	}
}