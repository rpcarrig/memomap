/**
 * FaveAdapter.java
 * 
 * A FaveAdapter is an extension of ArrayAdapter which only accepts arrays of 
 * Favorite objects. FaveAdapters are used in conjunction with a list item 
 * layout to format each entry in the ListView.
 * 
 * @version 1.0 (first created 25 March 2013)
 * @author  Ryan P. Carrigan
 */

package com.rpcarrig.memomapa;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class FaveAdapter extends ArrayAdapter<Favorite>{
	// Store the class name for the debug log.
	// private static final String CLASS = "FaveAdapter";

	private ArrayList<Favorite> faveArray; 
	private Context currentContext;		
	private double latitude, longitude;

	/**
	 * The only constructor for a MemoAdapter.
	 * @param currentContext: the current currentContext
	 * @param rId:     the resource ID for the layout file
	 * @param items:   the ArrayList of memo objects
	 * @param loc:	   a location
	 */
	public FaveAdapter(Context context, int rId, ArrayList<Favorite> items, 
			double lon, double lat){
		super(context, rId, items);	
		Log.d("FaveAdapter", "constructor");
		currentContext  = context;
		longitude = lon;
		latitude  = lat;
		faveArray = items;
	}
	
	private String formatM(double d){
		DecimalFormat formatter = new DecimalFormat("###");
		return formatter.format(d) + "m";
	}
	
	/**
	 * formatKm appears to convert meters to kilometers.
	 * @param d: the double value of meters
	 * @return:  a String containing the value in km
	 */
	private String formatKm(double d){
		DecimalFormat formatter = new DecimalFormat("###,###.##");
		d = d/1000;
		return formatter.format(d) + "km";
	}
	
	/**
	 * setDistance accepts a Memo object and gets its latitude and longitude,
	 * then calls Location.distanceBetween to calculate the distance in meters
	 * (as a double) between the currentLocation and the memo's location. Then,
	 * it stores the calculated distance by setting it to the memo's distance.
	 * @param memo: the memo to calculate and set the distance
	 */
	private double getDistance(Favorite fave){
		float[] results = { -1, -1, -1 };
		Location.distanceBetween(
				latitude,
				longitude,
				fave.getLatitude(), 
				fave.getLongitude(),
				results);
		double d = results[0];
		return d;
	}
	
	/**
	 * setDistColor affects the color of the text on the ListView item 
	 * indicating its distance from the currentLocation. There are four
	 * customizable levels of distance which can be assigned different colors
	 * to apply. One example would be that, as the distance gets smeller,
	 * the color goes from red to green.
	 * @param text: the TextView whose color is to be set
	 * @param dist: the distance value affecting the color change
	 */
	private void setDistColor(TextView text, double dist){
		int closest = 100,  closestColor = Color.GREEN,
			closer  = 250,  closerColor  = Color.parseColor("#ADFF2F"),
			close   = 500,  closeColor   = Color.YELLOW,
			far     = 1000, farColor	 = Color.parseColor("#FFA500"),
			textColor,	    distantColor = Color.RED;
		
		if(dist <= closest)					  textColor = closestColor;	else
		if(dist >  closest && dist <= closer) textColor = closerColor;	else
		if(dist >  closer  && dist <  close)  textColor = closeColor;	else
		if(dist >= close   && dist <  far)    textColor = farColor;
		else textColor = distantColor;
		
		text.setTextColor(textColor);
	}
		
	/**
	 * getView
	 */
	public View getView(int position, View convertView, ViewGroup parents){
		View view = convertView;
		
		if(view == null){
			LayoutInflater inflater = (LayoutInflater) currentContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.favelist_item, null);
		}
		
		Favorite fave = faveArray.get(position);
		if(fave != null){
			
			double d = getDistance(fave);
			
			TextView 
				title      = (TextView) view.findViewById(R.id.fl_Title),
				body       = (TextView) view.findViewById(R.id.fl_Body),
				distanceTo = (TextView) view.findViewById(R.id.fl_DistanceTo);
			if(title != null && body != null){
				String s = "";
				if(fave.getFaveAddress().isEmpty()){
					s = String.format("[%d, %d]", fave.getLongitude(), 
							fave.getLatitude());
				} else s = fave.getFaveAddress();
				
				title.setText     ( fave.getFaveName());
				body.setText      ( s );
				setDistColor(distanceTo, d);
				if (d >= 500) distanceTo.setText(formatKm(d));
				else          distanceTo.setText(formatM(d));
			}
		}
		return view;
	}
}