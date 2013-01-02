package fr.esipe.oc3.km.receiver;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import fr.esipe.oc3.km.R;
import fr.esipe.oc3.km.services.UpdatingEventDbService;

public class AlarmManagerBroadcastReceiver extends BroadcastReceiver{

	private static final String TAG_ALARM = "fr.esipe.oc3.km.AlarmManagerBroadcastReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG_ALARM);
		//Acquire the lock
		wl.acquire();

		try {
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
			String formationId = preferences.getString(context.getResources().getString(R.string.formation_key), null);
			Calendar now = Calendar.getInstance();
			int year = now.get(Calendar.YEAR);
			int weekOfYear = now.get(Calendar.WEEK_OF_YEAR);



			Intent intentService = new Intent(context, UpdatingEventDbService.class);
			intentService.putExtra(context.getResources().getString(R.string.event_intent_formation_id), formationId);
			intentService.putExtra(context.getResources().getString(R.string.event_intent_year), year);
			intentService.putExtra(context.getResources().getString(R.string.event_intent_week_of_year), weekOfYear);
			intentService.putExtra(context.getResources().getString(R.string.event_intent_delete), false);
			context.startActivity(intentService);

		} finally {
			//release the lock
			wl.release();
		}
	}


	public void SetAlarm(Context context)
	{
		AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
		//After after 5 seconds

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		String frequency = preferences.getString(context.getResources().getString(R.string.frequency_key), null);
		am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS * Integer.parseInt(frequency) , pi); 
	}

	public void CancelAlarm(Context context)
	{
		Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
		PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(sender);
	}
}
