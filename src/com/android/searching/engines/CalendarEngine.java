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
import android.provider.CalendarContract;

import com.android.searching.ContentManager.Results;

public class CalendarEngine extends Engine {

	public CalendarEngine(Context context, String type) {
		super(context, type);
		if (sCalendarIcon == null) {
			Intent intent = new Intent();
			Uri uri = ContentUris.withAppendedId(
					CalendarContract.Events.CONTENT_URI, 0);
			intent.setAction(Intent.ACTION_VIEW);
			intent.setData(uri);
			PackageManager pm = context.getPackageManager();
			List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
			if (list != null && list.size() == 1) {
				sCalendarIcon = list.get(0).loadIcon(pm);
			}
		}
	}

	private static Drawable sCalendarIcon = null;

	@Override
	protected void doSearch(Context context, Results results, String pattern,
			boolean isPresearch) {

		String selection = null;
		if (!pattern.equals("")) {
			selection = CalendarContract.Events.TITLE + " like '%" + pattern
					+ "%'";
		}

		ContentResolver cr = context.getContentResolver();
		Cursor cursor = cr.query(CalendarContract.Events.CONTENT_URI, null,
				selection, null, null);
		if (cursor != null) {
			int idNum = cursor.getColumnIndex(CalendarContract.Events._ID);
			int titleNum = cursor.getColumnIndex(CalendarContract.Events.TITLE);
			int descNum = cursor
					.getColumnIndex(CalendarContract.Events.DESCRIPTION);
			while (cursor.moveToNext()) {
				String id = cursor.getString(idNum);
				String title = cursor.getString(titleNum);
				String desc = cursor.getString(descNum);
				results.add(new CalendarResult(null, title, id, desc));
			}
			cursor.close();
		}

	}

	public class CalendarResult extends Engine.IResult {

		protected CalendarResult(Drawable icon, String title, String id,
				String desc) {
			super(id, icon, title, desc);
		}

		public Drawable getIcon() {
			return sCalendarIcon == null ? sDefaultIcon : sCalendarIcon;
		}

		@Override
		public void onClick(Context context) {
			Uri uri = ContentUris.withAppendedId(
					CalendarContract.Events.CONTENT_URI, Long.parseLong(mId));
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.setData(uri);
			context.startActivity(intent);

		}

	}

}
