package com.android.searching;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {
	private static final boolean DEBUG = false;
	private static final String TAG = "SettingsActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View mainView = getLayoutInflater().inflate(R.layout.activity_settings,
				null);
		LinearLayout layout = (LinearLayout) mainView
				.findViewById(R.id.linearLayout_main_settingsActivity);

		// layout.addView(getLayoutInflater().inflate(R.layout.separator2,
		// null));

		// download app only
		View view = getLayoutInflater().inflate(R.layout.setting_item, null);
		TextView textView = (TextView) view
				.findViewById(R.id.textView_itemTitle_settingActivity);
		textView.setText(R.string.setting_name_displayApp);
		textView = (TextView) view
				.findViewById(R.id.textView_itemDesc_settingActivity);
		textView.setText(R.string.setting_desc_displayApp);
		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						SettingsActivity.this);
				builder.setTitle(R.string.setting_name_displayApp);
				// 0 is download only; 1 is all app
				builder.setSingleChoiceItems(R.array.setting_downloadOnly,
						ConfigManager.getDownloadAppOnly() ? 0 : 1,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								ConfigManager.setDownloadAppOnly(which == 0);
								if (DEBUG) {
									Log.d(TAG, "Set downloadAppOnly to "
											+ (which == 0));
								}
								dialog.dismiss();
							}
						});
				AlertDialog dialog = builder.create();
				dialog.show();
			}
		});
		layout.addView(view);

		// file types
		view = getLayoutInflater().inflate(R.layout.setting_item, null);
		textView = (TextView) view
				.findViewById(R.id.textView_itemTitle_settingActivity);
		textView.setText(R.string.setting_name_fileTypes);
		textView = (TextView) view
				.findViewById(R.id.textView_itemDesc_settingActivity);
		textView.setText(R.string.setting_desc_fileTypes);
		// TODO add pictures
		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final boolean[] checked = ConfigManager.getFileTypesChecked()
						.clone();
				AlertDialog.Builder builder = new AlertDialog.Builder(
						SettingsActivity.this);
				builder.setTitle(R.string.setting_name_fileTypes);
				builder.setMultiChoiceItems(R.array.setting_fileTypes,
						ConfigManager.getFileTypesChecked(),
						new OnMultiChoiceClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which, boolean isChecked) {
								checked[which] = isChecked;
							}
						});
				builder.setPositiveButton(R.string.setting_fileTypes_save,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								ConfigManager.setFileTypesChecked(checked);
							}
						});
				builder.setNegativeButton(R.string.setting_fileTypes_cancel,
						null);
				AlertDialog dialog = builder.create();
				dialog.show();
			}
		});
		layout.addView(view);

		setContentView(mainView);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

	@Override
	public void onPause() {
		super.onPause();
		ConfigManager.writeImmediately();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_displayAll:
			Toast.makeText(this, ConfigManager.getConfigs(), Toast.LENGTH_LONG)
					.show();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
