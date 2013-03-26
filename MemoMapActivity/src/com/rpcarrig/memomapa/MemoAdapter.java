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

import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MemoAdapter extends ArrayAdapter<Memo>{
	// Store the class name for the debug log.
	// private static final String CLASS = "MemoAdapter";
	private int fillColor   = 0x100000A0,
				strokeColor = Color.BLUE,
				strokeWidth = 2;	
	
	private ArrayList<Memo> memoArray; 
	private Context currentContext;		
	private FragmentManager manager;
	private Location currentLocation;

	/**
	 * The only constructor for a MemoAdapter.
	 * @param currentContext: the current currentContext
	 * @param rId:     the resource ID for the layout file
	 * @param items:   the ArrayList of memo objects
	 * @param loc:	   a location
	 */
	public MemoAdapter(Context context, int rId, ArrayList<Memo> items, 
			Location loc, FragmentManager fm){
		super(context, rId, items);	
		Log.d("MemoAdapter", "constructor");
		currentContext  = context;
		currentLocation = loc;
		manager			= fm;
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
	 * setDistance accepts a Memo object and gets its latitude and longitude,
	 * then calls Location.distanceBetween to calculate the distance in meters
	 * (as a double) between the currentLocation and the memo's location. Then,
	 * it stores the calculated distance by setting it to the memo's distance.
	 * @param memo: the memo to calculate and set the distance
	 */
	private void setDistance(Memo memo){
		float[] results = { -1, -1, -1 };
		Location.distanceBetween(
				currentLocation.getLatitude(),
				currentLocation.getLongitude(),
				memo.getLatitude(), 
				memo.getLongitude(),
				results);
		memo.setDistance(results[0]);
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
			closer  = 1000,  closerColor  = Color.parseColor("#ADFF2F"),
			close   = 2500,  closeColor   = Color.YELLOW,
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
			setDistance(memo);
			double d = memo.getDistance();

			GoogleMap mapFragment = ((MapFragment)manager
					.findFragmentById(R.id.top_fragment)).getMap();
			//BitmapDescriptor bd = BitmapDescriptorFactory
			//		.fromResource(R.drawable.ic_launcher);
			
			Marker mark = mapFragment.addMarker(new MarkerOptions()
			//	.icon(bd)
				.position(memo.getLatLong())
				.snippet(memo.getMemoTitle())
				.title(memo.getMemoBody()));
			memo.setMarker(mark);
			
			Circle circle = mapFragment.addCircle(new CircleOptions()
					.center(memo.getLatLong())
					.strokeWidth(strokeWidth)
					.strokeColor(strokeColor)
					.fillColor(fillColor)
					.radius(memo.getRadius()));
			memo.setCircle(circle);
			
			TextView 
				title      = (TextView) view.findViewById(R.id.ml_Title),
				body       = (TextView) view.findViewById(R.id.ml_Body),
				distanceTo = (TextView) view.findViewById(R.id.ml_DistanceTo);
			if(title != null && body != null){
				title.setText     ( memo.getMemoTitle());
				body.setText      ( memo.getMemoBody() );
				setDistColor(distanceTo, d);
				if (d >= 500) distanceTo.setText(formatKm(d));
				else          distanceTo.setText(formatM(d));
			}
		}
		return view;
	}
}