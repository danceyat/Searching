package com.android.searching.engines;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import com.android.searching.ContentManager.Results;
import com.android.searching.ConfigManager;
import com.android.searching.R;

public class FileEngine extends Engine {

	private List<String> mimeType = null;

	public FileEngine(Context context, String type) {
		super(context, type);
	}

	private void initMimeType() {
		mimeType = new ArrayList<String>();
		String fileTypes[] = ConfigManager.getFileTypes();
		for (String type : fileTypes) {
			mimeType.add(MimeTypeMap.getSingleton().getMimeTypeFromExtension(
					type));
		}
	}

	private HashMap<String, FileInfo> fileInfos = new HashMap<String, FileInfo>();

	private class FileInfo {
		private String fileName;
		private String suffix;

		public FileInfo(String fileName, String suffix) {
			this.fileName = fileName;
			this.suffix = suffix;
		}

		public String getFileName() {
			return fileName;
		}

		public String getSuffix() {
			return suffix;
		}

	}

	@Override
	protected void doSearch(Context context, Results results, String pattern,
			boolean isPresearch) {
		initMimeType();
		String selection = null;
		if (!pattern.equals("")) {
			selection = MediaStore.Files.FileColumns.DATA + " like '%"
					+ pattern + "%'";
		}

		ContentResolver cr = context.getContentResolver();
		Resources resources = context.getResources();

		String[] columns = new String[] { MediaStore.Files.FileColumns.TITLE,
				MediaStore.Files.FileColumns.DATA };
		Uri uri = MediaStore.Files.getContentUri("external");

		StringBuilder sb = new StringBuilder();
		for (int index = 0; index < mimeType.size() - 1; ++index) {
			sb.append(MediaStore.Files.FileColumns.MIME_TYPE + "='"
					+ mimeType.get(index) + "' or ");
		}
		if (mimeType.size() - 1 >= 0) {
			sb.append(MediaStore.Files.FileColumns.MIME_TYPE + "='"
					+ mimeType.get(mimeType.size() - 1) + "'");
		}

		String selectByType = null;
		if (selection != null) {
			selectByType = "(" + sb.toString() + ")";
			selection = selectByType + " and " + selection;
		} else {
			String tmpStr = sb.toString();
			selection = tmpStr.length() <= 0 ? null : tmpStr;
		}

		Cursor cursor = cr.query(uri, columns, selection, null,
				MediaStore.Files.FileColumns.SIZE + " ASC");

		if (cursor != null) {
			int dataIndex = cursor
					.getColumnIndex(MediaStore.Files.FileColumns.DATA);
			int titleIndex = cursor
					.getColumnIndex(MediaStore.Files.FileColumns.TITLE);

			while (cursor.moveToNext()) {
				String title = cursor.getString(titleIndex);
				String data = cursor.getString(dataIndex);
				String suffix = getFileSuffix(new File(data));
				Drawable drawable = null;
				if (suffix != null) {
					if (suffix.equals(".txt")) {
						drawable = resources.getDrawable(R.drawable.ic_txt);
					} else if (suffix.equals(".html") || suffix.equals(".htm")) {
						drawable = resources.getDrawable(R.drawable.ic_html);
					} else if (suffix.equals(".pdf")) {
						drawable = resources.getDrawable(R.drawable.ic_pdf);
					} else if (suffix.equals(".doc")) {
						drawable = resources.getDrawable(R.drawable.ic_doc);
					} else if (suffix.equals(".ppt")) {
						drawable = resources.getDrawable(R.drawable.ic_ppt);
					} else if (suffix.equals(".xls")) {
						drawable = resources.getDrawable(R.drawable.ic_xls);
					}
				}
				results.add(new DocumentsResult(drawable, null, title, data));
			}
			cursor.close();
		}

	}

	protected void doSearch2(Context context, Results results, String pattern) {

		File path = Environment.getExternalStorageDirectory();

		if (fileInfos.isEmpty()) {
			getAllFileInfo(path);
		}

		Iterator<Entry<String, FileInfo>> it = fileInfos.entrySet().iterator();
		Resources resources = context.getResources();
		while (it.hasNext()) {
			Entry<String, FileInfo> entry = it.next();
			FileInfo fileInfo = entry.getValue();
			String fileName = fileInfo.getFileName();
			String suffix = fileInfo.getSuffix();
			Drawable drawable = null;
			if (suffix.equals(".txt")) {
				drawable = resources.getDrawable(R.drawable.ic_txt);
			} else if (suffix.equals(".html") || suffix.equals(".htm")) {
				drawable = resources.getDrawable(R.drawable.ic_html);
			}
			if (fileName.contains(pattern)) {
				String filePath = entry.getKey();
				results.add(new DocumentsResult(drawable, null, fileName,
						filePath));
			}
		}

	}

