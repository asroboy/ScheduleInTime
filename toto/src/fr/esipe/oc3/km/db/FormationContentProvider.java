package fr.esipe.oc3.km.db;



import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class FormationContentProvider extends ContentProvider{

	public static final Uri CONTENT_URI = Uri.parse("content://fr.esipe.oc3.provider.formation");
	public static final String CONTENT_PROVIDER_MIME = "vnd.android.cursor.item/vnd.km.provider.formation";


	private FormationHelper helper;
	private SQLiteDatabase formationdb;



	@Override
	public boolean onCreate() {
		helper = new FormationHelper(getContext());
		formationdb = helper.getWritableDatabase();
		return ( formationdb == null) ? false : true;
	}


	private long getId(Uri uri) {
		String lastPathSegment = uri.getLastPathSegment();
		if (lastPathSegment != null) {
			try {
				return Long.parseLong(lastPathSegment);
			} catch (NumberFormatException e) {
				Log.e("KM", "Number Format Exception : " + e);
			}
		}
		return -1;
	}

	
	@Override
	public String getType(Uri uri) {
		return CONTENT_PROVIDER_MIME;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
	
		try {
			long id = formationdb.insertOrThrow(FormationHelper.FORMATIONS_TABLE_NAME, null, values);

			if (id == -1) {
				throw new SQLException("Failed to insert row into " + uri);
			} else {
				return ContentUris.withAppendedId(uri, id);
			}
		} finally {
			getContext().getContentResolver().notifyChange(uri, null);
		}
	}



	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		long id = getId(uri);
		try {
			if (id < 0) {
				return 	formationdb.query(FormationHelper.FORMATIONS_TABLE_NAME,
						projection, selection, selectionArgs, null, null,
						sortOrder);
			} else {
				return 	formationdb.query(FormationHelper.FORMATIONS_TABLE_NAME,
						projection, FormationHelper.KEY_ID+ "=" + id, null, null, null,
						null);
			}
		} finally {
			getContext().getContentResolver().notifyChange(uri, null);
			
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {

		long id = getId(uri);
		try {
			if (id < 0)
				return formationdb.update(FormationHelper.FORMATIONS_TABLE_NAME,
						values,
						selection,
						selectionArgs);
			else
				return formationdb.update(FormationHelper.FORMATIONS_TABLE_NAME,
						values, FormationHelper.KEY_ID+ "=" + id, null);
		} finally {
			getContext().getContentResolver().notifyChange(uri, null);
		}
	}
	
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		long id = getId(uri);

		int count;
		try {
			if(id < 0) {
				count = formationdb.delete(FormationHelper.FORMATIONS_TABLE_NAME, where, whereArgs);
			}
			else {
				count = formationdb.delete(FormationHelper.FORMATIONS_TABLE_NAME,
						FormationHelper.KEY_ID + "=" + id, whereArgs);
			}

		} finally {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return count;
	}


}
