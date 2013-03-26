/**
 * Memo.java
 * 
 * @author  Ryan P. Carrigan
 * @version 1.1 (first created 21 March 2013)
 */

package com.rpcarrig.memomapa;

import java.util.Locale;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import android.annotation.SuppressLint;
import android.text.format.Time;

public class Memo {
	private int id,
				radius;
	private double distance,
				   latitude,
				   longitude;
	private Circle circle;
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
	public int getRadius()		{ return radius; 	}
	public Circle getCircle()   { return circle;    }
	public double getDistance()	{ return distance;	}
	public double getLatitude()	{ return latitude; 	}
	public LatLng getLatLong()  { return new LatLng(latitude, longitude); }
	public double getLongitude(){ return longitude; }
	public Marker getMarker()   { return marker;    }
	public String getMemoBody()	{ return memoBody; 	}
	public String getMemoDate()	{ return memoDate; 	}
	public String getMemoTitle(){ return memoTitle; }
	
	public void setCircle(Circle c)   {circle = c;     }
	public void setDistance(double d) { distance = d;  }
	public void setId(int i)		  { id = i; 	   }
	public void setLatitude(double l) { latitude = l;  }
	public void setLongitude(double l){ longitude = l; }
	public void setRadius(int r)	  { radius = r;    }
	public void setMarker(Marker m)   { marker = m;    }
	public void setMemoBody(String b) { memoBody = b;  }
	public void setMemoDate(String d) { memoDate = d;  }
	public void setMemoTitle(String t){ memoTitle = t; }
}
