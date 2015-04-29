package com.el1t.iolite;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class AutonetService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "com.el1t.iolite.action.FOO";
    private static final String ACTION_BAZ = "com.el1t.iolite.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.el1t.iolite.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.el1t.iolite.extra.PARAM2";
    private Cookie[] mCookies;
    private ArrayList<EighthBlockItem> blockList;
    private ArrayList<AsyncTask> mTasks;
    private SharedPreferences preferences;
    private SharedPreferences.Editor mEditor;



    public AutonetService() {
        super("AutonetService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Log.d("TAG","Entering service...");
            preferences= getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
            mEditor= preferences.edit();
            mCookies = LoginActivity.getCookies(preferences);
            mTasks = new ArrayList<>();
            new BlockListRequest().execute("https://iodine.tjhsst.edu/api/eighth/list_blocks");
        }
    }


    public void signUp(ArrayList<EighthBlockItem> output)//sign up for blocks with preferred activities
    {

        blockList=output;
        int blockAID;
        SimpleDateFormat simpleDateformat = new SimpleDateFormat("E");

        Log.d("TAG","Standard signup...");
        for (EighthBlockItem item : blockList) {
            Log.d("tag", item.getDate().toString());
            String day = simpleDateformat.format(item.getDate());
            String block = item.getBlock();

            String a = day + block;

            blockAID = preferences.getInt(a, 999);


            if (item.getEighth().getAID() == 999)
            {

                mTasks.add(new SignupRequest(blockAID, item.getBID()).execute("https://iodine.tjhsst.edu/api/eighth/signup_activity"));


            }


        }
        //saveSelection(2954);

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
            signUp(result);
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
