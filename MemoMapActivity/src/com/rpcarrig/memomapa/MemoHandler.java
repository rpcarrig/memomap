package com.rpcarrig.memomapa;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class MemoHandler extends SQLiteOpenHelper {
	private static final String CLASS = "MemoHandler";
			
	public static final String
		DATABASE_NAME = "memoMapMemoDatabase",
		TABLE_MEMOS	  = "memos",
		
		KEY_MID	   = "memo_id",
		KEY_MTITLE = "memo_title",
		KEY_MBODY  = "memo_body",
		KEY_MDATE  = "memo_date",
		KEY_MLAT   = "memo_latitude",
		KEY_MLONG  = "memo_longitude",
		KEY_MRAD   = "memo_radius";
	private String selectFromMemos = "SELECT  * FROM " + TABLE_MEMOS,
				   selection,
				   table;
	private static final int DATABASE_VERSION = 2;

	private static MemoHandler memoDbInstance = null;
	
	
	private MemoHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		Log.d(CLASS, "MemoHandler (constructor)");
	}
	
	public static MemoHandler getInstance(Context context){
		Log.d(CLASS, "getInstance");
		
		//if(memoDbInstance == null) memoDbInstance = new MemoHandler(context);
		//return memoDbInstance;
		
		return new MemoHandler(context);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(CLASS, "onCreate");		
		String createMemoTable = "CREATE TABLE " + TABLE_MEMOS 
				+ "(" 
				+ KEY_MID 	 + " INTEGER PRIMARY KEY," 
				+ KEY_MTITLE + " TEXT,"
				+ KEY_MBODY  + " TEXT,"
				+ KEY_MLAT 	 + " REAL,"
				+ KEY_MLONG  + " REAL,"
				+ KEY_MRAD 	 + " INTEGER,"
				+ KEY_MDATE  + " TEXT"
				+ ")";
		db.execSQL(createMemoTable);
		db.close();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(CLASS, "onUpgrade");
		//deleteAllMemos();
		onCreate(db);
	}
	
	/**
	 *  ALL C.R.U.D. OPERATIONS BELOW
	 */	
	
	/*
	public void addMemo(Memo memo) {
		Log.d(CLASS, "addMemo");
		ContentValues values = new ContentValues();
		values.put(KEY_MLAT, 	memo.getLatitude());
		values.put(KEY_MLONG, 	memo.getLongitude());
		values.put(KEY_MTITLE, 	memo.getMemoTitle());
		values.put(KEY_MBODY, 	memo.getMemoBody());
		values.put(KEY_MDATE, 	memo.getMemoDate());
		values.put(KEY_MRAD, 	memo.getRadius());
		
		SQLiteDatabase db = memoDbInstance.getWritableDatabase();
		db.insert(TABLE_MEMOS, null, values);
		db.close();
	}
	
	public void deleteMemo(Memo memo) {
		Log.d(CLASS, "deleteMemo");
		table	  = TABLE_MEMOS;
		selection = KEY_MID + "=?";
		String[] whereArgs = { String.valueOf(memo.getId()) };
		
		SQLiteDatabase db = memoDbInstance.getWritableDatabase();
		db.delete(table, selection, whereArgs);
		db.close();
	}
	
	public void deleteAllMemos(){
		Log.d(CLASS, "deleteAllMemos");
		getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + TABLE_MEMOS);
	}*/
	
	public ArrayList<Memo> getAllMemos() {
		Log.d(CLASS, "getAllMemos");

		ArrayList<Memo> memoList = new ArrayList<Memo>();
	
		Cursor cursor = getWritableDatabase()
				.rawQuery(selectFromMemos, null);

		while (cursor.moveToNext()) {
			Memo memo = new Memo(
					Integer.parseInt(cursor.getString(0)),
					cursor.getString(1), cursor.getString(2),
					Double.parseDouble(cursor.getString(3)),
					Double.parseDouble(cursor.getString(4)),
					Integer.parseInt(cursor.getString(5)));
			memoList.add(memo);
		}
		return memoList;
	}
	/**
	public ArrayList<Memo> getAllMemosSortedBy(int flag) {
		Log.d(CLASS, "getAllMemosSortedBy " + flag);
		String o = KEY_MDATE;
		switch(flag){
			case 0:  o = KEY_MTITLE;
			case 1:  o = KEY_MBODY;
			default: o = KEY_MDATE;
		}
		Cursor cursor = getWritableDatabase().rawQuery(selectFromMemos
				+ " ORDER BY " + o, null);
		ArrayList<Memo> memoList = new ArrayList<Memo>();
		if(cursor.moveToFirst()){
			do {
				Memo memo = new Memo(
						Integer.parseInt(cursor.getString(0)),
						cursor.getString(1), cursor.getString(2),
						Double.parseDouble(cursor.getString(3)),
						Double.parseDouble(cursor.getString(4)),
						Integer.parseInt(cursor.getString(5)));
				memoList.add(memo);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return memoList;		
	}
	public ArrayList<Memo> getDummyMemos(){
		Log.d(CLASS, "getDummyMemos");
		ArrayList<Memo> memoArray = new ArrayList<Memo>();
		memoArray.add(new Memo("Name", "Body", 36, -79, 25));
		memoArray.add(new Memo("Milk", "get some", 36.07, -79.85, 100));
		memoArray.add(new Memo("Party", "Don't forget!", 36.066, -79.848, 50));
		memoArray.add(new Memo("Bills", "be turrible", 36.0665, -79.8487, 25));
		return memoArray;
	}
	
	public ArrayList<LatLng> getMemoLocations(){
		Log.d(CLASS, "getMemoLocations");
		ArrayList<LatLng> locArray = new ArrayList<LatLng>();
		Cursor cursor = getWritableDatabase().rawQuery(selectFromMemos, null);

		if(cursor.moveToFirst()){
			do {
				LatLng ll = new LatLng(Double.parseDouble(cursor.getString(3)),
						Double.parseDouble(cursor.getString(4)));
				locArray.add(ll);
			} while (cursor.moveToNext());
		}
		cursor.close();		
		return locArray;
	}
	
	public Memo getMemo(int id) {
		Log.d(CLASS, "getMemo: " + id);
		table	  = TABLE_MEMOS;
		selection = KEY_MID + "=?";
		String[] args    = new String[]{ String.valueOf(id) }, 
				 columns = new String[]{ KEY_MID, KEY_MTITLE, KEY_MBODY, 
							KEY_MDATE, KEY_MLAT, KEY_MLONG, KEY_MRAD };
			     
		Cursor cursor = getWritableDatabase()
				.query(table, columns, selection, args, null, null, null);
		if (cursor != null) cursor.moveToFirst();
		
		Memo memo = new Memo(
				Integer.parseInt(cursor.getString(0)),
				cursor.getString(1), cursor.getString(2),
				Double.parseDouble(cursor.getString(3)),
				Double.parseDouble(cursor.getString(4)),
				Integer.parseInt(cursor.getString(5)));
		cursor.close();
		return memo;
	}
	
	public int getMemoCount() {
		Log.d(CLASS, "getMemoCount");
		Cursor cursor = getWritableDatabase().rawQuery(selectFromMemos, null);
		cursor.close();
		return cursor.getCount();
	}
	
	public int updateMemo(Memo memo) {
		Log.d(CLASS, "updateMemo");
		ContentValues values = new ContentValues();
		values.put(KEY_MLAT, 	memo.getLatitude());
		values.put(KEY_MLONG, 	memo.getLongitude());
		values.put(KEY_MTITLE, 	memo.getMemoTitle());
		values.put(KEY_MBODY, 	memo.getMemoBody());
		values.put(KEY_MRAD, 	memo.getRadius());
		values.put(KEY_MDATE, 	memo.getMemoDate());

		table	  = TABLE_MEMOS;
		selection = KEY_MID + "=?";
		String[] whereArgs = { String.valueOf(memo.getId()) };
		return getWritableDatabase().update(table, values, selection, 
				whereArgs);
	}*/
}

