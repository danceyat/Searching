package com.android.searching.engines;

import java.util.List;
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
	protected void doSearch(Context context, Results results, String pattern,
			boolean isPresearch) {
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

			while (cursor.moveToNext()) {
				results.add(new SmsResult(null, cursor.getString(idNum), cursor
						.getString(addressNum), cursor.getString(bodyNum)));
			}
			cursor.close();
		}

	}

	public class SmsResult extends Engine.IResult {

		protected SmsResult(Drawable icon, String id, String address,
				String body) {
			super(id, icon, address, body);
		}

		@Override
		public Drawable getIcon() {
			return sSmsIcon == null ? sDefaultIcon : sSmsIcon;
		}

		@Override
		public void onClick(Context context) {
			Uri uri = ContentUris.withAppendedId(Uri.parse(smsAll),
					Long.parseLong(mId));
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.setData(uri);
			context.startActivity(intent);
		}

	}

}
