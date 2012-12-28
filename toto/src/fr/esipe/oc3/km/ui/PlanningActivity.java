package fr.esipe.oc3.km.ui;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import fr.esipe.agenda.parser.Event;
import fr.esipe.agenda.parser.Formation;
import fr.esipe.agenda.parser.Parser;
import fr.esipe.oc3.km.PlanningPreference;
import fr.esipe.oc3.km.R;
import fr.esipe.oc3.km.UpdatingDatabaseService;
import fr.esipe.oc3.km.db.FormationContentProvider;
import fr.esipe.oc3.km.db.FormationHelper;

public class PlanningActivity extends FragmentActivity{

	private MyFragmentPagerAdapter pagerAdapter;
	private List<String> listNameFormation;
	private List<Formation> listFormation;
	private Vector<Event>	listEvents;
	private ProgressDialog dialog;
	private SharedPreferences preferences;
	private UpdatingDBServiceReceiver receiver;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		listEvents = new Vector<Event>();
		preferences = PreferenceManager.getDefaultSharedPreferences(PlanningActivity.this);
		String formationId = preferences.getString("formationId", null);
		listNameFormation = new Vector<String>();
		
		
		if(isEmptyDatabase(FormationContentProvider.CONTENT_URI) || formationId == null) {
			// recover the formation to database AsyncTask
			new GetFormationsFromServer().execute("");
		} else {
			displayView();
			
			Event event = new Event();
			event.setStartTime(new Date());
			event.setEndTime(new Date());
			List<String> labels = new Vector<String>();
			labels.add("Java");
			labels.add("Duris");
			labels.add("OC3");
			labels.add("2B15-28");
			labels.add("EXAMEN");
			event.setLabels(labels);
			
			
			Event event1 = new Event();
			event1.setStartTime(new Date());
			event1.setEndTime(new Date());
			List<String> labels2 = new Vector<String>();
			labels2.add("Smartphone");
			labels2.add("Yoann");
			labels2.add("OC3");
			labels2.add("1B09");
			event1.setLabels(labels2);
			
			Event event11 = new Event();
			event11.setStartTime(new Date());
			event11.setEndTime(new Date());
			List<String>labels1 = new Vector<String>();
			labels1.add("RFIP");
			labels1.add("Paret");
			labels1.add("OC3");
			labels1.add("1B15");
			event11.setLabels(labels1);
			listEvents.add(event);
			listEvents.add(event1);
			listEvents.add(event11);
//			listEvents = null;
			
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
		// On current position
		//    save in database current week, current week -+ 1;
		// When swiping to right (go backward)
		//		delete current position +1
		//			getStartTime
		//			check if equals to current week +1
		//				delete row
		// 		get current position of fragment - 2
		//      
		// When swiping to left (go forward)
		// 		delete current position - 1
		//			getStartTime
		//			check if equals to current week -1
		//				delete row
		// 		get current position of fragment + 2
		//     
		// UpdateView

	}

	public class UpdatingDBServiceReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent arg1) {
			//updateView
			Toast.makeText(context, "Update finish", Toast.LENGTH_SHORT).show();
		}
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		IntentFilter filter = new IntentFilter(UpdatingDatabaseService.DATABASE_UPDATED);
		receiver = new UpdatingDBServiceReceiver();
		registerReceiver(receiver, filter);
		
		startUpdatingDbEventService();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		unregisterReceiver(receiver);
	}
	
	private void startUpdatingDbEventService() {
		Intent intent = new Intent(this,UpdatingDatabaseService.class);
		String formationId = preferences.getString("formationId", null);
		intent.putExtra("formationId", formationId);
		intent.putExtra("year", 2013);
		intent.putExtra("weekOfYear", 2);
		startService(intent);
	}
	
	

	private void displayView() {
		setContentView(R.layout.weekviewpager);
		/** Getting a reference to the ViewPager defined the layout file */
		ViewPager pager = (ViewPager) findViewById(R.id.pager);

		/** Getting fragment manager */
		FragmentManager fm = getSupportFragmentManager();

		/** Instantiating FragmentPagerAdapter */
		SparseArray<Vector<Event>> events = new SparseArray<Vector<Event>>();
		events.put(52, listEvents);
		pagerAdapter = new MyFragmentPagerAdapter(fm, events);

		/** Setting the pagerAdapter to the pager object */
		pager.setAdapter(pagerAdapter);
		Calendar now = Calendar.getInstance();

		pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
				
			}
		}); 
		pager.setCurrentItem(now.get(Calendar.WEEK_OF_YEAR));
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
		getMenuInflater().inflate(R.menu.title_bar, menu);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_search:

			//search week
			break;

		case R.id.menu_refresh:
			startUpdatingDbEventService();
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
				SharedPreferences.Editor editor = preferences.edit();
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
