package com.android.searching.engines;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import com.android.searching.ContentManager;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

public abstract class Engine {
	private static final String TAG = "Engine";
	private static final String SUFFIX = "Engine";
	private static final String PACKAGE_NAME = Engine.class.getPackage()
			.getName();

	public static boolean initialEngines(final Map<String, Engine> engines,
			String[] engineNames, Context context) {
		Class<?> clazz;
		for (String name : engineNames) {
			String className = PACKAGE_NAME + "." + name + SUFFIX;
			try {
				clazz = Class.forName(className);
				Constructor<?> constructor = clazz
						.getConstructor(new Class<?>[] { Context.class,
								String.class });
				Engine engine = (Engine) constructor.newInstance(new Object[] {
						context, name });
				engines.put(name, engine);
			} catch (ClassNotFoundException e) {
				Log.e(TAG, "Error initializing engine: " + name
						+ ", no relative class found: " + e.getMessage());
			} catch (NoSuchMethodException e) {
				Log.e(TAG, "Error initializing engine: " + name
						+ ", no suitable constructor found: " + e.getMessage());
			} catch (InstantiationException e) {
				Log.e(TAG,
						"Error initializing engine: " + name
								+ ", the class cannot be instantiated: "
								+ e.getMessage());
			} catch (IllegalAccessException e) {
				Log.e(TAG, "Error initializing engine: " + name
						+ ", constructor is not accessible: " + e.getMessage());
			} catch (IllegalArgumentException e) {
				Log.e(TAG, "Error initializing engine: " + name
						+ ", errors in arguments: " + e.getMessage());
			} catch (InvocationTargetException e) {
				Log.e(TAG,
						"Error initializing engine: " + name
								+ ", exception thrown in constructor: "
								+ e.getMessage());
			}
		}
		return true;
	}

	public final boolean presearch() {
		try {
			synchronized (mLock) {
				doSearch(mContext, null, null, true);
			}
		} catch (Exception e) {
			Log.w(TAG, "Presearch failed: " + e.getMessage());
			return false;
		}
		return true;
	}

	public final boolean search(final ContentManager.Results results,
			String pattern) {
		try {
			synchronized (mLock) {
				doSearch(mContext, results, pattern, false);
			}
		} catch (Exception e) {
			Log.w(TAG, "Search failed: " + e.getMessage());
			return false;
		}

		return true;
	}

	private final Object mLock = new Object();
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

	// lock it before call down; protect data in subclass
	protected abstract void doSearch(final Context context,
			final ContentManager.Results results, String pattern,
			boolean isPresearch) throws Exception;

	public abstract class IResult {
		protected int id = -1;
		protected Drawable mIcon = null;
		protected String mText = null;

		protected IResult(Drawable icon, String text) {
			mIcon = icon;
			mText = text;
		}

		protected IResult(int id, Drawable icon, String text) {
			this(icon, text);
			this.id = id;
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
