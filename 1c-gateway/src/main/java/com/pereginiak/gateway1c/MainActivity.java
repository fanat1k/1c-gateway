package com.pereginiak.gateway1c;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.*;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.pereginiak.gateway1c.web.WebServer;
import com.pereginiak.gateway1c.web.WebServerListener;

import java.io.IOException;

public class MainActivity extends Activity {

    private SocketServer socketService;

    NfcAdapter nfcAdapter;

    PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(Constants.TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Properties.init(this);

        startSocketServer();

        startWebServer();

        startNfcService();

        //TODO(kasian @2018-04-08): check if it works to run app in background
        moveTaskToBack(true);
    }


/*
    public void startNfcReaderIntent(View view) {
        Intent nfcReaderAction = new Intent(this, NfcReader.class);
        startActivity(nfcReaderAction);
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
            Log.e(Constants.TAG, "onServiceDisconnected()");
        }

        @Override
        public void onBindingDied(ComponentName name) {
            Log.e(Constants.TAG, "onBindingDied()");
        }
    };

    private void startWebServer() {
        Log.i(Constants.TAG, "startWebServer()");
        WebServer webServer = new WebServer(Properties.getWebServerPort(), new WebServerListener() {
            @Override
            public String getMessage() {
                Log.i(Constants.TAG, "getMessage");
                return socketService.getValueFromSocket();
            }

            @Override
            public void putMessage(String message) {
                Log.i(Constants.TAG, "putMessage:" + message);
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
            Log.e(Constants.TAG, "Can't start webserver: " + e);
            e.printStackTrace();
        }
    }


    /////////////////////////////////////// NFC
    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        readFromIntent(intent);
    }

    @Override
    public void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
    }

    private void startNfcService() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
        }

        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        addNfcButtonSwitcherListener();
    }

    public void addNfcButtonSwitcherListener() {
        ToggleButton toggleButton = (ToggleButton) findViewById(R.id.nfcToggleButton);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    onResume();
                    showMessage("NFC is ON");
                } else {
                    onPause();
                    showMessage("NFC is OFF");
                }
            }
        });
    }

    private void readFromIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
                Tag myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                showAlert(dumpTagData(myTag));
            } else {
                showAlert("Tag discovered:" + action);
            }
        } else {
            showAlert("Unknown action=" + action);
        }
    }

    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void showAlert(String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Tag Info");
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }


    // from https://android.jlelse.eu/create-a-nfc-reader-application-for-android-74cf24f38a6f
    private String dumpTagData(Tag tag) {
        StringBuilder sb = new StringBuilder();
        byte[] id = tag.getId();
        String prefix = "android.nfc.tech.";
        sb.append("Technologies: ");
        for (String tech : tag.getTechList()) {
            sb.append(tech.substring(prefix.length()));
            sb.append(", ");
        }

        sb.delete(sb.length() - 2, sb.length());

        for (String tech : tag.getTechList()) {
            if (tech.equals(MifareClassic.class.getName())) {
                sb.append('\n');
                String type = "Unknown";

                try {
                    MifareClassic mifareTag = MifareClassic.get(tag);

                    switch (mifareTag.getType()) {
                        case MifareClassic.TYPE_CLASSIC:
                            type = "Classic";
                            break;
                        case MifareClassic.TYPE_PLUS:
                            type = "Plus";
                            break;
                        case MifareClassic.TYPE_PRO:
                            type = "Pro";
                            break;
                    }
                    sb.append("Mifare Classic type: ");
                    sb.append(type);
                    sb.append('\n');

                    sb.append("Mifare size: ");
                    sb.append(mifareTag.getSize() + " bytes");
                    sb.append('\n');

                    sb.append("Mifare sectors: ");
                    sb.append(mifareTag.getSectorCount());
                    sb.append('\n');

                    sb.append("Mifare blocks: ");
                    sb.append(mifareTag.getBlockCount());
                } catch (Exception e) {
                    sb.append("Mifare classic error: " + e.getMessage());
                }
            }

            if (tech.equals(MifareUltralight.class.getName())) {
                sb.append('\n');
                MifareUltralight mifareUlTag = MifareUltralight.get(tag);
                String type = "Unknown";
                switch (mifareUlTag.getType()) {
                    case MifareUltralight.TYPE_ULTRALIGHT:
                        type = "Ultralight";
                        break;
                    case MifareUltralight.TYPE_ULTRALIGHT_C:
                        type = "Ultralight C";
                        break;
                }
                sb.append("Mifare Ultralight type: ");
                sb.append(type);
            }
        }

        return sb.toString();
    }
}
