package com.android.searching.engines;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import com.android.searching.ContentManager.Results;

public class AppEngine extends Engine {

	public AppEngine(Context context, String type) {
		super(context, type);
	}

	// use TreeMap to sort in alphabet order, by app label
	private TreeMap<String, AppInfo> map = new TreeMap<String, AppInfo>();

	@Override
	protected void doSearch(Context context, Results results, String pattern) {

		if (map.isEmpty()) {
			map = getAllPackage(context);
		}

		// TODO ignore case
		if (pattern != null && !pattern.isEmpty()) {
			Iterator<Entry<String, AppInfo>> it = map.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, AppInfo> entry = it.next();
				String labelName = entry.getKey();
				if (labelName.contains(pattern)) {
					AppInfo info = entry.getValue();
					results.add(new AppResult(info.icon, labelName,
							info.packageName, info.intent));
				}
			}
		} else {
			Iterator<Entry<String, AppInfo>> iterator = map.entrySet()
					.iterator();
			while (iterator.hasNext()) {
				Entry<String, AppInfo> entry = iterator.next();
				AppInfo info = entry.getValue();
				results.add(new AppResult(info.icon, entry.getKey(),
						info.packageName, info.intent));
			}
		}
	}

	private TreeMap<String, AppInfo> getAllPackage(Context context) {
		PackageManager pm = context.getPackageManager();
		Intent intent;
		for (ApplicationInfo appInfo : pm.getInstalledApplications(0)) {
			String labelName = appInfo.loadLabel(pm).toString();
			Drawable icon = appInfo.loadIcon(pm);
			if ((intent = pm.getLaunchIntentForPackage(appInfo.packageName)) != null) {
				map.put(labelName, new AppInfo(appInfo.packageName, intent,
						icon));
			}

		}

		TreeMap<String, AppInfo> treemap = new TreeMap<String, AppInfo>(map);
		return treemap;

	}

	private class AppInfo {
		private String packageName;
		private Drawable icon;
		private Intent intent;

		public AppInfo(String packageName, Intent intent, Drawable icon) {
			this.packageName = packageName;
			this.intent = intent;
			this.icon = icon;
		}
	}

	public class AppResult extends Engine.IResult {
		String pkgName = null;
		Intent intent = null;

		protected AppResult(Drawable icon, String text, String pkgName,
				Intent intent) {
			super(icon, text);
			this.pkgName = pkgName;
			this.intent = intent;
		}

		@Override
		public void onClick(Context context) {
			context.startActivity(intent);
		}

	}
}
