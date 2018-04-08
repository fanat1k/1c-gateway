package com.pereginiak.gateway1c;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;

public class SocketServer extends Service {

    private ServerSocket serverSocket;

    //TODO(kasian @2018-04-08): make thread safe; is volatile enough for that?
    private volatile CommunicationThread currentSocketThread;

    ConcurrentLinkedQueue<String> socketClientValues = new ConcurrentLinkedQueue<>();

    private static final String TAG = "SocketServer";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "onStartCommand");

        Executors.newSingleThreadExecutor().submit(new ServerThread());
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.v(TAG, "onDestroy");
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    public class LocalBinder extends Binder {
        SocketServer getService() {
            return SocketServer.this;
        }
    }

    public boolean isClientConnected() {
        return currentSocketThread != null;
    }

    //TODO(kasian @2018-03-22): return ONLY THE LAST value if any came from socket client
    public String getValueFromSocket() {
        Log.i(TAG, "getValueFromSocket()");

        if (isClientConnected()) {
            return socketClientValues.poll();
        }
        return null;
    }

    public void putValueToSocket(String message) {
        Log.i(TAG, "putValueToSocket:" + message);
        sendToClient(message);
    }

    private void sendToClient(String message) {
        if (currentSocketThread == null) {
            Log.e(TAG, "client is not connected");
        } else {
            Log.i(TAG, "writeToSocket:" + message);
            try {
                currentSocketThread.writeToSocket(message);
            } catch (IOException e) {
                Log.e(TAG, "Can't write to socket:" + e);
                e.printStackTrace();
            }
        }
    }

    private class ServerThread implements Runnable {
        @Override
        public void run() {
            Socket socket;
            try {
                serverSocket = new ServerSocket(Constants.SOCKET_SERVER_PORT);
            } catch (IOException e) {
                Log.e(TAG, "Can't start Server Socket:" + e);
                e.printStackTrace();
            }

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Log.i(TAG, "serverSocket.accept()");
                    socket = serverSocket.accept();

                    Log.i(TAG, "Client has connected");
                    if (currentSocketThread == null) {
                        CommunicationThread task = new CommunicationThread(socket);
                        currentSocketThread = task;
                        Executors.newSingleThreadExecutor().submit(task);
                    } else {
                        Log.e(TAG, "Client already connected. Server doesn't support multiple connections");
                        if (socket.isConnected()) {
                            socket.close();
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                    e.printStackTrace();
                }
            }
        }
    }

    private class CommunicationThread implements Runnable {
        private Socket clientSocket;
        private BufferedReader input;

        public CommunicationThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
            try {
                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            } catch (IOException e) {
                Log.e(TAG, e.toString());
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String inputLine = input.readLine();

                    if (inputLine == null) {
                        currentSocketThread = null;
                        Thread.currentThread().interrupt();
                    } else {
                        if (!inputLine.isEmpty()) {
                            Log.i(TAG, "received from client: " + inputLine);
                            socketClientValues.add(inputLine);

                            //TODO(kasian @2018-03-22): answer to client? or later after successfull delivery to 1C?
                            //writeToSocket("[OK]");
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                    e.printStackTrace();
                }
            }

            Log.i(TAG, "Client has disconnected");
        }

        public void writeToSocket(String inputLine) throws IOException {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            out.write(inputLine);
            out.newLine();
            out.flush();
        }
    }
}
