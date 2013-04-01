/**
 * MemoAdapter.java
 * 
 * A MemoAdapter is an extension of ArrayAdapter which only accepts arrays of 
 * Memo objects. MemoAdapters are used in conjunction with a memo list item 
 * layout to format each entry in the ListView.
 * 
 * @version 1.1 (first created 22 March 2013)
 * @author  Ryan P. Carrigan
 */

package com.rpcarrig.memomapa;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

public class MemoAdapter extends ArrayAdapter<Memo>{
	private static final String TAG = "MemoAdapter";
	
	private ArrayList<Memo> memoArray; 
	private Context currentContext;
	private LatLng location;

	
	public MemoAdapter(Context context, int rId, ArrayList<Memo> items, LatLng loc){
		super(context, rId, items);	
		Log.d(TAG, "constructor");
		currentContext  = context;
		if(loc != null)
			location = new LatLng(loc.latitude, loc.longitude);
		else location = new LatLng(-98.5795, 39.8282);
		memoArray       = items;
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
	 * setDistColor affects the color of the text on the ListView item 
	 * indicating its distance from the currentLocation. There are four
	 * customizable levels of distance which can be assigned different colors
	 * to apply. One example would be that, as the distance gets smeller,
	 * the color goes from red to green.
	 * @param text: the TextView whose color is to be set
	 * @param dist: the distance value affecting the color change
	 */
	private void setDistColor(TextView text, double dist){
		int closest = 250,  closestColor = Color.GREEN,
			closer  = 1000, closerColor  = Color.parseColor("#ADFF2F"),
			close   = 2500, closeColor   = Color.YELLOW,
			far     = 5000, farColor	 = Color.parseColor("#FFA500"),
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
			view = inflater.inflate(R.layout.memolist_item, null);
		}
		
		Memo memo = memoArray.get(position);
		if(memo != null){
			double d = memo.getDistance(location);
			TextView 
				title      = (TextView) view.findViewById(R.id.ml_Title),
				body       = (TextView) view.findViewById(R.id.ml_Body),
				distanceTo = (TextView) view.findViewById(R.id.ml_DistanceTo);
			if(title != null && body != null){
				title.setText     ( memo.getLocationName());
				body.setText      ( memo.getMemoBody() );
				setDistColor(distanceTo, d);
				if (d >= 500) distanceTo.setText(formatKm(d));
				else          distanceTo.setText(formatM(d));
			}
		}
		return view;
	}
}