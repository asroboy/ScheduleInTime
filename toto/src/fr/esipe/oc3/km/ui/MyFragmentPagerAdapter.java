package fr.esipe.oc3.km.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class MyFragmentPagerAdapter extends FragmentPagerAdapter{

	
	private static final int NUMBER_OF_WEEK = 52;
	
	public MyFragmentPagerAdapter(FragmentManager fm) {
		super(fm);
	}
	

	/** This method will be invoked when a page is requested to create */
	@Override
	public Fragment getItem(int position) {
		OneWeekView fragment = new OneWeekView();
		Bundle data = new Bundle();
		data.putInt("current_page", position+1);
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
