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
import android.view.ContextMenu;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
    private ListAdapter adapter;
    private boolean streaming = false;
    private SendingClient sendingClient;
    private Triple me;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sendingClient = new SendingClient(getApplicationContext());

        Button streamingButton = (Button) findViewById(R.id.streaming_button);
        streamingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!streaming) {
                    streaming = true;
                    ((Button) findViewById(R.id.streaming_button)).setText("End Streaming");
                    userList.get(0).status = "streaming";
                    sendingClient.sendInform(userList);
                } else {
                    streaming = false;
                    ((Button) findViewById(R.id.streaming_button)).setText("Start Streaming");
                    userList.get(0).status = "idle";
                    sendingClient.sendInform(userList);
                }
                updateListView();
            }
        });

        if (Utils.getIPAddress(true).equals(""))Utils.log("Not able to retrieve IP! Check Wifi connection");

        askForUsername(false);

        userList.add(new Triple(username, Utils.getIPAddress(true), "idle"));
        me = userList.get(0);

        adapter = new ListAdapter(this, R.layout.listitem, userList);
        ListView listView = (ListView) findViewById(R.id.mobile_list);
        listView.setAdapter(adapter);
        registerForContextMenu(listView);

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
        adapter.notifyDataSetChanged();
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.mobile_list) {
            ListView lv = (ListView) v;
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
            Triple t = (Triple) lv.getItemAtPosition(acmi.position);

            if (t != me) {
                if (t.status.equals("streaming") && !me.status.equals(t.name)) {
                    menu.add("Subscribe");
                }
                else if (me.status.equals(t.name)) {
                    menu.add("Unsubscribe");
                }
            }

        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Utils.log(item.getTitle().toString());
        switch (item.getTitle().toString()) {
            case "Subscribe":
                int listPosition = info.position;
                userList.get(0).status = userList.get(listPosition).name;
                sendingClient.sendInform(userList);
                updateListView();
                return true;
            case "Unsubscribe":
                userList.get(0).status = "idle";
                sendingClient.sendInform(userList);
                updateListView();
                return true;
            default:
                return super.onContextItemSelected(item);
        }

    }
}
