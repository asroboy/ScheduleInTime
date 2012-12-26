package fr.esipe.oc3.km.db;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import fr.esipe.agenda.parser.Event;

public class EventProvider {

	private final EventHelper helper;

	public EventProvider(Context context) {
		helper = new EventHelper(context);
	}

	/**
	 * Return all events from database
	 * @return
	 */
	public Cursor getEvents() {
		SQLiteDatabase sqLiteDb = helper.getReadableDatabase();
		String selectQuery = "SELECT  * FROM " + EventHelper.EVENTS_TABLE_NAME;
		return sqLiteDb.rawQuery(selectQuery, null);
	}

	/**
	 * Insert all element from listFormation in database
	 * @param listFormation
	 * @return
	 */
	public long insert(Event event) {
		SQLiteDatabase sqLiteDb = helper.getWritableDatabase();

		ContentValues values = new ContentValues();
		List<String> labels = event.getLabels();

		values.put(EventHelper.FORMATION_ID_NAME_COLUMN, event.getFormationId());

		if(labels.size() >= 3) {
			values.put(EventHelper.TOPIC_NAME_COLUMN, labels.get(0));
			values.put(EventHelper.TEACHERS_NAME_COLUMN, labels.get(1));
			values.put(EventHelper.CLASSROOM_NAME_COLUMN, labels.get(2));
			values.put(EventHelper.BRANCH_NAME_COLUMN, labels.get(3));
			if(labels.size() == 4)
				values.put(EventHelper.EXAMEN_NAME_COLUMN, labels.get(4));
		}

		values.put(EventHelper.START_TIME_NAME_COLUMN, event.getStartTime().getTime());
		values.put(EventHelper.END_TIME_NAME_COLUMN, event.getEndTime().getTime());
		return sqLiteDb.insert(EventHelper.EVENTS_TABLE_NAME, null, values);
	}

	public long delete(Event event){
		SQLiteDatabase sqLiteDb = helper.getWritableDatabase();

		long startTime = event.getStartTime().getTime();

		String where = EventHelper.START_TIME_NAME_COLUMN + " =? " + startTime;
		//		String where = EventHelper.TOPIC_NAME_COLUMN + " =? " + event.getLabels().get(0) 
		//				+ " AND " + EventHelper.FORMATION_ID_NAME_COLUMN + " =? " + event.getFormationId()
		//				+ " AND " + EventHelper.START_TIME_NAME_COLUMN + " =? " + startTime;

		return sqLiteDb.delete(EventHelper.EVENTS_TABLE_NAME, where, null);
		//return sqLiteDb.delete(EventHelper.EVENTS_TABLE_NAME, EventHelper.BRANCH_NAME_COLUMN+ "=? ", new String[] {event.getFormationId()});
	}

	public void close() {

		helper.close();
	}

	public boolean exists(Event event) {

		String currentEvent = String.valueOf(event.getStartTime().getTime());
		//		String currentEvent = event.getLabels().get(0)
		//				+ " " + event.getFormationId()
		//				+ " " + event.getStartTime().getTime();
		Cursor c = null;
		c = getEvents();

		boolean isPresent = false;
		if(c.getCount() > 0){

			if(c.moveToFirst()) {
				do {
					String tmp = c.getString(c.getColumnIndex(EventHelper.START_TIME_NAME_COLUMN)); 

					//				String tmp = c.getString(c.getColumnIndex(EventHelper.TOPIC_NAME_COLUMN)) 
					//						+ " " + c.getString(c.getColumnIndex(EventHelper.FORMATION_ID_NAME_COLUMN))
					//						+ " " + c.getString(c.getColumnIndex(EventHelper.START_TIME_NAME_COLUMN));

					if( tmp.equals(currentEvent)){
						isPresent = true;
						break;
					}

				} while(c.moveToNext());
			}

		}
		return isPresent;
	}

	public long update(Event event) {
		SQLiteDatabase sqLiteDb = helper.getWritableDatabase();
		ContentValues values = new ContentValues();
		List<String> labels = event.getLabels();
		if(labels.size() >= 3) {
			values.put(EventHelper.FORMATION_ID_NAME_COLUMN, event.getFormationId());
			values.put(EventHelper.TOPIC_NAME_COLUMN, labels.get(0));
			values.put(EventHelper.TEACHERS_NAME_COLUMN, labels.get(1));
			values.put(EventHelper.CLASSROOM_NAME_COLUMN, labels.get(2));
			values.put(EventHelper.BRANCH_NAME_COLUMN, labels.get(3));
			//values.put(EventHelper.EXAMEN_NAME_COLUMN, labels.get(4));
			values.put(EventHelper.START_TIME_NAME_COLUMN, event.getStartTime().getTime());
			values.put(EventHelper.END_TIME_NAME_COLUMN, event.getEndTime().getTime());
		} else {
			
		}
		sqLiteDb.update(EventHelper.EVENTS_TABLE_NAME, values, null, null);


		return 12;
	}

}
