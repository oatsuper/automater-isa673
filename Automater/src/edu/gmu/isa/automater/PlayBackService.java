package edu.gmu.isa.automater;

import java.io.DataOutputStream;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class PlayBackService extends Service {

	private String TAG = "PlayBackService";
	private AutomaterDbAdapter mDbHelper;

	private Long runId = null;

	private java.lang.Process p;
	private Thread t;
	private DataOutputStream out;

	@Override
	public void onDestroy() {
		Toast message = Toast.makeText(this.getApplicationContext(),
				"Playback Completed",
				Toast.LENGTH_LONG);
		message.setGravity(Gravity.CENTER, 0, 0);
		message.show();

		stopProcess();
		stopService();
		super.onDestroy();
	}

	private void startService(long count) {
		Log.i(TAG, "startService - count: " + count);
		Toast message = Toast.makeText(this.getApplicationContext(),
				"Playback Started",
				Toast.LENGTH_SHORT);
		message.setGravity(Gravity.CENTER, 0, 0);
		message.show();
		
		mDbHelper = new AutomaterDbAdapter(this);
		mDbHelper.open();
		sendProcess(count);
	}

	private void stopService() {
		Log.i(TAG, "stopService");
		mDbHelper.close();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		long count = intent.getLongExtra("count", 1);
		runId = intent.getLongExtra(AutomaterDbAdapter.KEY_RUNID, 0);
		startService(count);
		return super.onStartCommand(intent, flags, startId);
	}

	private void sendProcess(final long count) {
		Log.i(TAG, "sendProcess");
		final Service service = this;
		t = new Thread() {
			@Override
			public void run() {
				try {

					p = Runtime.getRuntime().exec("su");
					out = new DataOutputStream(p.getOutputStream());
					String command = null;
					long prevTime = mDbHelper.getFirstTouchTime(runId);
					long currTime = -1;
					long sleep = 0;
					Cursor runCursor = mDbHelper.fetchTouchData(runId);
					for (int i = 0; i < count; i++) {
						Thread.sleep(2000);
						for (int j = 0; j < runCursor.getCount(); j++) {
							command = "sendevent " + Constants.DEVICE_TOUCHPAD + " " 
								+ runCursor.getLong(runCursor.getColumnIndex(AutomaterDbAdapter.KEY_TYPE)) + " "
								+ runCursor.getLong(runCursor.getColumnIndex(AutomaterDbAdapter.KEY_CODE)) + " "
								+ runCursor.getLong(runCursor.getColumnIndex(AutomaterDbAdapter.KEY_VALUE));
							currTime = runCursor.getLong(runCursor.getColumnIndex(AutomaterDbAdapter.KEY_CURRENT_TIME));
							sleep = currTime - prevTime;
							prevTime = currTime;
							
							if (sleep > 0) {
								Thread.sleep(sleep);
							} else {
								Thread.sleep(1);
							}

							out.writeBytes(command + ";\n");
							out.flush();
							
							runCursor.moveToNext();
							if (runCursor.isAfterLast()) {
								break;
							}
						}
						runCursor.moveToFirst();
					}
					runCursor.close();
					Thread.sleep(500);
					out.writeBytes("exit\n");
					out.flush();
					service.stopSelf();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		t.start();
	}

	private void stopProcess() {
		Log.i(TAG, "stopProcess");
		try {
			if (t != null) {
				t.join();
			}
			if (out != null) {
				out.close();
			}
			if (p != null) {
				p.destroy();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
