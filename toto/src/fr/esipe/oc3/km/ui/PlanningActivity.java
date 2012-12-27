package fr.esipe.oc3.km.ui;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import fr.esipe.agenda.parser.Event;
import fr.esipe.agenda.parser.Formation;
import fr.esipe.agenda.parser.Parser;
import fr.esipe.oc3.km.PlanningPreference;
import fr.esipe.oc3.km.R;
import fr.esipe.oc3.km.db.FormationContentProvider;
import fr.esipe.oc3.km.db.FormationHelper;

public class PlanningActivity extends FragmentActivity{

	private MyFragmentPagerAdapter pagerAdapter;
	private List<String> listNameFormation;
	private List<Formation> listFormation;
	private ProgressDialog dialog;
	private SharedPreferences preferences;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		preferences = PreferenceManager.getDefaultSharedPreferences(PlanningActivity.this);
		String formationId = preferences.getString("formationId", null);
		Toast.makeText(this, formationId, Toast.LENGTH_SHORT).show();
		listNameFormation = new Vector<String>();
		
		
		if(isEmptyDatabase(FormationContentProvider.CONTENT_URI) || formationId == null) {
			// recover the formation to database AsyncTask
			new GetFormationsFromServer().execute("");
		} else {
			setContentView(R.layout.weekviewpager);
			/** Getting a reference to the ViewPager defined the layout file */
			ViewPager pager = (ViewPager) findViewById(R.id.pager);

			/** Getting fragment manager */
			FragmentManager fm = getSupportFragmentManager();

			/** Instantiating FragmentPagerAdapter */
			Event event = new Event();
			event.setStartTime(new Date());
			event.setEndTime(new Date());
			List<String> labels = new Vector<String>();
			labels.add("Smartphone");
			labels.add("Yoann");
			labels.add("OC3");
			labels.add("1B09");
			event.setLabels(labels);
			List<Event> events = new Vector<Event>();
			events.add(event);
			events.add(event);
			events.add(event);
			events.add(event);
			events.add(event);
			pagerAdapter = new MyFragmentPagerAdapter(fm, events);

			/** Setting the pagerAdapter to the pager object */
			pager.setAdapter(pagerAdapter);
			Calendar now = Calendar.getInstance();

			pager.setCurrentItem(now.get(Calendar.WEEK_OF_YEAR));

		}


		//Boite de dialogue
		//Choix formation
		//save parameters for starting Service Calendar now, formation id, year
		//If(database empty)
		//	Wait on result
		//	ProgressDialog
		//else
		//	bouton refresh moving
		//	Display view
		//	On result, refresh view







		//		startService(new Intent(this,UpdatingDatabaseService.class));

	}



	/**
	 * Check if database is empty
	 * @param uri
	 * @return isEmpty ? true : false
	 */
	public boolean isEmptyDatabase(Uri uri) {
		Cursor cursor = getContentResolver().query(uri, null, null, null, null);
		boolean state = true;
		if(cursor.getCount() == 0)
			state = true;
		else
			state = false;

		cursor.close();
		return state;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.title_bar, menu);
		return true;
	}

	//Add button refresh, to force launching service

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_search:

			break;

		case R.id.menu_refresh:
			break;

		case R.id.menu_settings:
			startActivity(new Intent(PlanningActivity.this,PlanningPreference.class));
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	
	
	
	
	
	
	//-----------------------------------------------------------
	//	GetFormation



	/**
	 * Show dialog to choose formation
	 */
	private void showFormationDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(PlanningActivity.this);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(PlanningActivity.this, android.R.layout.simple_list_item_1, listNameFormation);
		builder.setTitle("Select your formation");
		builder.setCancelable(false);
		builder.setAdapter(adapter, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				//Saved in preference the formation Id
				String formationId = listFormation.get(which).getId();
				SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(PlanningActivity.this);
				SharedPreferences.Editor editor = settings.edit();
				editor.putString("formationId", formationId);
				editor.commit();

			}
		});
		builder.create().show();
	}


	private class GetFormationsFromServer extends AsyncTask<String, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = ProgressDialog.show(PlanningActivity.this, 
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
			addingFormationToDatabase();
			showFormationDialog();
		}

	}

	public void addingFormationToDatabase() {

		Uri mUri = FormationContentProvider.CONTENT_URI;

		for(Formation formation : listFormation) {
			ContentValues values = new ContentValues();
			Cursor cursor = getContentResolver().query(mUri,
					null, 
					FormationHelper.FORMATION_ID + "=?",
					new String[] {formation.getId()}, 
					null);

			values.put(FormationHelper.GROUP_COLUMN, formation.getGroup());
			values.put(FormationHelper.NAME_COLUMN, formation.getName());
			values.put(FormationHelper.FORMATION_ID, formation.getId());

			if(cursor.getCount() < 1) {
				getContentResolver().insert(mUri, values);
			} else {
				getContentResolver().update(mUri, values,
						FormationHelper.FORMATION_ID + "=?", 
						new String[] {formation.getId()});
			}

			// Adding listviewName
			listNameFormation.add(formation.getName());
		}
	}

}
