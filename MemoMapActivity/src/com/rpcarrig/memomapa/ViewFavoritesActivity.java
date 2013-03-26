package com.rpcarrig.memomapa;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ViewFavoritesActivity extends Activity {
	ArrayList<Favorite> faves;
	double lat = 0, lon = 0;
	
	private final String CLASS = "ViewFavoritesActivity";
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_viewfavelist);
		FaveHandler dbHandler = FaveHandler.getInstance(MemoMap.getInstance());
		
		Bundle bundle = getIntent().getExtras();
		if(bundle != null){
			lat = bundle.getDouble("lat");
			lon = bundle.getDouble("lon");
		}
		
		ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);

	    ListView listView = (ListView) findViewById(R.id.faveList);
		if(listView != null){
			faves = dbHandler.getAllFaves();
				listView.setAdapter(new FaveAdapter(
					getApplicationContext(), 0, faves, 0, 0));
			OnItemClickListener listen = new OnItemClickListener() {
				  @Override
				  public void onItemClick(AdapterView<?> parent,
						  View view, int position, long id) {
					  useFave(id);
				  }
				};
			listView.setOnItemClickListener(listen);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(CLASS, "onCreateOptionsMenu");
		getMenuInflater().inflate(R.menu.activity_viewfavelist, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected( MenuItem item ){
		Log.d(CLASS, "onOptionsItemSelected");
	    switch(item.getItemId()){
	    	case android.R.id.home:
	    		finish();
	    		return true;
	    	default:
	    		return super.onOptionsItemSelected(item);
	    }
	}
	
	public void useFave(long id){
		Log.d(CLASS, "useFave");
		Favorite fave = faves.get((int)id);
		Toast.makeText(this, fave.getFaveName(), Toast.LENGTH_SHORT).show();
	}
}
