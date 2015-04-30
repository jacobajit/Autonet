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
import java.util.HashMap;
import java.util.List;
import 	java.text.SimpleDateFormat;
import java.util.concurrent.ExecutionException;

import android.app.AlarmManager;
import android.app.PendingIntent;
import javax.net.ssl.HttpsURLConnection;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.support.v7.widget.Toolbar;


public class AutonetActivity extends ActionBarActivity /*implements AutonetFragment.OnFragmentInteractionListener*/ {
    private Cookie[] mCookies;
    private ArrayList<EighthBlockItem> blockList;
    private ArrayList<AsyncTask> mTasks;
    private SharedPreferences preferences;
    private SharedPreferences.Editor mEditor;
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    private ArrayList<String> savedActivities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        preferences= getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
        mEditor= preferences.edit();


        setContentView(R.layout.activity_autonet);

        Toolbar mActionBarToolbar = (Toolbar) findViewById(R.id.autonet_toolbar);
        setSupportActionBar(mActionBarToolbar);
        getSupportActionBar().setTitle("Autonet");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final Intent intent = getIntent();
        if(intent.getBooleanExtra("newData",false))//checks if an activity preference selection is being sent from SignupActivity
        {
            Log.d("TAG","Updating new data received...");
            Log.d("TAG",intent.getStringExtra("blockName"));
            Log.d("TAG",intent.getIntExtra("AID", -1)+"");

            updateData(intent.getStringExtra("blockName"),intent.getIntExtra("AID",-1));
            String blockActivityName=intent.getStringExtra("blockName")+"name";
            Log.d("TAG",blockActivityName);
            updateData(blockActivityName,intent.getStringExtra("activityName"));

        }
        mCookies = LoginActivity.getCookies(preferences);
        mTasks = new ArrayList<>();


        savedActivities=new ArrayList<String>();
        savedActivities.add("MonB: "+preferences.getString("MonBname","Select an activity."));//first 4 letters must be formatted this way for now
        savedActivities.add("WedA: "+preferences.getString("WedAname","Select an activity."));
        savedActivities.add("WedB: "+preferences.getString("WedBname","Select an activity."));
        savedActivities.add("FriA: "+preferences.getString("FriAname","Select an activity."));
        savedActivities.add("FriB: "+preferences.getString("FriBname","Select an activity."));

        final ListView listview = (ListView) findViewById(R.id.autonet_list);

        final StableArrayAdapter adapter = new StableArrayAdapter(this,
                android.R.layout.simple_list_item_1, savedActivities);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                                            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                //saveSelection(findNextBID(parent.getItemAtPosition(position).toString().substring(0,4)),parent.getItemAtPosition(position).toString().substring(0,4));
                String block=parent.getItemAtPosition(position).toString().substring(0,4);
                int nextBID=findNextBID(block);
                saveSelection(nextBID,block);

            }
        });


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





    }

    public int findNextBID(String blockname)  {
        ArrayList<EighthBlockItem> list= null;
        Log.d("TAG","Looking for"+blockname);
        try {
            list = new BlockListRequest().execute("https://iodine.tjhsst.edu/api/eighth/list_blocks").get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        for(EighthBlockItem item:list)
        {
            SimpleDateFormat simpleDateformat = new SimpleDateFormat("E");
            String day = simpleDateformat.format(item.getDate());
            String block = item.getBlock();

            String a = day + block;
            Log.d("TAG",a);
            if(a.equals(blockname))
            {
                Log.d("TAG",item.getBID()+"");
                return item.getBID();
            }

        }
        Log.d("TAG","Not found.");

        return -1;
    }
    public void updateData(String blockName,int AID)//save preferences for a certain block
    {
        mEditor.putInt(blockName, AID).commit();
        Log.d("TAG","Data updated");

    }
    public void updateData(String blockName,String activity)//save preferences for a certain block
    {
        mEditor.putString(blockName, activity).commit();
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
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                Intent intent = new Intent(this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }
}

