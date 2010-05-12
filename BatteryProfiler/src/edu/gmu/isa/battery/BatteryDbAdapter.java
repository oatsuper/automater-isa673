package edu.gmu.isa.battery;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class BatteryDbAdapter {

	private static final String TAG = "BatteryDbAdapter";

	public static final String STATE_RUNNING = "running";
	public static final String STATE_ENDING = "ending";
	public static final String STATE_STOPPED = "stopped";
	
	public static final String KEY_RUNID = "_id";
	public static final String KEY_STATE = "state";
	public static final String KEY_INTERVAL = "interval";
	public static final String KEY_DESCRIPTION = "desc";
	public static final String KEY_PROGRAM = "app";
	public static final String KEY_START_TIME = "sdate";
	public static final String KEY_END_TIME = "edate";

	public static final String KEY_CAPID = "_capId";
	public static final String KEY_CAPTURE_TIME = "cdate";
	public static final String KEY_VOLTAGE = "voltage";

	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;


	private static final String DATABASE_NAME = "profiler";
	private static final String RUN_TABLE = "run_meta_data";
	private static final String RECORD_BATTERY_TABLE = "run_data";
	private static final int DATABASE_VERSION = 1;

	private static final String DATABASE_CREATE = "create table "
		+ RUN_TABLE + "("
		+ KEY_RUNID + " integer primary key autoincrement, "
		+ KEY_STATE + " text not null,"
		+ KEY_INTERVAL + " integer not null,"
		+ KEY_DESCRIPTION + " text not null,"
		+ KEY_PROGRAM + " text not null,"
		+ KEY_START_TIME + " date,"
		+ KEY_END_TIME + " date);";

	private static final String DATABASE_CREATE2 = "create table "
		+ RECORD_BATTERY_TABLE + "("
		+ KEY_CAPID + " integer primary key autoincrement, "
		+ KEY_RUNID + " integer not null,"
		+ KEY_VOLTAGE + " integer not null,"
		+ KEY_CAPTURE_TIME + " date);";

	private final Context mCtx;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
			db.execSQL(DATABASE_CREATE2);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + RUN_TABLE);
			onCreate(db);
		}
	}

	public BatteryDbAdapter(Context ctx) {
		this.mCtx = ctx;
	}

	public BatteryDbAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		Log.i(TAG,"close");
		mDbHelper.close();
	}

	private String getDate() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm::ss");
		Date date = new Date();
		return dateFormat.format(date);
	}

	public long addRun(String desc, String app, long interval) {
		if (mDb.isOpen()) {
			Log.i(TAG, "AddRun - Desc: " + desc + "\nApp: " + app);
			ContentValues insertValues = new ContentValues();
			insertValues.put(KEY_STATE, STATE_RUNNING);
			insertValues.put(KEY_INTERVAL, interval);
			insertValues.put(KEY_DESCRIPTION, desc);
			insertValues.put(KEY_PROGRAM, app);
			insertValues.put(KEY_START_TIME, getDate());
			return mDb.insert(RUN_TABLE, null, insertValues);
		}
		return 0;
	}
	
	public boolean startEndRun(long runId) {
		if (mDb.isOpen()) {
			Log.i(TAG, "EndRun - RunId: " + runId);
			ContentValues args = new ContentValues();
			args.put(KEY_STATE, STATE_ENDING);
			return mDb.update(RUN_TABLE, args, KEY_RUNID + "=" + runId, null) > 0;
		}
		return false;
	}

	public boolean endRun(long runId) {
		if (mDb.isOpen()) {
			Log.i(TAG, "EndRun - RunId: " + runId);
			ContentValues args = new ContentValues();
			args.put(KEY_STATE, STATE_STOPPED);
			args.put(KEY_END_TIME, getDate());
			return mDb.update(RUN_TABLE, args, KEY_RUNID + "=" + runId, null) > 0;
		}
		return false;
	}

	public long addRecord(long runId, long voltage) {
		if (mDb.isOpen()) {
			Log.i(TAG, "AddRecord - RunId: " + runId + " - Voltage: " + voltage);
			ContentValues insertValues = new ContentValues();
			insertValues.put(KEY_RUNID, runId);
			insertValues.put(KEY_VOLTAGE, voltage);
			insertValues.put(KEY_CAPTURE_TIME, getDate());
			return mDb.insert(RECORD_BATTERY_TABLE, null, insertValues);
		}
		return 0;
	}

	public boolean deleteRun(long runId) {
		Log.i(TAG, "DeleteRun - RunId: " + runId);
		int i = mDb.delete(RUN_TABLE, KEY_RUNID + "=" + runId, null);
		i += mDb.delete(RECORD_BATTERY_TABLE, KEY_RUNID + "=" + runId, null);
		return i > 0;
	}

	/**
	 * We should not use this so I won't have any functions linked to it in the meantime
	 * @return
	 */
	public boolean deleteAll(){
		Log.i(TAG, "Delete All");
		int i = mDb.delete(RUN_TABLE, null, null);
		i += mDb.delete(RECORD_BATTERY_TABLE, null, null);
		return i > 0;
	}

	/**
	 * Return a Cursor over the list of all notes in the database
	 * 
	 * @return Cursor over all notes
	 */
	public Cursor fetchAllRuns() {
		Log.i(TAG, "Fetch All Runs");
		return mDb.query(RUN_TABLE, new String[] {KEY_RUNID,
				KEY_STATE,
				KEY_INTERVAL,
				KEY_DESCRIPTION,
				KEY_PROGRAM,
				KEY_START_TIME,
				KEY_END_TIME},
				null, null, null, null, KEY_START_TIME);
	}

	public Cursor fetchRun(long runId) throws SQLException {
		Cursor mCursor =
			mDb.query(true, RUN_TABLE, new String[] {KEY_RUNID,
					KEY_STATE, KEY_INTERVAL, KEY_DESCRIPTION,
					KEY_PROGRAM, KEY_START_TIME, KEY_END_TIME},
					KEY_RUNID + "=" + runId, null,
					null, null, null, null);
		Log.i(TAG, "FetchRun - RunId: " + runId);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public Cursor fetchRunData(long runId) throws SQLException {
		Cursor mCursor =
			mDb.query(true, RECORD_BATTERY_TABLE, new String[] {KEY_RUNID,
					KEY_CAPID, KEY_VOLTAGE, KEY_CAPTURE_TIME},
					KEY_RUNID + "=" + runId, null,
					null, null, KEY_CAPID, null);
		Log.i(TAG, "FetchRunData - RunId: " + runId);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	public boolean isRunning(long runId) throws SQLException {
		Cursor mCursor =
			mDb.query(true, RUN_TABLE, new String[] {KEY_STATE},
					KEY_RUNID + "=" + runId, null,
					null, null, null, null);
		Log.i(TAG, "isRunning - RunId: " + runId);
		if (mCursor != null && mCursor.getCount() > 0) {
			mCursor.moveToFirst();
			String result = mCursor.getString(mCursor.getColumnIndex(BatteryDbAdapter.KEY_STATE));
			mCursor.close();
			return result.equals(STATE_RUNNING);
		}
		return false;
	}
	
	public Cursor getPrevEndRun() throws SQLException {
		Cursor mCursor =
			mDb.query(true, RUN_TABLE, new String[] {KEY_RUNID},
					KEY_STATE + "='" + STATE_ENDING + "'", null,
					null, null, null, null);
		Log.i(TAG, "getCurrentRun");
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor; 
	}
	
	public Cursor getCurrentRun() throws SQLException {
		Cursor mCursor =
			mDb.query(true, RUN_TABLE, new String[] {KEY_RUNID},
					KEY_STATE + "='" + STATE_RUNNING + "'", null,
					null, null, null, null);
		Log.i(TAG, "getCurrentRun");
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor; 
	}
}
