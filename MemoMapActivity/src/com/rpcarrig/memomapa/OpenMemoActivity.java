/**
 * Provides the interface for viewing memos from the notification.
 * 
 * @author  Ryan P. Carrigan
 * @version 1.01 18 April 2013
 */

package com.rpcarrig.memomapa;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class OpenMemoActivity extends Activity {
	TextView locationName,
			 memoBody,
			 date,
			 lat,
			 lon,
			 radius;
	
	/* Sets fields to the data from the memo. */
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_openmemo);
		
		Bundle bundle = getIntent().getExtras();
		int id = bundle.getInt("id");
		
		Memo m = DataHandler.getInstance(MemoMap.getInstance()).getMemo(id);
		
		locationName = (TextView)findViewById(R.id.view_locname);
		memoBody 	 = (TextView)findViewById(R.id.view_body);
		date		 = (TextView)findViewById(R.id.view_date);
		lat			 = (TextView)findViewById(R.id.view_lat);
		lon			 = (TextView)findViewById(R.id.view_long);
		radius		 = (TextView)findViewById(R.id.view_radius);
		
		locationName.setText(m.getLocationName());
		memoBody.setText(m.getMemoBody());
		date.setText(m.getMemoDate());
		lat.setText("" + m.getLatitude());
		lon.setText("" + m.getLongitude());
		radius.setText("" + m.getRadius());
	}
}
