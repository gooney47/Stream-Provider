package com.example.stream_provider;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.example.stream_provider.streaming.StreamingTest;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    public static final int SERVER_PORT = 42312;
    private ArrayList<Triple> userList = new ArrayList<Triple>();
    private String username = null;
    private ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        askForUsername(false);

        userList.add(new Triple(username, Utils.getIPAddress(true), "idle"));

        adapter = new ArrayAdapter<String>(this, R.layout.listitem, getListViewStrings());
        ListView listView = (ListView) findViewById(R.id.mobile_list);
        listView.setAdapter(adapter);

        SendingClient sendingClient = new SendingClient(getApplicationContext());
        ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
        UDPListeningThread udpListeningThread = new UDPListeningThread();
        udpListeningThread.runThread(getApplicationContext(), userList, worker, sendingClient, this);
        Log.d("Stream-Provider", "Started UDP Listening Thread.");


        worker.scheduleAtFixedRate(new HelloThread(sendingClient, username),
                0,  //initial delay
                5, //run every x seconds
                TimeUnit.SECONDS);
        Log.d("Stream-Provider", "Started Hello Thread.");
    }

    public void updateListView() {
        adapter.clear();
        adapter.addAll(getListViewStrings());
        adapter.notifyDataSetChanged();
    }

    private ArrayList<String> getListViewStrings() {
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < userList.size(); i++) {
            list.add(userList.get(i).name + " (" + userList.get(i).ip + ") - " + userList.get(i).status);
        }
        return list;
    }

    private void askForUsername(boolean alwaysRequest) {
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        username = prefs.getString("user_name", null);
        if (alwaysRequest || username == null) {
            EditText input = new EditText(this);
            input.setId(1000);
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(input).setTitle("Enter your username!")
                    .setPositiveButton("Ok",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    EditText theInput = (EditText) ((AlertDialog) dialog)
                                            .findViewById(1000);
                                    String enteredText = theInput.getText()
                                            .toString();
                                    username = new String(enteredText);
                                    if (!enteredText.equals("")) {
                                        SharedPreferences.Editor editor = prefs
                                                .edit();
                                        editor.putString("user_name",
                                                enteredText);
                                        editor.apply();

                                    }
                                }
                            }).create();
            dialog.show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_changeusername) {
            askForUsername(true);
            return true;
        }
        //Start streaming test
        else if (id == R.id.action_teststreaming) {
            Intent intent = new Intent(this, StreamingTest.class);
            startActivity(intent);
            return true;
        }


        return super.onOptionsItemSelected(item);
    }
}
