package edu.gmu.isa.automater;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class AutoDroid extends ListActivity {

	private static final String TAG = "Automater";

	private static final int ACTIVITY_RECORD = 0;
	private static final int ACTIVITY_STOP = 1;

	private static final int RECORD_ID = Menu.FIRST;
	private static final int DELETE_ALL_ID = Menu.FIRST + 1;
	private static final int DELETE_ID = Menu.FIRST + 2;

	private AutoDroidDbAdapter mDbHelper;
	private ProgressDialog progress;
	private ProgressThread progressThread;

	final Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			String status = msg.getData().getString("status");
			if (status.equals(AutoDroidDbAdapter.STATE_STOPPED)) {
				progress.dismiss();
				progressThread.setState(ProgressThread.STATE_DONE);
				fillData();
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.runs_list);
		registerForContextMenu(getListView());

		Log.i(TAG, "onCreate");

		mDbHelper = new AutoDroidDbAdapter(this);
		mDbHelper.open();

		//Stop any recording
		Cursor cursor = mDbHelper.getCurrentRun();
		if (cursor.getCount() > 0) {
			long currentRunId = cursor.getLong(cursor.getColumnIndex(AutoDroidDbAdapter.KEY_RUNID));
			if (currentRunId > 0) {
				Log.i(TAG, "currentRun: " + currentRunId);
				stopRecording(currentRunId);
			}
		}
		cursor.close();

		fillData();
	}

	private void fillData() {
		Cursor runCursor = mDbHelper.fetchAllRuns();
		startManagingCursor(runCursor);

		String[] from = new String[]{AutoDroidDbAdapter.KEY_DESCRIPTION,
				AutoDroidDbAdapter.KEY_PROGRAM};

		int[] to = new int[]{R.id.text1, R.id.text2};

		AutoDroidAdapter runs = 
			new AutoDroidAdapter(this, R.layout.runs_row, runCursor, from, to);
		setListAdapter(runs);
	}

	protected void stopRecording(long runId) {
		Intent i = new Intent(this, RecordService.class);
		i.putExtra(AutoDroidDbAdapter.KEY_RUNID, runId);
		stopService(i);
		
		progress = new ProgressDialog(AutoDroid.this);
		progress.setMessage("Saving previous run.");
		progress.setCancelable(false);
		progress.show();

		progressThread = new ProgressThread(handler, runId);
		progressThread.start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		Log.i(TAG, "onCreateOptionsMenu");
		menu.add(0, RECORD_ID, 0, R.string.record_new);
		menu.add(1, DELETE_ALL_ID, 0, R.string.delete_all);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch(item.getItemId()) {
		case RECORD_ID:
			createRun();
			return true;
		case DELETE_ALL_ID:
			mDbHelper.deleteAll();
			fillData();
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	private void createRun() {
		Log.i(TAG, "createRun");
		Intent i = new Intent(this, Record.class);
		startActivityForResult(i, ACTIVITY_RECORD);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent i = new Intent(this, Record.class);
		i.putExtra(AutoDroidDbAdapter.KEY_RUNID, id);
		startActivityForResult(i, ACTIVITY_STOP);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (intent != null 
				&& intent.getStringExtra("action").equals("close")) {
			setResult(RESULT_OK);
			finish();
		} else {
			fillData();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(TAG,"onDestroy");
		mDbHelper.close();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, DELETE_ID, 0, R.string.menu_delete);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case DELETE_ID:
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
			mDbHelper.deleteRun(info.id);
			fillData();
			return true;
		}
		return super.onContextItemSelected(item);
	}

	private class ProgressThread extends Thread {
		Handler mHandler;
		long mRunId;
		final static int STATE_DONE = 0;
		final static int STATE_RUNNING = 1;
		int mState;

		ProgressThread(Handler h, long runId) {
			mHandler = h;
			mRunId = runId;
		}

		public void run() {
			mState = STATE_RUNNING;
			while (mState == STATE_RUNNING) {
				try {
					while(mDbHelper.isRunning(mRunId)) {
						Thread.sleep(1500);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				Message msg = mHandler.obtainMessage();
				Bundle b = new Bundle();
				b.putString("status", AutoDroidDbAdapter.STATE_STOPPED);
				msg.setData(b);
				mHandler.sendMessage(msg);
			}
		}

		public void setState(int state) {
			mState = state;
		}
	}
}