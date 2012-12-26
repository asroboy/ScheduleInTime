package fr.esipe.oc3.km.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import fr.esipe.agenda.parser.Formation;

public class ListFormationProvider {

	private final ListFormationHelper helper;

	public ListFormationProvider(Context context) {
		helper = new ListFormationHelper(context);
	}

	/**
	 * Return all formations from database
	 * @return
	 */
	public Cursor getFormations() {
		SQLiteDatabase sqLiteDb = helper.getReadableDatabase();
		String selectQuery = "SELECT  * FROM " + ListFormationHelper.FORMATIONS_TABLE_NAME;
		return sqLiteDb.rawQuery(selectQuery, null);
	}

	/**
	 * Insert a formation in database
	 * @param listFormation
	 * @return
	 */
	public long insert(Formation formation) {
		SQLiteDatabase sqLiteDb = helper.getWritableDatabase();

		ContentValues values = new ContentValues();

		values.put(ListFormationHelper.GROUP_NAME_COLUMN, formation.getGroup());
		values.put(ListFormationHelper.NAME_NAME_COLUMN, formation.getName());
		values.put(ListFormationHelper.ID_NAME_COLUMN, formation.getId());

		return sqLiteDb.insert(ListFormationHelper.FORMATIONS_TABLE_NAME, null, values);
	}

	public long delete(Formation formation){
		SQLiteDatabase sqLiteDb = helper.getWritableDatabase();
		return sqLiteDb.delete(ListFormationHelper.FORMATIONS_TABLE_NAME, ListFormationHelper.ID_NAME_COLUMN+ "=? ", new String[] {formation.getId()});
	}

	
	public boolean exists(String id) {

		Cursor c = null;
		c = getFormations();
		
		boolean isPresent = false;
		if(c.getCount() > 0){

			if(c.moveToFirst()) {
				do {
				String tmp = c.getString(c.getColumnIndex(ListFormationHelper.ID_NAME_COLUMN));

				if( tmp.equals(id))
					isPresent = true;
				
				} while(c.moveToNext());
			}
			
		}
		return isPresent;
	}
	
	public void close() {

		helper.close();
	}


}
