package edu.mit.dormbell.dormbell;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicInteger;

import edu.mit.dormbell.org.json.json.JSONArray;
import edu.mit.dormbell.org.json.json.JSONObject;

public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        ProfileFragment.OnFragmentInteractionListener, RingRingFragment.OnFragmentInteractionListener,
        DoorbellsFragment.OnFragmentInteractionListener, LeaderboardFragment.OnFragmentInteractionListener,
        SettingsFragment.OnFragmentInteractionListener, FeedbackFragment.OnFragmentInteractionListener {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    public static MainActivity context;
    public static JSONObject appData;

    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    SharedPreferences prefs;
    LocationServices locServices;
    String regid;

    //identifiers for the fragments
    private static final int PROFILE=1;
    private static final int RING_RING=2;
    private static final int DOORBELL=3;
    private static final int LEADERBOARD=4;
    private static final int SETTINGS=5;
    private static final int FEEDBACK=6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        initAppData(getFilesDir().getAbsolutePath());

        //Check device for Play Services APK. If check succeeds, proceed with
        //GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = GCMIntentService.getRegistrationId(this);
            if (regid.isEmpty())
                GCMIntentService.registerInBackground();
        } else
            System.out.println("No valid Google Play Services APK found.");



        if(getIntent().getDataString() != null)
            if(getIntent().getDataString().contains("18.181.2.180:666"))
            {
                final String data = getIntent().getDataString().substring(24);
                if(data.startsWith("e"))
                {
                    System.out.println("event sent "+data.substring(1));
                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.accumulate("to", MainActivity.appData.getString("host"));
                        jsonObject.accumulate("hash", data.substring(1));
                        sendJSONToBackend(jsonObject);
                    } catch(JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

        Bundle data = getIntent().getExtras();
        if(data!=null)
            Log.i("mainact", data.toString());
        if(data != null)
            if(data.getString("sender") != null && data.getString("sender").equals("chat"))
                try {
                    JSONArray eve = appData.getJSONArray("events");
                    for(int i =0; i < eve.length(); i++) {
                        JSONObject curEve = eve.getJSONObject(i);
                        if(curEve.getString("hash").equals(data.get("hash"))) {
                        }
                    }
                } catch (JSONException e) {e.printStackTrace();}

        locServices = new LocationServices();
    }

    /**
     * Load appData from fileDir or create from scratch if fileDir does not exist
     * @param fileDir to lead from
     */
    public static void initAppData(String fileDir)
    {
        File defFile = new File(fileDir+"/appData.txt");
        if(!defFile.exists())
            try {
                new PrintWriter(new FileWriter(defFile.getAbsolutePath()));
            }catch(IOException e) {}

        try {
            BufferedReader br = new BufferedReader(new FileReader(defFile));
            String line;
            StringBuilder datBuf = new StringBuilder();
            while ((line = br.readLine()) != null) {
                datBuf.append(line);
                datBuf.append('\n');
            }
            br.close();
            appData = new JSONObject(datBuf.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("recreating appdata");
            JSONArray locks = new JSONArray();
            String usr = new String("testUser1");
            appData = new JSONObject();
            try {
                appData.put("locations", locks);
                appData.put("username", usr);
            } catch (Exception e1) {}
        }
    }

    /**
     * Save the appData JSON to file
     * @param fileDir to save file to
     */
    public static void closeAppData(String fileDir)
    {
        File defFile = new File(fileDir+"/appData.txt");
        PrintWriter out;
        try {
            out = new PrintWriter(new FileWriter(defFile.getAbsolutePath()));
            out.println(appData.toString());
            //out.println("");	//uncomment to reset the database
            out.close();
        }catch(IOException e) {}
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        locServices.connect();
    }

    // Play Services APK check here too.
    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if(cm.getActiveNetworkInfo() == null)
            Toast.makeText(context, "No Internet connection detected, Dormbell entering offline mode", Toast.LENGTH_SHORT).show();
    }
/*
	public void onConfigurationChanged() {
		mainPagerAdapter.getMain()
	}*/

    private boolean checkPlayServices() {
        System.out.println("checking play services");
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            else {
                System.out.println("This device is not supported.");
                Toast.makeText(context, "Sorry, Google Play Services is required for this app", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    public static void hideSoftKeyboard(Activity activity, View v) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    //called by fragments to perform some function common to many fragments
    public static void setupUI(View view) {

        //Set up touch listener for non-text box views to hide keyboard.
        if(!(view instanceof EditText))
            view.setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(context,v);
                    return false;
                }

            });

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup)
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
    }

    /**
     * send specified jsonObject to the backend server
     * @param jsonObject
     */
    public static void sendJSONToBackend(final JSONObject jsonObject)
    {
        new AsyncTask<Object, Object, Object>() {
            @Override
            protected Object doInBackground(Object... arg0) {
                if (Looper.myLooper() == null)
                    Looper.prepare();
                String msg = "";
                InputStream inputStream = null;

                try {

                    // 1. create HttpClient
                    HttpClient httpclient = new DefaultHttpClient();

                    // 2. make POST request to the given URL
                    HttpPost httpPost = new HttpPost("http://18.181.2.180:3667");

                    String json = "";

                    // 4. convert JSONObject to JSON to String
                    json = jsonObject.toString();

                    // 5. set json to StringEntity
                    StringEntity se = new StringEntity(json);

                    // 6. set httpPost Entity
                    httpPost.setEntity(se);

                    // 7. Set some headers to inform server about the type of the content
                    httpPost.setHeader("Accept", "application/json");
                    httpPost.setHeader("Content-type", "application/json");

                    HttpParams httpParams = httpclient.getParams();
                    HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
                    HttpConnectionParams.setSoTimeout(httpParams, 5000);
                    httpPost.setParams(httpParams);

                    // 8. Execute POST request to the given URL
                    System.out.println("executing"+json);
                    HttpResponse httpResponse = httpclient.execute(httpPost);
                    // 9. receive response as inputStream
                    inputStream = httpResponse.getEntity().getContent();

                    // 10. convert inputstream to string
                    if(inputStream != null)
                        msg = "it worked";
                    else
                        msg = "Did not work!";

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return msg;
            }
        }.execute(null, null, null);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        switch (position+1) {
            case PROFILE:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, ProfileFragment.newInstance(PROFILE))
                        .commit();
                break;
            case RING_RING:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, RingRingFragment.newInstance(RING_RING))
                        .commit();
                break;
            case DOORBELL:
                fragmentManager.beginTransaction()
                        .replace(R.id.container,DoorbellsFragment.newInstance(DOORBELL))
                        .commit();
                break;
            case LEADERBOARD:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, LeaderboardFragment.newInstance(LEADERBOARD))
                        .commit();
                break;
            case SETTINGS:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, SettingsFragment.newInstance(SETTINGS))
                        .commit();
                break;
            case FEEDBACK:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, FeedbackFragment.newInstance(FEEDBACK))
                        .commit();
                break;
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
            case 4:
                mTitle = getString(R.string.title_section4);
                break;
            case 5:
                mTitle = getString(R.string.title_section5);
                break;
            case 6:
                mTitle = getString(R.string.title_section6);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(int id) {

    }

    @Override
    protected void onPause()
    {
        super.onPause() ;
        closeAppData(getFilesDir().getAbsolutePath());
    }

    @Override
    protected void onStop() {
        super.onStop();
        closeAppData(getFilesDir().getAbsolutePath());

        locServices.disconnect();
    }
}
