package edu.mit.dormbell.setup;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Order;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Locale;

import edu.mit.dormbell.GCMIntentService;
import edu.mit.dormbell.MainActivity;
import edu.mit.dormbell.R;

public class SetupActivity extends Activity implements Validator.ValidationListener {

    @Order(value = 1)
    @NotEmpty(message = "you cannot be nameless")
    private EditText fullname;

    @Order(value = 2)
    @NotEmpty(message = "you need an identifier")
    private EditText username;

    private static SetupActivity activity;
    private static MainActivity context = MainActivity.context;
    private static String TAG = "SetupActivity";

    private Validator validator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        activity = this;
        validator = new Validator(this);
        validator.setValidationListener(this);

        fullname = (EditText) findViewById(R.id.fullname);
        username = (EditText) findViewById(R.id.username);
    }

    public void checkname(View v) {
        new ChecknameTask().execute(username.getText().toString());
    }

    @Override
    public void onValidationSucceeded() {

        Toast.makeText(this, "Yay! we got it right!", Toast.LENGTH_SHORT).show();
        checkname(null);
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);

            // Display error messages ;)
            if (view == fullname) {
                fullname.setError(message);
            }
            else if (view == username) {
                username.setError(message);
            }
        }
    }

    public void onContinue(View v)
    {
        validator.validate();
    }

    protected static boolean precise; //whether we are matching the entered name and regId

    public class ChecknameTask extends AsyncTask<String, Object, Boolean> {

        @Override
        protected Boolean doInBackground(String... name) {
            if(name.length == 0)
                return false;

            if (Looper.myLooper() == null)
                Looper.prepare();
            Message ms = new Message();
            Bundle dat = new Bundle();
            dat.putString("command", "checkName");
            dat.putString("value", "progress");
            ms.setData(dat);
            contextHandler.sendMessage(ms);

            InputStream inputStream = null;

            try {

                // 1. create HttpClient
                HttpClient httpclient = new DefaultHttpClient();

                // 2. make POST request to the given URL
                System.out.println();

                HttpGet httpGet;
                if(precise)
                    httpGet = new HttpGet("http://18.181.2.180:3667?checkName="+name[0].toUpperCase(Locale.US)+"&regId="+ GCMIntentService.getRegistrationId(context));
                else
                    httpGet = new HttpGet("http://18.181.2.180:3667?checkName="+name[0].toUpperCase(Locale.US));

                // 7. Set some headers to inform server about the type of the content
                httpGet.setHeader("Accept", "application/json");
                httpGet.setHeader("Content-type", "application/json");

                HttpParams httpParams = httpclient.getParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
                HttpConnectionParams.setSoTimeout(httpParams, 5000);
                httpGet.setParams(httpParams);

                // 8. Execute POST request to the given URL
                System.out.println("executing");
                HttpResponse httpResponse = httpclient.execute(httpGet);
                // 9. receive response as inputStream
                inputStream = httpResponse.getEntity().getContent();

                // 10. convert inputstream to string
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putString("command", "checkName");

                data.putBoolean("progress", false);
                if(inputStream != null)
                    if(convertStreamToString(inputStream).contains("true"))
                        data.putBoolean("value", true);
                    else
                        data.putBoolean("value", false);
                else
                    data.putBoolean("progress", true);
                msg.setData(data);
                contextHandler.sendMessage(msg);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    }


    protected static Handler contextHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.getData().getString("command").equals("checkName"))
            {
                View visible;
                View gone1;
                View gone2;
                if(msg.getData().getBoolean("progress")) {
                    visible = activity.findViewById(R.id.nameProgress);
                    gone1 = activity.findViewById(R.id.nameGood);
                    gone2 = activity.findViewById(R.id.nameBad);
                }
                else if(msg.getData().getBoolean("value"))
                {
                    visible = activity.findViewById(R.id.nameBad);
                    gone1 = activity.findViewById(R.id.nameGood);
                    gone2 = activity.findViewById(R.id.nameProgress);
                }
                else
                {
                    visible = activity.findViewById(R.id.nameGood);
                    gone1 = activity.findViewById(R.id.nameBad);
                    gone2 = activity.findViewById(R.id.nameProgress);
                }
                gone1.setVisibility(View.GONE);
                gone2.setVisibility(View.GONE);
                visible.setVisibility(View.VISIBLE);
            }
        }
    };

    protected String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null)
                sb.append(line + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
