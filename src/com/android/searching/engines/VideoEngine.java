package com.android.searching.engines;


import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;

import com.android.searching.ContentManager.Results;

public class VideoEngine extends Engine {
//	private static Drawable sVideoIconDefalut = null;

	public VideoEngine(Context context, String type) {
		super(context, type);
//		if (sVideoIconDefalut == null) {
//			PackageManager pm = context.getPackageManager();
//			Intent intent = new Intent();
//			intent.setAction(Intent.ACTION_VIEW);
//			intent.setType(MediaStore.Video.Media.CONTENT_TYPE);
//			List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
//			if (list != null && list.size() == 1) {
//				sVideoIconDefalut = list.get(0).loadIcon(pm);
//			}
//		}
	}

	@Override
	protected void doSearch(Context context, Results results, String pattern, boolean isPresearch) {
		String selection = null;
		if (!pattern.equals("")) {
			selection = MediaStore.Video.Media.DISPLAY_NAME + " like '%"
					+ pattern + "%'";
		}

		ContentResolver cr = context.getContentResolver();
		Cursor cursor = cr.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
				null, selection, null, null);

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inDither = false;
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		if (cursor != null) {
			int idNum = cursor.getColumnIndex(MediaStore.Video.Media._ID);
			int nameNum = cursor
					.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME);

			while (cursor.moveToNext()) {
				String id = cursor.getString(idNum);
				String name = cursor.getString(nameNum);
				
				Bitmap bitmap = MediaStore.Video.Thumbnails.getThumbnail(cr,
						Long.parseLong(id), Images.Thumbnails.MICRO_KIND,
						options);
				
				Drawable icon = new BitmapDrawable(context.getResources(),
						bitmap);

				results.add(new VedioResult(icon, null, id, name));

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

//		@Override
//		public Drawable getIcon() {
//			return mIcon != null ? mIcon
//					: (sVideoIconDefalut == null ? sDefaultIcon
//							: sVideoIconDefalut);
//		}

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
