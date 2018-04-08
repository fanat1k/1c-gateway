package com.pereginiak.gateway1c;

import android.content.Context;

public class Properties {
    private static Integer webServerPort;
    private static Integer socketServerPort;
    private static String ipAddress;

    public static void init(Context context) {
        PropertyReader propertyReader = new PropertyReader(context);

        webServerPort = Integer.valueOf(propertyReader.getProperty("WEB_SERVER_PORT"));
        socketServerPort = Integer.valueOf(propertyReader.getProperty("SOCKET_SERVER_PORT"));
        ipAddress = propertyReader.getProperty("IP_ADDRESS");
    }


    public static int getWebServerPort() {
        return webServerPort;
    }

    public static int getSocketServerPort() {
        return socketServerPort;
    }

    public static String getIpAddress() {
        return ipAddress;
    }
}
