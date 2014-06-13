package com.android.searching.engines;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.android.searching.ContentManager.Results;

public class MmsEngine extends Engine {

	public MmsEngine(Context context, String type) {
		super(context, type);
		if (sMmsIcon == null) {
			Intent intent = new Intent();
			Uri uri = ContentUris.withAppendedId(Uri.parse(mmsAllUri), 0);
			intent.setAction(Intent.ACTION_VIEW);
			intent.setData(uri);
			PackageManager pm = context.getPackageManager();
			List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
			if (list != null && list.size() == 1) {
				sMmsIcon = list.get(0).loadIcon(pm);
			}
		}
	}

	private static String mmsAllUri = "content://mms/";
	private static Drawable sMmsIcon = null;

	@Override
	protected void doSearch(Context context, Results results, String pattern) {
		String selection = null;

		ContentResolver cr = context.getContentResolver();
		Uri uri = Uri.parse(mmsAllUri);
		Cursor cursor = cr.query(uri, new String[] { "_id", "sub", "date" },
				selection, null, null);

		if (cursor != null) {
			int idNum = cursor.getColumnIndex("_id");
			int subNum = cursor.getColumnIndex("sub");
			int dateNum = cursor.getColumnIndex("date");
			while (cursor.moveToNext()) {
				String id = cursor.getString(idNum);
				String sub = cursor.getString(subNum);
				String date = cursor.getString(dateNum);
				String addrUri = mmsAllUri + id + "/addr/";
				Cursor cur = cr.query(Uri.parse(addrUri),
						new String[] { "address" }, "msg_id = " + id, null,
						null);
				while (cur.moveToNext()) {
					String addr = cur.getString(cur.getColumnIndex("address"));
					if (addr.contains(pattern)) {
						results.add(new MmsResult(null, null, id, sub, String
								.valueOf(Long.parseLong(date) * 1000), addr));
					}
				}

			}

			cursor.close();
		}

	}

	public class MmsResult extends Engine.IResult {

		private String id;
		private String subject;
		private String date;
		private String address;

		protected MmsResult(Drawable icon, String text, String id,
				String subject, String date, String address) {
			super(icon, text);
			this.id = id;
			this.subject = subject;
			this.date = date;
			this.address = address;
		}

		private String formatDate(String date) {
			Date d = new Date(Long.parseLong(date));
			SimpleDateFormat format = new SimpleDateFormat("yy-M-d",
					Locale.getDefault());
			return format.format(d);
		}

		@Override
		public String getText() {
			StringBuilder sb = new StringBuilder();
			sb.append("From: ").append(address).append('\n').append("Sub: ")
					.append(subject).append('\n').append("date: ")
					.append(formatDate(date));

			return sb.toString();
		}

		@Override
		public void onClick(Context context) {
			Uri uri = ContentUris.withAppendedId(Uri.parse(mmsAllUri),
					Long.parseLong(id));
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.setData(uri);
			context.startActivity(intent);
		}

	}

}
