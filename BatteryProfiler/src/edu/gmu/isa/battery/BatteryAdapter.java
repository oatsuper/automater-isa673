package edu.gmu.isa.battery;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;

public class BatteryAdapter extends SimpleCursorAdapter {
	private Cursor c;
	
	public BatteryAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
		this.c = c;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ImageView imageView = (ImageView) view.findViewById(R.id.statusIcon);
		int state_col = this.c.getColumnIndex(BatteryDbAdapter.KEY_STATE);
		if (state_col < 0) {
			imageView.setImageResource(R.drawable.battery);
		} else {
			String state = this.c.getString(state_col);
			if (state.equals(BatteryDbAdapter.STATE_RUNNING)){
				imageView.setImageResource(R.drawable.record);
			} else {
				imageView.setImageResource(R.drawable.report);
			}
		}
		super.bindView(view, context, cursor);
	}
	
}
