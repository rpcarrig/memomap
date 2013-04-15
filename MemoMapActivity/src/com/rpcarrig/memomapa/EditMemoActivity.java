package com.rpcarrig.memomapa;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

public class EditMemoActivity extends Activity {
	private static final String TAG = "EditMemoActivity";
	Double mLatitude, mLongitude;
	Integer mId, mRadius;
	Memo memo;
	String memoBody, locationName;
	EditText eMemoBody, eLocName, eRadius;
	TextView eLatitude, eLongitude;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		setContentView(R.layout.activity_editmemo);
		ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Bundle bundle = getIntent().getExtras();
        mId = bundle.getInt("id");
        
		memo = DataHandler.getInstance(MemoMap.getInstance()).getMemo(mId);
		eMemoBody = (EditText)findViewById(R.id.edit_body);
		eLocName = (EditText)findViewById(R.id.edit_locname);
		eRadius = (EditText)findViewById(R.id.edit_rad);
		eLatitude = (TextView)findViewById(R.id.view_lat);
		eLongitude = (TextView)findViewById(R.id.view_long);
		
		
		mLatitude = memo.getLatitude();
		mLongitude = memo.getLongitude();
		memoBody = memo.getMemoBody();
		locationName = memo.getLocationName();
		mRadius = memo.getRadius();
	}
	
	@Override
	public void onResume(){
		super.onResume();
		eLatitude.setText(mLatitude.toString());
		eLocName.setText(locationName);
		eLongitude.setText(mLongitude.toString());
		eMemoBody.setText(memoBody);
		eRadius.setText(mRadius.toString());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_editmemo, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.menu_editmemo:
				saveMemo();
				return true;
			default:
				startActivity(new Intent(this, MemoMapActivity.class));
	            return true;
		}
	}
	
	public void saveMemo(){
		memo.setMemoBody(eMemoBody.getText().toString());
		memo.setLocationName(eLocName.getText().toString());
		memo.setRadius(Integer.parseInt(eRadius.getText().toString()));
		DataHandler.getInstance(MemoMap.getInstance()).updateMemo(memo);
		finish();
	}
}
