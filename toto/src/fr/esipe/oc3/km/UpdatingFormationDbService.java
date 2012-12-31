package fr.esipe.oc3.km;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import fr.esipe.agenda.parser.Formation;
import fr.esipe.agenda.parser.Parser;
import fr.esipe.oc3.km.db.FormationProvider;

public class UpdatingFormationDbService extends Service {

	public static final String DATABASE_FORMATIONS_UPDATED = "fr.esipe.oc3.km.UpdatingFormationDbService.action.DATABASE_FORMATIONS_UPDATED";
	private ArrayList<String> listNameFormation;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		new GetFormationsFromServer().execute("");
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Intent intent = new Intent(DATABASE_FORMATIONS_UPDATED);
		intent.putStringArrayListExtra("formation", listNameFormation);
		sendBroadcast(intent);
	}

	
	private class GetFormationsFromServer extends AsyncTask<String, Void, Boolean> {


		private List<Formation> listFormation;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(String... param) {

			Parser p = new Parser();
			try {
				listFormation = p.parseFormationList();
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
			addingFormationToDatabase(listFormation);
			stopSelf();
		}

	}



	public void addingFormationToDatabase(List<Formation> listFormation) {

		FormationProvider provider = new FormationProvider(this);

		for(Formation formation : listFormation) {

			provider.insert(formation);
			listNameFormation.add(formation.getName());
		}
		provider.close();
	
	}
}
