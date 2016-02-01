package com.example.stream_provider;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class HelloThread implements Runnable {
    private final static int INTERVAL = 1000 * 60 * 1; //1 minutes
    private Context context = null;

    public HelloThread(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        String message = "HELLO;test\n";

        try {
            DatagramSocket datagramSocket = new DatagramSocket();
            DatagramPacket dp;
            dp = new DatagramPacket(message.getBytes(), message.length(), getBroadcastAddress(), MainActivity.SERVER_PORT);
            datagramSocket.setBroadcast(true);
            datagramSocket.send(dp);
            Log.d("Stream-Provider", "Sent broadcast message.");
        }
        catch (Exception e) {
            Log.e("Stream-Provider", "lmao", e);
        }
    }

    private InetAddress getBroadcastAddress() throws IOException {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        // handle null somehow

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) (broadcast >> (k * 8));
        return InetAddress.getByAddress(quads);
    }
    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {}
        return null;
    }
}
