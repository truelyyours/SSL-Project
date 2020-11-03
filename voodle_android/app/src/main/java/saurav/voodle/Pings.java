package saurav.voodle;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class Pings extends AppCompatActivity {
    private ActionBarDrawerToggle abdt = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pings);
        final Intent intent = getIntent();

        Toolbar myToolbar = findViewById(R.id.ping_toolbar);
        DrawerLayout dl = findViewById(R.id.ping_linear);
        abdt = new ActionBarDrawerToggle(this, dl, myToolbar, R.string.open, R.string.close);
        abdt.setDrawerIndicatorEnabled(true);

        dl.addDrawerListener(abdt);
        abdt.syncState();

        NavigationView navigationView = findViewById(R.id.ping_nav_view);
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
                    String pref_file = "saurav.voodle.login";
                    SharedPreferences sp = getSharedPreferences(pref_file, MODE_PRIVATE);
                    sp.edit().clear().apply();
                    Intent inte = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(inte);
                    finish();
                }
//                if (id == R.id.my_pings) {
//                    //#TODO: pass the relevent fields to the pings page.
//                    Intent it = new Intent(getApplicationContext(),Pings.class);
//                    it.putExtra("ldap",intent.getStringExtra("ldap"));
//                    it.putExtra("ses_id",intent.getStringExtra("ses_id"));
//                    startActivity(it);
//                }
                if (id == R.id.my_feedback) {
                    //#TODO: pass the received values as it is here or find some alternate way that server the same purpose.
                    Intent it1 = new Intent(getApplicationContext(),forEachSess.class);
                    it1.putExtra("ldap",intent.getStringExtra("ldap"));
                    it1.putExtra("ses_id",intent.getStringExtra("ses_id"));
                    it1.putExtra("identifier",intent.getStringExtra("identifier"));
                    it1.putExtra("total",intent.getStringExtra("total"));
                    it1.putExtra("name",intent.getStringExtra("name"));
                    startActivity(it1);
                }
                return true;
            }
        });
        setSupportActionBar(myToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ////////////////////////////////////////////////////////////////////////

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return abdt.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }
}
