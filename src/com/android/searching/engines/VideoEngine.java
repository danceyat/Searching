package com.android.searching.engines;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;

import com.android.searching.ContentManager.Results;

public class VideoEngine extends Engine {

	public VideoEngine(Context context, String type) {
		super(context, type);
	}

	@Override
	protected void doSearch(Context context, Results results, String pattern) {
		String selection = null;
		if (!pattern.equals("")) {
			selection = MediaStore.Video.Media.DISPLAY_NAME + " like '%"
					+ pattern + "%'";
		}

		ContentResolver cr = context.getContentResolver();
		Cursor cursor = cr.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
				null, selection, null, null);

		if (cursor != null) {
			int idNum = cursor.getColumnIndex(MediaStore.Video.Media._ID);
			int nameNum = cursor
					.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME);

			while (cursor.moveToNext()) {
				String id = cursor.getString(idNum);
				String name = cursor.getString(nameNum);
				results.add(new VedioResult(null, null, id, name));

			}
			cursor.close();
		}

	}

	public class VedioResult extends Engine.IResult {

		private String id;
		private String name;

		protected VedioResult(Drawable icon, String text, String id, String name) {
			super(icon, text);
			this.id = id;
			this.name = name;
		}

		@Override
		public String getText() {
			return name;
		}

		@Override
		public void onClick(Context context) {
			Uri uri = ContentUris.withAppendedId(
					MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
					Long.parseLong(id));
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.setData(uri);
			context.startActivity(intent);

		}

	}

}
