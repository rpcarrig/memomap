package com.rpcarrig.memomapa;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.rpcarrig.memomapa.MemoMapService.GpsBinder;

public class CreateMemoActivity extends Activity {
	private final String CLASS = "CreateMemoActivity";
	boolean gpsBound = false;
	private int rad,
				rad_l1 = 50,
				rad_l2 = 250,
				rad_l3 = 500;
	private Double lat,
		   		   lon;
	private RadioButton radius_a,
						radius_b,
						radius_c;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_creatememo);
		
		ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
		
	    if (getIntent().getExtras() != null){
	    	Intent i = getIntent();
	    	lat = i.getDoubleExtra("lat", 0);
	    	lon = i.getDoubleExtra("lon", 0);
	    }
	    
	    TextView longitude = (TextView)findViewById(R.id.tv_LongValue),
				 latitude = (TextView) findViewById(R.id.tv_LatValue);		
		radius_a = (RadioButton) findViewById(R.id.rb_RadiusA); 
		radius_b = (RadioButton) findViewById(R.id.rb_RadiusB); 
		radius_c = (RadioButton) findViewById(R.id.rb_RadiusC);
		radius_a.setText(rad_l1 + "m");
		radius_b.setText(rad_l2 + "m");
		radius_c.setText(rad_l3 + "m");
		
		longitude.setText(lon.toString());
		latitude.setText(lat.toString());
	    
		//Intent gpsIntent = new Intent(this, MemoMapService.class);
		//bindService(gpsIntent, serviceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(CLASS, "onCreateOptionsMenu");
		getMenuInflater().inflate(R.menu.activity_creatememo, menu);
		return true;
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
	}
	
	@Override
	public boolean onOptionsItemSelected( MenuItem item ){
		Log.d(CLASS, "onOptionsItemSelected");
	    switch(item.getItemId()){
		    case R.id.menu_savememo:
		    	saveMemo(null);
		    case android.R.id.home:
		    	finish();
	    	default:
	    		return super.onOptionsItemSelected(item);
	    }
	}
	
	public void cancelMemo(View view) {
		Log.d(CLASS, "cancelMemo");
		finish();
	}

	public void saveMemo(View view) {
		Log.d(CLASS, "saveMemoClick");
		
		EditText 
			editTitleTxt = (EditText) findViewById(R.id.et_Title),
			editBodyTxt = (EditText) findViewById(R.id.et_Body);		
		String 
			title = editTitleTxt.getText().toString(), 
			body = editBodyTxt.getText().toString();
		CheckBox saveToFaves = (CheckBox)findViewById(R.id.checkbox);
		
		FaveHandler favHandler = FaveHandler.getInstance(this);
		Favorite fave = new Favorite(title, null, lat, lon);
		if (saveToFaves.isChecked()) favHandler.addFave(fave);
		
		DbHandler dbHandler = DbHandler.getInstance(this);
		if (radius_a.isChecked())      rad = rad_l1;
		else if (radius_b.isChecked()) rad = rad_l2;
		else if (radius_c.isChecked()) rad = rad_l3;
		else rad = 25;

		Memo memo = new Memo(title, body, lat, lon, rad);
		if (title != null && body != null) {
			dbHandler.addMemo(memo);
			Toast.makeText(getApplicationContext(), "Memo saved to database.",
					Toast.LENGTH_SHORT).show();
		} else Toast.makeText(this, "Fields cannot be blank.", 
				Toast.LENGTH_SHORT).show();
		
		finish();
	}
}
