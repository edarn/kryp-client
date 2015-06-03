package se.tna.weathermaster;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import se.tna.commonloggerservice.KrypgrundsService;

public class MyBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent startServiceIntent = new Intent(context, KrypgrundsService.class);
    context.startService(startServiceIntent);
    
    Intent startActivityIntent = new Intent(context, KrypgrundGUI.class);
    startActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(startActivityIntent);
    
	}
}
