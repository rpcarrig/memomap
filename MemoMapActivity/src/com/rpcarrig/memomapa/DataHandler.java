/**
 * Handles all of the local database modifications. 
 * 
 * @author  Ryan P. Carrigan, Drew Markle
 * @version 3.11 23 April 2013
 */

package com.rpcarrig.memomapa;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.provider.Settings.Secure;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

public class DataHandler extends SQLiteOpenHelper {
	public static final String
		DATABASE_NAME = "memoMapMemoDatabase",
		TABLE_MEMOS	  = "memos",
		TABLE_FAVES	  = "favorites",

		KEY_AID	   = "memo_andid",
		KEY_MID	   = "memo_id",
		KEY_PUBID  = "memo_pubid",
		KEY_MLOC   = "memo_title",
		KEY_MBODY  = "memo_body",
		KEY_MDATE  = "memo_date",
		KEY_MLAT   = "memo_latitude",
		KEY_MLONG  = "memo_longitude",
		KEY_MRAD   = "memo_radius",
		KEY_FID	   = "fave_id",
		KEY_FNAME  = "fave_name",
		KEY_FADDR  = "fave_address",
		KEY_FLAT   = "fave_latitude",
		KEY_FLONG  = "fave_longitude",
		KEY_FDATE  = "fave_date";

	private String selectFromFaves = "SELECT  * FROM " + TABLE_FAVES,
				   selectFromMemos = "SELECT  * FROM " + TABLE_MEMOS,
				   selection,
				   table;
	
	private static final int DATABASE_VERSION = 1; // controls the database versioning
	private static DataHandler dbInstance = null;  // allows only one DataHandler to exist
	
	
	private DataHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/* Returns an instance of the data handler. */
	public static DataHandler getInstance(Context context){
		if(dbInstance == null) dbInstance = new DataHandler(context);
		return dbInstance;
	}

	/* Creates the database tables. */
	@Override
	public void onCreate(SQLiteDatabase db) {
		String createMemoTable = "CREATE TABLE " + TABLE_MEMOS 
				+ "(" 
				+ KEY_MID 	 + " INTEGER PRIMARY KEY," 
				+ KEY_MLOC   + " TEXT,"
				+ KEY_MBODY  + " TEXT,"
				+ KEY_MLAT 	 + " REAL,"
				+ KEY_MLONG  + " REAL,"
				+ KEY_MRAD 	 + " INTEGER,"
				+ KEY_MDATE  + " TEXT,"
				+ KEY_PUBID  + " INTEGER,"
				+ KEY_AID    + " TEXT"
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

	/* Upgrades the database when the version changes. */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		deleteAllFaves();
		deleteAllMemos();
		onCreate(db);
	}

	/* Adds a new favorite to the fave database. */
	public void addFave(Favorite fave) {
		ContentValues values = new ContentValues();
		values.put(KEY_FADDR, fave.getFaveAddress());
		values.put(KEY_FLAT,  fave.getLatitude());
		values.put(KEY_FLONG, fave.getLongitude());
		values.put(KEY_FNAME, fave.getFaveName());

		SQLiteDatabase db = getWritableDatabase();
		db.insert(TABLE_FAVES, null, values);
		db.close();
	}

	/* Adds a new private memo to the local database. */
	public void addMemo(Memo memo) {
		ContentValues values = new ContentValues();
		values.put(KEY_MLAT, 	memo.getLatitude());
		values.put(KEY_MLONG, 	memo.getLongitude());
		values.put(KEY_MLOC, 	memo.getLocationName());
		values.put(KEY_MBODY, 	memo.getMemoBody());
		values.put(KEY_MDATE, 	memo.getMemoDate());
		values.put(KEY_MRAD, 	memo.getRadius());
		values.put(KEY_PUBID,   memo.getPublicId());
		values.put(KEY_AID,     getAndroidId());
		
		SQLiteDatabase db = getWritableDatabase();
		db.insert(TABLE_MEMOS, null, values);
		db.close();
		
		exportMemo(memo);
	}
	
