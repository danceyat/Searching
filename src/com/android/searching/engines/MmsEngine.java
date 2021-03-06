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

import com.android.searching.ContentManager.Results;

public class MmsEngine extends Engine {

	public MmsEngine(Context context, String type) {
		super(context, type);
		if (sMmsIcon == null) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setType("vnd.android-dir/mms-sms");
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
	protected void doSearch(Context context, Results results, String pattern,
			boolean isPresearch) {
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

		private String subject;
		// private String date;
		private String address;

		protected MmsResult(Drawable icon, String text, String id,
				String subject, String date, String address) {
			super(id, icon, text);
			this.subject = subject;
			// this.date = date;
			this.address = address;
		}

		// private String formatDate(String date) {
		// Date d = new Date(Long.parseLong(date));
		// SimpleDateFormat format = new SimpleDateFormat("yy-M-d",
		// Locale.getDefault());
		// return format.format(d);
		// }

		@Override
		public String getText() {
			// StringBuilder sb = new StringBuilder();
			// sb.append("From: ").append(address).append('\n').append("Sub: ")
			// .append(subject).append('\n').append("date: ")
			// .append(formatDate(date));
			//
			// return sb.toString();
			return "Sub: " + subject;
		}

		@Override
		public String getDesc() {
			return "From: " + address;
		}

		@Override
		public Drawable getIcon() {
			return sMmsIcon == null ? sDefaultIcon : sMmsIcon;
		}

		@Override
		public void onClick(Context context) {
			Uri uri = ContentUris.withAppendedId(Uri.parse(mmsAllUri),
					Long.parseLong(mId));
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.setData(uri);
			context.startActivity(intent);
		}

	}

}
