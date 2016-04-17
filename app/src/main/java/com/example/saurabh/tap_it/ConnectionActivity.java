package com.example.saurabh.tap_it;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.saurabh.tap_it.R.id.usernameNav;

public class ConnectionActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    TextView email;
    String token;
    public static Context contextOfApplication;
    NfcAdapter mNfcAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Patch username and email in NavBar
        SharedPreferences sharedPreferences=this.getSharedPreferences("com.example.saurabh.tap_it", Context.MODE_APPEND);
        token = sharedPreferences.getString("token", "");

        try {
            String name = sharedPreferences.getString("username", "");
            //String userEmail = sharedPreferences.getString("email", "");

            Log.i("Login Username in connection activity", name);


            TextView username  = (TextView) findViewById(R.id.usernameNav);
            username.setText(name);
            mNfcAdapter = NfcAdapter.getDefaultAdapter(this);


        }
        catch(NullPointerException e){ Log.i("Error is username variable/NFC Check ConnectionActivity",e.toString());}

        contextOfApplication = getApplicationContext(); //passing the context to user
        putInListView(contextOfApplication);

        //NFC call
        NfcAdapter mAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mAdapter == null) {
            Toast.makeText(this, "Sorry this device does not have NFC", Toast.LENGTH_LONG).show();
            return;
        }

        if (!mAdapter.isEnabled()) {
            Toast.makeText(this, "Please enable NFC via Settings.", Toast.LENGTH_LONG).show();
        }

       // mAdapter.setNdefPushMessageCallback(this, this);
    }


    public void putInListView(final Context context){

        SharedPreferences sharedPreferences=getSharedPreferences("com.example.saurabh.tap_it", Context.MODE_APPEND);
        String token = sharedPreferences.getString("token", "");
        RequestQueue queue = Volley.newRequestQueue(this);

        String url ="http://52.36.159.253/api/v0.1/relationship/list?token="+token;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            SharedPreferences sharedPreferences=getSharedPreferences("com.example.saurabh.tap_it", Context.MODE_APPEND);
                            String token = sharedPreferences.getString("token", "");

                            Log.i("Token value in request try", token);

                            JSONObject responseArray=new JSONObject(response);
                            JSONArray jsonResponse = responseArray.getJSONArray("response");


                            ArrayList<User> users = new ArrayList<User>();
                            String name,company;

                            for(int i=0;i<jsonResponse.length();i++){

                                JSONObject connectionObject=jsonResponse.getJSONObject(i);
                                name=connectionObject.getString("name");

                                Log.i("Name in User array", name);

                                String companyDetail=connectionObject.getString("company");
                                JSONObject companyObject=new JSONObject(companyDetail);
                                company=companyObject.getString("company_name");

                                Log.i("User-Company in Array", company);

                                users.add(new User(name, company));

                                //ArrayList<User> arrayOfUsers = User.getUsers();
                                CustomUsersAdapter adapter = new CustomUsersAdapter(context, users);

                                // Attach the adapter to a ListView
                                ListView listView = (ListView) findViewById(R.id.lvUsers);
                                listView.setAdapter(adapter);

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);

    }

        @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.connection, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {   //QR code

        } else if (id == R.id.nav_gallery) {  //NFC

          


        }  else if (id == R.id.nav_manage) {  //Profile

            Intent call=new Intent(getApplicationContext(),ProfileActivity.class);
            startActivity(call);

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_logout) {
            //First set the default value
            SharedPreferences sharedPreferences=this.getSharedPreferences("com.example.saurabh.tap_it", Context.MODE_PRIVATE);
            sharedPreferences.edit().putString("loggedIn", "no").apply();

            //Goes back to login activity
            Intent call = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(call);
            finish();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
