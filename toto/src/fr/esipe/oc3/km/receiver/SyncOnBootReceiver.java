package fr.esipe.oc3.km.receiver;

import fr.esipe.oc3.km.R;
import fr.esipe.oc3.km.R.string;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SyncOnBootReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

		boolean startOnBoot = settings.getBoolean(context.getResources().getString(R.string.auto_start_key), false);
		if(startOnBoot){

//			Intent service = new Intent(context, UnlockService.class);
//			context.startService(service);
		}
	}

}
