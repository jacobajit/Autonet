package com.el1t.iolite;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.el1t.iolite.item.EighthBlockItem;
import com.el1t.iolite.parser.EighthActivityXmlParser;
import com.el1t.iolite.parser.EighthBlockXmlParser;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import 	java.text.SimpleDateFormat;
import android.app.AlarmManager;
import android.app.PendingIntent;
import javax.net.ssl.HttpsURLConnection;
import android.content.Context;


public class AutonetActivity extends ActionBarActivity {
    private Cookie[] mCookies;
    private ArrayList<EighthBlockItem> blockList;
    private ArrayList<AsyncTask> mTasks;
    private SharedPreferences preferences;
    private SharedPreferences.Editor mEditor;
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        preferences= getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
        mEditor= preferences.edit();

        setContentView(R.layout.activity_autonet);
        final Intent intent = getIntent();
        if(intent.getBooleanExtra("newData",false))//checks if an activity preference selection is being sent from SignupActivity
        {
            updateData(intent.getStringExtra("blockName"),intent.getIntExtra("AID",-1));
        }
        mCookies = LoginActivity.getCookies(preferences);
        mTasks = new ArrayList<>();
        updateData("MonB",628);//set up random preferences for testing
        updateData("WedA",2707);
        updateData("WedB", 2934);
        updateData("FriA",207);
        updateData("FriB",993);

        Context context=this;

        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intentReceiver = new Intent(context, AlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intentReceiver, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 3);
        int minute=(int)Math.random()*60;
        calendar.set(Calendar.MINUTE, minute);


        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent);


        //Intent mServiceIntent = new Intent(this, AutonetService.class);//sign up for activities
        //this.startService(mServiceIntent);


    }
    public void updateData(String blockName,int AID)//save preferences for a certain block
    {
        mEditor.putInt(blockName, AID).commit();
        Log.d("TAG","Data updated");

    }

    public void saveSelection(int BID,String block) //Get an activity selection through SignupActivity
    {
        // Send data to SignupActivity
        final Intent intent = new Intent(this, SignupActivity.class);
        intent.putExtra("block",block);
        intent.putExtra("BID", BID);
        intent.putExtra("returnAID", true);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_autonet, menu);
        return true;
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


    private class BlockListRequest extends AsyncTask<String, Void, ArrayList<EighthBlockItem>> {
        private static final String TAG = "Block List Connection";
        @Override
        protected ArrayList<EighthBlockItem> doInBackground(String... urls) {

            HttpsURLConnection urlConnection;
            ArrayList<EighthBlockItem> response = null;
            try {
                urlConnection = (HttpsURLConnection) new URL(urls[0]).openConnection();
                // Add cookies to header
                for(Cookie cookie : mCookies) {
                    urlConnection.setRequestProperty("Cookie", cookie.getName() + "=" + cookie.getValue());
                }
                // Begin connection
                urlConnection.connect();
                // Parse xml from server
                response = EighthBlockXmlParser.parse(urlConnection.getInputStream(), getApplicationContext());
                // Close connection
                urlConnection.disconnect();
            } catch (XmlPullParserException e) {
                Log.e(TAG, "XML Error.", e);
            } catch (Exception e) {
                Log.e(TAG, "Connection Error.", e);
            }
            return response;
        }

        @Override
        protected void onPostExecute(ArrayList<EighthBlockItem> result) {


            super.onPostExecute(result);

        }
    }
    private class SignupRequest extends AsyncTask<String, Void, Boolean> {
        private static final String TAG = "Signup Connection";
        private final String AID;
        private final String BID;

        public SignupRequest(int AID, int BID) {
            this.AID = Integer.toString(AID);
            this.BID = Integer.toString(BID);
        }

        @Override
        protected Boolean doInBackground(String... urls) {
            final DefaultHttpClient client = new DefaultHttpClient();
            boolean result = false;
            try {
                // Setup client
                client.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.RFC_2109);
                HttpPost post = new HttpPost(new URI(urls[0]));
                // Add cookies
                for(Cookie cookie : mCookies) {
                    post.setHeader("Cookie", cookie.getName() + "=" + cookie.getValue());
                }
                // Add parameters
                final List<NameValuePair> data = new ArrayList<>(2);
                data.add(new BasicNameValuePair("aid", AID));
                data.add(new BasicNameValuePair("bid", BID));
                post.setEntity(new UrlEncodedFormEntity(data));

                // Send request
                HttpResponse response = client.execute(post);

                // Parse response
                result = EighthActivityXmlParser.parseSuccess(response.getEntity().getContent());
            } catch (XmlPullParserException e) {
                Log.e(TAG, "XML error.", e);
            } catch (URISyntaxException e) {
                Log.e(TAG, "URL -> URI error");
            } catch (IOException e) {
                Log.e(TAG, "Connection error.", e);
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            mTasks.remove(this);
            if(result) {
                Log.d(TAG,"success");
                //postSubmit(Response.SUCCESS);
            } else {
                Log.d(TAG,"fail");
                //postSubmit(Response.FAIL);
            }
        }
    }
}
