package com.android.searching;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.HandlerThread;
import android.view.LayoutInflater;
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
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;
import com.android.searching.ContentManager.Results;
import com.android.searching.engines.Engine;

public class MainActivity extends Activity implements View.OnClickListener {
	private final int PREVIEW_NUM = 3;
	private final int PREVIEW_DRAWABLE_SIZE = ListActivity.DRAWABLE_SIZE;
	public static final String EXTRA_RESULT_TYPE = "resultType";

	TextView mTextView = null;
	LinearLayout mLinearLayout = null;
	ContentManager mContentManager = null;
	Button mButton = null;
	HandlerThread mThread = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View view = getLayoutInflater().inflate(R.layout.activity_main, null);
		mLinearLayout = (LinearLayout) view
				.findViewById(R.id.linearLayout_results_mainActivity);
		mTextView = (TextView) view
				.findViewById(R.id.editText_pattern_mainActivity);
		mButton = (Button) view.findViewById(R.id.button_search_mainActivity);
		mButton.setOnClickListener(this);
		setContentView(view);

		mThread = new HandlerThread("manager");
		mThread.start();
		mContentManager = new ContentManager(this, mThread.getLooper());
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
			startActivity(new Intent(this, SettingsActivity.class));
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
				R.layout.alertdialog_content_item, ContentManager.ALL) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				TextView textView = null;
				if (convertView == null) {
					LayoutInflater li = (LayoutInflater) getContext()
							.getSystemService(LAYOUT_INFLATER_SERVICE);
					textView = (TextView) li.inflate(
							R.layout.alertdialog_content_item, null);
				} else {
					textView = (TextView) convertView;
				}

				String type = getItem(position);
				textView.setText(getString(ContentManager.TITLE_RESOURCES
						.get(type)));
				int drawable = mContentManager.containsContent(type) ? ContentManager.ICON_RESOURCES_SELECTED
						.get(type) : ContentManager.ICON_RESOURCES.get(type);
				textView.setCompoundDrawablesWithIntrinsicBounds(0, drawable,
						0, 0);
				return textView;
			}
		});
		gridView.setColumnWidth(120);
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String type = parent.getAdapter().getItem(position).toString();
				if (mContentManager.containsContent(type)) {
					mContentManager.removeContent(new String[] { type });
					TextView textView = (TextView) view;
					textView.setCompoundDrawablesWithIntrinsicBounds(0,
							ContentManager.ICON_RESOURCES.get(type), 0, 0);
				} else {
					mContentManager.addContent(new String[] { type });
					TextView textView = (TextView) view;
					textView.setCompoundDrawablesWithIntrinsicBounds(0,
							ContentManager.ICON_RESOURCES_SELECTED.get(type),
							0, 0);
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
					int drawable = isChecked ? ContentManager.ICON_RESOURCES_SELECTED
							.get(type) : ContentManager.ICON_RESOURCES
							.get(type);
					textView.setCompoundDrawablesWithIntrinsicBounds(0,
							drawable, 0, 0);
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
			String pattern = mTextView.getText().toString();
			mContentManager.search(pattern);
			break;
		}
	}

	public void onFinishAttaching() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mButton.setEnabled(true);
			}
		});
	}

	public void attachResults(final Results results) {
		if (results.size() > 0) {
			View view = getLayoutInflater()
					.inflate(R.layout.results_head, null);
			TextView title = (TextView) view
					.findViewById(R.id.textView_resultTitle);
			TextView count = (TextView) view
					.findViewById(R.id.textView_resultCount);

			title.setText(ContentManager.TITLE_RESOURCES.get(results.getType()));
			count.setText("(" + results.size() + ")");
			mLinearLayout.addView(view);
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(MainActivity.this,
							ListActivity.class);
					intent.putExtra(EXTRA_RESULT_TYPE, results.getType());
					startActivity(intent);
				}
			});

			int index = 0, max = Math.min(PREVIEW_NUM, results.size());
			while (index < max) {
				if (index != 0) {
					mLinearLayout.addView(getLayoutInflater().inflate(
							R.layout.separator, null));
				}
				TextView item = (TextView) getLayoutInflater().inflate(
						R.layout.listitem, null);
				final Engine.IResult result = results.get(index);
				// TODO reduce output height and width, in ListActivity
				String[] texts = result.getText().split("\n");
				if (texts.length > 2) {
					item.setText(texts[0] + "\n...");
				} else {
					item.setText(result.getText());
				}
				if (result.getIcon() != null) {
					result.getIcon().setBounds(0, 0, PREVIEW_DRAWABLE_SIZE,
							PREVIEW_DRAWABLE_SIZE);
					item.setCompoundDrawables(result.getIcon(), null, null,
							null);
				}
				item.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						result.onClick(MainActivity.this);
					}
				});
				mLinearLayout.addView(item);
				index++;
			}
		}
	}
}
