package fr.esipe.oc3.km.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.util.SparseArray;
import fr.esipe.agenda.parser.Event;

public class MyFragmentPagerAdapter extends FragmentPagerAdapter{


	private static int NUMBER_OF_WEEK;
	private SparseArray<Vector<Event>> events;


	public MyFragmentPagerAdapter(FragmentManager fm) {
		super(fm);
		
	}

	public void setData(SparseArray<Vector<Event>> events) {
		this.events = events;
		if(this.events == null)
			NUMBER_OF_WEEK = 0;
		else
			NUMBER_OF_WEEK = 4;
//			NUMBER_OF_WEEK = this.events.size();
		
	}

	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}
	
	/** This method will be invoked when a page is requested to create */
	@Override
	public Fragment getItem(int position) {
		List<Event> listEvent = events.get(position + 1);

		OneWeekView fragment = new OneWeekView();
		Bundle data = new Bundle();
		ArrayList<EventParcelable> eventsParce = new ArrayList<EventParcelable>();
		if(listEvent != null) {

			for(Event event : listEvent) {

				Log.d("KM", " lab = " + event.getLabels().get(0));
				eventsParce.add(new EventParcelable(event.getFormationId(), 
						event.getLabels(), 
						event.getStartTime(), 
						event.getEndTime()));
			}
		}

		data.putParcelableArrayList("events", eventsParce);
		fragment.setArguments(data);
		return fragment;
	}

	/** Returns the number of pages */
	@Override
	public int getCount() {
		return NUMBER_OF_WEEK;
	}

	/** Returns the title of pages */
	@Override
	public CharSequence getPageTitle(int position) {
		return "week #" + ( position + 1 );
	}

}
