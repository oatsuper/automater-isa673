package edu.gmu.isa.battery;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;
import android.util.Log;

public class RecordService extends Service {

	private String TAG = "BatteryService";
	private BatteryDbAdapter mDbHelper;

	private Timer timer = new Timer();
	private Long runId = null;
	private BroadcastReceiver battReceiver;

	@Override
	public void onDestroy() {
		stopService();
		super.onDestroy();
	}

	private void _registerReceiver(final long runId) {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_BATTERY_CHANGED);

		battReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				context.unregisterReceiver(this);
				int mah = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
				mDbHelper.addRecord(runId, mah);
			}
		};

		registerReceiver(battReceiver, filter);
	}

	private void startService(final long interval) {
		Log.i(TAG, "startService - interval: " + interval);
		mDbHelper = new BatteryDbAdapter(this);
		mDbHelper.open();
		timer.scheduleAtFixedRate( new TimerTask() {
			@Override
			public void run() {
				Log.i(TAG,"Interval: " + interval + " runId: " + runId);
				_registerReceiver(runId);
			}
		}, 0, interval * 1000);
	}

	private void stopService() {
		Log.i(TAG, "stopService");
		mDbHelper.endRun(runId);
		mDbHelper.close();
		if (timer != null){
			timer.cancel();
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		long interval = intent.getLongExtra(BatteryDbAdapter.KEY_INTERVAL, 5);
		runId = intent.getLongExtra(BatteryDbAdapter.KEY_RUNID, 0);
		startService(interval);
		return super.onStartCommand(intent, flags, startId);
	}
}
