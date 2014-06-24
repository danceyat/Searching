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
import android.widget.Toast;

import com.android.searching.ContentManager.Results;
import com.android.searching.R;

public class SettingEngine extends Engine {
	public SettingEngine(Context context, String type) {

		super(context, type);
		mPm = context.getPackageManager();
		ResolveInfo resolveInfo;

		if (packageName == null) {
			resolveInfo = getInstancePackageResolveInfo(mPm);
			packageName = resolveInfo.activityInfo.packageName;
			if (mSettingIcon == null) {
				mSettingIcon = resolveInfo.loadIcon(mPm);
			}
		}
	}

	private PackageManager mPm = null;
	private static String packageName = null;
	private static Drawable mSettingIcon = null;

	private ResolveInfo getInstancePackageResolveInfo(PackageManager pm) {
		Intent settingsIntent = new Intent(
				android.provider.Settings.ACTION_SETTINGS);
		return pm.resolveActivity(settingsIntent, 0);

	}

	@Override
	protected void doSearch(Context context, Results results, String pattern,
			boolean isPresearch) {
		try {

			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_DEFAULT);
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
						// TODO filter out those setting object with no label name
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

		protected SettingResult(Drawable icon, String labelName,
				String className) {
			super(icon, labelName, className);

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
				Toast.makeText(context, R.string.startup_app_fail,
						Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}

		}

	}

}
