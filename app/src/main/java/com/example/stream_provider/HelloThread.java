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
    private final SendingClient sendingClient;
    private final String name;

    public HelloThread(SendingClient sendingClient, String name) {
        this.sendingClient = sendingClient;
        this.name = name;
    }

    @Override
    public void run() {
        sendingClient.sendHello(name);
    }
}
