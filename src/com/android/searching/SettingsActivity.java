package com.android.searching;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {
	private TextView mPreviewNum;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View view = (LinearLayout) getLayoutInflater().inflate(
				R.layout.activity_settings, null);
		mPreviewNum = (TextView) view
				.findViewById(R.id.editText_previewNum_settingsActivity);
		setContentView(view);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings_save:
			int previewNum = Integer.parseInt(mPreviewNum.getText().toString());
			Toast.makeText(this, "num is " + previewNum, Toast.LENGTH_LONG)
					.show();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
