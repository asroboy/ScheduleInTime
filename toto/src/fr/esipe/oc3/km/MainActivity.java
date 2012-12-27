package fr.esipe.oc3.km;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import fr.esipe.agenda.parser.Event;
import fr.esipe.agenda.parser.Formation;
import fr.esipe.oc3.km.db.FormationContentProvider;
import fr.esipe.oc3.km.db.FormationHelper;
import fr.esipe.oc3.km.ui.PlanningActivity;

public class MainActivity extends ListActivity {

	List<String> liste = new Vector<String>();
	List<Formation> listFormation = null;
	ProgressDialog dialog;
	FormationContentProvider helper;
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

		Event event = new Event();
		Calendar cal = Calendar.getInstance();
		event.setStartTime(new Date());
		cal.setTime(event.getStartTime());
		Log.d("KM","" + cal.get(Calendar.WEEK_OF_YEAR));
		
		
		Button button = (Button) findViewById(R.id.button1);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(MainActivity.this,PlanningActivity.class);

				//				Calendar now = Calendar.getInstance();
				intent.putExtra("formationid", String.valueOf(750984));

				intent.putExtra("year", 2012);
				intent.putExtra("weekOfYear", 50);
				//intent.putExtra("weekOfYear", now.get(Calendar.WEEK_OF_YEAR));
				startActivity(intent);

			}
		});

		//helper = new FormationContentProvider();

		
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

		case R.id.menu_settings:
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	

	private void updateViewFromDb() {
		String[] column = new String[] {
				FormationHelper.KEY_ID,
				FormationHelper.GROUP_COLUMN,
				FormationHelper.NAME_COLUMN,
				FormationHelper.FORMATION_ID,
		};

		Uri mUri = FormationContentProvider.CONTENT_URI;

		Cursor cursor = getContentResolver().query(mUri, column, null, null, null);
		Log.v("KM", "-" + cursor.getCount());
		//Cursor cursor = helper.getFormations();
		cursorAdapter = new SimpleCursorAdapter(MainActivity.this, 
				android.R.layout.simple_list_item_1, 
				cursor, 
				new String[] {FormationHelper.NAME_COLUMN}, 
				new int[] {android.R.id.text1},0);
		setListAdapter(cursorAdapter);
	}


	@Override
	protected void onStop() {
		super.onStop();
	}
}