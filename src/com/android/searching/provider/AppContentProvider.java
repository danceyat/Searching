package com.android.searching.provider;

import com.android.searching.Apps;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class AppContentProvider extends ContentProvider {

	private DataBaseHelper mOpenHelper;
	private static final String DATABASE_NAME = "searching_app.db";
	private static final String APP_TABLE_NAME = "apps";
	private static final int DATABASE_VERSION = 1;
	private static final String CREATE_TABLE = "CREATE TABLE " + APP_TABLE_NAME
			+ "(" + Apps._ID + " INTEGER PRIMARY KEY, " + Apps.LABEL + " TEXT, "
			+ Apps.PACKAGENAME + " TEXT, " + Apps.CLASSNAME + " TEXT, " + Apps.ISSYSTEMAPP + " INTEGER);";

	private static final int APPS = 1;
	private static final int APP_ID = 2;
	private static final UriMatcher sUriMatcher;

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(Apps.AUTHORITY, APP_TABLE_NAME, APPS);
		sUriMatcher.addURI(Apps.AUTHORITY, APP_TABLE_NAME + "#", APP_ID);
	}

	@Override
	public boolean onCreate() {
		mOpenHelper = new DataBaseHelper(getContext(), DATABASE_NAME, null,
				DATABASE_VERSION);

		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder sqb = new SQLiteQueryBuilder();
		switch (sUriMatcher.match(uri)) {
		case APPS:
			sqb.setTables(APP_TABLE_NAME);
			break;

		case APP_ID:
			sqb.setTables(APP_TABLE_NAME);
			sqb.appendWhere(Apps._ID + "=" + uri.getPathSegments().get(1));
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = Apps.DEFAULT_SORT_ORDER;
		} else {
			orderBy = sortOrder;
		}

		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor cursor = sqb.query(db, projection, selection, selectionArgs,
				null, null, orderBy);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case APPS:
			return Apps.CONTENT_TYPE;

		case APP_ID:
			return Apps.CONTENT_ITEM_TYPE;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		if (sUriMatcher.match(uri) != APPS) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		if (values == null) {
			throw new IllegalArgumentException("values is null");
		}

		if (!values.containsKey(Apps.LABEL)
				|| !values.containsKey(Apps.PACKAGENAME) || !values.containsKey(Apps.CLASSNAME)) {
			throw new IllegalArgumentException("bad values");
		}

		SQLiteDatabase sdb = mOpenHelper.getWritableDatabase();
		long rowId = sdb.insert(APP_TABLE_NAME, null, values);
		if (rowId > 0) {
			Uri appUri = ContentUris.withAppendedId(Apps.CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(appUri, null);
			return appUri;
		}
		throw new SQLException("Failed to insert row into" + uri);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase sdb = mOpenHelper.getWritableDatabase();
		int count;
		switch (sUriMatcher.match(uri)) {
		case APPS:
			count = sdb.delete(APP_TABLE_NAME, selection, selectionArgs);
			break;

		case APP_ID:
			String appId = uri.getPathSegments().get(1);
			count = sdb.delete(APP_TABLE_NAME, Apps._ID
					+ "="
					+ appId
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection
							+ ')' : ""), selectionArgs);
			break;

		default:
			throw new IllegalArgumentException("Unnown URI" + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		switch (sUriMatcher.match(uri)) {
		case APPS:
			count = db.update(APP_TABLE_NAME, values, selection, selectionArgs);
			break;

		case APP_ID:
			String appId = uri.getPathSegments().get(1);
			count = db.update(APP_TABLE_NAME, values, Apps._ID
					+ "="
					+ appId
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection
							+ ')' : ""), selectionArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknow URI " + uri);
		}

		return count;
	}

	private static class DataBaseHelper extends SQLiteOpenHelper {

		public DataBaseHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_TABLE);

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + APP_TABLE_NAME);
			onCreate(db);
		}

	}

}
