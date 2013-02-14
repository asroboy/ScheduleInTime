package fr.esipe.oc3.km.services;

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
import fr.esipe.oc3.km.R;
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
		listNameFormation = new ArrayList<String>(); 
		new GetFormationsFromServer().execute("");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Intent intent = new Intent(DATABASE_FORMATIONS_UPDATED);
		intent.putStringArrayListExtra(getResources().getString(R.string.formation_intent_list_name), listNameFormation);
		sendBroadcast(intent);
	}


	/**
	 * Get List of formation from server
	 * @author Kevin M
	 *
	 */
	private class GetFormationsFromServer extends AsyncTask<String, Void, List<Formation>> {


		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected List<Formation> doInBackground(String... param) {

			Parser p = new Parser();
			List<Formation> listFormation = null;
			try {
				listFormation = p.parseFormationList();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return listFormation;
		}

		@Override
		protected void onPostExecute(List<Formation> listFormation) {
			super.onPostExecute(listFormation);	
			addingFormationToDatabase(listFormation);
			stopSelf();
		}

	}



	/**
	 * Add formations in Database
	 * @param listFormation
	 */
	public void addingFormationToDatabase(List<Formation> listFormation) {

		FormationProvider provider = new FormationProvider(this);

		if(listFormation != null) {
			for(Formation formation : listFormation) {

				provider.insert(formation);
				listNameFormation.add(formation.getName());
			}
			provider.close();
		}

	}
}
