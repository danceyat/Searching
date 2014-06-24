package com.android.searching;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.util.Log;

public class ConfigManager {
	private static final boolean DEBUG = false;
	private static final String TAG = "ConfigManager";

	public static final String FILE_TYPE_PDF = "pdf";
	public static final String FILE_TYPE_TXT = "txt";
	public static final String FILE_TYPE_HTML = "html";
	public static final String FILE_TYPE_DOC = "doc";
	public static final String FILE_TYPE_PPT = "ppt";
	public static final String FILE_TYPE_XLS = "xls";
	public static final int FILE_TYPES_NUM = 6;

	private static final String FIRST_RUN = "first_run";
	private static final String PREVIEW_COUNT = "preview_count";
	private static final String RESULT_ITEM_DRAWABLE_SIZE = "result_drawable_size";
	private static final String DOWNLOAD_APP_ONLY = "download_only";
	private static final String FILE_TYPES = "file_types";

	private static final String CONFIG_FILENAME = "config";
	private static final int DEFAULT_PREVIEW_COUNT = 3;
	private static final int DEFAULT_RESULT_ITEM_DRAWABLE_SIZE = 80;
	private static final String CONTENT_SEPARATOR = ",";

	private final File mFile;
	private final Map<String, String> mConfig = new HashMap<String, String>();
	private boolean mFirstRun;
	private int mPreviewCount;
	private int mResultItemDrawableSize;
	private boolean mDownloadOnly;
	private Set<String> mFileTypes; // keep align with
									// R.array.setting_fileTypes

	private static ConfigManager _INSTANCE = null;

	public static int getPreviewCount() {
		return _INSTANCE.mPreviewCount;
	}

	public static void setPreviewCount(int count) {
		if (count > 0) {
			_INSTANCE.mPreviewCount = count;
		} else {
			Log.w(TAG, "Invalid preview count " + count + ".");
		}
	}

	public static int getResultItemDrawableSize() {
		return _INSTANCE.mResultItemDrawableSize;
	}

	public static void setResultItemDrawableSize(int size) {
		if (size > 0) {
			_INSTANCE.mResultItemDrawableSize = size;
		} else {
			Log.w(TAG, "Invalid result item drawable size " + size + ".");
		}
	}

	public static boolean getFirstRun() {
		return _INSTANCE.mFirstRun;
	}

	public static void setFirstRun(boolean firstRun) {
		_INSTANCE.mFirstRun = firstRun;
	}

	public static boolean getDownloadAppOnly() {
		return _INSTANCE.mDownloadOnly;
	}

	public static void setDownloadAppOnly(boolean downloadAppOnly) {
		_INSTANCE.mDownloadOnly = downloadAppOnly;
	}

	public static String[] getFileTypes() {
		String[] ret = new String[_INSTANCE.mFileTypes.size()];
		return _INSTANCE.mFileTypes.toArray(ret);
	}

	public static void setFileTypes(String[] types) {
		_INSTANCE.mFileTypes.clear();
		for (String type : types) {
			_INSTANCE.mFileTypes.add(type);
		}
	}

