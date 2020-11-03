package saurav.voodle;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.MatrixCursor;
import android.os.AsyncTask;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


// #TODO: send the recorded responses to the server from scq,mcq,sat.Done but recheck it before submission.
// #TODO: Complete the final screen
// #TODO: Complete the back_button.
// #TODO: Add a ping link in the navigation bar. When clicked gets directe to Ping_activity.
// # TODO: complete the ping_activity.
// #TODO: Improvise the webview on horizontal as well as vertical overflow in for_each_sess.xml

public class ForQuestions extends AppCompatActivity {
    Intent intent;
    int on;
    int last;
    boolean commented = false;
    String ses_id;
    String Ldap;
    int active = 1;
    String user_comment = "";
    String user_ping = "";
    ListView LV;
    JSONArray response = new JSONArray();
    int ok = -1;
    JSONArray com = new JSONArray();
    ArrayList<Integer> attempts;

    private void display_commments() {
        String[] columnNames = new String[]{"_id","ldap", "comment"};
        MatrixCursor contents = new MatrixCursor(columnNames, 30);

        for (int i = 0; i < com.length(); i++) {
            String ldp = "";
            String comt = "";
            String timestamp = "";
            try {
                ldp = new JSONObject(com.get(i).toString()).getString("ldap");
                timestamp = new JSONObject(com.get(i).toString()).getString("time");
                ldp = ldp + " @ " + timestamp;
                comt = new JSONObject(com.get(i).toString()).getString("comment");
            } catch (JSONException et) {
                et.printStackTrace();
            }
            String[] something = new String[3];
            something[0] = Integer.toString(i);
            something[1] = ldp;
            something[2] = comt;
            contents.addRow(something);
        }
        int[] resourceIds = new int[]{R.id.nav_ldap,R.id.the_ldap, R.id.the_comment};
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(getApplicationContext(), R.layout.comments, contents, columnNames, resourceIds, 0);
        LV.setAdapter(adapter);
        ((BaseAdapter) LV.getAdapter()).notifyDataSetChanged();
    }

    private void display(String ps, String type, final String comments, String options) {
        if (on <= last) {
            new Active().execute();
            TextView que;
            final CheckBox accepted;
            try {
                com = new JSONArray(comments);
            }
            catch (JSONException expt) {expt.printStackTrace(); }

            switch (type) {
                case "scq": {
                    setContentView(R.layout.scq);
                    LV = findViewById(R.id.comments_view_scq);
                    accepted = findViewById(R.id.scq_checked);

                    final RadioButton btn1 = findViewById(R.id.opt1);
                    final RadioButton btn2 = findViewById(R.id.opt2);
                    final RadioButton btn3 = findViewById(R.id.opt3);
                    final RadioButton btn4 = findViewById(R.id.opt4);
                    que = findViewById(R.id.que_scq);
                    que.setText(ps);

                    try {
                        btn1.setText(new JSONArray(intent.getStringExtra("options")).get(0).toString());
                        btn2.setText(new JSONArray(intent.getStringExtra("options")).get(1).toString());
                        btn3.setText(new JSONArray(intent.getStringExtra("options")).get(2).toString());
                        btn4.setText(new JSONArray(intent.getStringExtra("options")).get(3).toString());
                    } catch (JSONException exp) {
                        exp.printStackTrace();
                    }

                    display_commments();
                    final EditText edt = findViewById(R.id.comment_in_scq);


                    ImageButton com_btn = findViewById(R.id.send_scq);
                    final JSONArray finalCom = com;
                    com_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            user_comment = edt.getText().toString().trim();
                            edt.getText().clear();
                            LinearLayout ll;
                            ll = findViewById(R.id.edit_ll_scq);
//                            ll.isFocusableInTouchMode();
                            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(ll.getWindowToken(),0);
                            if (user_comment.length() != 0) {
                                new PostComment().execute();
                                if (commented) {
                                    try {
                                        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm",Locale.US);
                                        Date date = new Date();
                                        String temp = formatter.format(date);
                                        finalCom.put(new JSONObject("{\"ldap\":\"" + Ldap + " @ " + temp + "\",\"comment\":\"" + user_comment + "\"}"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        Toast.makeText(getApplicationContext(), "Something wrong with comments", Toast.LENGTH_SHORT).show();                                    }
//                                        edt.setText("");
                                        commented = false;
                                }
                            }
                        }
                    });
                    Log.i("FINAL_COM",finalCom.toString());
                    com =  new JSONArray();
                    com = finalCom;
                    Log.i("COM_",com.toString());
                    display_commments();

                    //PING----------------------------------------------------
                    ImageButton ping_btn = findViewById(R.id.send_scq_ping);
                    final EditText png = findViewById(R.id.ping_in_scq);
                    ping_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            user_ping = png.getText().toString().trim();
                            png.getText().clear();
                            LinearLayout ll;
                            ll = findViewById(R.id.edit_ll_scq_ping);
                            ll.isFocusableInTouchMode();
                            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(ll.getWindowToken(),0);
                            if (user_ping.length() != 0) {
                                new PostPing().execute();
                            }
                        }
                    });

