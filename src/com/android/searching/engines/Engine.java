package com.android.searching.engines;

import java.lang.reflect.Constructor;
import java.util.Map;

import com.android.searching.ContentManager;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

public abstract class Engine {
	private static final String SUFFIX = "Engine";
	private static final String PACKAGE_NAME = Engine.class.getPackage()
			.getName();

	public static boolean initialEngines(final Map<String, Engine> engines,
			String[] engineNames, Context context) {
		for (String name : engineNames) {
			try {
				String className = PACKAGE_NAME + "." + name + SUFFIX;
				Class<?> clazz = Class.forName(className);
				Constructor<?> constructor = clazz
						.getConstructor(new Class<?>[] { Context.class,
								String.class });
				Engine engine = (Engine) constructor.newInstance(new Object[] {
						context, name });
				engines.put(name, engine);
			} catch (Exception e) {
				Log.e("Searching", "Initial " + name + " engine failed!");
				e.printStackTrace();
				break;
			}
		}
		return true;
	}

	public final boolean search(final ContentManager.Results results,
			String pattern) {
		try {
			doSearch(mContext, results, pattern);
		} catch (Exception e) {
			Log.e("Searching", e.getMessage());
			return false;
		}

		return true;
	}

	private final Context mContext;
	protected final String mType;
	protected Drawable sDefaultIcon;

	protected Engine(Context context, String type) {
		mContext = context;
		mType = type;

		if (sDefaultIcon == null) {

			Integer iconRes = ContentManager.DEFAULT_ICON_RESOURCES.get(mType);
			if (iconRes != null) {
				sDefaultIcon = mContext.getResources().getDrawable(
						iconRes.intValue());
			}
		}
	}

	protected abstract void doSearch(final Context context,
			final ContentManager.Results results, String pattern)
			throws Exception;

	public abstract class IResult {
		protected Drawable mIcon = null;
		protected String mText = null;

		protected IResult(Drawable icon, String text) {
			mIcon = icon;
			mText = text;
		}

		public Drawable getIcon() {
			return mIcon == null ? sDefaultIcon : mIcon;
		}

		public String getText() {
			return mText;
		}

		public abstract void onClick(Context context);
	};
}
