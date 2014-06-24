package com.android.searching;

import com.android.searching.engines.Engine;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ListActivity extends Activity {
	public static int DRAWABLE_SIZE = 80;

	private ContentManager mContentManager = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String type = getIntent()
				.getStringExtra(MainActivity.EXTRA_RESULT_TYPE);
		mContentManager = ContentManager.getInstance();
		if (mContentManager == null) {
			// TODO
		}

		View view = getLayoutInflater().inflate(R.layout.activity_list, null);
		ListView listView = (ListView) view
				.findViewById(R.id.listView_results_listActivity);
		setTitle(ContentManager.TITLE_RESOURCES.get(type));
		listView.setAdapter(new ArrayAdapter<Engine.IResult>(this,
				R.layout.results_item2, mContentManager.getResults(type)) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = convertView == null ? getLayoutInflater().inflate(
						R.layout.results_item2, null) : convertView;
				final Engine.IResult result = getItem(position);
				return Utils.formatResult(view, result, ListActivity.this);
			}
		});
		setContentView(view);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
