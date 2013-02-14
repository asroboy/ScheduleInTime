package fr.esipe.oc3.km.receiver;

import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import fr.esipe.oc3.km.R;
import fr.esipe.oc3.km.services.UpdatingEventDbService;

public class SyncOnBootReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		boolean startOnBoot = settings.getBoolean(context.getResources().getString(R.string.auto_start_key), false);
		String formationId = settings.getString(context.getResources().getString(R.string.formation_key), null);
		
		Calendar now = Calendar.getInstance();
		int year = now.get(Calendar.YEAR);
		int weekOfYear = now.get(Calendar.WEEK_OF_YEAR);
		if(startOnBoot){

			Intent intentdb = new Intent(context,UpdatingEventDbService.class);
			intentdb.putExtra(context.getResources().getString(R.string.event_intent_formation_id), formationId);
			intentdb.putExtra(context.getResources().getString(R.string.event_intent_year), year);
			intentdb.putExtra(context.getResources().getString(R.string.event_intent_week_of_year), weekOfYear);
			intentdb.putExtra(context.getResources().getString(R.string.event_intent_delete), false);
			intentdb.putExtra(context.getResources().getString(R.string.event_intent_number_of_week), 6);
			context.startService(intent);
		}
	}

}
