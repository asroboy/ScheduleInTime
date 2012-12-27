package fr.esipe.oc3.km.ui;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import fr.esipe.agenda.parser.Event;

public class MyFragmentPagerAdapter extends FragmentPagerAdapter{

	
	private static final int NUMBER_OF_WEEK = 52;
	private List<Event> events;
	
	public MyFragmentPagerAdapter(FragmentManager fm, List<Event> events) {
		super(fm);
		this.events = events;
	}
	

	/** This method will be invoked when a page is requested to create */
	@Override
	public Fragment getItem(int position) {
		OneWeekView fragment = new OneWeekView();
		Bundle data = new Bundle();
		ArrayList<EventParcelable> eventsParce = new ArrayList<EventParcelable>();
		for(Event event : events) {
			eventsParce.add(new EventParcelable(event.getFormationId(), 
					event.getLabels(), 
					event.getStartTime(), 
					event.getEndTime()));
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
