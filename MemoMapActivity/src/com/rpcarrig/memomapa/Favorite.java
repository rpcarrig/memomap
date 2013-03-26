package com.rpcarrig.memomapa;

import android.text.format.Time;

public class Favorite {
	int id;
	String
		faveAddress,
		faveDate,
		faveName;
	double
		longitude,
		latitude;
		
	public Favorite(){ }
	
	/**
	 * This is the general purpose constructor.
	 * @param name
	 * @param addr
	 * @param lat
	 * @param lon
	 */
	public Favorite(String name, String addr, double lat, double lon){
		faveDate = new Time(Time.getCurrentTimezone()).toString();
		
		faveName 	= name;
		faveAddress = addr;
		latitude 	= lat;
		longitude 	= lon;
	}
	
	/**
	 * This constructor is for use by the database handler.
	 * @param i
	 * @param name
	 * @param addr
	 * @param lat
	 * @param lon
	 */
	public Favorite(int i, String name, String addr, double lat, double lon){
		faveDate = new Time(Time.getCurrentTimezone()).toString();
		
		faveName 	= name;
		faveAddress = addr;
		id 			= i;
		latitude 	= lat;
		longitude 	= lon;
	}
	
	public int getId()				{ return id; 			}	
	public double getLatitude()		{ return latitude; 		}
	public double getLongitude()	{ return longitude; 	}
	public String getFaveAddress()	{ return faveAddress;	}
	public String getFaveDate()		{ return faveDate;		}
	public String getFaveName()		{ return faveName; 		}
	
	public void setId(int i)			{ id 			= i;}
	public void setFaveDate(String d)	{ faveDate 		= d;}
	public void setFaveName(String n)	{ faveName		= n;}
	public void setFaveAddress(String t){ faveAddress 	= t;}
	public void setLatitude(double l)	{ latitude 		= l;}
	public void setLongitude(double l)	{ longitude 	= l;}
}
