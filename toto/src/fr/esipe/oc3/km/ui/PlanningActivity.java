package fr.esipe.oc3.km.ui;

import java.util.Calendar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import fr.esipe.oc3.km.R;
import fr.esipe.oc3.km.UpdatingDatabaseService;

public class PlanningActivity extends FragmentActivity{

	private MyFragmentPagerAdapter pagerAdapter;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.weekviewpager);
		 /** Getting a reference to the ViewPager defined the layout file */
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
 
        /** Getting fragment manager */
        FragmentManager fm = getSupportFragmentManager();
 
        /** Instantiating FragmentPagerAdapter */
        pagerAdapter = new MyFragmentPagerAdapter(fm);
 
        /** Setting the pagerAdapter to the pager object */
        pager.setAdapter(pagerAdapter);
        Calendar now = Calendar.getInstance();
       
        pager.setCurrentItem(now.get(Calendar.WEEK_OF_YEAR));
        
        startService(new Intent(this,UpdatingDatabaseService.class));
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	//Add button refresh, to force launching service

	

}
