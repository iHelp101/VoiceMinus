package com.ihelp101.voiceminus;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;

import org.w3c.dom.Text;

import java.util.concurrent.ExecutionException;

public class MainActivity extends ActionBarActivity {
        class AccountAdapter extends ArrayAdapter<Account> {
        AccountAdapter() {
            super(MainActivity.this, android.R.layout.simple_list_item_single_choice);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            CheckedTextView tv = (CheckedTextView) view.findViewById(android.R.id.text1);
            Account account = getItem(position);
            tv.setText(account.name);

            return view;
        }
    }

    int currentActivity = 1;

    Account NULL;

    ListView lv;
    AccountAdapter accountAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentActivity = 1;

        Check();

        Button button = (Button) findViewById(R.id.button);
        Button button2 = (Button) findViewById(R.id.button2);

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                currentActivity = 2;

                setContentView(R.layout.voice);
                accountAdapter = new AccountAdapter();

                lv = (ListView) findViewById(R.id.list);
                lv.setAdapter(accountAdapter = new AccountAdapter());

                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Account account = accountAdapter.getItem(position);

                        final String previousAccount = preferences.getString("account", null);
                        new Thread() {
                            @Override
                            public void run() {
                                super.run();
                                invalidateToken(previousAccount);
                            }
                        }.start();

                        if (account == NULL) {
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.remove("rnr");
                            editor.apply();

                            editor.remove("account");
                            editor.apply();
                            return;
                        }

                        lv.clearChoices();
                        lv.requestLayout();
                        getToken(account, position);
                    }
                });

                String selectedAccount = preferences.getString("account", null);

                NULL = new Account("Disable", "com.google");
                accountAdapter.add(NULL);
                int selected = 0;
                for (Account account : AccountManager.get(MainActivity.this).getAccountsByType("com.google")) {
                    if (account.name.equals(selectedAccount))
                        selected = accountAdapter.getCount();
                    accountAdapter.add(account);
                }

                lv.setItemChecked(selected, true);
                lv.requestLayout();


            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                String authToken = preferences.getString("authToken","");
                try {
                    fetchRnrSe(authToken);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Failed to fetch RNR: " +e,
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    void invalidateToken(String account) {
        if (account == null)
            return;

        try {
            // grab the auth token
            Bundle bundle = AccountManager.get(this).getAuthToken(new Account(account, "com.google"), "grandcentral", true, null, null).getResult();
            String authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
            AccountManager.get(this).invalidateAuthToken("com.google", authToken);
            Log.i(LOGTAG, "Token invalidated.");
        }
        catch (Exception e) {
            Log.e(LOGTAG, "error invalidating token", e);
        }
    }

    private static final String LOGTAG = "VoiceMinus";

    void getToken(final Account account, final int position) {
        AccountManager am = AccountManager.get(this);
        if (am == null)
            return;
        am.getAuthToken(account, "grandcentral", null, this, new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    Bundle bundle = future.getResult();
                    final String authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);

                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("account",account.name);
                    editor.apply();

                    editor.putString("authToken", authToken);
                    editor.apply();


                    lv.setItemChecked(position, true);
                    lv.requestLayout();
                    Log.i(LOGTAG, "Token retrieved.");
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }, new Handler());
    }

    void fetchRnrSe(String authToken) throws ExecutionException, InterruptedException {
        JsonObject userInfo = Ion.with(this)
                .load("https://www.google.com/voice/request/user")
                .setHeader("Authorization", "GoogleLogin auth=" + authToken)
                .asJsonObject()
                .get();

        String rnrse = userInfo.get("r").getAsString();

        if (rnrse != null) {

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("rnr", rnrse);
            editor.apply();

            Toast.makeText(getApplicationContext(), "Fetched Token!",
                    Toast.LENGTH_LONG).show();

            Check();
        } else {
            Toast.makeText(getApplicationContext(), "Fetching Token Failed!",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void Check() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        String account = preferences.getString("account", null);
        String authToken = preferences.getString("authToken", null);
        String rnr = preferences.getString("rnr", null);

        if (account !=null && authToken != null && rnr != null) {
            TextView textView = (TextView) findViewById(R.id.textView);
            textView.setVisibility(View.VISIBLE);
        } else {
            TextView textView = (TextView) findViewById(R.id.textView);
            textView.setVisibility(View.INVISIBLE);
        }
    }

    public void onResume() {
        super.onResume();
        Check();
    }

    @Override
    public void onBackPressed() {
        if (currentActivity == 2) {
            Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(myIntent);
        }
    }
}
