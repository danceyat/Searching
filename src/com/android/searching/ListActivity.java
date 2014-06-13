package com.android.searching;

import com.android.searching.engines.Engine;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

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
				R.layout.listitem, mContentManager.getResults(type)) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				TextView textView = null;
				if (convertView != null) {
					textView = (TextView) convertView;
				} else {
					textView = (TextView) getLayoutInflater().inflate(
							R.layout.listitem, null);
				}
				final Engine.IResult result = getItem(position);
				// reduce output height
				String[] texts = result.getText().split("\n");
				if (texts.length > 1) {
					textView.setText(texts[0] + "\n...");
				} else {
					textView.setText(result.getText());
				}
				if (result.getIcon() != null) {
					result.getIcon().setBounds(0, 0, DRAWABLE_SIZE,
							DRAWABLE_SIZE);
					textView.setCompoundDrawables(result.getIcon(), null, null,
							null);
				}

				textView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						result.onClick(ListActivity.this);
					}
				});

				return textView;
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
}
