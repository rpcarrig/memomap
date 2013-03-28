/**
 * Memo.java
 * 
 * @author  Ryan P. Carrigan
 * @version 1.1 (first created 21 March 2013)
 */

package com.rpcarrig.memomapa;

import java.util.Locale;

import android.annotation.SuppressLint;
import android.location.Location;
import android.text.format.Time;

import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class Memo {
	private int id,
				radius,
				
				fillColor,
				strokeColor,
				strokeWidth;
	private double distance,
				   latitude,
				   longitude;
	private Marker marker;
	private String memoBody,
				   memoDate,
				   memoTitle;

	public Memo(){ }
	
	public Memo(String title, String body, double lat, double lon, int rad){
		distance  = -1;
		latitude  = lat;
		longitude = lon;
		radius 	  = rad;
		memoBody  = body;
		memoDate  = new Time(Time.getCurrentTimezone()).toString();
		memoTitle = title;
	}
	
	public Memo(int i, String title, String body, double lat, double lon, 
			int rad){
		distance  = -1;
		id		  = i;
		latitude  = lat;
		longitude = lon;
		radius 	  = rad;
		memoBody  = body;
		memoDate  = new Time(Time.getCurrentTimezone()).toString();
		memoTitle = title;
	}
	
	public Memo(int i, String title, String body, double lat, double lon, 
			int rad, String date){
		distance  = -1;
		id		  = i;
		latitude  = lat;
		longitude = lon;
		radius 	  = rad;
		memoBody  = body;
		memoDate  = date;
		memoTitle = title;
	}
	
	public static Memo copy(Memo m){
		return new Memo(m.id, m.memoTitle, m.memoBody, m.latitude, m.longitude,
				m.radius, m.memoDate);
	}
	
	@SuppressLint("DefaultLocale")
	public String toString(){
		String s;
		s = String.format(Locale.getDefault(), "[%s] %s -- (%f, %f)", 
				memoTitle, memoBody, longitude, latitude);
		return s;
	}
	
	/**
	 * Setter and getters!
	 */
	public int getId()			{ return id; 		}
	public String getMemoBody()	{ return memoBody; 	}
	public String getMemoDate()	{ return memoDate; 	}
	public String getMemoTitle(){ return memoTitle; }
	public int getRadius()		{ return radius; 	}
	public double getLatitude()	{ return latitude; 	}
	public double getLongitude(){ return longitude; }
	public double getDistance() { return distance;  }
	public double getDistance(LatLng loc){
		if(loc == null){ loc = new LatLng(0, 0); }
		float[] results = { -1, -1, -1 };
		Location.distanceBetween(
				loc.latitude,
				loc.longitude,
				latitude, 
				longitude,
				results);
		setDistance(results[0]);
		return distance;
	}
	public LatLng getLatLong()  { return new LatLng(latitude, longitude); }
	public CircleOptions getCircleOptions() {
		return new CircleOptions()
			.center(getLatLong())
			.strokeWidth(strokeWidth)
			.strokeColor(strokeColor)
			.fillColor(fillColor)
			.radius(getRadius()); 
	}
	
	public Marker getMarker(){ return marker; }
	public MarkerOptions getMarkerOptions()   {
		return new MarkerOptions()
			.position(getLatLong())
			.snippet(memoTitle)
			.title(memoBody);
	}
	public void setDistance(double d) { distance = d;  }
	public void setId(int i)		  { id = i; 	   }
	public void setLatitude(double l) { latitude = l;  }
	public void setLongitude(double l){ longitude = l; }
	public void setRadius(int r)	  { radius = r;    }
	public void setMarker(Marker m)	  { marker = m;    }
	public void setMemoBody(String b) { memoBody = b;  }
	public void setMemoDate(String d) { memoDate = d;  }
	public void setMemoTitle(String t){ memoTitle = t; }
}
