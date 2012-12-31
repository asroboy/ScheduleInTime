package fr.esipe.oc3.km.ui;

import java.io.File;
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
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import fr.esipe.agenda.parser.Event;
import fr.esipe.agenda.parser.Formation;
import fr.esipe.oc3.km.PlanningPreference;
import fr.esipe.oc3.km.R;
import fr.esipe.oc3.km.UpdatingEventDbService;
import fr.esipe.oc3.km.UpdatingFormationDbService;
import fr.esipe.oc3.km.providers.EventContentProvider;

public class PlanningActivity extends FragmentActivity{

	private static final int PREF_REQUEST = 7;
	private MyFragmentPagerAdapter pagerAdapter;
	private List<Formation> listFormation;
	private SharedPreferences preferences;
	private UpdatedEventDbServiceReceiver receiverEvent;
	private UpdatedFormationDbServiceReceiver receiverFormation;
	private ViewPager pager;
	private ProgressDialog chargingUi;

	private int weekOfYear;
	private int year;
	private  TransparentPanel popup;
	private boolean state = false;



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
		/** Setting the pagerAdapter to the pager object */
		pager.setAdapter(pagerAdapter);


		initPopup();


		weekOfYear = 41;
		year = 2012;
		
		//			TODO
		//				Modify year in title_bar
		//				Get Event for current, past and future week (Done)
		//				when swiping, remove correct one
		//				replace temp value by constant in value folder
		//				Implementing SyncAdapter
		//				Modify view while getting event from dataBase
		//				Modify Parameters to add current formation and a list to change formation (Done)
		//				Add button like gmail to go back to current week (almost Done)
		//				On boot receiver to get refresh database
		//				(If database is modified, then add notification)
		//				Create a new Google Agenda and save the current week in it
		//				Result preference (Done)
		//				Cursor finalized without prior close()

	}


	/**
	 * InitPopup to display on non current page
	 */
	private void initPopup() {

		popup = (TransparentPanel) findViewById(R.id.panel);
		popup.setVisibility(View.GONE);
		TextView tv = (TextView)findViewById(R.id.tv);
		tv.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				pager.setCurrentItem(weekOfYear - 1);

			}
		});
	}

	public boolean databaseExist(String name) {
		File d = getApplicationContext().getDatabasePath(name);
		if(d.exists())
			return true;
		else 
			return false;
	}

	//-------------------------------
	//  Activity
	//-------------------------------

	@Override
	protected void onStart() {
		super.onStart();
		
		IntentFilter filterEvent = new IntentFilter(UpdatingEventDbService.DATABASE_EVENTS_UPDATED);
		receiverEvent = new UpdatedEventDbServiceReceiver();
		registerReceiver(receiverEvent, filterEvent);

		IntentFilter filterFormation = new IntentFilter(UpdatingFormationDbService.DATABASE_FORMATIONS_UPDATED);
		receiverFormation = new UpdatedFormationDbServiceReceiver();
		registerReceiver(receiverFormation, filterFormation);

		preferences = PreferenceManager.getDefaultSharedPreferences(PlanningActivity.this);
		String formationId = preferences.getString(getResources().getString(R.string.formation_key), null);

		if(formationId == null) {
			chargingUi = ProgressDialog.show(this, null, "Downloading formation...", true);
			chargingUi.setCancelable(false);
			startUpdatingFormationDbService();
		} else if (isEmptyDatabase(EventContentProvider.CONTENT_URI, weekOfYear, formationId)) {
			chargingUi = ProgressDialog.show(this, null, "Downloading planning...", true);
			chargingUi.setCancelable(false);
			startUpdatingEventDbService(formationId, year, weekOfYear, true);

		} else {
			new UpdatingUiFromDatabase().execute("");
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
		unregisterReceiver(receiverEvent);
		unregisterReceiver(receiverFormation);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	
	//-- end Activity
	
	
	
	/**
	 * Start service to update database with current weekOfYear
	 */
	private void startUpdatingEventDbService(String formationId, int year, int weekOfYear, boolean delete) {
		Intent intent = new Intent(this,UpdatingEventDbService.class);
		intent.putExtra("formationId", formationId);
		intent.putExtra("year", year);
		intent.putExtra("weekOfYear", weekOfYear);
		intent.putExtra("delete", delete);
		startService(intent);
	}

	/**
	 * Start service to update database with current weekOfYear
	 */
	private void startUpdatingFormationDbService() {
		Intent intent = new Intent(this,UpdatingFormationDbService.class);
		startService(intent);
	}

	/**
	 * Update the UI when database is updated
	 */
	public class UpdatedEventDbServiceReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			chargingUi.dismiss();
			new UpdatingUiFromDatabase().execute("");
		}
	}

	public class UpdatedFormationDbServiceReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			chargingUi.dismiss();
			List<String> listNameFormation = intent.getStringArrayListExtra("formation");
			String formationId = showFormationDialog(listNameFormation);
			startUpdatingEventDbService(formationId, year, weekOfYear, true);
		}
	}


	/**
	 * Check if database is empty
	 * @param uri
	 * @return isEmpty ? true : false
	 */
	public boolean isEmptyDatabase(Uri uri, int currentWeek, String formationId) {
		Cursor cursor = null;
		cursor = getContentResolver().query(uri,
				new String[] {EventContentProvider.FORMATION_ID_COLUMN},
				EventContentProvider.FORMATION_ID_COLUMN + " =?", 
				new String[]{formationId}, null);
		boolean state = true;

		
		if(cursor != null && cursor.getCount() > 0)
			state = false;

		cursor.close();
		return state;
	}


	//----------------------------------
	//  Menu
	//----------------------------------
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.title_bar, menu);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		//		case R.id.menu_search:
		//			//search week
		//			break;

		case R.id.menu_refresh:
			state = !state;
			setRefreshing(state, item);
			//			startUpdatingDbEventService();
			break;

		case R.id.menu_settings:
			startActivityForResult(new Intent(PlanningActivity.this,PlanningPreference.class), PREF_REQUEST);
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}


	private void setRefreshing(boolean refreshing, MenuItem item) {

		if (refreshing)
			item.setActionView(R.layout.action_bar_progress);
		else
			item.setActionView(null);
	}
	//--- End Menu -----------






	//---------------------------------------
	//	GetFormation
	//---------------------------------------


	/**
	 * Show dialog to choose formation
	 */
	private String showFormationDialog(List<String> listNameFormation) {
		AlertDialog.Builder builder = new AlertDialog.Builder(PlanningActivity.this);
		String formationId = null;
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(PlanningActivity.this, android.R.layout.simple_list_item_1, listNameFormation);
		builder.setTitle("Select your formation");
		builder.setCancelable(false);
		builder.setAdapter(adapter, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				//Saved in preference the formation Id
				String formationId = listFormation.get(which).getId();
				SharedPreferences.Editor editor = preferences.edit();
				editor.putString(getResources().getString(R.string.formation_key), formationId);
				editor.commit();

			}
		});
		builder.create().show();
		return formationId;
	}


	//	private class GetFormationsFromServer extends AsyncTask<String, Void, Boolean> {
	//
	//		private ProgressDialog dialog;
	//
	//		@Override
	//		protected void onPreExecute() {
	//			super.onPreExecute();
	//			dialog = ProgressDialog.show(PlanningActivity.this, 
	//					"Recover data",
	//					"Charging in progress...",
	//					true);
	//			dialog.setCancelable(false);
	//		}
	//
	//		@Override
	//		protected Boolean doInBackground(String... param) {
	//
	//			Parser p = new Parser();
	//			try {
	//				listFormation = p.parseFormationList();
	//			} catch (MalformedURLException e) {
	//				e.printStackTrace();
	//			} catch (IOException e) {
	//				e.printStackTrace();
	//			}
	//			return null;
	//		}
	//
	//		@Override
	//		protected void onPostExecute(Boolean result) {
	//			super.onPostExecute(result);
	//			dialog.dismiss();
	//			addingFormationToDatabase();
	//			showFormationDialog();
	//		}
	//
	//	}
	//
	//
	//
	//	public void addingFormationToDatabase() {
	//
	//		FormationProvider provider = new FormationProvider(this);
	//		//		Uri mUri = FormationContentProvider.CONTENT_URI;
	//
	//		for(Formation formation : listFormation) {
	//
	//			provider.insert(formation);
	//			listNameFormation.add(formation.getName());
	//		}
	//		provider.close();
	//
	//		//			ContentValues values = new ContentValues();
	//		//			
	//		//			Cursor cursor = provider.getItemFormation(formation);
	//		//			Cursor cursor = getContentResolver().query(mUri,
	//		//					null, 
	//		//					FormationHelper.FORMATION_ID + "=?",
	//		//					new String[] {formation.getId()}, 
	//		//					null);
	//
	//		//			values.put(FormationHelper.GROUP_COLUMN, formation.getGroup());
	//		//			values.put(FormationHelper.NAME_COLUMN, formation.getName());
	//		//			values.put(FormationHelper.FORMATION_ID, formation.getId());
	//
	//		//			if(cursor.getCount() < 1) {
	//
	//		//				getContentResolver().insert(mUri, values);
	//		//			} else {
	//		//				getContentResolver().update(mUri, values,
	//		//						FormationHelper.FORMATION_ID + "=?", 
	//		//						new String[] {formation.getId()});
	//		//			}
	//
	//		// Adding listviewName
	//
	//	}

	// -- End formation --------------------

	//---------------------------------------------------------
	//    Get events from database
	//---------------------------------------------------------


	public class UpdatingUiFromDatabase extends AsyncTask<String, Void, SparseArray<Vector<Event>>> {

		private Uri mUri; 
		private ProgressDialog dialog;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();

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
			Cursor cursor = null;
			int t = - 1;
			while (t < 5) {

				int mweekOfYear = weekOfYear + t;

				cursor = getContentResolver().query(mUri, null,
						EventContentProvider.WEEK_OF_EVENTS + "=?",
						new String[] { String.valueOf(mweekOfYear) },
						null);

				Vector<Event> listEvents = new Vector<Event>();
				if (cursor != null) {
					if (cursor.moveToFirst()) {
						do {

							Event event = new Event();
							List<String> labels = new Vector<String>();
							event.setFormationId(cursor.getString(cursor
									.getColumnIndex(EventContentProvider.FORMATION_ID_COLUMN)));
							long date = Long
									.parseLong(cursor.getString(cursor
											.getColumnIndex(EventContentProvider.START_TIME_NAME_COLUMN)));
							event.setStartTime(new Date(date));

							date = Long
									.parseLong(cursor.getString(cursor
											.getColumnIndex(EventContentProvider.END_TIME_NAME_COLUMN)));
							event.setEndTime(new Date(date));

							labels.add(cursor.getString(cursor
									.getColumnIndex(EventContentProvider.TOPIC_NAME_COLUMN)));
							labels.add(cursor.getString(cursor
									.getColumnIndex(EventContentProvider.TEACHERS_NAME_COLUMN)));
							labels.add(cursor.getString(cursor
									.getColumnIndex(EventContentProvider.BRANCH_NAME_COLUMN)));
							labels.add(cursor.getString(cursor
									.getColumnIndex(EventContentProvider.CLASSROOM_NAME_COLUMN)));
							event.setLabels(labels);

							listEvents.add(event);
						} while (cursor.moveToNext());
						eventsByWeek.put(mweekOfYear, listEvents);
					}
					t++;
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

	//-- End Get Event from DB-----------------


	//-----------------------------------------
	//  Updating UI
	//-----------------------------------------

	public void updatingUi(SparseArray<Vector<Event>> result) {


		pagerAdapter.setData(result);
		pagerAdapter.notifyDataSetChanged();
		pager.setAdapter(pagerAdapter);
		pager.setCurrentItem(weekOfYear);

		pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				if(arg0 == weekOfYear){
					popup.startAnimation(AnimationUtils.loadAnimation(PlanningActivity.this, R.anim.popup_hide));
					popup.setVisibility(View.GONE);
				}else if(popup.getVisibility() != View.VISIBLE) {
					popup.setVisibility(View.VISIBLE);
					popup.startAnimation(AnimationUtils.loadAnimation(PlanningActivity.this, R.anim.popup_show));
					TextView tv = (TextView)findViewById(R.id.tv);
					tv.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							pager.setCurrentItem(weekOfYear);

						}
					});
				}
			}

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				Log.d("KM", "" + position + " " + positionOffset + " " + positionOffsetPixels);

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {


			}
		}); 
	}

	//--- End Updating UI -----------------------------------
}
