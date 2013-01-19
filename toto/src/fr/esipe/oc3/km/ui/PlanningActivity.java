package fr.esipe.oc3.km.ui;

import java.io.File;
import java.util.Calendar;
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
import android.widget.Toast;
import fr.esipe.agenda.parser.Event;
import fr.esipe.oc3.km.PlanningPreference;
import fr.esipe.oc3.km.R;
import fr.esipe.oc3.km.db.FormationHelper;
import fr.esipe.oc3.km.db.FormationProvider;
import fr.esipe.oc3.km.providers.EventContentProvider;
import fr.esipe.oc3.km.services.UpdatingEventDbService;
import fr.esipe.oc3.km.services.UpdatingFormationDbService;

public class PlanningActivity extends FragmentActivity{

	private MyFragmentPagerAdapter pagerAdapter;
	private SharedPreferences preferences;
	private UpdatedEventDbServiceReceiver receiverEvent;
	private UpdatedFormationDbServiceReceiver receiverFormation;
	private ViewPager pager;
	private ProgressDialog chargingUi;
	private Menu menu;

	private int weekOfYear;
	private int year;
	private  TransparentPanel popup;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.weekviewpager);
		/** Getting a reference to the ViewPager defined the layout file */
		pager = (ViewPager) findViewById(R.id.pager);

		/** Getting fragment manager */
		FragmentManager fm = getSupportFragmentManager();

		/** Instantiating FragmentPagerAdapter */

		pagerAdapter = new MyFragmentPagerAdapter(this, fm);
		pagerAdapter.setData(null);
		/** Setting the pagerAdapter to the pager object */
		pager.setAdapter(pagerAdapter);


		initPopup();


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
				pager.setCurrentItem(weekOfYear);

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

		Calendar now = Calendar.getInstance();
		year = now.get(Calendar.YEAR);
		weekOfYear = now.get(Calendar.WEEK_OF_YEAR);



		preferences = PreferenceManager.getDefaultSharedPreferences(PlanningActivity.this);
		String formationId = preferences.getString(getResources().getString(R.string.formation_key), null);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt("currPagePosition", weekOfYear);
		editor.commit();

		if(formationId == null) {
			chargingUi = ProgressDialog.show(this, null, getResources().getString(R.string.formation_progress_diag) + "...", true);
			chargingUi.setCancelable(false);
			startUpdatingFormationDbService();
		} else if (isEmptyDatabase(EventContentProvider.CONTENT_URI, weekOfYear, formationId)) {
			chargingUi = ProgressDialog.show(this, null, getResources().getString(R.string.event_progress_diag) + "...", true);
			chargingUi.setCancelable(false);
			startUpdatingEventDbService(formationId, year, weekOfYear, true, 6);

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
	private void startUpdatingEventDbService(String formationId, int year, int weekOfYear, boolean delete, int numberOfWeek) {
		Intent intent = new Intent(this,UpdatingEventDbService.class);
		intent.putExtra(getResources().getString(R.string.event_intent_formation_id), formationId);
		intent.putExtra(getResources().getString(R.string.event_intent_year), year);
		intent.putExtra(getResources().getString(R.string.event_intent_week_of_year), weekOfYear);
		intent.putExtra(getResources().getString(R.string.event_intent_delete), delete);
		intent.putExtra(getResources().getString(R.string.event_intent_number_of_week), numberOfWeek);
		startService(intent);
	}

	/**
	 * Start service to update database with current weekOfYear
	 */
	private void startUpdatingFormationDbService() {

		Intent intent = new Intent(PlanningActivity.this,UpdatingFormationDbService.class);
		startService(intent);
	}

	/**
	 * Update the UI when database is updated
	 */
	public class UpdatedEventDbServiceReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(chargingUi != null)	
				chargingUi.dismiss();
			setRefreshing(false);
			new UpdatingUiFromDatabase().execute("");
		}
	}

	public class UpdatedFormationDbServiceReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			chargingUi.dismiss();
			List<String> listNameFormation = intent.getStringArrayListExtra(
					getResources().getString(R.string.formation_intent_list_name));
			showFormationDialog(listNameFormation);
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
		this.menu = menu;
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.menu_refresh:
			setRefreshing(true);
			preferences = PreferenceManager.getDefaultSharedPreferences(PlanningActivity.this);
			String formationId = preferences.getString(getResources().getString(R.string.formation_key), null);
			startUpdatingEventDbService(formationId, year, weekOfYear, false, 6);
			break;

		case R.id.menu_settings:
			startActivity(new Intent(PlanningActivity.this,PlanningPreference.class));
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}


	private void setRefreshing(boolean refreshing) {

		MenuItem item = menu.findItem(R.id.menu_refresh);

		if (refreshing)
			item.setActionView(R.layout.action_bar_progress);
		else {
			item.setActionView(null);
			Toast.makeText(PlanningActivity.this, "Database updated", Toast.LENGTH_SHORT).show();
		}
	}
	//--- End Menu -----------




	/**
	 * Show dialog to choose formation
	 */
	private void showFormationDialog(final List<String> listNameFormation) {
		final FormationProvider provider = new FormationProvider(PlanningActivity.this);
		AlertDialog.Builder builder = new AlertDialog.Builder(PlanningActivity.this);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(PlanningActivity.this, android.R.layout.simple_list_item_1, listNameFormation);
		builder.setTitle("Select your formation");
		builder.setCancelable(false);
		builder.setAdapter(adapter, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				//Saved in preference the formation Id
				Cursor c = provider.getItemFormation(listNameFormation.get(which));
				String formationId = null;
				if(c != null) {
					if(c.moveToFirst()) {
						formationId = c.getString(c.getColumnIndex(FormationHelper.FORMATION_ID));
					}
				}
				SharedPreferences.Editor editor = preferences.edit();
				editor.putString(getResources().getString(R.string.formation_key), formationId);
				editor.commit();
				chargingUi = ProgressDialog.show(PlanningActivity.this, null, getResources().getString(R.string.event_progress_diag) + "...", true);
				chargingUi.setCancelable(false);
				startUpdatingEventDbService(formationId, year, weekOfYear, true, 6);
			}
		});
		builder.create().show();
	}




	public class UpdatingUiFromDatabase extends AsyncTask<String, Void, SparseArray<Vector<Event>>> {

		private Uri mUri; 
		private ProgressDialog dialog;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			mUri = EventContentProvider.CONTENT_URI;

			dialog = ProgressDialog.show(PlanningActivity.this, 
					null,
					getResources().getString(R.string.event_refresh_prog_diag)+"...",
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
		preferences = PreferenceManager.getDefaultSharedPreferences(PlanningActivity.this);
		int currpage = preferences.getInt("currPagePosition", weekOfYear);
		pager.setCurrentItem(currpage);


		pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {

				SharedPreferences.Editor editor = preferences.edit();
				editor.putInt("currPagePosition", pager.getCurrentItem());
				editor.commit();

				int pageCount = pagerAdapter.getCount();
				if (position == 0){
					pager.setCurrentItem(pageCount-2,false);
				} else if (position == pageCount-1){
					pager.setCurrentItem(1,false);
				}

				if(position == weekOfYear){
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
					preferences = PreferenceManager.getDefaultSharedPreferences(PlanningActivity.this);
					String formationId = preferences.getString(getResources().getString(R.string.formation_key), null);

					if (position < weekOfYear){

						setRefreshing(true);
						startUpdatingEventDbService(formationId, year, position - 1, false, 1);
					}
					else {
						if (position >= weekOfYear + 2) {
							setRefreshing(true);
							startUpdatingEventDbService(formationId, year, position, false, 6);
						}

					}

				}
			}

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				Log.d("KM", "" + position + " " + positionOffset + " " + positionOffsetPixels + " " + pager.getCurrentItem());


			}

			@Override
			public void onPageScrollStateChanged(int position) {
				//				Log.d("KM", "" + position + " " + pager.getCurrentItem());
			}
		}); 
	}

	//--- End Updating UI -----------------------------------
}
