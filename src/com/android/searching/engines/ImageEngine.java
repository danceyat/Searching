package com.android.searching.engines;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;

import com.android.searching.ContentManager.Results;

public class ImageEngine extends Engine {

	public ImageEngine(Context context, String type) {
		super(context, type);
	}

	@Override
	protected void doSearch(Context context, Results results, String pattern) {
		String selection = null;
		if (!pattern.equals("")) {
			selection = MediaStore.Images.Media.DISPLAY_NAME + " like '%"
					+ pattern + "%'";

		}

		Cursor cursor = context.getContentResolver().query(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, selection,
				null, null);
		if (cursor != null) {
			int idNum = cursor.getColumnIndex(MediaStore.Images.Media._ID);
			int nameNum = cursor
					.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
			while (cursor.moveToNext()) {
				String id = cursor.getString(idNum);
				String name = cursor.getString(nameNum);
				results.add(new PhotoResult(null, null, id, name));

			}

			cursor.close();
		}
	}

	public class PhotoResult extends Engine.IResult {

		private String id;
		private String name;

		protected PhotoResult(Drawable icon, String text, String id, String name) {
			super(icon, text);
			this.id = id;
			this.name = name;
		}

		@Override
		public String getText() {
			StringBuilder sb = new StringBuilder();
			sb.append(id).append('\n').append(name);
			return sb.toString();
		}

		@Override
		public void onClick(Context context) {
			Uri uri = ContentUris.withAppendedId(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
					Long.parseLong(id));
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.setData(uri);
			context.startActivity(intent);

		}

	}

}
