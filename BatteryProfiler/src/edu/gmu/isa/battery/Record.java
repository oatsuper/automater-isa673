package edu.gmu.isa.battery;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Record extends Activity {

	private String TAG = "Record";
	private BatteryDbAdapter mDbHelper;

	private static final int ACTIVITY_REPORT = 0;

	private EditText mDescText;
	private EditText mAppText;
	private EditText mIntervalText;
	private Long mRunId;

	private String desc;
	private String app;
	private long interval;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDbHelper = new BatteryDbAdapter(this);
		mDbHelper.open();

		setContentView(R.layout.record);

		mDescText = (EditText) findViewById(R.id.desc);
		mAppText = (EditText) findViewById(R.id.app);
		mIntervalText = (EditText) findViewById(R.id.interval);

		Button recordButton = (Button) findViewById(R.id.record);
		Button viewButton = (Button) findViewById(R.id.report_view);

		mRunId = getRunId(savedInstanceState);

		populateFields();

		if (mRunId == null) {
			viewButton.setVisibility(View.GONE);
			recordButton.setVisibility(View.VISIBLE);
			recordButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					if (validate()) {
						startRecording();
						Intent i = new Intent();
						i.putExtra("action", "close");
						setResult(RESULT_OK,i);
						finish();
					} else {
						showAlert("Please fill out all information.");
					}
				}
			});
		} else {
			recordButton.setVisibility(View.GONE);
			viewButton.setVisibility(View.VISIBLE);
			viewButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					viewReport();
				}
			});
		}
	}

	private void viewReport() {
		Log.i(TAG, "viewReport");
		Intent i = new Intent(this, Report.class);
		i.putExtra(BatteryDbAdapter.KEY_RUNID, mRunId);
		startActivityForResult(i, ACTIVITY_REPORT);
	}

	private void populateFields() {
		if (mRunId != null) {
			mDescText.setEnabled(false);
			mAppText.setEnabled(false);
			mIntervalText.setEnabled(false);
			
			Cursor note = mDbHelper.fetchRun(mRunId);
			startManagingCursor(note);
			mDescText.setText(note.getString(
					note.getColumnIndexOrThrow(BatteryDbAdapter.KEY_DESCRIPTION)));
			mAppText.setText(note.getString(
					note.getColumnIndexOrThrow(BatteryDbAdapter.KEY_PROGRAM)));
			mIntervalText.setText(note.getString(
					note.getColumnIndexOrThrow(BatteryDbAdapter.KEY_INTERVAL)));
			getInterval();
			setTitle(mAppText.getText().toString());
		} else {
			setTitle("New Run");
		}
	}

	private Long getRunId(Bundle savedInstanceState) {
		Long runId = savedInstanceState != null ? savedInstanceState.getLong(BatteryDbAdapter.KEY_RUNID) 
				: null;
		if (runId == null) {
			Bundle extras = getIntent().getExtras();            
			runId = extras != null ? extras.getLong(BatteryDbAdapter.KEY_RUNID) 
					: null;
		}
		return runId;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(BatteryDbAdapter.KEY_RUNID, mRunId);
		outState.putLong(BatteryDbAdapter.KEY_INTERVAL, interval);
	}

	private void startRecording() {
		Log.i(TAG, "startRecording");
		long id = mDbHelper.addRun(desc, app, interval);
		if (id > 0) {
			mRunId = id;
		}
		Intent i = new Intent(this, RecordService.class);
		i.putExtra(BatteryDbAdapter.KEY_RUNID, mRunId);
		i.putExtra(BatteryDbAdapter.KEY_INTERVAL, interval);
		startService(i);
	}

	private boolean getInterval() {
		String intervalText = mIntervalText.getText().toString().trim();
		try {
			interval = Long.parseLong(intervalText);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	private boolean validate() {
		desc = mDescText.getText().toString().trim();
		app = mAppText.getText().toString().trim();
		if (desc.length() == 0 
				|| app.length() == 0
				|| !getInterval()
				|| interval <= 0) {
			return false;
		}
		return true;
	}

	private void showAlert(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message)
		.setCancelable(true)
		.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "onDestroy");
		mDbHelper.close();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		finish();
	}
}
