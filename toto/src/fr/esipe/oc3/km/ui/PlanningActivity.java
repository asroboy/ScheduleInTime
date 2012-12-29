package fr.esipe.oc3.km.ui;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
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
import fr.esipe.agenda.parser.Event;
import fr.esipe.agenda.parser.Formation;
import fr.esipe.agenda.parser.Parser;
import fr.esipe.oc3.km.PlanningPreference;
import fr.esipe.oc3.km.R;
import fr.esipe.oc3.km.UpdatingDatabaseService;
import fr.esipe.oc3.km.db.FormationProvider;
import fr.esipe.oc3.km.providers.EventContentProvider;

public class PlanningActivity extends FragmentActivity{

	private MyFragmentPagerAdapter pagerAdapter;
	private List<String> listNameFormation;
	private List<Formation> listFormation;
	private SharedPreferences preferences;
	private UpdatingDBServiceReceiver receiver;
	private ViewPager pager;

	private String formationId;
	private int weekOfYear;
	private int year;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.weekviewpager);
		/** Getting a reference to the ViewPager defined the layout file */
		pager = (ViewPager) findViewById(R.id.pager);

		/** Getting fragment manager */
		FragmentManager fm = getSupportFragmentManager();

		/** Instantiating FragmentPagerAdapter */

		pagerAdapter = new MyFragmentPagerAdapter(fm);
		pagerAdapter.setData(null);
		pagerAdapter.notifyDataSetChanged();
		/** Setting the pagerAdapter to the pager object */
		pager.setAdapter(pagerAdapter);


		preferences = PreferenceManager.getDefaultSharedPreferences(PlanningActivity.this);
		formationId = preferences.getString("formationId", null);
		listNameFormation = new Vector<String>();


		if(formationId == null) {
			// recover the formation to database AsyncTask
			new GetFormationsFromServer().execute("");
		} else {
			weekOfYear = 2;
			year = 2013;

			//			Cursor finalized withou prior close()
			//			TODO
			//				Modify year in title_bar
			//				Get Event for current, past and future week
			//				when swiping, remove correct one
			//				replace temp value by constant in value folder
			//				Implementing SyncAdapter
			//				Modify view getting event from dataBase
			//				Modify Parameters to add current formation and a list to change formation
			//				On boot receiver to get refresh database
			//				(If database is modified, then add notification)
			//				Create a new Google Agenda and save the current week in it

		}
	}

	public class UpdatingDBServiceReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent arg1) {
			//updateView
			new UpdatingUiFromDatabase().execute("");
			//			Toast.makeText(context, "Update finish", Toast.LENGTH_SHORT).show();
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
		intent.putExtra("formationId", formationId);
		intent.putExtra("year", year);
		intent.putExtra("weekOfYear", weekOfYear);
		startService(intent);
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
				formationId = listFormation.get(which).getId();
				SharedPreferences.Editor editor = preferences.edit();
				editor.putString("formationId", formationId);
				editor.commit();

			}
		});
		builder.create().show();
	}


	private class GetFormationsFromServer extends AsyncTask<String, Void, Boolean> {

		private ProgressDialog dialog;

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

		FormationProvider provider = new FormationProvider(this);
		//		Uri mUri = FormationContentProvider.CONTENT_URI;

		for(Formation formation : listFormation) {

			provider.insert(formation);
			listNameFormation.add(formation.getName());

			//			ContentValues values = new ContentValues();
			//			
			//			Cursor cursor = provider.getItemFormation(formation);
			//			Cursor cursor = getContentResolver().query(mUri,
			//					null, 
			//					FormationHelper.FORMATION_ID + "=?",
			//					new String[] {formation.getId()}, 
			//					null);

			//			values.put(FormationHelper.GROUP_COLUMN, formation.getGroup());
			//			values.put(FormationHelper.NAME_COLUMN, formation.getName());
			//			values.put(FormationHelper.FORMATION_ID, formation.getId());

			//			if(cursor.getCount() < 1) {

			//				getContentResolver().insert(mUri, values);
			//			} else {
			//				getContentResolver().update(mUri, values,
			//						FormationHelper.FORMATION_ID + "=?", 
			//						new String[] {formation.getId()});
			//			}

			// Adding listviewName

		}
	}

	//---------------------------------------------------------
	//    Get events from database
	//---------------------------------------------------------


	public class UpdatingUiFromDatabase extends AsyncTask<String, Void, SparseArray<Vector<Event>>> {

		private Uri mUri;
		private Vector<Event> listEvents;
		private ProgressDialog dialog;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			listEvents = new Vector<Event>();
			mUri = EventContentProvider.CONTENT_URI;

			dialog = ProgressDialog.show(PlanningActivity.this, 
					"Recover data",
					"Charging in progress...",
					true);
			dialog.setCancelable(false);
		}

		@Override
		protected SparseArray<Vector<Event>> doInBackground(String... params) {

			SparseArray<Vector<Event>> eventsByWeek = new SparseArray<Vector<Event>>();

			Cursor cursor = getContentResolver().query(mUri, null,
					EventContentProvider.WEEK_OF_EVENTS + "=?",
					new String[] {String.valueOf(weekOfYear)},
					null);

			if(cursor != null) {
				if(cursor.moveToFirst()) {
					do {

						Event event = new Event();
						List<String> labels = new Vector<String>();
						event.setFormationId(cursor.getString(cursor.getColumnIndex(EventContentProvider.FORMATION_ID_COLUMN)));
						long date = Long.parseLong(cursor.getString(cursor.getColumnIndex(EventContentProvider.START_TIME_NAME_COLUMN)));
						event.setStartTime(new Date(date));
						date = Long.parseLong(cursor.getString(cursor.getColumnIndex(EventContentProvider.END_TIME_NAME_COLUMN)));
						event.setEndTime(new Date(date));

						labels.add(cursor.getString(cursor.getColumnIndex(EventContentProvider.TOPIC_NAME_COLUMN)));
						labels.add(cursor.getString(cursor.getColumnIndex(EventContentProvider.TEACHERS_NAME_COLUMN)));
						labels.add(cursor.getString(cursor.getColumnIndex(EventContentProvider.BRANCH_NAME_COLUMN)));
						labels.add(cursor.getString(cursor.getColumnIndex(EventContentProvider.CLASSROOM_NAME_COLUMN)));
						event.setLabels(labels);

						listEvents.add(event);
					} while(cursor.moveToNext());

					eventsByWeek.put(weekOfYear,listEvents);
				}
			}


			return eventsByWeek;
		}


		@Override
		protected void onPostExecute(SparseArray<Vector<Event>> result) {
			super.onPostExecute(result);
			dialog.dismiss();
			updatingUi(result);

		}
	}

	public void updatingUi(SparseArray<Vector<Event>> result) {


		pagerAdapter.setData(result);
		pagerAdapter.notifyDataSetChanged();
		pager.setCurrentItem(weekOfYear -1 );

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
		//pager.setCurrentItem(now.get(weekOfYear));
	}

}
