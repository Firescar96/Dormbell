package edu.mit.dormbell;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

public class LocationAPI implements
GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener,
            LocationListener {

	private MainActivity context = MainActivity.context;
    private final String TAG = "LocationServices";
	private LocationRequest locRequest;
    private GoogleApiClient mGoogleApiClient;

	public LocationAPI()
	{

        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

		locRequest = LocationRequest.create();
        locRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // Use high accuracy
        locRequest.setInterval(5000); // Set the update interval to 5 seconds
        locRequest.setFastestInterval(5000); // Set the fastest update interval to 5 second
	}

	public void connect() {mGoogleApiClient.connect();}

	public void disconnect() {mGoogleApiClient.disconnect();}

	public Location getLastLocation()
    {return LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);}

	/*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle dataBundle) {

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, locRequest, this);

    	sendLocationToBackend();
    }

    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution())
			System.out.println("location could not happen but a solution exists");
			/*
			 * Thrown if Google Play services canceled the original
			 * PendingIntent
			 */
		else
			/*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            System.exit(1);
    }
    
    @Override
    public void onLocationChanged(Location location) {
    	sendLocationToBackend();
    }
    
    public void sendLocationToBackend() {
        new AsyncTask<Object,Object,Object>() {
			@Override
			protected Object doInBackground(Object ... param) {
                try {
                    
                    JSONObject jsonObject = new JSONObject();
                    JSONObject lokiSon = new JSONObject();
                    Location loki = getLastLocation();
                    lokiSon.accumulate("latitude", loki.getLatitude());
                    lokiSon.accumulate("longitude", loki.getLongitude());
                    jsonObject.put("location", lokiSon);
                    jsonObject.accumulate("username", MainActivity.appData.getString("username"));
         
                    MainActivity.sendJSONToBackend(jsonObject);
         
                } catch (Exception e) {
                    e.printStackTrace();;
                }
				return param;
            }
        }.execute(null, null, null);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiClient connection has been suspend");
    }
}