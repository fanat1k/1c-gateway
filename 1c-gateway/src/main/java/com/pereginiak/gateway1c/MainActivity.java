package com.pereginiak.gateway1c;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.util.EventListener;

TODO1: put project to repository
TODO2(kasian @2018-04-08): try to run this activity in background (from another main activity)

public class MainActivity extends Activity {

    private SocketServer socketService;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HealthChecker.isIpSetUp();

        startSocketServer();

        startWebServer();
    }

/*
    @Override
    protected void onStart() {
        super.onStart();

        TextView outputView = (TextView) findViewById(R.id.outputView);
        outputView.setMovementMethod(new ScrollingMovementMethod());
        showLogcat();
    }
*/

    private void startSocketServer() {
        Intent socketServiceIntent = new Intent(this, SocketServer.class);
        startService(socketServiceIntent);
        bindService(socketServiceIntent, socketServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection socketServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            SocketServer.LocalBinder binder = (SocketServer.LocalBinder) service;
            socketService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //TODO(kasian @2018-03-22):
        }

        @Override
        public void onBindingDied(ComponentName name) {
            //TODO(kasian @2018-03-22):
        }
    };

    private void startWebServer() {
        Log.i(TAG, "startWebServer()");

        WebServer webServer = new WebServer(Constants.WEB_SERVER_PORT, new WebServerListener() {
            @Override
            public String getMessage() {
                Log.i(TAG, "getMessage");
                return socketService.getValueFromSocket();
            }

            @Override
            public void putMessage(String message) {
                Log.i(TAG, "putMessage:" + message);
                socketService.putValueToSocket(message);
            }

            @Override
            public boolean isClientConnected() {
                return socketService.isClientConnected();
            }
        });
        try {
            webServer.start();
        } catch (IOException e) {
            Log.e(TAG, "Can't start webserver: " + e);
            e.printStackTrace();
        }
    }

    public interface WebServerListener extends EventListener {
        String getMessage();

        void putMessage(String message);

        boolean isClientConnected();
    }

/*
    private void showLogcat() {
        try {
            Process process = Runtime.getRuntime().exec("logcat -d");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            StringBuilder log=new StringBuilder();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                log.append(line);
            }
            TextView tv = (TextView)findViewById(R.id.outputView);
            tv.setText(log.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
*/
}
