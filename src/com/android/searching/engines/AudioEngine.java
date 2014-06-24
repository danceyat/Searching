package com.android.searching.engines;

import java.util.List;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;

import com.android.searching.Utils;
import com.android.searching.ContentManager.Results;

public class AudioEngine extends Engine {

	private static Drawable sAudioIconDefault = null;

	public AudioEngine(Context context, String type) {
		super(context, type);
		if (sAudioIconDefault == null) {
			PackageManager pm = context.getPackageManager();
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_APP_MUSIC);
			List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
			if (list != null && list.size() == 1) {
				sAudioIconDefault = list.get(0).loadIcon(pm);
			}
		}
	}

	@Override
	protected void doSearch(Context context, Results results, String pattern,
			boolean isPresearch) {

		String selection = null;
		if (!pattern.equals("")) {
			selection = MediaStore.Audio.Media.DISPLAY_NAME + " like '%"
					+ pattern + "%'";
		}

		// TODO should we search title???
		ContentResolver cr = context.getContentResolver();
		Cursor cursor = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				null, selection, null, null);

		if (cursor != null) {
			int idNum = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
			int nameNum = cursor
					.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
			int sizeNum = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE);

			while (cursor.moveToNext()) {
				String id = cursor.getString(idNum);
				String name = cursor.getString(nameNum);
				String size = cursor.getString(sizeNum);
				results.add(new AudioResult(null, name, id, Utils
						.getReadableSizeString(size)));
			}

			cursor.close();
		}
	}

	public class AudioResult extends Engine.IResult {

		protected AudioResult(Drawable icon, String name, String id, String desc) {
			super(id, icon, name, desc);
		}

		@Override
		public Drawable getIcon() {
			return sAudioIconDefault == null ? sDefaultIcon : sAudioIconDefault;
		}

		@Override
		public void onClick(Context context) {
			Uri uri = ContentUris.withAppendedId(
					MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
					Long.parseLong(mId));
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.setData(uri);
			context.startActivity(intent);

		}

	}

}
