/**
 * This activity provides an interface for the editing updating memos in the database.
 * 
 * @author  Ryan P. Carrigan
 * @version 1.11 16 April 2013
 */

package com.rpcarrig.memomapa;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

public class EditMemoActivity extends Activity {
	Memo     memo;
	String   memoBody,
		     locationName;
	EditText mMemoBody,
			 mLocName, 
			 mRadius;
	TextView mLatitude, 
			 mLongitude;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_editmemo);
		ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Bundle bundle = getIntent().getExtras();
        
		memo         = DataHandler.getInstance(MemoMap.getInstance()).getMemo(bundle.getInt("id"));
		
		mMemoBody    = (EditText)findViewById(R.id.edit_body);
		mLocName     = (EditText)findViewById(R.id.edit_locname);
		mRadius      = (EditText)findViewById(R.id.edit_rad);
		mLatitude    = (TextView)findViewById(R.id.view_lat);
		mLongitude   = (TextView)findViewById(R.id.view_long);
	}
	
	/* Sets the text fields with information from the memo. */
	@Override
	public void onResume(){
		super.onResume();
		mLatitude.setText(memo.getLatitude() + "");
		mLocName.setText(memo.getLocationName());
		mLongitude.setText(memo.getLongitude()+ "");
		mMemoBody.setText(memo.getMemoBody());
		mRadius.setText(memo.getRadius());
	}
	
	/* Creates the action bar. */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_editmemo, menu);
		return true;
	}
	
	/* Saves the memo when the action bar button is touched. */
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
	
	/* Updates the memo in the database with the modified information. */
	public void saveMemo(){
		memo.setMemoBody(mMemoBody.getText().toString());
		memo.setLocationName(mLocName.getText().toString());
		memo.setRadius(Integer.parseInt(mRadius.getText().toString()));
		DataHandler.getInstance(MemoMap.getInstance()).updateMemo(memo);
		finish();
	}
}
