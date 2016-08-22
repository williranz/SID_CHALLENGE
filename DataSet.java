package com.willyranz.sid_challenge;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created William on 8/21/2016.
 */
public class DataSet extends ListActivity
{
    Button B;
    private static String url = "https://raw.githubusercontent.com/onaio/ona-tech/master/data/water_points.json";

    private static final String WF = "water_functioning";
    private static final String CV = "communities_villages";
    private static final String WPC = "water_point_condition";
    private static final String WST = "water_source_type";

    ArrayList<HashMap<String, String>> jsonlist = new ArrayList<HashMap<String, String>>();

    ListView lv ;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dataset_main);
        B = (Button)findViewById(R.id.button);
        B.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parseJson();
            }
        });

        parseJson();
    }

    private class ProgressTask extends AsyncTask<String, Void, Boolean>
    {
        private ProgressDialog dialog;
        private ListActivity activity;
        private Context context;

        public ProgressTask(ListActivity activity) {
            this.activity = activity;
            context = activity;
            dialog = new ProgressDialog(context);
        }

        protected void onPreExecute()
        {
            this.dialog.setMessage("Fetching Data");
            this.dialog.show();
        }

        @Override
        protected void onPostExecute(final Boolean success)
        {
            if (dialog.isShowing())
            {
                dialog.dismiss();
                B.setEnabled(false);
            }

            ListAdapter adapter = new SimpleAdapter(context, jsonlist, R.layout.list_item, new String[]
                    {WF, CV, WPC, WST}, new int[]{R.id.wF, R.id.cV, R.id.wPC, R.id.wST});

            setListAdapter(adapter);
            lv = getListView();
        }

        protected Boolean doInBackground(final String... args)
        {
            JSONParser jParser = new JSONParser();
            JSONArray json = jParser.getJSONFromUrl(url);

            for (int i = 0; i < json.length(); i++)
            {
                try
                {
                    JSONObject c = json.getJSONObject(i);
                    String wf = c.getString(WF);
                    String cv = c.getString(CV);
                    String wpc = c.getString(WPC);
                    String wst = c.getString(WST);
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put(WF, "water_functioning : " + wf);
                    map.put(CV, "communities_villages : " + cv);
                    map.put(WPC, "water_point_condition : " + wpc);
                    map.put(WST, "water_source_type : " + wst);

                    jsonlist.add(map);
                }

                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem m = menu.findItem(R.id.dtset);
        m.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.exit) {
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory(Intent.CATEGORY_HOME);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(homeIntent);
        }
        else
        {
            Intent i = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    public void parseJson()
    {
        ConnectivityManager connec = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connec != null && ((connec.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) || (connec.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED)))
        {
            new ProgressTask(DataSet.this).execute();
            B.setEnabled(false);
        }
        else if (connec != null && ((connec.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.DISCONNECTED) || (connec.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.DISCONNECTED )))
        {
            Toast.makeText(getApplicationContext(), "Internet connected needed!", Toast.LENGTH_SHORT).show();
            B.setEnabled(true);
        }
    }
}
