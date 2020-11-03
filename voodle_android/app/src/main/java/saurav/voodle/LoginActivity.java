package saurav.voodle;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.AsyncTask;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mLdapView;
    private EditText mPasswordView;
    private View mProgressView;
    private SharedPreferences sp;


    void showProgress(final boolean show) {
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        findViewById(R.id.sign_in_button).setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form.
        mLdapView = findViewById(R.id.ldap);
        mPasswordView = findViewById(R.id.password);
        mProgressView = findViewById(R.id.login_progress);

        String pref_file = "saurav.voodle.login";
        sp = getSharedPreferences(pref_file, MODE_PRIVATE);
        String temp_1 = sp.getString("ldap", "");
        String temp_2 = sp.getString("pass", "");

        if (!temp_1.equals("") && !temp_2.equals("")) {
            showProgress(true);
            new UserLoginTask(temp_1, temp_2).execute(((CheckBox) findViewById(R.id.remember_me)).isChecked());
        }


        mLdapView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_NEXT) {
                    mPasswordView.requestFocus();
                    return true;
                }
                return false;
            }
        });
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_GO) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button SignInButton = findViewById(R.id.sign_in_button);
        SignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }

    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mLdapView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String ldap_val = mLdapView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }
        else password = convertToMd5(password);

        // Check for a ldap.
        if (TextUtils.isEmpty(ldap_val)) {
            mLdapView.setError(getString(R.string.error_field_required));
            focusView = mLdapView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        }
        else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(ldap_val, password);
            mAuthTask.execute(((CheckBox) findViewById(R.id.remember_me)).isChecked());
        }
    }

    // Shows the progress UI and hides the login button

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    String details;
    public class UserLoginTask extends AsyncTask<Boolean, Void, String> {

        private final String ldap;
        private final String pass;
        private boolean authenticated;

        UserLoginTask(String ldap, String pass) {
            this.ldap = ldap;
            this.pass = pass;
            authenticated = false;
        }
        @Override
        protected String doInBackground(final Boolean... params) {
            // TODO: 192.168.0.101 - Change web address to permanent
            try {
                JSONObject response = new JSONObject(server_connect.connect("ldap", ldap, "password", pass));
                try {
                    return response.getString("message");
                }
                catch (JSONException e) {
                    authenticated = true;
                    Boolean checked = params[0];
                    // Account exists, return true if the password matches.
                    String pref_file = "saurav.voodle.login";
                    sp = getSharedPreferences(pref_file, MODE_PRIVATE);
                    if (checked) {
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("ldap", ldap);
                        editor.putString("pass", pass);
                        editor.apply();
                    } else {
                        sp.edit().clear().apply();
                    }
                    return response.toString();
                }
            }
            catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return  "Cannot connect to Server";
        }

        @Override
        protected void onPostExecute(final String jsonObject) {
            mAuthTask = null;
            showProgress(false);
            if (authenticated) {
                ((EditText) findViewById(R.id.ldap)).getText().clear();
                ((EditText) findViewById(R.id.password)).getText().clear();
                Intent intent = new Intent(getApplicationContext(), SelectAssignment.class);
                intent.putExtra("ldap", ldap);
                intent.putExtra("pass", pass);
                intent.putExtra("data", jsonObject);
                startActivity(intent);
                finish();
            }

            else {
                if (jsonObject.equals("Invalid Credentials")) {
                    mPasswordView.setError(jsonObject);
                    mPasswordView.requestFocus();
                }
                else if (jsonObject.equals("LDAP must be integer")) {
                    mLdapView.setError(jsonObject);
                    mLdapView.requestFocus();
                }
                else {
                    Toast.makeText(getApplicationContext(), jsonObject, Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
            Toast.makeText(getApplicationContext(), "cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    String convertToMd5(String val) {
        MessageDigest mdEnc;
        String md5 = null;
        try {
            mdEnc = MessageDigest.getInstance("MD5");
            mdEnc.reset();
            mdEnc.update(val.getBytes("utf-8"));
            md5 = new BigInteger(1, mdEnc.digest()).toString(16);
            while (md5.length() < 32) {
                md5 = "0" + md5;
            }
        }
        catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            Toast.makeText(getApplicationContext(), "Error: Password cannot be converted to MD5 due to its unavailability. You are restricted from login for security reasons", Toast.LENGTH_LONG).show();
        }
        return md5;
    }
}