	/* Adds a downloaded public memo to the local database. */
	public void addPublicMemo(Memo memo) {
		ArrayList<Memo> memoList = getAllMemos();
		for(Memo m : memoList) {
			if (m.getPublicId() == memo.getPublicId()) {
				deleteMemo(m);
			}
		}
		ContentValues values = new ContentValues();
		values.put(KEY_MLAT,   memo.getLatitude());
		values.put(KEY_MLONG,  memo.getLongitude());
		values.put(KEY_MLOC,   memo.getLocationName());
		values.put(KEY_MBODY,  memo.getMemoBody());
		values.put(KEY_MDATE,  memo.getMemoDate());
		values.put(KEY_MRAD,   memo.getRadius());
		values.put(KEY_PUBID,  memo.getPublicId());
		values.put(KEY_AID,    getAndroidId());

		SQLiteDatabase db = getWritableDatabase();
		db.insert(TABLE_MEMOS, null, values);
		db.close();
	}

	/* Deletes a favorite from the fave database. */
	public void deleteFave(Favorite fave) {
		table	  = TABLE_FAVES;
		selection = KEY_FID + "=?";
		String[] whereArgs = { String.valueOf(fave.getId()) };

		SQLiteDatabase db = getWritableDatabase();
		db.delete(table, selection, whereArgs);
		db.close();		
	}

	/* Deletes a memo from the memo database. */
	public void deleteMemo(Memo memo) {
		table	  = TABLE_MEMOS;
		selection = KEY_MID + "=?";
		String[] whereArgs = { String.valueOf(memo.getId()) };

		SQLiteDatabase db = getWritableDatabase();
		db.delete(table, selection, whereArgs);
		db.close();
	}

