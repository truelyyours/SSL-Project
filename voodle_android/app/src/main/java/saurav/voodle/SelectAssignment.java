package saurav.voodle;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.MatrixCursor;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class SelectAssignment extends AppCompatActivity {
    private ActionBarDrawerToggle abdt = null;
    private Intent intent = null;
    private String name = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_assignment);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        DrawerLayout dl = findViewById(R.id.linear_select_courses);
        abdt = new ActionBarDrawerToggle(this, dl, myToolbar, R.string.open, R.string.close);
        abdt.setDrawerIndicatorEnabled(true);

        dl.addDrawerListener(abdt);
        abdt.syncState();

        intent = getIntent();
        final String ldap = intent.getStringExtra("ldap");
        final String pass = intent.getStringExtra("pass");
        try {
            JSONObject data = new JSONObject(intent.getStringExtra("data"));
            name = data.getString("name");
            final SwipeRefreshLayout refreshLayout = findViewById(R.id.pull_refresh);
            refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    class UserLoginTask extends AsyncTask<Void, Void, String> {
                        @Override
                        protected String doInBackground(Void... params) {
                            try {
                                return server_connect.connect("ldap", ldap, "password", pass);
                            } catch (ConnectException e) {
                                return "";
                            }
                        }

                        @Override
                        protected void onPostExecute(final String jsonObject) {
                            assign_listView(jsonObject);
                            refreshLayout.setRefreshing(false);
                        }
                    }
                    new UserLoginTask().execute();
                }
            });
            NavigationView navigationView = findViewById(R.id.nav_view);
            View headerView = navigationView.getHeaderView(0);
            TextView navLdap = headerView.findViewById(R.id.nav_ldap);
            TextView navName = headerView.findViewById(R.id.nav_user);
            navLdap.setText(ldap);
            navName.setText(name);
            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    int id = menuItem.getItemId();
                    if (id == R.id.logout) {
                        String pref_file = "saurav.voodle.login";
                        SharedPreferences sp = getSharedPreferences(pref_file, MODE_PRIVATE);
                        sp.edit().clear().apply();
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    if (id == R.id.my_pings) {
                        Intent it1 = new Intent(getApplicationContext(),Pings.class);
                        it1.putExtra("ldap",ldap);
                        it1.putExtra("name",name);

                    }
                    return true;
                }
            });
            setSupportActionBar(myToolbar);

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            assign_listView(data.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return abdt.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    JSONArray keys;
    public void assign_listView(String jsonData) {
        try {
            JSONArray sessions = new JSONObject(jsonData).getJSONArray("sessions");
            keys = new JSONObject(jsonData).getJSONArray("keys");
            ListView courses = findViewById(R.id.courses);
            String[] columnNames = new String[]{"_id", "session", "instructor", "time"};
            MatrixCursor contents = new MatrixCursor(columnNames, 30);
            final ArrayList identifiers = new ArrayList();
            for (int i = 0; i < sessions.length(); i++) {
                String s_name = sessions.getJSONObject(i).getString("session");
                String instructor = sessions.getJSONObject(i).getString("instructor");
                String time = String.valueOf(sessions.getJSONObject(i).getInt("time"));
                int identifier = sessions.getJSONObject(i).getInt("identifier");
                String[] something = new String[4];
                identifiers.add(identifier);
                something[0] = String.valueOf(i + 1);
                something[1] = s_name;
                something[2] = instructor;
                something[3] = time;
                contents.addRow(something);
            }
            final ArrayList<Integer> idfiers = identifiers;
            int[] resourceIds = new int[]{R.id.nav_ldap, R.id.session_name, R.id.instructor, R.id.time};
            SimpleCursorAdapter adapter = new SimpleCursorAdapter(getApplicationContext(), R.layout.sessions, contents, columnNames, resourceIds, 0);
            courses.setAdapter(adapter);
            courses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                    boolean found = false;
                    for (int i = 0;i<keys.length();i++){
                        try {
                            if (keys.getJSONObject(i).getString("key").equals(Integer.toString(idfiers.get(position)))) {
                                found = true;
                                class UserLoginTask extends AsyncTask<Void, Void, String> {
                                    @Override
                                    protected String doInBackground(Void... params) {
                                        try {
                                            return server_connect.connect("ldap", getIntent().getStringExtra("ldap"), "identifier",Integer.toString(idfiers.get(position)));
                                        } catch (ConnectException e) {
                                            return "";
                                        }
                                    }

                                    @Override
                                    protected void onPostExecute(final String received_data) {
                                        Log.i("received_",received_data);
                                        String total ="0";
                                        try {
                                            String err = new JSONObject(received_data).getString("message");
                                            Toast.makeText(getApplicationContext(), err, Toast.LENGTH_SHORT).show();
                                        }
                                        catch (JSONException e) {
                                            try {
                                                total = new JSONObject(received_data).getString("total");
                                            }
                                            catch (JSONException ei) {ei.printStackTrace(); }
                                            Intent info = new Intent(getApplicationContext(), forEachSess.class);
                                            info.putExtra("identifier", received_data);
                                            info.putExtra("ses_id",Integer.toString(idfiers.get(position)));
                                            info.putExtra("ldap", intent.getStringExtra("ldap"));
                                            info.putExtra("total",total);
                                            info.putExtra("name",name);
                                            startActivity(info);}
                                    }
                                }
                                UserLoginTask ult = new UserLoginTask();
                                ult.execute();
                                break;
                            }
                        }
                        catch (JSONException e){ e.printStackTrace(); }
                    }
                    if (! found) {
                        Toast.makeText(getApplicationContext(), "You are not allowed to access this session.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

class server_connect {
    static String connect(String... params) throws ConnectException {
        try {
            URL url = new URL("http://192.168.0.101/student_interface.php");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            boolean convert = params[2].equals("identifier"); //extra may be delted in future.
            String data = params[0] + "=" + URLEncoder.encode(params[1], "UTF-8") + "&" + params[2] + "=" + URLEncoder.encode(params[3], "UTF-8");
            OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
            wr.write(data);
            wr.flush();
            BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
            StringBuilder sb = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }
            String response = sb.toString();
            connection.disconnect();
            return response;
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new ConnectException("Server not Reachable");
    }
}