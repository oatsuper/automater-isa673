package edu.gmu.isa.battery;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

public class Report extends ListActivity {

    private static final String TAG = "Report";

	private BatteryDbAdapter mDbHelper;
	private Long mRunId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.runs_list);

		Log.i(TAG, "onCreate");

		mDbHelper = new BatteryDbAdapter(this);
		mDbHelper.open();
		
		Bundle extras = getIntent().getExtras();            
		mRunId = extras != null ? extras.getLong(BatteryDbAdapter.KEY_RUNID) 
				: null;

		fillData();
	}

	private void fillData() {
		Cursor runCursor = mDbHelper.fetchRunData(mRunId);
		startManagingCursor(runCursor);
		
		String[] from = new String[]{BatteryDbAdapter.KEY_VOLTAGE,
				BatteryDbAdapter.KEY_CAPTURE_TIME};

		int[] to = new int[]{R.id.text1, R.id.text2};

		BatteryAdapter data = 
			new BatteryAdapter(this, R.layout.runs_row, runCursor, from, to);
		setListAdapter(data);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(TAG,"onDestroy");
		mDbHelper.close();
	}

}