package fr.esipe.oc3.km;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CursorAdapter;
import fr.esipe.agenda.parser.Formation;
import fr.esipe.agenda.parser.Parser;
import fr.esipe.oc3.km.db.ListFormationProvider;
import fr.esipe.oc3.km.ui.PlanningActivity;

public class MainActivity extends ListActivity {

	List<String> liste = new Vector<String>();
	List<Formation> listFormation = null;
	ProgressDialog dialog;
	ListFormationProvider helper;
	private SimpleCursorAdapter cursorAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		/**
		 * Au démarrage de l'application:
		 * Soit la base de donnée listformation existe
		 * 		donc listEvent existe	
		 * 			Suivant semaine
		 * 				affiche le planning
		 * Soit la base de donnée listFormation n'existe pas
		 * 		listEvent vide
		 * 			Récupère la liste des formations
		 * 			Sélectionne formation (dans boite de dialogue avec Expandable list view)
		 * 			affiche emploi du temps semaine en cours et semaine suivante de la formation
		 * 
		 * Bouton refresh:
		 * 			Met à jour l'emploi du temps actuel et semaine suivante
		 */

		Button button = (Button) findViewById(R.id.button1);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(MainActivity.this,PlanningActivity.class);

				Calendar now = Calendar.getInstance();
				intent.putExtra("formationid", String.valueOf(750984));

				intent.putExtra("year", 2012);
				intent.putExtra("weekOfYear", 50);
				//intent.putExtra("weekOfYear", now.get(Calendar.WEEK_OF_YEAR));
				startActivity(intent);

			}
		});

		helper = new ListFormationProvider(this);
		QueryFormationHtml recoverFormation = new QueryFormationHtml();
		recoverFormation.execute("");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.title_bar, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_search:
			
			break;

		case R.id.menu_refresh:
			break;
			
		case R.id.menu_about:
			break;
			
		case R.id.menu_settings:
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private class QueryFormationHtml extends AsyncTask<String, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = ProgressDialog.show(MainActivity.this, 
					"Recover data",
					"Charging in progress...",
					true);
			dialog.setCancelable(false);
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
			dialog.dismiss();
			addingFormationDatabase();
			updateViewFromDb();
		}

	}

	public void addingFormationDatabase() {
		for(Formation formation : listFormation) {
			if(!helper.exists(formation.getId())) {
				helper.insert(formation);
			}
		}
	}

	private void updateViewFromDb() {
		Cursor cursor = helper.getFormations();
		cursorAdapter = new SimpleCursorAdapter(MainActivity.this, 
				android.R.layout.simple_list_item_1, 
				cursor, 
				new String[] {"name"}, 
				new int[] {android.R.id.text1}, 
				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

		setListAdapter(cursorAdapter);
	}

	@Override
	protected void onStop() {
		super.onStop();
		helper.close();
	}
}
