package fr.esipe.oc3.km.services;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Vector;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import fr.esipe.agenda.parser.Event;
import fr.esipe.agenda.parser.Parser;
import fr.esipe.oc3.km.R;
import fr.esipe.oc3.km.providers.EventContentProvider;

public class UpdatingEventDbService extends Service{

	public static final String DATABASE_EVENTS_UPDATED = "fr.esipe.oc3.km.UpdatingEventDbService.action.DATABASE_EVENTS_UPDATED";
	private String formationId;
	private int year;
	private int weekOfYear;
	private boolean delete;
	private int numberOfWeek;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	

	/**
	 * récupère le planning de la semaine courante et la semaine suivante
	 * programme le reveil de ce service
	 * stocke dans un nouveau calendrier l'emploi du temps actuel
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		formationId = intent.getStringExtra(getResources().getString(R.string.event_intent_formation_id));
		year = intent.getIntExtra(getResources().getString(R.string.event_intent_year), 2012);
		weekOfYear = intent.getIntExtra(getResources().getString(R.string.event_intent_week_of_year), 51);
		delete = intent.getBooleanExtra(getResources().getString(R.string.event_intent_delete), false);
		numberOfWeek = intent.getIntExtra(getResources().getString(R.string.event_intent_number_of_week), 6);
		
		GetEventsFromServer recoverEvent = new GetEventsFromServer();
		recoverEvent.execute();

		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Intent intent = new Intent(DATABASE_EVENTS_UPDATED);
		sendBroadcast(intent);
	}
	
	public void addingEventDatabase(List<Event> listEvents, int mweekOfyear) {
		Uri mUri = EventContentProvider.CONTENT_URI;

		if(delete){
			getContentResolver().delete(mUri, EventContentProvider.WEEK_OF_EVENTS + "=? AND NOT "+
					EventContentProvider.FORMATION_ID_COLUMN + "=?", new String[]{ String.valueOf(mweekOfyear), formationId});
		}
		
		String[] columnsLabels = new String[] {
				EventContentProvider.TOPIC_NAME_COLUMN,
				EventContentProvider.TEACHERS_NAME_COLUMN,
				EventContentProvider.CLASSROOM_NAME_COLUMN,
				EventContentProvider.BRANCH_NAME_COLUMN,
				EventContentProvider.EXAMEN_NAME_COLUMN
		};
		
		for(Event event : listEvents) {
			ContentValues values = new ContentValues();
			Cursor cursor = getContentResolver().query(mUri,
					null, 
					EventContentProvider.START_TIME_NAME_COLUMN + "=?",
					new String[] {String.valueOf(event.getStartTime().getTime())}, 
					null);

			List<String> labels = event.getLabels();
			for(int i = 0; i < labels.size(); i++){
				values.put(columnsLabels[i], labels.get(i));
			}
			values.put(EventContentProvider.WEEK_OF_EVENTS, mweekOfyear);
			values.put(EventContentProvider.FORMATION_ID_COLUMN, event.getFormationId());
			values.put(EventContentProvider.START_TIME_NAME_COLUMN, event.getStartTime().getTime());
			values.put(EventContentProvider.END_TIME_NAME_COLUMN, event.getEndTime().getTime());
			
			
			if(cursor == null || cursor.getCount() < 1) {
				getContentResolver().insert(mUri, values);
			} else {
				getContentResolver().update(mUri, values,
						EventContentProvider.START_TIME_NAME_COLUMN + "=?",
						new String[] {String.valueOf(event.getStartTime().getTime())});
			}

		}
	}
	
	
	private class GetEventsFromServer extends AsyncTask<String, Void, Boolean> {

		private List<Event> listEvent;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			listEvent = new Vector<Event>();			
		}
		
		@Override
		protected Boolean doInBackground(String... param) {
			
			Parser p = new Parser();
			try {
				for(int i = -1; i < numberOfWeek - 1; i++){
					listEvent = p.parseWeeklyPlanning(formationId, year, weekOfYear + i);
					addingEventDatabase(listEvent, weekOfYear + i);
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			stopSelf();
		}
		
	}
}
