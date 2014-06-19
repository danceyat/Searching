package com.android.searching.engines;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.android.searching.ContentManager.Results;

public class SmsEngine extends Engine {

	public SmsEngine(Context context, String type) {
		super(context, type);
		if (sSmsIcon == null) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setType("vnd.android-dir/mms-sms");
			PackageManager pm = context.getPackageManager();
			List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
			if (list != null && list.size() == 1) {
				sSmsIcon = list.get(0).loadIcon(pm);
			}
		}
	}

	private static Drawable sSmsIcon = null;
	private static String smsAll = "content://sms/";

	@Override
	protected void doSearch(Context context, Results results, String pattern, boolean isPresearch) {
		String selection = null;
		if (!pattern.equals("")) {
			selection = "address like '%" + pattern + "%' or body like '%"
					+ pattern + "%'";
		}

		final Uri smsUri = Uri.parse(smsAll);
		Cursor cursor = context.getContentResolver().query(smsUri, null,
				selection, null, "date desc");

		if (cursor != null) {
			int idNum = cursor.getColumnIndex("_id");
			int addressNum = cursor.getColumnIndex("address");
			int bodyNum = cursor.getColumnIndex("body");
			int dateNum = cursor.getColumnIndex("date");

			while (cursor.moveToNext()) {
				results.add(new SmsResult(null, null, cursor.getString(idNum),
						cursor.getString(addressNum),
						cursor.getString(bodyNum), cursor.getString(dateNum)));
			}
			cursor.close();
		}

	}

	public class SmsResult extends Engine.IResult {
		private String id;
		private String address;
		private String body;
		private String date;

		protected SmsResult(Drawable icon, String text, String id,
				String address, String body, String date) {
			super(icon, text);
			this.id = id;
			this.address = address;
			this.body = body;
			this.date = date;
		}

		@Override
		public String getText() {
			StringBuilder sb = new StringBuilder();
			sb.append(address).append('\n').append(body).append('\n')
					.append(formatDate(date));

			return sb.toString();
		}

		@Override
		public Drawable getIcon() {
			return sSmsIcon == null ? sDefaultIcon : sSmsIcon;
		}

		private String formatDate(String date) {
			Date d = new Date(Long.parseLong(date));
			SimpleDateFormat format = new SimpleDateFormat("yy-M-d",
					Locale.CHINESE);
			return format.format(d);
		}

		@Override
		public void onClick(Context context) {
			Uri uri = ContentUris.withAppendedId(Uri.parse(smsAll),
					Long.parseLong(id));
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.setData(uri);
			context.startActivity(intent);
		}

	}

}
