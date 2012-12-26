package fr.esipe.oc3.km.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.SyncStateContract.Columns;

public class EventHelper extends SQLiteOpenHelper {
	
	protected static int DATABASE_VERSION = 1;
	protected static final String DATABASE_NAME = "event.db";
	protected static final String EVENTS_TABLE_NAME = "eventList";
	
	protected static final String FORMATION_ID_NAME_COLUMN = "formationid";
	protected static final String TOPIC_NAME_COLUMN = "topic";
	protected static final String TEACHERS_NAME_COLUMN = "teacher";
	protected static final String CLASSROOM_NAME_COLUMN = "classroom";
	protected static final String BRANCH_NAME_COLUMN = "branch";
	protected static final String EXAMEN_NAME_COLUMN = "examen";
	protected static final String START_TIME_NAME_COLUMN = "startime";
	protected static final String END_TIME_NAME_COLUMN = "endtime";


	public EventHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase sqLiteDb) {
		sqLiteDb.execSQL("CREATE TABLE IF NOT EXISTS "
				+ EVENTS_TABLE_NAME
				+ " (" + Columns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ FORMATION_ID_NAME_COLUMN + " TEXT, "
				+ TOPIC_NAME_COLUMN + " TEXT, "
				+ TEACHERS_NAME_COLUMN + " TEXT, "
				+ CLASSROOM_NAME_COLUMN + " TEXT, "
				+ BRANCH_NAME_COLUMN + " TEXT, "
				+ EXAMEN_NAME_COLUMN + " TEXT, "
				+ START_TIME_NAME_COLUMN + " INTEGER, "
				+ END_TIME_NAME_COLUMN + " INTEGER);");
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDb, int arg1, int arg2) {
		sqLiteDb.execSQL("DROP TABLE IF EXISTS "
				+EVENTS_TABLE_NAME + ";");
		onCreate(sqLiteDb);
	}

}
