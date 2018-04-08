package com.pereginiak.gateway1c;

import android.util.Log;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class HealthChecker {

    private SocketServer socketServer;

    private static final String TAG = "HealthChecker";

    public HealthChecker(SocketServer socketServer) {
        this.socketServer = socketServer;
    }

    public static boolean isIpSetUp() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        if (inetAddress.getHostAddress().equals(Properties.getIpAddress())) {
                            Log.i(TAG, "IP is OK");
                            return true;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            Log.e(TAG, e.toString());
        }

        Log.w(TAG, "IP is not set");
        return false;
    }

    public static boolean isClientConnected(MainActivity.WebServerListener webServerListener) {
        return webServerListener.isClientConnected();
    }
}
