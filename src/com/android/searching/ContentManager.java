package com.android.searching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.android.searching.engines.Engine;
import com.android.searching.R;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class ContentManager extends Handler {
	private static final boolean DEBUG = true;
	private static final String TAG = "Searching";

	private static final int MSG_RESULT_READY = 1;

	public static final String APP = "App";
	public static final String CONTACT = "Contact";
	public static final String SMS = "Sms";
	public static final String MMS = "Mms";
	public static final String CALENDAR = "Calendar";
	public static final String BOOKMARK = "Bookmark";
	public static final String IMAGE = "Image";
	public static final String AUDIO = "Audio";
	public static final String VIDEO = "Video";
	public static final String SETTING = "Setting";
	public static final String FILE = "File";

	public static final String[] ALL = new String[] { APP, CONTACT, SMS, MMS,
			CALENDAR, BOOKMARK, IMAGE, AUDIO, VIDEO, SETTING, FILE };

	public final static Map<String, Integer> TITLE_RESOURCES = new HashMap<String, Integer>();
	public final static Map<String, Integer> ICON_RESOURCES = new HashMap<String, Integer>();
	public final static Map<String, Integer> ICON_RESOURCES_SELECTED = new HashMap<String, Integer>();
	public final static Map<String, Integer> DEFAULT_ICON_RESOURCES = new HashMap<String, Integer>();
	private final Context mContext;
	private final Map<String, Engine> ENGINES = new HashMap<String, Engine>();
	private final Set<String> mContent = new HashSet<String>();
	private final Map<String, Results> mResults = new HashMap<String, ContentManager.Results>();
	private int mToBeProcessed = 0;

	private static ContentManager _instance = null;

	public static ContentManager getInstance() {
		return _instance;
	}

	public ContentManager(Context context, Looper looper) {
		super(looper);

		mContext = context;
		// TODO check return value
		initRes();

		addContent(ALL);
		_instance = this;
	}

	private boolean initRes() {
		TITLE_RESOURCES.put(APP, R.string.title_app);
		TITLE_RESOURCES.put(CONTACT, R.string.title_contact);
		TITLE_RESOURCES.put(SMS, R.string.title_sms);
		TITLE_RESOURCES.put(MMS, R.string.title_mms);
		TITLE_RESOURCES.put(CALENDAR, R.string.title_calendar);
		TITLE_RESOURCES.put(BOOKMARK, R.string.title_bookmark);
		TITLE_RESOURCES.put(IMAGE, R.string.title_image);
		TITLE_RESOURCES.put(AUDIO, R.string.title_audio);
		TITLE_RESOURCES.put(VIDEO, R.string.title_video);
		TITLE_RESOURCES.put(SETTING, R.string.title_setting);
		TITLE_RESOURCES.put(FILE, R.string.title_file);

		// TODO change to appropriate icon
		ICON_RESOURCES.put(APP, R.drawable.ic_action_null);
		ICON_RESOURCES.put(CONTACT, R.drawable.ic_action_null);
		ICON_RESOURCES.put(SMS, R.drawable.ic_action_null);
		ICON_RESOURCES.put(MMS, R.drawable.ic_action_null);
		ICON_RESOURCES.put(CALENDAR, R.drawable.ic_action_null);
		ICON_RESOURCES.put(BOOKMARK, R.drawable.ic_action_null);
		ICON_RESOURCES.put(IMAGE, R.drawable.ic_action_null);
		ICON_RESOURCES.put(AUDIO, R.drawable.ic_action_null);
		ICON_RESOURCES.put(VIDEO, R.drawable.ic_action_null);
		ICON_RESOURCES.put(SETTING, R.drawable.ic_action_null);
		ICON_RESOURCES.put(FILE, R.drawable.ic_action_null);

		ICON_RESOURCES_SELECTED.put(APP, R.drawable.ic_action_null_selected);
		ICON_RESOURCES_SELECTED
				.put(CONTACT, R.drawable.ic_action_null_selected);
		ICON_RESOURCES_SELECTED.put(SMS, R.drawable.ic_action_null_selected);
		ICON_RESOURCES_SELECTED.put(MMS, R.drawable.ic_action_null_selected);
		ICON_RESOURCES_SELECTED.put(CALENDAR,
				R.drawable.ic_action_null_selected);
		ICON_RESOURCES_SELECTED.put(BOOKMARK,
				R.drawable.ic_action_null_selected);
		ICON_RESOURCES_SELECTED.put(IMAGE, R.drawable.ic_action_null_selected);
		ICON_RESOURCES_SELECTED.put(AUDIO, R.drawable.ic_action_null_selected);
		ICON_RESOURCES_SELECTED.put(VIDEO, R.drawable.ic_action_null_selected);
		ICON_RESOURCES_SELECTED
				.put(SETTING, R.drawable.ic_action_null_selected);
		ICON_RESOURCES_SELECTED.put(FILE, R.drawable.ic_action_null_selected);

		DEFAULT_ICON_RESOURCES.put(APP, R.drawable.ic_app);
		DEFAULT_ICON_RESOURCES.put(CONTACT, R.drawable.ic_contact);
		DEFAULT_ICON_RESOURCES.put(SMS, R.drawable.ic_sms);
		DEFAULT_ICON_RESOURCES.put(MMS, R.drawable.ic_mms);
		DEFAULT_ICON_RESOURCES.put(CALENDAR, R.drawable.ic_calendar);
		DEFAULT_ICON_RESOURCES.put(BOOKMARK, R.drawable.ic_bookmark);
		DEFAULT_ICON_RESOURCES.put(IMAGE, R.drawable.ic_image);
		DEFAULT_ICON_RESOURCES.put(AUDIO, R.drawable.ic_audio);
		DEFAULT_ICON_RESOURCES.put(VIDEO, R.drawable.ic_video);
		DEFAULT_ICON_RESOURCES.put(SETTING, R.drawable.ic_setting);
		DEFAULT_ICON_RESOURCES.put(FILE, R.drawable.ic_file);

		for (String type : ALL) {
			mResults.put(type, new Results(type));
		}

		return Engine.initialEngines(ENGINES, ALL, mContext);
	}

	public void addContent(String[] contents) {
		for (String content : contents) {
			mContent.add(content);
		}
	}

	public void removeContent(String[] contents) {
		for (String content : contents) {
			mContent.remove(content);
		}
	}

	public void removeAll() {
		mContent.clear();
	}

	public boolean containsContent(String content) {
		return mContent.contains(content);
	}

	public boolean containsAll() {
		return mContent.size() == ALL.length;
	}

	public Results getResults(String type) {
		return mResults.get(type);
	}

	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case MSG_RESULT_READY:
			final String type = msg.obj.toString();
			final MainActivity activity = (MainActivity) mContext;
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					activity.attachResults(mResults.get(type));
				}
			});
			if (DEBUG) {
				Log.d(TAG, "Attached type: " + type);
			}
			// TODO timeout for some thread exception
			if (--mToBeProcessed == 0) {
				activity.onFinishAttaching();
			}
			break;
		default:
			break;
		}
	}

	public void search(String pattern) {
		searchInMultiThread(pattern);
	}

	// TODO cache
	private void searchInMultiThread(final String pattern) {
		if (mContent.size() == 0) {
			return;
		}
		// TODO threads pool
		mToBeProcessed = 0;
		for (final String type : mContent) {
			final Results results = mResults.get(type);
			results.clear();
			final Engine engine = ENGINES.get(type);
			if (engine != null) {
				Thread thread = new Thread(new Runnable() {
					@Override
					public void run() {
						if (engine.search(results, pattern)) {
							obtainMessage(MSG_RESULT_READY, type)
									.sendToTarget();
							if (DEBUG) {
								Log.d(TAG, "Message sent, type is " + type);
							}
						}
					}
				});
				thread.start();
				mToBeProcessed++;
			}
			Log.i(TAG, "Engine " + type + " has started!");
		}
	}

	public static class Results extends ArrayList<Engine.IResult> {
		private static final long serialVersionUID = -4653042453148052976L;
		private String mResultType = null;

		public Results(String type) {
			mResultType = type;
		}

		public String getType() {
			return mResultType;
		}
	}
}
