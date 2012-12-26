package fr.esipe.oc3.km;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import fr.esipe.agenda.parser.Event;
import fr.esipe.agenda.parser.Parser;
import fr.esipe.oc3.km.db.EventProvider;

public class UpdatingDatabaseService extends Service{

	private List<Event> listEvent = null;
	private String formationId;
	private int year;
	private int weekOfYear;
	private ProgressDialog dialog;
	private EventProvider helper;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d("KM", "service");
		helper = new EventProvider(getApplicationContext());
	}

	/**
	 * récupère le planning de la semaine courante et la semaine suivante
	 * programme le reveil de ce service
	 * stocke dans un nouveau calendrier l'emploi du temps actuel
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		formationId = intent.getStringExtra("formationId");
		year = intent.getIntExtra("year", 2012);
		weekOfYear = intent.getIntExtra("weekOfYear", 51);
		Log.d("KM", "before");
		QueryEventHtmlPlanning recoverEvent = new QueryEventHtmlPlanning();
		recoverEvent.execute();

		Log.d("KM", "after");
		//program next wake up
		stopSelf();
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		helper.close();
		Log.d("KM", "finish");
	}
	
	public void addingEventDatabase() {
		for(Event event : listEvent) {
			if(!helper.exists(event)) {
				helper.insert(event);
			}
			else {
				helper.update(event);
			}
		}
	}
	
	
	private class QueryEventHtmlPlanning extends AsyncTask<String, Void, Boolean> {

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
		}
		
	}
}
