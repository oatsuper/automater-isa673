package edu.gmu.isa.automater;

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
	private AutoDroidDbAdapter mDbHelper;

	private static final int ACTIVITY_REPLAY = 0;

	private EditText mDescText;
	private EditText mAppText;
	private Long mRunId;

	private String desc;
	private String app;
	private boolean errorState = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDbHelper = new AutoDroidDbAdapter(this);
		mDbHelper.open();

		setContentView(R.layout.record);

		mDescText = (EditText) findViewById(R.id.desc);
		mAppText = (EditText) findViewById(R.id.app);

		Button recordButton = (Button) findViewById(R.id.record);
		Button replayButton = (Button) findViewById(R.id.replay);

		mRunId = getRunId(savedInstanceState);

		populateFields();

		if (mRunId == null) {
			replayButton.setVisibility(View.GONE);
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
			replayButton.setEnabled(!errorState);
			replayButton.setVisibility(View.VISIBLE);
			replayButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					viewReplay();
					Intent i = new Intent();
					i.putExtra("action", "close");
					setResult(RESULT_OK,i);
				}
			});
		}
	}
	
	private void viewReplay() {
		Log.i(TAG, "viewReplay");
		Intent i = new Intent(this, Replay.class);
		i.putExtra(AutoDroidDbAdapter.KEY_RUNID, mRunId);
		startActivityForResult(i, ACTIVITY_REPLAY);
	}

	private void populateFields() {
		if (mRunId != null) {
			mDescText.setEnabled(false);
			mAppText.setEnabled(false);
			
			Cursor note = mDbHelper.fetchRun(mRunId);
			startManagingCursor(note);
			mDescText.setText(note.getString(
					note.getColumnIndexOrThrow(AutoDroidDbAdapter.KEY_DESCRIPTION)));
			mAppText.setText(note.getString(
					note.getColumnIndexOrThrow(AutoDroidDbAdapter.KEY_PROGRAM)));
			errorState = (note.getString(
					note.getColumnIndexOrThrow(AutoDroidDbAdapter.KEY_STATE)).
					equals(AutoDroidDbAdapter.STATE_ERROR)) ? true : false;
			if (errorState) {
				showAlert("Error while recording. Please press the power button to indicate the end of capturing."
						+ " This capture cannot be replayed.");
			}
			setTitle(mAppText.getText().toString());
		} else {
			setTitle("New Run");
		}
	}

	private Long getRunId(Bundle savedInstanceState) {
		Long runId = savedInstanceState != null ? savedInstanceState.getLong(AutoDroidDbAdapter.KEY_RUNID) 
				: null;
		if (runId == null) {
			Bundle extras = getIntent().getExtras();            
			runId = extras != null ? extras.getLong(AutoDroidDbAdapter.KEY_RUNID) 
					: null;
		}
		return runId;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(AutoDroidDbAdapter.KEY_RUNID, mRunId);
	}

	private void startRecording() {
		Log.i(TAG, "startRecording");
		long id = mDbHelper.addRun(desc, app);
		if (id > 0) {
			mRunId = id;
		}
		Intent i = new Intent(this, RecordService.class);
		i.putExtra(AutoDroidDbAdapter.KEY_RUNID, mRunId);
		startService(i);
	}
	
	private boolean validate() {
		desc = mDescText.getText().toString().trim();
		app = mAppText.getText().toString().trim();
		if (desc.length() == 0 
				|| app.length() == 0) {
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
