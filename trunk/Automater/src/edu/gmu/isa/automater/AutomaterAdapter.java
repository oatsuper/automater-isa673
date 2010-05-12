package edu.gmu.isa.automater;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;

public class AutomaterAdapter extends SimpleCursorAdapter {
	private Cursor c;

	public AutomaterAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
		this.c = c;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ImageView imageView = (ImageView) view.findViewById(R.id.statusIcon);
		int state_col = this.c.getColumnIndex(AutomaterDbAdapter.KEY_STATE);
		String state = this.c.getString(state_col);
		if (state.equals(AutomaterDbAdapter.STATE_ERROR)){
			imageView.setImageResource(R.drawable.error_icon);
		} else if (state.equals(AutomaterDbAdapter.STATE_RUNNING)){
			imageView.setImageResource(R.drawable.record);
		} else {
			imageView.setImageResource(R.drawable.automater);
		}
		super.bindView(view, context, cursor);
	}

}
