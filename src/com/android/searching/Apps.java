package com.android.searching;

import android.net.Uri;
import android.provider.BaseColumns;

public class Apps implements BaseColumns {
	public static final String AUTHORITY = "com.android.searching.provider.Application";

	private Apps() {

	}

	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/apps");

	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.google.app";

	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.google.app";

	public static final String DEFAULT_SORT_ORDER = "label ASC";

	public static final String LABEL = "label";
	public static final String PACKAGENAME = "packagename";
	public static final String CLASSNAME = "classname";
	public static final String ISSYSTEMAPP = "issystemapp";

}
