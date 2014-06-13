package com.android.searching.engines;

import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.TreeMap;
import java.util.List;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;

import com.android.searching.CursorJoinerUtil;
import com.android.searching.ContentManager.Results;

public class ContactEngine extends Engine {
	public ContactEngine(Context context, String type) {
		super(context, type);
		if (sContactIconDefault == null) {
			PackageManager pm = context.getPackageManager();
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_PICK);
			intent.setData(ContactsContract.Contacts.CONTENT_URI);
			List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
			if (list != null && list.size() == 1) {
				sContactIconDefault = list.get(0).loadIcon(pm);
			}
		}
	}

	private static final boolean DEBUG = false;
	private static Drawable sContactIconDefault = null;
	private static final Uri CONTACTS_URI = ContactsContract.Contacts.CONTENT_URI;
	private static final Uri PHONE_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

	// use table join
	private void search2(Context context, Results results, String pattern)
			throws Exception {
		String selectByName = null;
		String selectByNum = null;
		boolean isSelectionByName = false;
		boolean isSelectionByNum = false;
		// HashMap<String, ContactResult> reslutMap = new HashMap<String,
		// ContactResult>();

		if (!pattern.equals("")) {
			selectByName = ContactsContract.Contacts.DISPLAY_NAME + " like '%"
					+ pattern + "%'";
			selectByNum = ContactsContract.CommonDataKinds.Phone.NUMBER
					+ " like '%" + pattern + "%'";
			isSelectionByName = true;
			isSelectionByNum = true;
		}

		ContentResolver resolver = context.getContentResolver();
		Cursor contacts = null;
		Cursor phone = null;
		while (pattern.equals("") || isSelectionByName || isSelectionByNum) {
			if (!pattern.equals("")) {
				if (isSelectionByNum) {
					contacts = resolver.query(CONTACTS_URI, null, null, null,
							ContactsContract.Contacts._ID + " ASC");
					phone = resolver.query(PHONE_URI, null, selectByNum, null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID
									+ " ASC");

				} else if (isSelectionByName) {
					contacts = resolver.query(CONTACTS_URI, null, selectByName,
							null, ContactsContract.Contacts._ID + " ASC");
					phone = resolver.query(PHONE_URI, null, null, null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID
									+ " ASC");
					isSelectionByName = false;
				}
			} else {
				contacts = resolver.query(CONTACTS_URI, null, null, null,
						ContactsContract.Contacts._ID + " ASC");
				phone = resolver.query(PHONE_URI, null, null, null,
						ContactsContract.CommonDataKinds.Phone.CONTACT_ID
								+ " ASC");
			}

			int contactsIdIndex = contacts
					.getColumnIndex(ContactsContract.Contacts._ID);
			int contactsNameIndex = contacts
					.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
			int phoneContactIdIndex = phone
					.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
			int phoneNumberIndex = phone
					.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

			if (DEBUG) {
				while (contacts.moveToNext()) {
					Log.i("Searching1",
							"id is " + contacts.getString(contactsIdIndex)
									+ ", name is "
									+ contacts.getString(contactsNameIndex));
				}

				while (phone.moveToNext()) {
					Log.i("Searching2",
							"id is " + phone.getString(phoneContactIdIndex)
									+ ", number is "
									+ phone.getString(phoneNumberIndex));
				}
			}

			CursorJoinerUtil joiner = new CursorJoinerUtil(
					contacts,
					new String[] { ContactsContract.Contacts._ID },
					phone,
					new String[] { ContactsContract.CommonDataKinds.Phone.CONTACT_ID });

			String id = null, name = null, number = null;
			ArrayList<String> numbers = new ArrayList<String>();
			for (CursorJoinerUtil.Result result : joiner) {
				switch (result) {
				case BOTH:
					if (id != null) {
						ContactResult cr = new ContactResult(null, null, id,
								name, numbers);
						results.add(cr);
						// reslutMap.put(id, cr);
					}
					id = contacts.getString(contactsIdIndex);
					name = contacts.getString(contactsNameIndex);
					number = phone.getString(phoneNumberIndex);
					numbers.clear();
					numbers.add(number);
					if (DEBUG) {
						Log.i("Searching0", "BOTH case: id is " + id
								+ ", name is " + name + ", number is " + number);
					}
					break;
				case RIGHT:
					if (id != null
							&& id.equals(phone.getString(phoneContactIdIndex))) {
						number = phone.getString(phoneNumberIndex);
						numbers.add(number);
					}
					if (DEBUG) {
						Log.i("Searching0", "RIGHT case: id is " + id
								+ ", name is " + name + ", number is " + number);
					}
					break;
				case LEFT:
					if (phone.isAfterLast()) {
						break;
					}

					if (isSelectionByNum) {
						break;
					}

					if (id != null) {
						ContactResult cr = new ContactResult(null, null, id,
								name, numbers);
						results.add(cr);
						// reslutMap.put(id, cr);
					}

					id = contacts.getString(contactsIdIndex);
					name = contacts.getString(contactsNameIndex);
					numbers.clear();

					ContactResult cr = new ContactResult(null, null, id, name,
							numbers);
					results.add(cr);
					// reslutMap.put(id, cr);

					id = null;

					if (DEBUG) {
						String id1 = contacts.getString(contactsIdIndex);
						String name1 = contacts.getString(contactsNameIndex);
						String number1 = phone.getString(phoneNumberIndex);
						if (DEBUG) {
							Log.i("Searching0", "LEFT case: id is " + id1
									+ ", name is " + name1 + ", number is "
									+ number1);
						}
					}
					break;
				default:
					throw new Exception("No such case!");
				}
			}
			if (id != null) {
				ContactResult cr = new ContactResult(null, null, id, name,
						numbers);
				results.add(cr);
				// reslutMap.put(id, cr);
			}

			contacts.close();
			phone.close();
			isSelectionByNum = false;
			if (pattern.equals("")) {
				break;
			}
		}

		// TODO remove duplicated contact
		// if (!pattern.equals("")) {
		// TreeMap<String, ContactResult> treeMap = new TreeMap<String,
		// ContactEngine.ContactResult>(
		// reslutMap);
		// results.clear();
		// results.addAll(treeMap.values());
		// }
	}

	@SuppressWarnings("unused")
	private void search1(Context context, Results results, String pattern) {
		String selection = null;
		if (!pattern.equals("")) {
			selection = ContactsContract.Contacts.DISPLAY_NAME + " like '%"
					+ pattern + "%'";
		}

		ContentResolver cr = context.getContentResolver();
		Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI,
				new String[] { ContactsContract.Contacts._ID,
						PhoneLookup.DISPLAY_NAME }, selection, null, null);
		if (cursor != null) {
			int idNum = cursor.getColumnIndex(ContactsContract.Contacts._ID);
			int nameNum = cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME);
			while (cursor.moveToNext()) {
				String id = cursor.getString(idNum);
				String name = cursor.getString(nameNum);

				Cursor phoneCursor = cr
						.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
								new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER },
								ContactsContract.CommonDataKinds.Phone.CONTACT_ID
										+ "=" + id, null, null);
				ArrayList<String> phones = new ArrayList<String>();
				if (phoneCursor != null) {
					while (phoneCursor.moveToNext()) {
						String number = phoneCursor
								.getString(phoneCursor
										.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						phones.add(number);
					}
					phoneCursor.close();
				}
				results.add(new ContactResult(null, null, id, name, phones));

			}
			cursor.close();
		}
	}

	@Override
	protected void doSearch(Context context, Results results, String pattern)
			throws Exception {
		search2(context, results, pattern);
	}

	public class ContactResult extends Engine.IResult {
		private String id = null;
		private String name = null;
		private List<String> numbers = null;

		protected ContactResult(Drawable icon, String text, String id,
				String name, List<String> phones) {
			super(icon, text);
			this.id = id;
			this.name = name;
			this.numbers = new ArrayList<String>(phones);
		}

		@Override
		public Drawable getIcon() {
			// TODO if has
			return sContactIconDefault == null ? sDefaultIcon
					: sContactIconDefault;
		}

		@Override
		public String getText() {
			// TODO
			StringBuilder sbBuilder = new StringBuilder();
			sbBuilder.append(name);
			for (String number : numbers) {
				sbBuilder.append('\n').append(number);
			}
			return sbBuilder.toString();
			//
			// return name + "   " + numbers.get(0);
		}

		@Override
		public void onClick(Context context) {
			Uri uri = ContentUris.withAppendedId(
					ContactsContract.Contacts.CONTENT_URI, Long.parseLong(id));
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.setData(uri);
			context.startActivity(intent);
		}

	}

}
