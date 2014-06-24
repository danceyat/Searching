package com.android.searching.engines;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.Toast;

import com.android.searching.Apps;
import com.android.searching.ConfigManager;
import com.android.searching.ContentManager.Results;
import com.android.searching.R;

public class AppEngine extends Engine {

	// use TreeMap to sort in alphabet order, by app label
	// private TreeMap<String, AppInfo> map = new TreeMap<String, AppInfo>();
	private String TAG = "AppEngine";
	private static boolean isFirstSearch = true;

	public AppEngine(Context context, String type) {
		super(context, type);
	}

	@Override
	protected void doSearch(Context context, Results results, String pattern,
			boolean isPresearch) {
		PackageManager pm = context.getPackageManager();

		if (isFirstSearch) {
			Intent intentToResolve = new Intent(Intent.ACTION_MAIN);
			intentToResolve.addCategory(Intent.CATEGORY_INFO);
			List<ResolveInfo> listByInfo = pm.queryIntentActivities(
					intentToResolve, 0);

			intentToResolve.removeCategory(Intent.CATEGORY_INFO);
			intentToResolve.addCategory(Intent.CATEGORY_LAUNCHER);
			List<ResolveInfo> listByLauncher = pm.queryIntentActivities(
					intentToResolve, 0);

			List<ResolveInfo> tmpList = new ArrayList<ResolveInfo>(
					listByLauncher);
			tmpList.retainAll(listByInfo);
			listByLauncher.removeAll(tmpList);
			listByInfo.addAll(listByLauncher);

			ContentResolver cr = context.getContentResolver();
			ContentValues values = new ContentValues();

			for (ResolveInfo resolveInfo : listByInfo) {
				ActivityInfo appInfo = resolveInfo.activityInfo;
				String labelName = appInfo.loadLabel(pm).toString();
				int isSystemApp = appInfo.applicationInfo.flags
						& ApplicationInfo.FLAG_SYSTEM;

				String s = Apps.PACKAGENAME + "='" + appInfo.packageName + "'";
				Cursor cursor = cr.query(Apps.CONTENT_URI, null, s, null, null);

				if (cursor != null && !cursor.moveToNext()) {
					values.put(Apps.LABEL, labelName);
					values.put(Apps.PACKAGENAME, appInfo.packageName);
					String className = appInfo.name;
					values.put(Apps.CLASSNAME, className);
					values.put(Apps.ISSYSTEMAPP, isSystemApp);

					cr.insert(Apps.CONTENT_URI, values);
					values.clear();
					cursor.close();
				}
			}
			isFirstSearch = false;
		}

		if (!isPresearch) {
			boolean isOnlyNonSystemApp = ConfigManager.getDownloadAppOnly();
			String selection = null;
			if (!pattern.equals("")) {
				selection = "(" + Apps.LABEL + " like '%" + pattern + "%')";
			}

			if (isOnlyNonSystemApp) {
				if (selection != null) {
					selection = Apps.ISSYSTEMAPP + "=0 and " + selection;
				} else {
					selection = Apps.ISSYSTEMAPP + "=0";
				}
			}

			Cursor cursor = context.getContentResolver().query(
					Apps.CONTENT_URI, null, selection, null, null);
			if (cursor != null) {
				int labelNum = cursor.getColumnIndex(Apps.LABEL);
				int pkgNameNum = cursor.getColumnIndex(Apps.PACKAGENAME);
				int classNameNum = cursor.getColumnIndex(Apps.CLASSNAME);

				while (cursor.moveToNext()) {
					String packageName = cursor.getString(pkgNameNum);
					String className = cursor.getString(classNameNum);
					Drawable icon = null;
					try {
						icon = pm.getApplicationIcon(packageName);
					} catch (NameNotFoundException e) {
						Log.i(TAG, e.getMessage());
						e.printStackTrace();
					}

					results.add(new AppResult(icon, cursor.getString(labelNum),
							packageName, className));
				}
				cursor.close();
			}
		}
	}

	public class AppResult extends Engine.IResult {
		// package name in field mDescription
		String mClassName;

		protected AppResult(Drawable icon, String text, String pkgName,
				String className) {
			super(icon, text, pkgName);
			this.mClassName = className;
		}

		@Override
		public void onClick(Context context) {
			try {
				Intent intent = new Intent();
				intent.setClassName(mDescription, mClassName);
				context.startActivity(intent);
			} catch (Exception e) {
				Toast.makeText(context, R.string.startup_app_fail,
						Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}

		}

	}
}
