package com.android.searching.engines;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;

import com.android.searching.ContentManager.Results;
import com.android.searching.Utils;

public class ImageEngine extends Engine {

	public ImageEngine(Context context, String type) {
		super(context, type);
	}

	@Override
	protected void doSearch(Context context, Results results, String pattern,
			boolean isPresearch) {
		String selection = null;
		if (!pattern.equals("")) {
			selection = MediaStore.Images.Media.DISPLAY_NAME + " like '%"
					+ pattern + "%'";

		}

		ContentResolver cr = context.getContentResolver();
		Cursor cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				null, selection, null, null);
		if (cursor != null) {
			int idNum = cursor.getColumnIndex(MediaStore.Images.Media._ID);
			int nameNum = cursor
					.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
			int sizeNum = cursor.getColumnIndex(MediaStore.Images.Media.SIZE);
			while (cursor.moveToNext()) {
				String id = cursor.getString(idNum);
				String name = cursor.getString(nameNum);
				String size = cursor.getString(sizeNum);
				Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(cr,
						Long.parseLong(id),
						MediaStore.Images.Thumbnails.MICRO_KIND, null);
				Drawable icon = new BitmapDrawable(context.getResources(),
						bitmap);
				results.add(new PhotoResult(icon, name, id, Utils
						.getReadableSizeString(size)));

			}

			cursor.close();
		}
	}

	public class PhotoResult extends Engine.IResult {

		protected PhotoResult(Drawable icon, String name, String id, String desc) {
			super(id, icon, name, desc);
		}

		@Override
		public void onClick(Context context) {
			Uri uri = ContentUris.withAppendedId(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
					Long.parseLong(mId));
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.setData(uri);
			context.startActivity(intent);

		}

	}

}