	/* Deletes the fave database table. */
	public void deleteAllFaves(){
		getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + TABLE_FAVES);
	}

	/* Deletes the memo database table. */
	public void deleteAllMemos(){
		getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + TABLE_MEMOS);
	}

	/* Returns an unsorted ArrayList of all favorites in the fave database. */
	public ArrayList<Favorite> getAllFaves() {
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

	/* Returns an unsorted ArrayList of all memos in the memo database. */
	public ArrayList<Memo> getAllMemos() {
		ArrayList<Memo> memoList = new ArrayList<Memo>();
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectFromMemos, null);

		if(cursor.moveToFirst()){
			do { Memo memo = new Memo(
						Integer.parseInt(cursor.getString(0)),		// memo ID
						cursor.getString(1),						// memo location
						cursor.getString(2),						// memo body
						Double.parseDouble(cursor.getString(3)),	// latitude
						Double.parseDouble(cursor.getString(4)),	// longitude
						Integer.parseInt(cursor.getString(5)),		// radius
						cursor.getString(6),						// memo date
						Integer.parseInt(cursor.getString(7)),		// publicId
						cursor.getString(8));						// android ID
				memoList.add(memo);
			} while (cursor.moveToNext());
		}

		db.close();
		return memoList;
	}

	/* Returns an ArrayList of all memos in the memo database sorted by the shortest distance. */
	public ArrayList<Memo> getAllClosestMemos(LatLng loc) {
		ArrayList<Memo> memoList = new ArrayList<Memo>();
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectFromMemos, null);

		if(cursor.moveToFirst()){
			do {
				Memo memo = new Memo(
						Integer.parseInt(cursor.getString(0)),		// memo ID
						cursor.getString(1),						// memo location
						cursor.getString(2),						// memo body
						Double.parseDouble(cursor.getString(3)),	// latitude
						Double.parseDouble(cursor.getString(4)),	// longitude
						Integer.parseInt(cursor.getString(5)),		// radius
						cursor.getString(6),						// memo date
						Integer.parseInt(cursor.getString(7)),		// publicId
						cursor.getString(8));						// android ID
				memoList.add(memo);
			} while (cursor.moveToNext());
		}
		db.close();

		final LatLng newLoc = loc;
		Collections.sort(memoList, new Comparator<Memo>() {
		    public int compare(Memo lhs, Memo rhs) {
		        return (int) (lhs.getDistance(newLoc) - rhs.getDistance(newLoc));
		    }
		});

		return memoList;
	}

	/* Returns an ArrayList of all memos in the memo database sorted by a specified parameter. */
	public ArrayList<Memo> getAllSortedMemos(String sortBy, boolean asc) {
		if(asc) sortBy = sortBy.concat(" ASC");
		Cursor cursor = getWritableDatabase().rawQuery(selectFromMemos
				+ " ORDER BY " + sortBy, null);
		ArrayList<Memo> memoList = new ArrayList<Memo>();
		if(cursor.moveToFirst()){
			do {
				Memo memo = new Memo(
						Integer.parseInt(cursor.getString(0)),		// memo ID
						cursor.getString(1),						// memo location
						cursor.getString(2),						// memo body
						Double.parseDouble(cursor.getString(3)),	// latitude
						Double.parseDouble(cursor.getString(4)),	// longitude
						Integer.parseInt(cursor.getString(5)),		// radius
						cursor.getString(6),						// memo date
						Integer.parseInt(cursor.getString(7)),		// publicId
						cursor.getString(8));						// android ID
				memoList.add(memo);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return memoList;		
	}

	/* Returns the fave database entry of a specified ID as a Favorite object. */
	public Favorite getFave(int id) {
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

	/* Returns an array of the locations of memos in the database. */
	public ArrayList<LatLng> getMemoLocations(){
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

	/* Returns the memo database entry of a specified ID as a Memo object. */
	public Memo getMemo(int id) {
		table	  = TABLE_MEMOS;
		selection = KEY_MID + "=?";
		String[] args    = new String[]{ String.valueOf(id) }, 
				 columns = new String[]{ KEY_MID, KEY_MLOC, KEY_MBODY, 
							KEY_MLAT, KEY_MLONG, KEY_MRAD, KEY_MDATE, KEY_PUBID, KEY_AID };
		Cursor cursor = getWritableDatabase()
				.query(table, columns, selection, args, null, null, null);
		if (cursor != null) cursor.moveToFirst();

		Memo memo = new Memo(
				Integer.parseInt(cursor.getString(0)),		// memo ID
				cursor.getString(1),						// memo location
				cursor.getString(2),						// memo body
				Double.parseDouble(cursor.getString(3)),	// latitude
				Double.parseDouble(cursor.getString(4)),	// longitude
				Integer.parseInt(cursor.getString(5)),		// radius
				cursor.getString(6),						// memo date
				Integer.parseInt(cursor.getString(7)),		// publicId
				cursor.getString(8));						// android ID
		cursor.close();
		return memo;
	}

	/* Returns the number of favorites in the fave database. */
	public int getFaveCount() {
		Cursor cursor = getWritableDatabase().rawQuery(selectFromFaves, null);
		cursor.close();
		return cursor.getCount();
	}

	/* Returns the number of memos in the memo database. */
	public int getMemoCount() {
		Cursor cursor = getWritableDatabase().rawQuery(selectFromMemos, null);
		cursor.close();
		return cursor.getCount();
	}

	/* Updates a favorite in the fave database. */
	public int updateFave(Favorite fave) {
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

	/* Updates a memo in to memo database. */
	public int updateMemo(Memo memo) {
		ContentValues values = new ContentValues();
		values.put(KEY_MLAT, 	memo.getLatitude());
		values.put(KEY_MLONG, 	memo.getLongitude());
		values.put(KEY_MLOC, 	memo.getLocationName());
		values.put(KEY_MBODY, 	memo.getMemoBody());
		values.put(KEY_MRAD, 	memo.getRadius());
		values.put(KEY_MDATE, 	memo.getMemoDate());
		values.put(KEY_PUBID,   memo.getPublicId());

		table	  = TABLE_MEMOS;
		selection = KEY_MID + "=?";
		String[] whereArgs = { String.valueOf(memo.getId()) };
		return getWritableDatabase().update(table, values, selection, 
				whereArgs);
	}
	
	/* Exports a Memo object to a memo file. */
	public void exportMemo(Memo m) {
		String filename = "memomap_" + m.getId() + ".memo";
		
		File root = Environment.getExternalStorageDirectory();
		File dir  = new File(root.getAbsolutePath() + "/memos");
		dir.mkdirs();
		
		File file = new File(dir, filename);
		
		try{
			FileOutputStream fos = new FileOutputStream(file);
			PrintWriter pw = new PrintWriter(fos);
			pw.println(m.getId());
			pw.println(m.getLocationName());
			pw.println(m.getMemoBody());
			pw.println(m.getLongitude());
			pw.println(m.getLatitude());
			pw.println(m.getRadius());
			pw.println(m.getMemoDate());
			pw.println(m.getPublicId());
			pw.println(m.getAndroidId());

			pw.flush();
			pw.close();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Toast.makeText(MemoMap.getInstance(), file.toString(), Toast.LENGTH_LONG).show();
	}
	
	/* Returns the device's unique Android ID. */
	public static String getAndroidId(){
		return Secure.getString(MemoMap.getInstance().getContentResolver(), Secure.ANDROID_ID);
	}

}