                    //---------------------------------------------------------------
                    accepted.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (accepted.isChecked()) {
                                ok = 1;
                            }
                            else {
                                ok = -1;
                            }
                            new Attempted().execute();
                        }
                    });

                    ImageButton btn = findViewById(R.id.scq_next);
                    Button fin = findViewById(R.id.finish_scq);
                    if (on == last) {
                        btn.setVisibility(View.GONE);
                        fin.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (accepted.isChecked()) {
                                    RadioGroup rg = findViewById(R.id.RGroup);
                                    if (rg.getCheckedRadioButtonId() == -1) {
                                        Toast.makeText(getApplicationContext(), "Please select an option", Toast.LENGTH_SHORT).show();
                                    } else {
                                        int id = rg.getCheckedRadioButtonId();
                                        RadioButton temp_but = rg.findViewById(id);
                                        response.put(temp_but.getText());
                                        new SendResponse().execute();

                                        Intent it1 = new Intent(getApplicationContext(), CompletionScreen.class);
                                        startActivity(it1);
                                        finish();
                                    }
                                }
                                else {
                                    Intent it1 = new Intent(getApplicationContext(), CompletionScreen.class);
                                    startActivity(it1);
                                    finish();
                                }
                            }
                        });
                    } else {
                        fin.setVisibility(View.GONE);
                        btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (accepted.isChecked()) {
                                    RadioGroup rg = findViewById(R.id.RGroup);
                                    if (rg.getCheckedRadioButtonId() == -1) {
                                        Toast.makeText(getApplicationContext(), "Please select an option", Toast.LENGTH_SHORT).show();
                                    } else {
                                        int id = rg.getCheckedRadioButtonId();
                                        RadioButton temp_but = rg.findViewById(id);
                                        response.put(temp_but.getText());
                                        new SendResponse().execute();
                                        on = on + 1;
                                        new UserLoginTask().execute();
                                    }
                                }
                                else {
                                    on = on + 1;
                                    new UserLoginTask().execute();
                                }
                            }
                        });
                    }

                    break;
                }

                case "mcq": {
                    setContentView(R.layout.mcq);
                    final CheckBox cb1 = findViewById(R.id.box1);
                    final CheckBox cb2 = findViewById(R.id.box2);
                    final CheckBox cb3 = findViewById(R.id.box3);
                    final CheckBox cb4 = findViewById(R.id.box4);
                    que = findViewById(R.id.que_mcq);
                    que.setText(ps);
                    accepted = findViewById(R.id.mcq_checked);

                    LV = findViewById(R.id.comments_view_mcq);
                    display_commments();

                    try {
                        cb1.setText(new JSONArray(intent.getStringExtra("options")).get(0).toString());
                        cb2.setText(new JSONArray(intent.getStringExtra("options")).get(1).toString());
                        cb3.setText(new JSONArray(intent.getStringExtra("options")).get(2).toString());
                        cb4.setText(new JSONArray(intent.getStringExtra("options")).get(3).toString());
                    } catch (JSONException exp) {
                        exp.printStackTrace();
                    }

                    ImageButton com_btn = findViewById(R.id.send_mcq);
                    final EditText edt = findViewById(R.id.comment_in_mcq);
                    final JSONArray finalCom = com;
                    com_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            user_comment = edt.getText().toString().trim();
                            edt.getText().clear();
                            LinearLayout ll;
                            ll = findViewById(R.id.edit_ll_mcq);
                            ll.isFocusableInTouchMode();
                            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(ll.getWindowToken(),0);
                            if (user_comment.length() != 0) {
                                new PostComment().execute();
                                if (commented) {
                                    try {
                                        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm",Locale.US);
                                        Date date = new Date();
                                        String temp = formatter.format(date);
                                        finalCom.put(new JSONObject("{\"ldap\":\"" + Ldap + " @ " + temp+ "\",\"comment\":\"" + user_comment + "\"}"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        Toast.makeText(getApplicationContext(), "Something wrong with comments", Toast.LENGTH_SHORT).show();                                    }
                                    edt.setText("");
                                    edt.clearFocus();
                                        commented = false;
                                }
                            }
                        }
                    });
                    com = finalCom;
                    Log.i("COMMENTS_",com.toString());
                    display_commments();

                    //PING===============================
                    ImageButton ping_btn = findViewById(R.id.send_mcq_ping);
                    final EditText png = findViewById(R.id.ping_in_mcq);
                    Log.i("PING_ME",user_ping);
                    ping_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            user_ping = png.getText().toString().trim();
                            png.getText().clear();
                            LinearLayout ll;
                            ll = findViewById(R.id.edit_ll_mcq_ping);
                            ll.isFocusableInTouchMode();
                            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(ll.getWindowToken(),0);
                            if (user_ping.length() != 0) {
                                new PostPing().execute();
                            }
                        }
                    });
                    //------------------------------------------

                    accepted.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (accepted.isChecked()) {
                                ok = 1;
                            }
                            else {
                                ok = -1;
                            }
                            new Attempted().execute();
                        }
                    });

                    ImageButton btn = findViewById(R.id.mcq_next);
                    Button fin = findViewById(R.id.finish_mcq);
                    if (on == last) {
                        btn.setVisibility(View.GONE);
                        fin.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (accepted.isChecked()) {
                                    boolean ok1=false,ok2=false,ok3=false,ok4=false;
                                    if (cb1.isChecked()) {
                                        response.put(cb1.getText());
                                        ok1 = true;
                                    }
                                    else if (cb2.isChecked()) {
                                        response.put(cb1.getText());
                                        ok2 = true;
                                    }
                                    else if (cb3.isChecked()) {
                                        response.put(cb1.getText());
                                        ok3 = true;
                                    }
                                    else if (cb4.isChecked()) {
                                        response.put(cb1.getText());
                                        ok4 = true;
                                    }
                                    if (!ok1 && !ok2 && !ok3 && !ok4) {
                                        Toast.makeText(getApplicationContext(), "Please select an option", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        new SendResponse().execute();
                                        Intent it1 = new Intent(getApplicationContext(), CompletionScreen.class);
                                        startActivity(it1);
                                        finish();
                                    }
                                }
                                else {
                                    Intent it1 = new Intent(getApplicationContext(), CompletionScreen.class);
                                    startActivity(it1);
                                    finish();
                                }
                            }
                        });
                    } else {
                        fin.setVisibility(View.GONE);
                        btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (accepted.isChecked()) {
                                    boolean ok1=false,ok2=false,ok3=false,ok4=false;
                                    if (cb1.isChecked()) {
                                        response.put(cb1.getText());
                                        ok1 = true;
                                    }
                                    else if (cb2.isChecked()) {
                                        response.put(cb1.getText());
                                        ok2 = true;
                                    }
                                    else if (cb3.isChecked()) {
                                        response.put(cb1.getText());
                                        ok3 = true;
                                    }
                                    else if (cb4.isChecked()) {
                                        response.put(cb1.getText());
                                        ok4 = true;
                                    }
                                    if (!ok1 && !ok2 && !ok3 && !ok4) {
                                        Toast.makeText(getApplicationContext(), "Please select an option", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        new SendResponse().execute();
                                        on = on + 1;
                                        new UserLoginTask().execute();
                                    }
                                }
                                else {
                                    on = on + 1;
                                    new UserLoginTask().execute();
                                }
                            }
                        });
                    }
                    break;
                }
                default: {
                    setContentView(R.layout.sat);
                    que = findViewById(R.id.que_sat);
                    que.setText(ps);
                    accepted = findViewById(R.id.sat_checked);
                    final EditText answer = findViewById(R.id.short_ans);

                    LV = findViewById(R.id.comments_view_sat);
                    display_commments();

                    accepted.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (accepted.isChecked()) {
                                ok = 1;
                            }
                            else {
                                ok = -1;
                            }
                            new Attempted().execute();
                        }
                    });

                    ImageButton com_btn = findViewById(R.id.send_sat);
                    final EditText edt = findViewById(R.id.comment_in_sat);
                    final JSONArray finalCom = com;
                    com_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            user_comment = edt.getText().toString().trim();
                            LinearLayout ll;
                            ll = findViewById(R.id.edit_ll_sat);
                            ll.isFocusableInTouchMode();
                            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(ll.getWindowToken(),0);
                            if (user_comment.length() != 0) {
                                new PostComment().execute();
                                if (commented) {
                                    try {
                                        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm",Locale.US);
                                        Date date = new Date();
                                        String temp = formatter.format(date);
                                        finalCom.put(new JSONObject("{\"ldap\":\"" + Ldap + " @ " + temp + "\",\"comment\":\"" + user_comment + "\"}"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        Toast.makeText(getApplicationContext(), "Something wrong with comments", Toast.LENGTH_SHORT).show();                                    }
                                    edt.setText("");
                                    edt.clearFocus();
                                        commented = false;
                                }
                            }
                        }
                    });
                    com = finalCom;
                    Log.i("COMMENTS_",com.toString());
                    display_commments();

                    //PING_------------------------------------
                    ImageButton ping_btn = findViewById(R.id.send_sat_ping);
                    final EditText png = findViewById(R.id.ping_in_sat);
                    ping_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            user_ping = png.getText().toString().trim();
                            png.getText().clear();
                            LinearLayout ll;
                            ll = findViewById(R.id.edit_ll_sat_ping);
                            ll.isFocusableInTouchMode();
                            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(ll.getWindowToken(),0);
                            if (user_ping.length() != 0) {
                                new PostPing().execute();
                            }
                        }
                    });

                    //-----------------------------------------------------

                    ImageButton btn = findViewById(R.id.sat_next);
                    Button fin = findViewById(R.id.finish_sat);
                    if (on == last) {
                        btn.setVisibility(View.GONE);
                        fin.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (accepted.isChecked()) {
                                    String sat_ans = answer.getText().toString().trim();
                                    if (sat_ans.length() != 0) {
                                        Intent it1 = new Intent(getApplicationContext(), CompletionScreen.class);
                                        startActivity(it1);
                                        finish();
                                    }
                                    else {
                                        Toast.makeText(getApplicationContext(),"Please enter your answer",Toast.LENGTH_SHORT).show();
                                    }
                                }
                                else {
                                    Intent it1 = new Intent(getApplicationContext(), CompletionScreen.class);
                                    startActivity(it1);
                                    finish();
                                }
                            }
                        });
                    } else {
                        fin.setVisibility(View.GONE);
                        btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (accepted.isChecked()) {
                                    String sat_ans = answer.getText().toString().trim();
                                    if (sat_ans.length() != 0) {
                                        on = on + 1;
                                        new UserLoginTask().execute();
                                    }
                                    else {
                                        Toast.makeText(getApplicationContext(),"Please enter your answer",Toast.LENGTH_SHORT).show();
                                    }
                                }
                                else {
                                    on = on + 1;
                                    new UserLoginTask().execute();
                                }
                            }
                        });
                    }
                    break;
                }
            }
        } else {
            active = -1;
            new Active().execute();
            Intent it1 = new Intent(getApplicationContext(),CompletionScreen.class);
            startActivity(it1);
            finish();
        }
    }

    class Attempted extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            try {
                String que = "[" + ses_id + "," + on + "]";
//                String ans = "{\"ldap\":\"" + Ldap + "\"," + "\"comment\":\"" + ok + "\"}";
                return server_connect.connect("attempted", Integer.toString(ok), "question_data", que);
            } catch (ConnectException e) {
                return "";
            }
        }

        @Override
        protected void onPostExecute(final String jsonObject) {
            try {
                String err = new JSONObject(jsonObject).getString("message");
                Toast.makeText(getApplicationContext(), err, Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {Log.i("ATTEMPTED_","DONE_OK"); }
        }
    }

    class SendResponse extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            try {
                String who = "[" + ses_id + "," + on + "]";
                String ans = "{\"ldap\":\"" + Ldap + "\"," + "\"comment\":\"" + response + "\"}";
                return server_connect.connect("who", who, "answer", ans);
            } catch (ConnectException e) {
                return "";
            }
        }

        @Override
        protected void onPostExecute(final String jsonObject) {
            try {
                String err = new JSONObject(jsonObject).getString("message");
                Toast.makeText(getApplicationContext(), err, Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {e.printStackTrace(); }
        }
    }

    class UserLoginTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            try {
                String que = "[" + ses_id + "," + on + "]";
                return server_connect.connect("ldap", Ldap, "question_data", que);
            } catch (ConnectException e) {
                return "";
            }
        }

        @Override
        protected void onPostExecute(final String jsonObject) {
            try {
                String err = new JSONObject(jsonObject).getString("message");
                Toast.makeText(getApplicationContext(), err, Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                try {
                    String q = new JSONObject(jsonObject).getString("problem");
                    JSONArray comments = new JSONArray(new JSONObject(jsonObject).getString("comments"));
                    String type = new JSONObject(jsonObject).getString("type");
                    JSONArray options = new JSONArray(new JSONObject(jsonObject).getString("options"));

                    display(q, type, comments.toString(), options.toString());
                } catch (JSONException ei) {
                    ei.printStackTrace();
                }
            }
        }
    }

    //Posts comments to the server and update the displaying comments.
    class PostComment extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            try {
                String who = "[" + ses_id + "," + on + "]";
                String que = "{\"ldap\":\"" + Ldap + "\"," + "\"comment\":\"" + user_comment + "\"}";
                return server_connect.connect("who", who, "comment", que);
            } catch (ConnectException e) {
                return "";
            }
        }

        @Override
        protected void onPostExecute(final String jsonObject) {
        try {
        String err = new JSONObject(jsonObject).getString("message");
        Toast.makeText(getApplicationContext(), err, Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
        commented = true;
        }
        }
    }
    //PING POST======================
    class PostPing extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            try {
                String who = "[" + ses_id + "," + on + "]";
                String que = "{\"ldap\":\"" + Ldap + "\"," + "\"comment\":\"" + user_ping + "\"}";
                return server_connect.connect("who", who, "ping", que);
            } catch (ConnectException e) {
                return "";
            }
        }

        @Override
        protected void onPostExecute(final String jsonObject) {
            try {
                Log.i("response", jsonObject);
                String err = new JSONObject(jsonObject).getString("message");
                Toast.makeText(getApplicationContext(), err, Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                commented = true;
            }
        }
    }

    class Active extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            try {
                Log.i("ACTIVE_",Integer.toString(active));
                String que = "[" + ses_id + "," + Ldap + "]";
                return server_connect.connect("active", Integer.toString(active), "question_data", que);
            } catch (ConnectException e) {
                Log.i("ACTIVE-","An exception");
                return "";
            }
        }

        @Override
        protected void onPostExecute (String params) {
            Log.i("ACTIVATION_",params);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_for_questions);
        Intent inte = getIntent();
        intent = inte;
        on = Integer.parseInt(inte.getStringExtra("num"));
        last = Integer.parseInt(inte.getStringExtra("total"));
        ses_id = inte.getStringExtra("ses_id");
        Ldap = inte.getStringExtra("ldap");
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST);

        display(intent.getStringExtra("problem"), intent.getStringExtra("type"), intent.getStringExtra("comments"), intent.getStringExtra("options"));
    }

    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle("EXIT");
        builder.setMessage("You will exit the feedback. Are you sure?");
        builder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
//                            ForQuestions.this.wait(1);
                            ForQuestions.super.onBackPressed();
                        }
                        catch (Exception e) { e.printStackTrace(); }
                    }
                });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    @Override
    protected void onPause() {
//        Toast.makeText(getApplicationContext(),"will call active with value -1",Toast.LENGTH_SHORT).show();
        active = -1;
        new Active().execute();
        super.onPause();
    }
}
