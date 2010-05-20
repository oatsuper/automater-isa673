package edu.gmu.isa.automater;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

public class RecordService extends Service {

	private String TAG = "RecordService";
	private AutoDroidDbAdapter mDbHelper;
	private Long runId = null;

	private java.lang.Process p;
	private Runtime r;
	private Thread t1;
	private boolean running = true;
	private boolean stopCalled = false;
	private long stopTime = 0;
	private BroadcastReceiver buttonReceiver;

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (!stopCalled) {
			stopProcess();
		}
		stopService();
	}

	private void startService() {
		Log.i(TAG, "startService");
		mDbHelper = new AutoDroidDbAdapter(this);
		mDbHelper.open();
		_registerReceiver();
		captureProcess();
	}
	
	private void _registerReceiver() {
		Log.i(TAG, "_registerReceiver");
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_OFF);

		buttonReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				stopProcess();
			}
		};
		registerReceiver(buttonReceiver, filter);
	}

	private void stopService() {
		Log.i(TAG, "stopService");
		unregisterReceiver(buttonReceiver);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		runId = intent.getLongExtra(AutoDroidDbAdapter.KEY_RUNID, 0);
		startService();
		return super.onStartCommand(intent, flags, startId);
	}

	private void captureProcess() {
		Log.i(TAG, "captureProcess");
		t1 = new Thread() {
			@Override
			public void run() {
				try {
					r = Runtime.getRuntime();
					p = r.exec("su");
					DataOutputStream out = new DataOutputStream(p.getOutputStream());
					out.writeBytes("getevent -tq;\n");
					out.flush();

					Log.i(TAG, "Input Stream Started");
					BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()),16);
					String inputText = null;

					long currTime = -1;
					int time = -1;
					int time2 = -1;

					String[] times = null;
					String timeStr = null;
					String timeStr2 = null;
					String type = null;
					String code = null;
					String value = null;
					String device = null;
					String[] split = null;

					while (running && (inputText = in.readLine()) != null) {

						try {
							split = inputText.split(" ");
							times = split[0].split("-");
							timeStr = times[0];
							timeStr2 = times[1].split(":")[0];

							time = Integer.parseInt(timeStr);
							time2 = Integer.parseInt(timeStr2);
							currTime = (time * 1000) + (time2 / 1000);

							device = split[1].split(":")[0];
							
							type = split[2];
							code = split[3];
							value = split[4];
							
							if (device.equals(Constants.DEVICE_KEYS)) {
								if (stopTime == 0 
										&& Integer.parseInt(type,16) == 1 
										&& Integer.parseInt(code,16) == 116 
										&& Integer.parseInt(value,16) == 1) {
									stopTime = currTime;
									Log.i(TAG, "STOPTIME: " + stopTime);
								}
							} else if (device.equals(Constants.DEVICE_TOUCHPAD)) {
								mDbHelper.addTouch(runId, Integer.parseInt(code,16), 
										Integer.parseInt(type, 16),
										Integer.parseInt(value, 16),
										currTime);
							}
						} catch (Exception e) {
							Log.e(TAG, "INPUTTEXT: " + inputText);
							e.printStackTrace();
						}
					}
					if (stopTime > 0) {
						mDbHelper.endRun(runId);
						Log.i(TAG, "Rows Deleted: " + mDbHelper.deleteAfter(runId, stopTime));
					} else {
						mDbHelper.errorRun(runId);
					}
					Log.i(TAG, "Input Stream Ended");
					mDbHelper.close();
					in.close();
					out.close();
					this.join();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		t1.start();
	}

	private void fillBuffer() throws Exception{
		Log.i(TAG, "fillBuffer");
		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					Process process = Runtime.getRuntime().exec("su");
					DataOutputStream out = new DataOutputStream(process.getOutputStream());
					for (int i = 0; i < 1024; i++) {
						out.writeBytes("sendevent " + Constants.DEVICE_TOUCHPAD + " 0 2 0;\n");
						out.flush();
						out.writeBytes("sendevent " + Constants.DEVICE_TOUCHPAD + " 0 0 0;\n");
						out.flush();
					}
					out.close();
					process.waitFor();
					process.destroy();
					Thread.sleep(1000);
					running = false;
					this.join();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		t.start();
	}

	private void stopProcess() {
		Log.i(TAG, "stopProcess");
		stopCalled = true;
		try {
			fillBuffer();
			if (p != null) {
				p.destroy();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
