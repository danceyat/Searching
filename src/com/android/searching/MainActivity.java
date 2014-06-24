package com.android.searching;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.HandlerThread;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.searching.ContentManager.Results;
import com.android.searching.engines.Engine;

public class MainActivity extends Activity implements View.OnClickListener {
	private static final boolean __FUTURE__ = false;
	private int mPreviewCount;
	private final int CONTENT_DRAWABLE_SIZE = 72;
	public static final String EXTRA_RESULT_TYPE = "resultType";

	EditText mEditText = null;
	LinearLayout mLinearLayout = null;
	ContentManager mContentManager = null;
	ConfigManager mConfigManager = null;
	Button mButton = null;
	HandlerThread mThread = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View view = getLayoutInflater().inflate(R.layout.activity_main, null);
		mLinearLayout = (LinearLayout) view
				.findViewById(R.id.linearLayout_results_mainActivity);
		mEditText = (EditText) view
				.findViewById(R.id.editText_pattern_mainActivity);
		if (__FUTURE__) {
			mEditText.addTextChangedListener(new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
					// TODO Auto-generated method stub
					mLinearLayout.removeAllViews();
					String pattern = mEditText.getText().toString();
					mContentManager.search(pattern);
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
					// TODO Auto-generated method stub

				}

				@Override
				public void afterTextChanged(Editable s) {
					// TODO Auto-generated method stub

				}
			});
		}
		mButton = (Button) view.findViewById(R.id.button_search_mainActivity);
		mButton.setOnClickListener(this);
		setContentView(view);

		mThread = new HandlerThread("manager");
		mThread.start();
		mContentManager = new ContentManager(this, mThread.getLooper());
		mConfigManager = new ConfigManager(getFilesDir());

		mPreviewCount = ConfigManager.getPreviewCount();
	}

	@Override
	protected void onPause() {
		super.onPause();
		ConfigManager.writeImmediately();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_selectContent:
			selectContent();
			break;
		case R.id.action_settings:
			startActivity(new Intent(getApplicationContext(),
					SettingsActivity.class));
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void selectContent() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.action_selectContent);
		View view = getLayoutInflater().inflate(R.layout.select_content, null);
		final GridView gridView = (GridView) view
				.findViewById(R.id.gridView_selectContent_alertDialog);
		final CheckBox checkBox = (CheckBox) view
				.findViewById(R.id.checkBox_selectAll_alertDialog);

		gridView.setAdapter(new ArrayAdapter<String>(this,
				R.layout.select_content_item, ContentManager.ALL) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				TextView textView = null;
				if (convertView == null) {
					textView = (TextView) MainActivity.this.getLayoutInflater()
							.inflate(R.layout.select_content_item, null);
				} else {
					textView = (TextView) convertView;
				}

				String type = getItem(position);
				textView.setText(getString(ContentManager.TITLE_RESOURCES
						.get(type)));
				int drawableRes = mContentManager.containsContent(type) ? ContentManager.ICON_RESOURCES_SELECTED
						.get(type) : ContentManager.ICON_RESOURCES.get(type);
				Drawable drawable = getResources().getDrawable(drawableRes);
				if (drawable != null) {
					drawable.setBounds(0, 0, CONTENT_DRAWABLE_SIZE,
							CONTENT_DRAWABLE_SIZE);
					textView.setCompoundDrawables(null, drawable, null, null);
				}
				return textView;
			}
		});
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String type = parent.getAdapter().getItem(position).toString();
				if (mContentManager.containsContent(type)) {
					mContentManager.removeContent(new String[] { type });
					TextView textView = (TextView) view;
					int drawableRes = ContentManager.ICON_RESOURCES.get(type);
					Drawable drawable = getResources().getDrawable(drawableRes);
					if (drawable != null) {
						drawable.setBounds(0, 0, CONTENT_DRAWABLE_SIZE,
								CONTENT_DRAWABLE_SIZE);
						textView.setCompoundDrawables(null, drawable, null,
								null);
					}
				} else {
					mContentManager.addContent(new String[] { type });
					TextView textView = (TextView) view;
					int drawableRes = ContentManager.ICON_RESOURCES_SELECTED
							.get(type);
					Drawable drawable = getResources().getDrawable(drawableRes);
					if (drawable != null) {
						drawable.setBounds(0, 0, CONTENT_DRAWABLE_SIZE,
								CONTENT_DRAWABLE_SIZE);
						textView.setCompoundDrawables(null, drawable, null,
								null);
					}
				}
				checkBox.setChecked(mContentManager.containsAll());
			}
		});

		checkBox.setChecked(mContentManager.containsAll());
		checkBox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean isChecked = checkBox.isChecked();
				if (isChecked) {
					mContentManager.addContent(ContentManager.ALL);
				} else {
					mContentManager.removeAll();
				}
				ListAdapter adapter = gridView.getAdapter();
				for (int i = 0; i < adapter.getCount(); i++) {
					TextView textView = (TextView) gridView.getChildAt(i);
					String type = adapter.getItem(i).toString();
					int drawableRes = isChecked ? ContentManager.ICON_RESOURCES_SELECTED
							.get(type) : ContentManager.ICON_RESOURCES
							.get(type);
					Drawable drawable = getResources().getDrawable(drawableRes);
					if (drawable != null) {
						drawable.setBounds(0, 0, CONTENT_DRAWABLE_SIZE,
								CONTENT_DRAWABLE_SIZE);
						textView.setCompoundDrawables(null, drawable, null,
								null);
					}
				}
			}
		});
		builder.setView(view);
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_search_mainActivity:
			mLinearLayout.removeAllViews();
			mButton.setEnabled(false);
			String pattern = mEditText.getText().toString();
			mContentManager.search(pattern);
			break;
		}
	}

	public void onFinishAttaching(final boolean isDone) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (!isDone) {
					Toast.makeText(MainActivity.this,
							R.string.hint_noContentSelected_mainActivity,
							Toast.LENGTH_SHORT).show();
				}
				// TODO nothing is found, still show something
				if (mLinearLayout.getChildCount() == 0) {

				}
				mButton.setEnabled(true);
			}
		});
	}

	private void attachResultsOnUi(final Results results) {
		if (results.size() > 0) {
			// add header
			View view = getLayoutInflater()
					.inflate(R.layout.results_head, null);
			TextView title = (TextView) view
					.findViewById(R.id.textView_resultTitle);
			TextView count = (TextView) view
					.findViewById(R.id.textView_resultCount);

			title.setText(ContentManager.TITLE_RESOURCES.get(results.getType()));
			count.setText("(" + results.size() + ")");
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(MainActivity.this,
							ListActivity.class);
					intent.putExtra(EXTRA_RESULT_TYPE, results.getType());
					startActivity(intent);
				}
			});
			mLinearLayout.addView(view);

			// add content
			int index = 0, max = Math.min(mPreviewCount, results.size());
			while (index < max) {
				if (index != 0) {
					mLinearLayout.addView(getLayoutInflater().inflate(
							R.layout.separator, null));
				}
				view = getLayoutInflater()
						.inflate(R.layout.results_item2, null);
				final Engine.IResult result = results.get(index);
				mLinearLayout.addView(Utils.formatResult(view, result,
						MainActivity.this));
				index++;
			}
		}
	}

	// this method may be called from other thread
	public void attachResults(final Results results) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				attachResultsOnUi(results);
			}
		});
	}
}
