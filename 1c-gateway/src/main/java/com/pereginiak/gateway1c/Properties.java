package com.pereginiak.gateway1c;

import android.content.Context;

public class Properties {
    private static Integer webServerPort;
    private static Integer socketServerPort;
    private static String socketServerIpAddress;
    private static Integer messageCleanInterval;
    private static Integer messageLiveTime;

    public static void init(Context context) {
        PropertyReader propertyReader = new PropertyReader(context);

        webServerPort = Integer.valueOf(propertyReader.getProperty("WEB_SERVER_PORT"));
        socketServerPort = Integer.valueOf(propertyReader.getProperty("SOCKET_SERVER_PORT"));
        socketServerIpAddress = propertyReader.getProperty("SOCKET_SERVER_IP");
        messageCleanInterval = Integer.valueOf(propertyReader.getProperty("MESSAGE_CLEAN_INTERVAL"));
        messageLiveTime = Integer.valueOf(propertyReader.getProperty("MESSAGE_LIVE_TIME"));
    }


    public static int getWebServerPort() {
        return webServerPort;
    }

    public static int getSocketServerPort() {
        return socketServerPort;
    }

    public static String getSocketServerIpAddress() {
        return socketServerIpAddress;
    }

    public static Integer getMessageCleanInterval() {
        return messageCleanInterval;
    }

    public static Integer getMessageLiveTime() {
        return messageLiveTime;
    }
}
