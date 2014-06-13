package com.android.searching.engines;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Browser;

import com.android.searching.ContentManager.Results;

public class BookmarkEngine extends Engine {

	public BookmarkEngine(Context context, String type) {
		super(context, type);
	}

	@Override
	protected void doSearch(Context context, Results results, String pattern) {
		String selection = Browser.BookmarkColumns.BOOKMARK + "=1";

		if (!pattern.equals("")) {
			selection = selection + " and " + Browser.BookmarkColumns.URL
					+ " like '%" + pattern + "%'";
		}

		ContentResolver cr = context.getContentResolver();

		Cursor cursor = cr.query(Browser.BOOKMARKS_URI, null, selection, null,
				null);
		if (cursor != null) {

			int titleNum = cursor.getColumnIndex(Browser.BookmarkColumns.TITLE);
			int urlNum = cursor.getColumnIndex(Browser.BookmarkColumns.URL);
			while (cursor.moveToNext()) {

				String title = cursor.getString(titleNum);
				String url = cursor.getString(urlNum);
				results.add(new BookmarksResult(null, null, title, url));
			}

			cursor.close();
		}
	}

	public class BookmarksResult extends Engine.IResult {

		private String title;
		private String url;

		protected BookmarksResult(Drawable icon, String text, String title,
				String url) {
			super(icon, text);

			this.title = title;
			this.url = url;
		}

		@Override
		public String getText() {
			StringBuilder sb = new StringBuilder();
			sb.append(title).append('\n').append(url);
			return sb.toString();
		}

		@Override
		public void onClick(Context context) {
			Uri uri = Uri.parse(url);
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.setData(uri);

			context.startActivity(intent);
		}

	}
}
