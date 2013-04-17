/**
 * Memo.java
 * 
 * @author  Ryan P. Carrigan
 * @version 1.1 (first created 21 March 2013)
 */

package com.rpcarrig.memomapa;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.location.Location;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class Memo {
	private boolean hasBeenRead,
					isShared;
	private int id,
				publicId,
				radius;
	private double distance,
				   latitude,
				   longitude;
	private Circle circle;
	private Marker marker;
	private String androidId,
				   memoBody,
				   memoDate,
				   locationName;
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());

	public Memo(){
		id 			 = -1;
		locationName = "Unknown Location";
		memoBody	 = "New Memo";
		latitude	 = 0;
		longitude	 = 0;
		radius		 = 0;
		memoDate	 = sdf.format(new Date());
		publicId	 = -1;
		androidId    = "Unknown Android ID";
	}
	
	public Memo(String loc, String body, double lat, double lon, int rad){
		distance  = -1;
		latitude  = lat;
		longitude = lon;
		radius 	  = rad;
		memoBody  = body;
		memoDate  = sdf.format(new Date());
		locationName = loc;
	}

	/** This constructor is used when a new memo is created from the Create activity. */
	public Memo(String loc, String body, double lat, double lon, int rad, String aId){
		id 			 = -1;
		locationName = loc;
		memoBody	 = body;
		latitude	 = lat;
		longitude	 = lon;
		radius		 = rad;
		memoDate	 = sdf.format(new Date());
		publicId	 = -1;
		androidId    = aId;
	}

	/** This constructor is used when building memos within the Data Handler. */
	public Memo(int i, String loc, String body, double lat, double lon, 
			int rad, String date, int pubId, String aId){
		id 			 = i;
		locationName = loc;
		memoBody	 = body;
		latitude	 = lat;
		longitude	 = lon;
		radius		 = rad;
		memoDate	 = date;
		publicId	 = pubId;
		androidId    = aId;
	}

	/** This constructor is used with the Server Handler. */
	public Memo(String loc, String body, double lat, double lon, 
			int rad, String date, int pubId, String aId){
		id			 = -1;
		locationName = loc;
		memoBody	 = body;
		latitude	 = lat;
		longitude	 = lon;
		radius		 = rad;
		memoDate	 = date;
		publicId	 = pubId;
		androidId    = aId;
	}

	public String toString(){
		String s;
		s = String.format(Locale.getDefault(), "[%s] %s -- (%f, %f)", 
				locationName, memoBody, longitude, latitude);
		return s;
	}

	/**
	 * Setter and getters!
	 */

	public boolean getSeen()	   { return hasBeenRead;  }
	public boolean getShared()	   { return isShared;	  }
	public int getId()			   { return id; 		  }
	public String getAndroidId()   { return androidId;	  }
	public String getMemoBody()    { return memoBody;  	  }
	public String getMemoDate()    { return memoDate;  	  }
	public String getLocationName(){ return locationName; }
	public int getPublicId()	   { return publicId;     }
	public int getRadius()		   { return radius; 	  }
	public double getLatitude()	   { return latitude; 	  }
	public double getLongitude()   { return longitude;    }
	public double getDistance()    { return distance;     }
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
	public Circle getCircle()   { return circle; }
	public LatLng getLatLong()  { return new LatLng(latitude, longitude); }
	public Marker getMarker(){ return marker; }

	public void setSeen(boolean b)		 { hasBeenRead = b;		 }
	public void setShared(boolean b)	 { isShared = b;		 }
	public void setAndroidId(String s)	 { androidId = s;	 }
	public void setDistance(double d)    { distance = d;     }
	public void setId(int i)		     { id = i; 	         }
	public void setLatitude(double l)    { latitude = l;     }
	public void setLongitude(double l)   { longitude = l;    }
	public void setRadius(int r)	     { radius = r;       }
	public void setCircle(Circle c)      { circle = c;       }
	public void setMarker(Marker m)	     { marker = m;       }
	public void setMemoBody(String b)    { memoBody = b;     }
	public void setMemoDate(String d)    { memoDate = d;     }
	public void setLocationName(String t){ locationName = t; }
}
