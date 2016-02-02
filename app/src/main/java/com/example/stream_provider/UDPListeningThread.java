package com.example.stream_provider;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;

public class UDPListeningThread
{
    private AsyncTask<Void, Void, Void> async;
    private boolean running = true;
    private Context applicationContext = null;
    private WifiManager.MulticastLock multicastLock;

    public void runThread(final Context context, final ArrayList<Triple> userList, final ScheduledExecutorService worker, final SendingClient sendingClient, final MainActivity mainActivity)
    {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        multicastLock = wifi.createMulticastLock("multicastLock");
        multicastLock.setReferenceCounted(false);
        multicastLock.acquire();

        applicationContext = context;
        async = new AsyncTask<Void, Void, Void>()
        {
            @Override
            protected Void doInBackground(Void... params)
            {
                byte[] lMsg = new byte[4096];
                DatagramPacket datagramPacket = new DatagramPacket(lMsg, lMsg.length);
                MulticastSocket socket = null;
                try {
                    socket = new MulticastSocket(MainActivity.SERVER_PORT);
                    InetAddress group = Utils.getBroadcastAddress(context);
                    socket.joinGroup(group);
                } catch (IOException e) {
                    e.printStackTrace();
                }


                try
                {
                    //datagramSocket = new DatagramSocket(MainActivity.SERVER_PORT);
                    Log.d("Stream-Provider", "Listening on port 9001 for incoming UDP packets.");

                    while(running)
                    {
                        socket.receive(datagramPacket);
                        String protocol = new String(datagramPacket.getData()).split("\n")[0].split(";")[0];
                        String msg = new String(datagramPacket.getData()).split("\n")[0].split(";")[1];
                        String ip = datagramPacket.getAddress().getHostAddress();
                        Handler mainHandler = new Handler(context.getMainLooper());

                        Runnable myRunnable = new Runnable() {
                            @Override
                            public void run() {((TextView) mainActivity.findViewById(R.id.textField)).setText("Multicast works!");}
                        };
                        switch (protocol) {
                            case "HELLO":
                                String name = msg;
                                if (!ip.equals(Utils.getIPAddress(true))) {
                                    Utils.log("Received hello from: " + ip);
                                    mainHandler.post(myRunnable);
                                    boolean alreadyContained = false;
                                    for (Triple t : userList) {
                                        if (t.ip.equals(ip)) alreadyContained = true;
                                    }
                                    if (!alreadyContained) {
                                        userList.add(new Triple(name, ip, Triple.STATUS_IDLE));
                                    }
                                    sendingClient.sendAnswer(userList, datagramPacket.getAddress().getHostAddress());
                                }

                                break;
                            case "ANSWER":
                                Utils.log("Received answer from: " + ip);
                                JSONArray userArray = new JSONArray(msg);
                                // add all entries which are not contained in the own userlist
                                for (int i = 0; i < userArray.length(); i++) {
                                    JSONObject jsonObject = userArray.getJSONObject(i);
                                    String currIp = jsonObject.getString("ip");
                                    boolean alreadyContained = false;
                                    for (Triple t: userList) {
                                        if (t.ip.equals(currIp)) alreadyContained = true;
                                    }
                                    if (!alreadyContained) userList.add(new Triple(jsonObject.getString("name"), jsonObject.getString("ip"), jsonObject.getString("status")));
                                }
                                // check if own userlist contains any users which are not contained in the received list and resend a hello to share them
                                for (int i = 0; i < userList.size(); i++) {
                                    String currIp = userList.get(i).ip;
                                    boolean alreadyContained = false;
                                    for (int j = 0; j < userArray.length(); j++) {
                                        JSONObject jsonObject = userArray.getJSONObject(j);
                                        if (jsonObject.getString("ip").equals(currIp)) alreadyContained = true;
                                    }
                                    if (!alreadyContained) sendingClient.sendAnswer(userList, ip);
                                }

                                worker.shutdown();

                                Utils.log("Stopped hello thread and updated userlist:");
                                Utils.printUserList(userList);
                                break;
                            case "INFORM":

                                break;
                            case "SUBSCRIBE":

                                break;
                            case "UNSUBSCRIBE":

                                break;
                            default:
                                Log.d("Stream-Provider", "received packet with unknown protocol: prot: " + protocol + " msg: " + msg);
                                break;
                        }
                    }
                }
                catch (Exception e)
                {
                    Log.e("Stream-Provider", e.getMessage());
                }
                finally
                {
                    if (socket != null)
                    {
                        socket.close();
                    }
                }

                return null;
            }
        };

        if (Build.VERSION.SDK_INT >= 11) async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else async.execute();
    }

    public void stopThread()
    {
        if (multicastLock != null) {
            multicastLock.release();
            multicastLock = null;
        }
        running = false;
    }
}