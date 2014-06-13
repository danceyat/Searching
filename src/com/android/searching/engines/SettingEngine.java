package com.android.searching.engines;

import java.util.HashSet;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.Toast;

import com.android.searching.ContentManager.Results;

public class SettingEngine extends Engine {
	public SettingEngine(Context context, String type) {

		super(context, type);
		mPm = context.getPackageManager();

		if (packageName == null) {
			packageName = getInstancePackageName(mPm);
		}

		if (mSettingIcon == null) {
			try {
				mSettingIcon = mPm.getApplicationIcon(mPm.getApplicationInfo(
						packageName, 0));
			} catch (NameNotFoundException e) {
				Log.e(TAG, e.getMessage());
				e.printStackTrace();
			}

		}
	}

	private PackageManager mPm = null;
	private String TAG = "SettingEngine";
	private static String packageName = null;
	private static Drawable mSettingIcon = null;

	private String getInstancePackageName(PackageManager pm) {
		Intent settingsIntent = new Intent(
				android.provider.Settings.ACTION_SETTINGS);
		return pm.resolveActivity(settingsIntent, 0).activityInfo.packageName;

	}

	@Override
	protected void doSearch(Context context, Results results, String pattern) {
		try {

			Intent intent = new Intent(Intent.ACTION_MAIN);
			List<ResolveInfo> infos = mPm.queryIntentActivities(intent,
					PackageManager.GET_INTENT_FILTERS);
			HashSet<String> classNameSet = new HashSet<String>();
			for (int index = 0; index < infos.size(); ++index) {
				classNameSet.add(infos.get(index).activityInfo.name);
			}

			PackageInfo pkgInfo = mPm.getPackageInfo(packageName,
					PackageManager.GET_ACTIVITIES);
			ActivityInfo[] activityInfos = pkgInfo.activities;
			for (ActivityInfo activityInfo : activityInfos) {
				String className = activityInfo.name;
				if (classNameSet.contains(className)) {
					String lableName = (String) activityInfo.loadLabel(mPm);
					if (pattern == null || pattern.equals("")
							|| lableName.contains(pattern)) {
						results.add(new SettingResult(null, lableName,
								className));
					}
				}
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

	}

	public class SettingResult extends Engine.IResult {

		private String className;

		protected SettingResult(Drawable icon, String text, String className) {
			super(icon, text);

			this.className = className;
		}

		public Drawable getIcon() {
			return mSettingIcon;
		}

		@Override
		public void onClick(Context context) {
			Intent intent = new Intent();
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setClassName(packageName, className);
			try {
				context.startActivity(intent);
			} catch (Exception e) {
				Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT)
						.show();
				e.printStackTrace();
			}

		}

	}

}
