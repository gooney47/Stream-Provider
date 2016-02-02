package com.example.stream_provider;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class SendingClient {

    private final Context context;

    public SendingClient(Context context) {
        this.context = context;
    }

    public void sendHello(String name) {

        new SendOperation().execute("HELLO", name, "broadcast");
    }



    public void sendAnswer(ArrayList<Triple> userList, String address) {
        JSONArray jsonArray = new JSONArray();
        for (Triple t: userList) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("name", t.name);
                jsonObject.put("ip", t.ip);
                jsonObject.put("status", t.status);
            } catch (JSONException e) {
                Log.e("Stream-Provider", "lmao", e);
            }
            jsonArray.put(jsonObject);
        }
        new SendOperation().execute("ANSWER", jsonArray.toString(), address);
    }

    class SendOperation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String message = params[0] + ";" + params[1] + "\n";
            InetAddress address = null;

            try {
                if (params[2].equals("broadcast")) address = Utils.getBroadcastAddress(context);
                else address = InetAddress.getByName(params[2]);
            } catch (UnknownHostException e) {
                Log.d("Stream-Provider", e.getMessage());
                return "-1";
            } catch (IOException e) {
                Log.d("Stream-Provider", e.getMessage());
                return "-1";
            }
            try {
                DatagramSocket datagramSocket = new DatagramSocket();
                DatagramPacket dp;
                dp = new DatagramPacket(message.getBytes(), message.length(), address, MainActivity.SERVER_PORT);
                datagramSocket.setBroadcast(true);
                datagramSocket.send(dp);
                return "0";
            }
            catch (Exception e) {
                Log.e("Stream-Provider", "lmao", e);
                return "-1";
            }
        }

        @Override
        protected void onPostExecute(String result) {}

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }
}