	public static String getConfigs() {
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<String, String> entry : _INSTANCE.mConfig.entrySet()) {
			builder.append(entry.getKey()).append("=").append(entry.getValue())
					.append("\n");
		}
		return builder.toString();
	}

	public static void writeImmediately() {
		_INSTANCE.refreshConfig();
		_INSTANCE.writeConfigToFile();
	}

	public static boolean[] getFileTypesChecked() {
		boolean[] ret = new boolean[FILE_TYPES_NUM];
		for (String type : _INSTANCE.mFileTypes) {
			if (type.equals(FILE_TYPE_PDF)) {
				ret[0] = true;
			} else if (type.equals(FILE_TYPE_TXT)) {
				ret[1] = true;
			} else if (type.equals(FILE_TYPE_HTML)) {
				ret[2] = true;
			} else if (type.equals(FILE_TYPE_DOC)) {
				ret[3] = true;
			} else if (type.equals(FILE_TYPE_PPT)) {
				ret[4] = true;
			} else if (type.equals(FILE_TYPE_XLS)) {
				ret[5] = true;
			}
		}
		return ret;
	}

	public static void setFileTypesChecked(boolean[] checked) {
		if (checked.length == FILE_TYPES_NUM) {
			_INSTANCE.mFileTypes.clear();
			if (checked[0]) {
				_INSTANCE.mFileTypes.add(FILE_TYPE_PDF);
			}
			if (checked[1]) {
				_INSTANCE.mFileTypes.add(FILE_TYPE_TXT);
			}
			if (checked[2]) {
				_INSTANCE.mFileTypes.add(FILE_TYPE_HTML);
			}
			if (checked[3]) {
				_INSTANCE.mFileTypes.add(FILE_TYPE_DOC);
			}
			if (checked[4]) {
				_INSTANCE.mFileTypes.add(FILE_TYPE_PPT);
			}
			if (checked[5]) {
				_INSTANCE.mFileTypes.add(FILE_TYPE_XLS);
			}
		}
	}

	public ConfigManager(File path) {
		mFile = new File(path, CONFIG_FILENAME);
		readConfigFromFile();
		parseConfig();

		_INSTANCE = this;
	}

	private void parseConfig() {
		String firstRunString = mConfig.get(FIRST_RUN);
		if (firstRunString == null || firstRunString.isEmpty()) {
			mFirstRun = true;
		} else {
			mFirstRun = Boolean.parseBoolean(mConfig.get(FIRST_RUN));
		}
		try {
			mPreviewCount = Integer.parseInt(mConfig.get(PREVIEW_COUNT));
		} catch (NumberFormatException e) {
			mPreviewCount = DEFAULT_PREVIEW_COUNT;
			Log.w(TAG, "Could not parse " + mConfig.get(PREVIEW_COUNT)
					+ " to integer, use default value(" + DEFAULT_PREVIEW_COUNT
					+ ") for " + PREVIEW_COUNT);
		}
		try {
			mResultItemDrawableSize = Integer.parseInt(mConfig
					.get(RESULT_ITEM_DRAWABLE_SIZE));
		} catch (NumberFormatException e) {
			mResultItemDrawableSize = DEFAULT_RESULT_ITEM_DRAWABLE_SIZE;
			Log.w(TAG,
					"Could not parse " + mConfig.get(RESULT_ITEM_DRAWABLE_SIZE)
							+ " to integer, use default value("
							+ DEFAULT_RESULT_ITEM_DRAWABLE_SIZE + ") for "
							+ RESULT_ITEM_DRAWABLE_SIZE);
		}
		mDownloadOnly = Boolean.parseBoolean(mConfig.get(DOWNLOAD_APP_ONLY));
		mFileTypes = new HashSet<String>();
		if (mFirstRun) {
			mFileTypes.add(FILE_TYPE_PDF);
			mFileTypes.add(FILE_TYPE_HTML);
			mFileTypes.add(FILE_TYPE_TXT);
			mFileTypes.add(FILE_TYPE_DOC);
			mFileTypes.add(FILE_TYPE_PPT);
			mFileTypes.add(FILE_TYPE_XLS);
		} else {
			String types = mConfig.get(FILE_TYPES);
			if (types != null && !types.isEmpty()) {
				for (String type : types.split(CONTENT_SEPARATOR)) {
					mFileTypes.add(type);
				}
			}
		}
		mFirstRun = false;
	}

	private void refreshConfig() {
		mConfig.put(FIRST_RUN, String.valueOf(mFirstRun));
		mConfig.put(PREVIEW_COUNT, String.valueOf(mPreviewCount));
		mConfig.put(RESULT_ITEM_DRAWABLE_SIZE,
				String.valueOf(mResultItemDrawableSize));
		mConfig.put(DOWNLOAD_APP_ONLY, String.valueOf(mDownloadOnly));
		if (mFileTypes.size() > 0) {
			StringBuilder builder = new StringBuilder();
			for (String type : mFileTypes) {
				builder.append(type).append(CONTENT_SEPARATOR);
			}
			mConfig.put(FILE_TYPES, builder.substring(0, builder.length() - 1));
		} else {
			mConfig.put(FILE_TYPES, "");
		}
	}

	private void readConfigFromFile() {
		try {
			FileReader fileReader = new FileReader(mFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line, name, value;
			while ((line = bufferedReader.readLine()) != null) {
				int index = line.indexOf("=");
				if (index == -1) {
					Log.w(TAG,
							"Error parsing config file: no '=' found in line "
									+ line);
					continue;
				} else {
					name = line.substring(0, index);
					value = line.substring(index + 1);
					mConfig.put(name, value);
					if (DEBUG) {
						Log.d(TAG, "Read config: " + name + "=" + value);
					}
				}
			}
			bufferedReader.close();
			fileReader.close();
		} catch (FileNotFoundException e) {
			Log.w(TAG, "Config file doesn't exist, start with no config.");
		} catch (IOException e) {
			Log.e(TAG, "Error reading config file: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void writeConfigToFile() {
		try {
			FileWriter fileWriter = new FileWriter(mFile);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			for (Map.Entry<String, String> entry : mConfig.entrySet()) {
				bufferedWriter.write(entry.getKey() + "=" + entry.getValue()
						+ "\n");
				if (DEBUG) {
					Log.d(TAG, "Writing config: " + entry.getKey() + "="
							+ entry.getValue());
				}
			}
			bufferedWriter.close();
			fileWriter.close();
		} catch (IOException e) {
			Log.e(TAG, "Error writing config file: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
