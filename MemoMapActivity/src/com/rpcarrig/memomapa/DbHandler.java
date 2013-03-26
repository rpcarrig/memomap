package com.rpcarrig.memomapa;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class DbHandler extends SQLiteOpenHelper {
	

	private static final String CLASS = "DatabaseHandler";
			
	public static final String
		DATABASE_NAME = "memoMapMemoDatabase",
		TABLE_MEMOS	  = "memos",
		TABLE_FAVES	  = "favorites",
		
		KEY_MID	   = "memo_id",
		KEY_MTITLE = "memo_title",
		KEY_MBODY  = "memo_body",
		KEY_MDATE  = "memo_date",
		KEY_MLAT   = "memo_latitude",
		KEY_MLONG  = "memo_longitude",
		KEY_MRAD   = "memo_radius",
		
		KEY_FID	  = "fave_id",
		KEY_FNAME = "fave_name",
		KEY_FADDR = "fave_address",
		KEY_FLAT  = "fave_latitude",
		KEY_FLONG = "fave_longitude",
		KEY_FDATE = "fave_date";
	private String selectFromFaves = "SELECT  * FROM " + TABLE_FAVES,
				   selectFromMemos = "SELECT  * FROM " + TABLE_MEMOS,
				   selection,
				   table;
	private static final int DATABASE_VERSION = 1;

	private static DbHandler dbInstance = null;
	private DbHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		Log.d(CLASS, "DbHandler (constructor)");
	}
	
	public static DbHandler getInstance(Context context){
		Log.d(CLASS, "getInstance");
		
		if(dbInstance == null) dbInstance = new DbHandler(context);
		return dbInstance;
	}
	/**
	public DbHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		Log.d(CLASS, "DbHandler (constructor)");
	}*/

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
		
		String createFaveTable = "CREATE TABLE " + TABLE_FAVES
				+ "("
				+ KEY_FID	+ " INTEGER PRIMARY KEY,"
				+ KEY_FNAME	+ " TEXT,"
				+ KEY_FADDR	+ " TEXT,"
				+ KEY_FLAT	+ " REAL,"
				+ KEY_FLONG	+ " REAL,"
				+ KEY_FDATE	+ " TEXT"
				+ ")";
		db.execSQL(createFaveTable);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(CLASS, "onUpgrade");
		deleteAllFaves();
		deleteAllMemos();
		onCreate(db);
	}
	
	/**
	 *  ALL C.R.U.D. OPERATIONS BELOW
	 */
	public void addFave(Favorite fave) {
		Log.d(CLASS, "addFave");
		ContentValues values = new ContentValues();
		values.put(KEY_FADDR, fave.getFaveAddress());
		values.put(KEY_FLAT,  fave.getLatitude());
		values.put(KEY_FLONG, fave.getLongitude());
		values.put(KEY_FNAME, fave.getFaveName());
		
		SQLiteDatabase db = getWritableDatabase();
		db.insert(TABLE_FAVES, null, values);
		db.close();
	}
	
	public void addMemo(Memo memo) {
		Log.d(CLASS, "addMemo");
		ContentValues values = new ContentValues();
		values.put(KEY_MLAT, 	memo.getLatitude());
		values.put(KEY_MLONG, 	memo.getLongitude());
		values.put(KEY_MTITLE, 	memo.getMemoTitle());
		values.put(KEY_MBODY, 	memo.getMemoBody());
		values.put(KEY_MDATE, 	memo.getMemoDate());
		values.put(KEY_MRAD, 	memo.getRadius());
		
		SQLiteDatabase db = getWritableDatabase();
		db.insert(TABLE_MEMOS, null, values);
		db.close();
	}
	
	public void deleteFave(Favorite fave) {
		Log.d(CLASS, "deleteFave");
		table	  = TABLE_FAVES;
		selection = KEY_FID + "=?";
		String[] whereArgs = { String.valueOf(fave.getId()) };
		
		SQLiteDatabase db = getWritableDatabase();
		db.delete(table, selection, whereArgs);
		db.close();		
	}
	
	public void deleteMemo(Memo memo) {
		Log.d(CLASS, "deleteMemo");
		table	  = TABLE_MEMOS;
		selection = KEY_MID + "=?";
		String[] whereArgs = { String.valueOf(memo.getId()) };
		
		SQLiteDatabase db = getWritableDatabase();
		db.delete(table, selection, whereArgs);
		db.close();
	}
	
	public void deleteAllFaves(){
		Log.d(CLASS, "deleteAllFaves");
		getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + TABLE_FAVES);
	}
	
	public void deleteAllMemos(){
		Log.d(CLASS, "deleteAllMemos");
		getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + TABLE_MEMOS);
	}
	
	public ArrayList<Favorite> getAllFaves() {
		Log.d(CLASS, "getAllFaves");
		Cursor cursor = getWritableDatabase().rawQuery(selectFromFaves, null);
		ArrayList<Favorite> faveList = new ArrayList<Favorite>();
		if(cursor.moveToFirst()){
			do {
				Favorite fave = new Favorite(
						Integer.parseInt(cursor.getString(0)),
						cursor.getString(1), cursor.getString(2),
						Double.parseDouble(cursor.getString(3)),
						Double.parseDouble(cursor.getString(4)));
				faveList.add(fave);
			} while (cursor.moveToNext());
		}
		cursor.close();	
		return faveList;
	}
	
	public ArrayList<Memo> getAllMemos() {
		Log.d(CLASS, "getAllMemos");
		
		ArrayList<Memo> memoList = new ArrayList<Memo>();
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectFromMemos, null);

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

		db.close();
		return memoList;
	}
	
	public Favorite getFave(int id) {
		Log.d(CLASS, "getFave");
		table     = TABLE_FAVES;
		selection = KEY_FID + "=?";
		String[] args    = new String[]{ String.valueOf(id) }, 
				 columns = new String[]{ KEY_FID, KEY_FNAME, KEY_FADDR,	
							KEY_FLAT, KEY_FLONG, KEY_FDATE};
		Cursor cursor = getWritableDatabase()
				.query(table, columns, selection, args, null, null, null);
		
		if (cursor != null) cursor.moveToFirst();
		Favorite fave = new Favorite(
				Integer.parseInt(cursor.getString(0)),
				cursor.getString(1), cursor.getString(2),
				Double.parseDouble(cursor.getString(3)),
				Double.parseDouble(cursor.getString(4)));
		cursor.close();
		return fave;
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
	
	public int getFaveCount() {
		Log.d(CLASS, "getFaveCount");
		Cursor cursor = getWritableDatabase().rawQuery(selectFromFaves, null);
		cursor.close();
		return cursor.getCount();
	}
	
	public int getMemoCount() {
		Log.d(CLASS, "getMemoCount");
		Cursor cursor = getWritableDatabase().rawQuery(selectFromMemos, null);
		cursor.close();
		return cursor.getCount();
	}
	
	public int updateFave(Favorite fave) {
		Log.d(CLASS, "updateFave");
		ContentValues values = new ContentValues();
		values.put(KEY_FADDR, fave.getFaveAddress());
		values.put(KEY_FLAT,  fave.getLatitude());
		values.put(KEY_FLONG, fave.getLongitude());
		values.put(KEY_FNAME, fave.getFaveName());
		values.put(KEY_FDATE, fave.getFaveDate());
		
		table	  = TABLE_FAVES;
		selection = KEY_FID + "=?";
		String[] whereArgs = { String.valueOf(fave.getId()) };	
		return getWritableDatabase().update(table, values, selection,
				whereArgs);		
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
	}
}