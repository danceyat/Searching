package com.android.searching.receiver;

import com.android.searching.Apps;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

public class AppReceiver extends BroadcastReceiver {

	private final static String TAG = "AppReceiver";
	private final String DATASCHEME = "pakcage:";

	@Override
	public void onReceive(Context context, Intent intent) {
		PackageManager pm = context.getPackageManager();
		String action = intent.getAction();
		String data = intent.getDataString();
		String packageName = data.substring(DATASCHEME.length());

		ContentResolver cr = context.getContentResolver();

		Intent tmpIntent = null;

		if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
			if ((tmpIntent = pm.getLaunchIntentForPackage(packageName)) != null) {				
				ContentValues values = new ContentValues();
				ApplicationInfo appInfo = null;
				try {
					appInfo = pm.getApplicationInfo(packageName, 0);
				} catch (NameNotFoundException e) {
					Log.e(TAG, e.getMessage());
					e.printStackTrace();
				}
				values.put(Apps.LABEL, appInfo.loadLabel(pm).toString());
				values.put(Apps.PACKAGENAME, packageName);
				values.put(Apps.CLASSNAME, tmpIntent.getComponent().getClassName());
				values.put(Apps.ISSYSTEMAPP, appInfo.flags & ApplicationInfo.FLAG_SYSTEM);
				cr.insert(Apps.CONTENT_URI, values);
			}
		} else if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
			String selection = Apps.PACKAGENAME + "='" + packageName+"'";
			cr.delete(Apps.CONTENT_URI, selection, null);
		} else if (action.equals(Intent.ACTION_PACKAGE_REPLACED)) {			
			if ((tmpIntent = pm.getLaunchIntentForPackage(packageName)) != null) {
				ContentValues values = new ContentValues();
				ApplicationInfo appInfo = null;
				try {
					appInfo = pm.getApplicationInfo(packageName, 0);
				} catch (NameNotFoundException e) {
					Log.e(TAG, e.getMessage());
					e.printStackTrace();
				}
				values.put(Apps.LABEL, appInfo.loadLabel(pm).toString());
				values.put(Apps.PACKAGENAME, packageName);
				values.put(Apps.CLASSNAME, tmpIntent.getComponent().getClassName());
				values.put(Apps.ISSYSTEMAPP, appInfo.flags & ApplicationInfo.FLAG_SYSTEM);
				String selection = Apps.PACKAGENAME + "='" + packageName+"'";
				cr.update(Apps.CONTENT_URI, values, selection, null);
			}
		} else {
		}

	}

}
