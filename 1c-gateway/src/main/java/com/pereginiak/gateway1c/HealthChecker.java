package com.pereginiak.gateway1c;

import android.util.Log;
import com.pereginiak.gateway1c.web.WebServerListener;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class HealthChecker {

    public static boolean isIpSetUp() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        if (inetAddress.getHostAddress().equals(Properties.getSocketServerIpAddress())) {
                            Log.i(Constants.TAG, "SocketServerIpAddress is configured:" + Properties.getSocketServerIpAddress());
                            return true;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            Log.e(Constants.TAG, e.toString());
        }

        Log.w(Constants.TAG, "SocketServerIpAddress is not configured");
        return false;
    }

    public static boolean isClientConnected(WebServerListener webServerListener) {
        return webServerListener.isClientConnected();
    }
}
