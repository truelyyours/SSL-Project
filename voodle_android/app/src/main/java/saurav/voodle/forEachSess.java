package saurav.voodle;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;


public class forEachSess extends AppCompatActivity {
    private ActionBarDrawerToggle abdt = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_for_each_sess);
        final Intent intent = getIntent();

        Toolbar myToolbar = findViewById(R.id.sess_toolbar);
        DrawerLayout dl = findViewById(R.id.sess_linear);
        abdt = new ActionBarDrawerToggle(this, dl, myToolbar, R.string.open, R.string.close);
        abdt.setDrawerIndicatorEnabled(true);

        dl.addDrawerListener(abdt);
        abdt.syncState();

        NavigationView navigationView = findViewById(R.id.sess_nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView navLdap = headerView.findViewById(R.id.nav_ldap);
        TextView navName = headerView.findViewById(R.id.nav_user);
        navLdap.setText(intent.getStringExtra("ldap"));
        navName.setText(intent.getStringExtra("name"));
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == R.id.logout) {
                    String pref_file;
                    pref_file = "saurav.voodle.login";
                    SharedPreferences sp = getSharedPreferences(pref_file, MODE_PRIVATE);
                    sp.edit().clear().apply();
                    Intent inte = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(inte);
                    finish();
                }
                if (id == R.id.my_pings) {
                    //#TODO: pass the relevent fields to the pings page.
                    Intent it = new Intent(getApplicationContext(),Pings.class);
                    it.putExtra("ldap",intent.getStringExtra("ldap"));
                    it.putExtra("ses_id",intent.getStringExtra("ses_id"));
                    it.putExtra("identifier",intent.getStringExtra("identifier"));
                    it.putExtra("total",intent.getStringExtra("total"));
                    it.putExtra("name",intent.getStringExtra("name"));
                    startActivity(it);
                }

//                if (id == R.id.my_feedback) {
//                    //#TODO: pass the received values as it is here or find some alternate way that server the same purpose.
//                    Intent it1 = new Intent(getApplicationContext(),ForQuestions.class);
//
//                    startActivity(it1);
//                }
                return true;
            }
        });
        setSupportActionBar(myToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ////////////////////////////////////////////////////////////////////////

        class Active extends AsyncTask<Void, Void, String> {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    String que = "[" + intent.getStringExtra("ses_id") + "," + intent.getStringExtra("ldap") + "]";
                    return server_connect.connect("active", Integer.toString(-1), "question_data", que);
                } catch (ConnectException e) {
                    return "";
                }
            }
        }

        new Active().execute();

        final TextView name = findViewById(R.id.each_name);
        final TextView Time = findViewById(R.id.each_time);
        final TextView inst = findViewById(R.id.each_instr);
        WebView wb = findViewById(R.id.webview);
        JSONObject datas;

        String instructor, time, s_instruction, s_name;
        try {
            datas = new JSONObject(intent.getStringExtra("identifier"));

            instructor = datas.getString("instructor");
            s_instruction = datas.getString("instruction");
            time = datas.getString("time");
            s_name = datas.getString("session");
            wb.loadData(String.format(" %s ",s_instruction),"text/html","utf-8");
            name.setText(s_name);
            Time.setText(time);
            inst.setText(instructor);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        class UserLoginTask extends AsyncTask<Void, Void, String> {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    String que = "[" + intent.getStringExtra("ses_id") + ", 1]";
                    return server_connect.connect("ldap", intent.getStringExtra("ldap"), "question_data", que);
                } catch (ConnectException e) {
                    return "";
                }
            }

            @Override
            protected void onPostExecute(final String jsonObject) {
                try {
                    Log.i("INSIDE POST","OK");
                    String err = new JSONObject(jsonObject).getString("message");
                    Toast.makeText(getApplicationContext(),err,Toast.LENGTH_SHORT).show();
                }
                catch (JSONException e) {
                    Intent it = new Intent(getApplicationContext(),ForQuestions.class);
                    try {
                        String q = new JSONObject(jsonObject).getString("problem");
                        JSONArray comments = new JSONArray(new JSONObject(jsonObject).getString("comments"));
                        String type = new JSONObject(jsonObject).getString("type");
                        JSONArray options = new JSONArray(new JSONObject(jsonObject).getString("options"));
                        String tot = intent.getStringExtra("total");
                        it.putExtra("ses_id",intent.getStringExtra("ses_id"));
                        it.putExtra("num","1");
                        it.putExtra("problem",q);
                        it.putExtra("type",type);
                        it.putExtra("options",options.toString());
                        it.putExtra("comments",comments.toString());
                        it.putExtra("total",tot);
                        it.putExtra("ldap",intent.getStringExtra("ldap"));
                        Log.i("Starting_","Going to ForQuestions");
                        startActivity(it);
                    }
                    catch (JSONException ei) { ei.printStackTrace(); }

                }
            }
        }

        ImageButton btn = findViewById(R.id.next_btn0);
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new UserLoginTask().execute();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return abdt.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }
}
