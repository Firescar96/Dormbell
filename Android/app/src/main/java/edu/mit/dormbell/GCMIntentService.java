/*
 * Copyright (c) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package edu.mit.dormbell;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;

import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

import edu.mit.dormbell.org.json.json.JSONArray;
import edu.mit.dormbell.org.json.json.JSONObject;

/**
 * This class manages Google Cloud Messaging push notifications and CloudQuery
 * subscriptions.
 */
public class GCMIntentService extends IntentService {

    public GCMIntentService(String name) {
		super(name);
	}
    public static final String PROPERTY_REG_ID = "registration_id";

    public static final String PROPERTY_APP_VERSION = "app_version";

    public static final String BROADCAST_ON_MESSAGE = "on-message-event";

    private static GCMIntentService thisService;
    
    static String SENDER_ID = "81193489522";
    
    static GoogleCloudMessaging gcm;
    static AtomicInteger msgId = new AtomicInteger();
    static MainActivity context = MainActivity.context;
    static String regId;
    
    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty())
			/*
             * Filter messages based on message type. Since it is likely that GCM will be
             * extended in the future with new message types, just ignore any message types you're
             * not interested in, or that you don't recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType))
				Log.i("CloudBackend", "onHandleIntent: message error");
			else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType))
				Log.i("CloudBackend","onHandleIntent: message deleted");
            // If it's a regular GCM message, do some work.
			else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
            	// Post notification of received message.
                System.out.println("Received: " + extras.toString());
                
                if(context == null)
                {
                	System.out.println("null conxtext");
                	/*Intent needIntent = new Intent(this, MainActivity.class);
                    needIntent.putExtra("purpose", "update");
                    needIntent.putExtra("mate", (String)extras.get("mate"));
                    needIntent.putExtra("event", (String)extras.get("event"));
                    needIntent.putExtra("chat", (String)extras.get("chat"));
                    needIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(needIntent);*/
                	System.out.println(getFilesDir().getAbsolutePath());
                	MainActivity.initAppData(getFilesDir().getAbsolutePath());
                }
                
				try {			
	                if(extras.get("mate") != null)
	                {
						//context.appData.getJSONArray("mates").put(extras.get("mate"));
	                }
	                
	                if(extras.get("event") != null)
	                {
	                	JSONObject eveData=new JSONObject("{\"event\":"+extras.get("event")+"}").getJSONObject("event");
	                	JSONArray eve = MainActivity.appData.getJSONArray("events");
	                	for(int i = 0; i < eve.length(); i++)
	                	{
		                	System.out.println(eveData.getString("hash"));
		                	System.out.println(eve.getJSONObject(i).getString("hash"));
	                		if(eveData.getString("hash").equals(eve.getJSONObject(i).getString("hash")))
	                			return;
	                	}
	                	eveData.accumulate("member",false);
	                	System.out.println(eveData.getLong("date"));
	                	System.out.println(Calendar.getInstance().getTimeInMillis());
	                	if(eveData.getLong("date") < Calendar.getInstance().getTimeInMillis())
	                		return;

	                	eve.put(eveData);
						Message msg = new Message();
						Bundle data = new Bundle();
						data.putString("type", "event."+eveData.getString("privacy"));
						data.putString("username", eveData.getString("username"));
						data.putString("date", eveData.getString("date"));
					    msg.setData(data);
					    contextHandler.sendMessage(msg);
	                }

                	MainActivity.closeAppData(getFilesDir().getAbsolutePath());
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
            }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        MainBroadcastReceiver.completeWakefulIntent(intent);
    }

    private static Handler contextHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.getData().getString("type").contains("event"))
                {
                	Calendar date = Calendar.getInstance();
                	date.setTimeInMillis(Long.parseLong(msg.getData().getString("date")));
    				long nMin = date.get(Calendar.MINUTE);
    				long nHour = date.get(Calendar.HOUR_OF_DAY);
    				String day = Calendar.getInstance().getTimeInMillis() > date.getTimeInMillis()? " (tomorrow)" : "";
    				

					boolean annoy = false;
    				if(msg.getData().getString("type").contains("closed"))
    					annoy = true;
    				
                	Notify("Food at "+nHour+":"+ String.format("%02d",nMin) + day,"Eat with "+msg.getData().getString("username"), new Bundle(), 0,annoy);
                	
                	if(context != null) {
                		try {
    						System.out.println(MainActivity.appData.getJSONArray("events"));
    					} catch (JSONException e) {
    						e.printStackTrace();
    					}
                    	//context.mainPagerAdapter.getMain().updateEvents(); TODO: update something/ just push notifications depeneding on page
                	}
                }
            }
        };
        
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private static void Notify(String notificationTitle, String notificationMessage,Bundle data, int id,boolean annoy) 
        {
        	NotificationCompat.Builder mBuilder =
        	        new NotificationCompat.Builder(thisService)
        	        //.setSmallIcon(R.drawable.ic_launcher) TODO: Make an app icon
        	        .setContentTitle(notificationTitle)
        	        .setContentText(notificationMessage)
        	        .setAutoCancel(true);
        	// Creates an explicit intent for an Activity in your app
        	Intent resultIntent = new Intent(thisService, MainActivity.class);
        	resultIntent.putExtras(data);

        	// The stack builder object will contain an artificial back stack for the
        	// started Activity.
        	// This ensures that navigating backward from the Activity leads out of
        	// your application to the Home screen.
        	TaskStackBuilder stackBuilder = TaskStackBuilder.create(thisService);
        	// Adds the back stack for the Intent (but not the Intent itself)
        	stackBuilder.addParentStack(MainActivity.class);
        	// Adds the Intent that starts the Activity to the top of the stack
        	stackBuilder.addNextIntent(resultIntent);
        	PendingIntent resultPendingIntent =
        	        stackBuilder.getPendingIntent(
        	            0,
        	            PendingIntent.FLAG_UPDATE_CURRENT
        	        );
        	mBuilder.setContentIntent(resultPendingIntent);
        	if(annoy) {
        		Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        		mBuilder.setSound(alarmSound);
        		long[] pattern = {50,100,10,100,10,200};
        		mBuilder.setVibrate(pattern);
        	}
        	NotificationManager mNotificationManager =
        	    (NotificationManager) thisService.getSystemService(Context.NOTIFICATION_SERVICE);
        	// mId allows you to update the notification later on.
        	mNotificationManager.notify(id, mBuilder.build());
        	 }
    
    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    public static String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGcmPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if(regId != null)
        {
        	System.out.println("Registration found." + registrationId);
        	return regId;
        }
        if (registrationId.isEmpty()) {
        	System.out.println("Registration not found.");
            return "";
        }
        else
        	System.out.println("Registration found." + registrationId);
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            System.out.println("App version changed.");
            return "";
        }
        return registrationId;
    }
    
    /**
     * @return the stored SharedPreferences for GCM
     */
    private static SharedPreferences getGcmPreferences(Context context) {
        return context.getSharedPreferences(GCMIntentService.class.getSimpleName(), MODE_PRIVATE);
    }
    
    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    public static void registerInBackground() {
        new AsyncTask<Object, Object, Object>() {
            @Override
			protected String doInBackground(Object... params) {
                String msg = "";
                try {
                    if (gcm == null)
						gcm = GoogleCloudMessaging.getInstance(context);
                    regId = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regId;

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
        			System.out.println("regID: "+regId);
                    if(MainActivity.appData.getString("username").length() > 0)
                    	sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the
                    // message using the 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regId);
                } catch (IOException | JSONException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }

                System.out.println(msg);
                return msg;
            }

        }.execute(null, null, null);
    }


    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * or CCS to send messages to your app.
     */
    private static void sendRegistrationIdToBackend() {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("regId", regId);
            jsonObject.put("username", MainActivity.appData.getString("username"));
            context.sendJSONToBackend(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void GCMIntentService()
    {
        //required constructor for the service
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private static void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        System.out.println("Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }
    
    /**
     * @return Application's {@code SharedPreferences}.
     */
    private static SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return context.getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }
    
    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (Exception e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
}
