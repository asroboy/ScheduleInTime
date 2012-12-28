package fr.esipe.oc3.km.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import fr.esipe.agenda.parser.Event;

public class MyFragmentPagerAdapter extends FragmentPagerAdapter{


	private static final int NUMBER_OF_WEEK = 52;
	private SparseArray<Vector<Event>> events;
	private List<Event> listEvent;


	public MyFragmentPagerAdapter(FragmentManager fm, SparseArray<Vector<Event>> events) {
		super(fm);
		this.events = events;
	}


	/** This method will be invoked when a page is requested to create */
	@Override
	public Fragment getItem(int position) {
		listEvent = events.get(position + 1);

		OneWeekView fragment = new OneWeekView();
		Bundle data = new Bundle();
		ArrayList<EventParcelable> eventsParce = new ArrayList<EventParcelable>();
		if(listEvent != null) {
			for(Event event : listEvent) {
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
