package edu.gmu.isa.automater;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Replay extends Activity {

	private String TAG = "Replay";

	private EditText mCountText;
	private Long mRunId;

	private long count;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.replay);

		mCountText = (EditText) findViewById(R.id.replay_count);

		Button replayButton = (Button) findViewById(R.id.replay);

		mRunId = getRunId(savedInstanceState);

		replayButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if (validate()) {
					startReplaying();
					setResult(RESULT_OK);
					finish();
				} else {
					showAlert();
				}
			}
		});
	}

	private Long getRunId(Bundle savedInstanceState) {
		Long runId = savedInstanceState != null ? savedInstanceState.getLong(AutomaterDbAdapter.KEY_RUNID) 
				: null;
		if (runId == null) {
			Bundle extras = getIntent().getExtras();            
			runId = extras != null ? extras.getLong(AutomaterDbAdapter.KEY_RUNID) 
					: null;
		}
		return runId;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(AutomaterDbAdapter.KEY_RUNID, mRunId);
	}

	private void startReplaying() {
		Log.i(TAG, "startReplaying: runId - " + mRunId);
		Intent i = new Intent(this, PlayBackService.class);
		i.putExtra(AutomaterDbAdapter.KEY_RUNID, mRunId);
		i.putExtra("count", count);
		startService(i);
	}

	private boolean validate() {
		String countText = mCountText.getText().toString().trim();
		try {
			count = Long.parseLong(countText);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return false;
		}

		if (count <= 0) {
			return false;
		}
		return true;
	}

	private void showAlert() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Please fill out all information.")
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
	}
}
