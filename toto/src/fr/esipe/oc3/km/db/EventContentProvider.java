package fr.esipe.oc3.km.db;



import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class EventContentProvider extends ContentProvider{

	
	public static final String AUTHORITY = "fr.esipe.oc3.km.provider";
	
	
	public static final Uri CONTENT_URI = Uri.parse("content://fr.esipe.oc3.km.provider.events");
	public static final String CONTENT_PROVIDER_ALL_MIME = "vnd.android.cursor.dir/vnd.km.provider.event";
	public static final String CONTENT_PROVIDER_ONE_MIME = "vnd.android.cursor.item/vnd.km.provider.event";

	private EventHelper helper;
	private SQLiteDatabase eventDb;

	// Constants that differentiate b/w different URI requests
	private static final int EVENTS   = 1;
	private static final int SINGLE_EVENT = 2;

	private static final UriMatcher uriMatcher;

	// construct a static UriMatcher object to add two URIs:
	// the first will service the URI for all EVENTS; the
	// second will service the URI for individual event.
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		// a URI that ends in planningevent corresponds to a request
		// for all events
		uriMatcher.addURI("fr.esipe.oc3.provider.events", 
				"planningevent", EVENTS);
		// a URI that ends in "planningevent" with a number
		// corresponds to a request to retrieve a specific event
		uriMatcher.addURI("fr.esipe.oc3.provider.events", 
				"planningevent/#", SINGLE_EVENT);
	}


	@Override
	public boolean onCreate() {
		helper = new EventHelper(getContext());
		eventDb = helper.getWritableDatabase();
		return ( helper == null) ? false : true;
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
	public int delete(Uri uri, String where, String[] whereArgs) {
		long id = getId(uri);

		int count;
		try {
			switch ( uriMatcher.match(uri) ) {
			case EVENTS:
				count = eventDb.delete(EventHelper.EVENTS_TABLE_NAME, where, whereArgs);
				break;
			case SINGLE_EVENT:
				count = eventDb.delete(EventHelper.EVENTS_TABLE_NAME,
						EventHelper.EVENT_ID + "=" + id, whereArgs);
				break;
			default: throw new IllegalArgumentException("Unsupported URI: " + uri);
			}
		} finally {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case EVENTS: 
			return CONTENT_PROVIDER_ALL_MIME;
			// if uriMatcher returns SINGLE_EVENT
		case SINGLE_EVENT: 
			return CONTENT_PROVIDER_ONE_MIME;
		default: throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {

		long rowId = eventDb.insert(EventHelper.EVENTS_TABLE_NAME, null, values);

		try {
			if ( rowId > 0 ) {
				Uri tmpuri = ContentUris.withAppendedId(CONTENT_URI, rowId);
				return tmpuri;
			}
		} finally {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		throw new SQLException("Failed to insert row into " + uri);
	}



	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		long id = getId(uri);
		try {
			switch (uriMatcher.match(uri)) {
			case EVENTS:
				return 	eventDb.query(EventHelper.EVENTS_TABLE_NAME,
						projection, selection, selectionArgs, null, null,
						sortOrder);

			case SINGLE_EVENT:
				return 	eventDb.query(EventHelper.EVENTS_TABLE_NAME,
						projection, EventHelper.EVENT_ID + "=" + id, null, null, null,
						null);
			default:
				break;
			}
		} finally {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where,
			String[] whereArgs) {

		long id = getId(uri);
		int count;
		try {
			switch (uriMatcher.match(uri)) {
			case EVENTS:
				count = eventDb.update(EventHelper.EVENTS_TABLE_NAME, values, where,
						whereArgs);
				break;
			case SINGLE_EVENT:
				count = eventDb.update(EventHelper.EVENTS_TABLE_NAME, values, 
						EventHelper.EVENT_ID + "=" + id, whereArgs);
				break;
			default:
				throw new IllegalArgumentException("Uknown URI " + uri);
			}
		} finally {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return count;
	}

}
