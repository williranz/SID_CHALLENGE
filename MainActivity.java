package com.willyranz.sid_challenge;

import android.app.Activity;
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
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by William on 8/19/2016.
 */
public class MainActivity extends Activity {

    int count=0;
    int functional=0;
    int totalbroken=0;
    float percentage;
    DecimalFormat df = new DecimalFormat("#.##");
    Button B;
    TextView TextCount;
    TextView TextFunctional;
    TextView TextDataCom;
    TextView TextRanking;
    TextView TextBroken;
    Ranking rank;

    private static String url = "https://raw.githubusercontent.com/onaio/ona-tech/master/data/water_points.json";

    private static final String WF = "water_functioning";
    private static final String CV = "communities_villages";

    ArrayList<HashMap<String, String>> jsonlist = new ArrayList<HashMap<String, String>>();
    List<String> list = new ArrayList<String>();
    List<String> list2 = new ArrayList<String>();
    List<Ranking> ARrank = new ArrayList<Ranking>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextCount = (TextView)findViewById(R.id.textCount);
        TextFunctional = (TextView)findViewById(R.id.textFunctional);
        TextDataCom = (TextView)findViewById(R.id.textDataCom);
        TextRanking = (TextView)findViewById(R.id.textRanking);
        TextBroken = (TextView)findViewById(R.id.textBroken);
        B = (Button)findViewById(R.id.button2);
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
        private MainActivity activity;
        private Context context;

        public ProgressTask(MainActivity activity) {
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
            }

            StringBuilder builder = new StringBuilder();
            StringBuilder builder2 = new StringBuilder();

            Set<String> unique = new HashSet<String>(list);
            for (String key : unique)
            {
                builder.append(key + ": " + Collections.frequency(list, key) + "\n");
            }

            Set<String> unique2 = new HashSet<String>(list2);
            for (String key : unique2)
            {
                rank = new Ranking(key , Collections.frequency(list2, key));
                ARrank.add(rank);
                totalbroken = totalbroken + Collections.frequency(list2, key);
            }

            Collections.sort(ARrank, new Comparator<Ranking>() {
                @Override
                public int compare(Ranking r1, Ranking r2) {
                    return r2.Total - r1.Total;
                }
            });

            for (int i = 0; i < ARrank.size(); i++)
            {
                percentage = (float)(ARrank.get(i).getTotal() * 100) / totalbroken;

                builder2.append("No." + (i+1) + " " + ARrank.get(i).getName() + " : " + ARrank.get(i).getTotal() + "   (" + df.format(percentage) + "%)" + "\n");
            }

            TextCount.setText("Total Water Points : " + count);
            TextFunctional.setText("Total Functioning Water Points : " + functional);
            TextDataCom.setText(builder.toString());
            TextRanking.setText(builder2.toString());
            TextBroken.setText("Total Broken Water Points : " + totalbroken);
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
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put(WF, "water_functioning : " + wf);
                    map.put(CV, "communities_villages : " + cv);

                    if(wf.equalsIgnoreCase("yes"))
                    {
                        functional++;
                    }
                    else
                    {
                        list2.add(cv);
                    }

                    list.add(cv);
                    jsonlist.add(map);
                }

                catch (JSONException e)
                {
                    e.printStackTrace();
                }
                count++;
            }
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem m = menu.findItem(R.id.main);
        m.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == R.id.exit) {
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory(Intent.CATEGORY_HOME);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(homeIntent);
        }
        else
        {
            Intent i = new Intent(getApplicationContext(),DataSet.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    public class Ranking {
        public String Name;
        public int Total;
        //constructor
        public Ranking(String a, int b)
        {
            Name = a;
            Total = b;
        }

        public String getName( )
        {
            return Name;
        }
        public int getTotal( )
        {
            return Total;
        }
    }

    public void parseJson()
    {
        ConnectivityManager connec = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connec != null && ((connec.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) || (connec.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED)))
        {
            new ProgressTask(MainActivity.this).execute();
            B.setEnabled(false);
        }
        else if (connec != null && ((connec.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.DISCONNECTED) || (connec.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.DISCONNECTED )))
        {
            Toast.makeText(getApplicationContext(), "Internet connected needed!", Toast.LENGTH_SHORT).show();
            B.setEnabled(true);
        }
    }
}