	private void getAllFileInfo(File dir) {
		File[] files = dir.listFiles();
		for (File file : files) {
			if (!file.isHidden()) {
				if (file.isDirectory()) {
					getAllFileInfo(file);

				} else if (file.isFile()) {
					try {
						String suffix = getFileSuffix(file);
						if (suffix == null || suffix.isEmpty()) {
							continue;
						}
						if (suffix.equals(".txt") || suffix.equals(".jar")
								|| suffix.equals(".html")
								|| suffix.equals(".htm")) {
							fileInfos.put(file.getCanonicalPath(),
									new FileInfo(file.getName(), suffix));
						} else {
							continue;
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

	}

	private static String getFileSuffix(File file) {
		String fName = file.getName();
		int dotIndex = fName.lastIndexOf(".");
		if (dotIndex < 0) {
			return null;
		}

		String suffix = fName.substring(dotIndex, fName.length()).toLowerCase(
				Locale.US);
		return suffix;

	}

	public class DocumentsResult extends Engine.IResult {
		private HashMap<String, String> MIMESet = new HashMap<String, String>();
		private boolean isInitMIMESet = false;

		private String fileName;
		private String filePath;

		protected DocumentsResult(Drawable icon, String text, String fileName,
				String filePath) {
			super(icon, text);
			this.fileName = fileName;
			this.filePath = filePath;
		}

		@Override
		public String getText() {
			StringBuilder sb = new StringBuilder();
			sb.append(fileName).append('\n').append(filePath);
			return sb.toString();
		}

		@Override
		public void onClick(Context context) {

			if (!isInitMIMESet) {
				isInitMIMESet = true;
				for (int i = 0; i < MIME_MapTable.length; i++) {
					MIMESet.put(MIME_MapTable[i][0], MIME_MapTable[i][1]);
				}
			}

			File file = new File(filePath);
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			String type = getMIMEType(file);

			intent.setDataAndType(Uri.fromFile(file), type);
			context.startActivity(intent);
		}

		private String getMIMEType(File file) {
			String type = "*/*";

			String suffix = getFileSuffix(file);
			if (suffix == null || suffix.isEmpty()) {
				return type;
			}

			if (MIMESet.containsKey(suffix)) {
				type = MIMESet.get(suffix);
			}

			return type;
		}

		private final String[][] MIME_MapTable = {
				{ ".3gp", "video/3gpp" },
				{ ".apk", "application/vnd.android.package-archive" },
				{ ".asf", "video/x-ms-asf" },
				{ ".avi", "video/x-msvideo" },
				{ ".bin", "application/octet-stream" },
				{ ".bmp", "image/bmp" },
				{ ".c", "text/plain" },
				{ ".class", "application/octet-stream" },
				{ ".conf", "text/plain" },
				{ ".cpp", "text/plain" },
				{ ".doc", "application/msword" },
				{ ".exe", "application/octet-stream" },
				{ ".gif", "image/gif" },
				{ ".gtar", "application/x-gtar" },
				{ ".gz", "application/x-gzip" },
				{ ".h", "text/plain" },
				{ ".htm", "text/html" },
				{ ".html", "text/html" },
				{ ".jar", "application/java-archive" },
				{ ".java", "text/plain" },
				{ ".jpeg", "image/jpeg" },
				{ ".jpg", "image/jpeg" },
				// { ".js", "application/x-javascript" },
				{ ".log", "text/plain" }, { ".m3u", "audio/x-mpegurl" },
				{ ".m4a", "audio/mp4a-latm" }, { ".m4b", "audio/mp4a-latm" },
				{ ".m4p", "audio/mp4a-latm" }, { ".m4u", "video/vnd.mpegurl" },
				{ ".m4v", "video/x-m4v" }, { ".mov", "video/quicktime" },
				{ ".mp2", "audio/x-mpeg" }, { ".mp3", "audio/x-mpeg" },
				{ ".mp4", "video/mp4" },
				{ ".mpc", "application/vnd.mpohun.certificate" },
				{ ".mpe", "video/mpeg" }, { ".mpeg", "video/mpeg" },
				{ ".mpg", "video/mpeg" }, { ".mpg4", "video/mp4" },
				{ ".mpga", "audio/mpeg" },
				{ ".msg", "application/vnd.ms-outlook" },
				{ ".ogg", "audio/ogg" }, { ".pdf", "application/pdf" },
				{ ".png", "image/png" },
				{ ".pps", "application/vnd.ms-powerpoint" },
				{ ".ppt", "application/vnd.ms-powerpoint" },
				{ ".prop", "text/plain" },
				{ ".rar", "application/x-rar-compressed" },
				{ ".rc", "text/plain" }, { ".rmvb", "audio/x-pn-realaudio" },
				{ ".rtf", "application/rtf" }, { ".sh", "text/plain" },
				{ ".tar", "application/x-tar" },
				{ ".tgz", "application/x-compressed" },
				{ ".txt", "text/plain" }, { ".wav", "audio/x-wav" },
				{ ".wma", "audio/x-ms-wma" }, { ".wmv", "audio/x-ms-wmv" },
				{ ".wps", "application/vnd.ms-works" },
				// {".xml", "text/xml"},
				{ ".xml", "text/plain" }, { ".z", "application/x-compress" },
				{ ".zip", "application/zip" }, { "", "*/*" } };

	}

}
