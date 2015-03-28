package edu.mit.dormbell.dormbell;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class MainBroadcastReceiver extends WakefulBroadcastReceiver {
    
    @Override
    public void onReceive(Context context, Intent intent) 
    {
    	
    	if(intent.getAction() == null)
    		return;
		
		if(intent.getAction().equals("com.google.android.c2dm.intent.RECEIVE"))
		{
	        // Explicitly specify that GcmIntentService will handle the intent.
	        ComponentName comp = new ComponentName(context.getPackageName(),
	                GCMIntentService.class.getName());
	        // Start the service, keeping the device awake while it is launching.
	        startWakefulService(context, intent.setComponent(comp));
	        //setResultCode(Activity.RESULT_OK);
		}
		
		if(intent.getAction().equals("android.intent.action.PACKAGE_REPLACED"))
		{
			GCMIntentService.context = (MainActivity) context;
			GCMIntentService.registerInBackground();
		}
    }
}
