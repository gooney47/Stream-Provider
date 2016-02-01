package com.example.stream_provider;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPListeningThread
{
    private AsyncTask<Void, Void, Void> async;
    private boolean running = true;
    private Context applicationContext = null;

    public void runThread(Context context)
    {
        applicationContext = context;
        async = new AsyncTask<Void, Void, Void>()
        {
            @Override
            protected Void doInBackground(Void... params)
            {
                byte[] lMsg = new byte[4096];
                DatagramPacket datagramPacket = new DatagramPacket(lMsg, lMsg.length);
                DatagramSocket datagramSocket = null;

                try
                {
                    datagramSocket = new DatagramSocket(MainActivity.SERVER_PORT);
                    Log.d("Stream-Provider", "Listening on port 9001 for incoming UDP packets.");

                    while(running)
                    {
                        Log.d("Stream-Provider", "waiting for new UDP packet");
                        datagramSocket.receive(datagramPacket);
                        String protocol = new String(datagramPacket.getData()).split("\n")[0].split(";")[0];
                        String msg = new String(datagramPacket.getData()).split("\n")[0].split(";")[1];
                        switch (protocol) {
                            case "HELLO":
                                String name = msg;
                                String ip = datagramPacket.getAddress().toString();
                                break;
                            case "ANSWER":

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
                    e.printStackTrace();
                }
                finally
                {
                    if (datagramSocket != null)
                    {
                        datagramSocket.close();
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
        running = false;
    }
}