package fr.esipe.oc3.km;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import fr.esipe.agenda.parser.Event;
import fr.esipe.agenda.parser.Parser;
import fr.esipe.oc3.km.providers.EventContentProvider;

public class UpdatingDatabaseService extends Service{

	public static final String DATABASE_UPDATED = "fr.esipe.oc3.km.UpdatingDatabaseService.action.DATABASE_UPDATED";
	private List<Event> listEvent = null;
	private String formationId;
	private int year;
	private int weekOfYear;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d("KM", "service");
	}

	/**
	 * récupère le planning de la semaine courante et la semaine suivante
	 * programme le reveil de ce service
	 * stocke dans un nouveau calendrier l'emploi du temps actuel
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		formationId = intent.getStringExtra("formationId");
		year = intent.getIntExtra("year", 2013);
		weekOfYear = intent.getIntExtra("weekOfYear", 51);
		Log.d("KM", "before");
		GetEventsFromServer recoverEvent = new GetEventsFromServer();
		recoverEvent.execute();

		Log.d("KM", "after");
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		Log.d("KM", "finish");
	}
	
	public void addingEventDatabase() {
		Uri mUri = EventContentProvider.CONTENT_URI;

		String[] columnsLabels = new String[] {
				EventContentProvider.TOPIC_NAME_COLUMN,
				EventContentProvider.TEACHERS_NAME_COLUMN,
				EventContentProvider.CLASSROOM_NAME_COLUMN,
				EventContentProvider.BRANCH_NAME_COLUMN,
				EventContentProvider.EXAMEN_NAME_COLUMN
		};
		for(Event event : listEvent) {
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
			values.put(EventContentProvider.WEEK_OF_EVENTS, weekOfYear);
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

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
		}
		
		@Override
		protected Boolean doInBackground(String... param) {
			
			Parser p = new Parser();
			try {
				Log.d("KM", "execute");
				listEvent = p.parseWeeklyPlanning(formationId, year, weekOfYear);
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
			addingEventDatabase();
			Intent intent = new Intent(DATABASE_UPDATED);
			sendBroadcast(intent);
			stopSelf();
		}
		
	}
}
