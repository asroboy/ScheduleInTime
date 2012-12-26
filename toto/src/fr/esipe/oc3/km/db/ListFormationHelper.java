package fr.esipe.oc3.km.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.SyncStateContract.Columns;

public class ListFormationHelper extends SQLiteOpenHelper {
	
	protected static final int DATABASE_VERSION = 1;
	protected static final String DATABASE_NAME = "listformation.db";
	protected static final String FORMATIONS_TABLE_NAME = "formationlist";
	protected static final String GROUP_NAME_COLUMN = "groups";
	protected static final String NAME_NAME_COLUMN = "name";
	protected static final String ID_NAME_COLUMN = "formationid";
	


	public ListFormationHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase sqLiteDb) {
		sqLiteDb.execSQL("CREATE TABLE IF NOT EXISTS "
				+ FORMATIONS_TABLE_NAME	
				+ " (" + Columns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " 
				+ GROUP_NAME_COLUMN + " TEXT, " 
				+ NAME_NAME_COLUMN + " TEXT, " 
				+ ID_NAME_COLUMN + " TEXT);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDb, int arg1, int arg2) {
		sqLiteDb.execSQL("DROP TABLE IF EXISTS "
				+FORMATIONS_TABLE_NAME + ";");
		onCreate(sqLiteDb);
	}

}
