package com.pereginiak.gateway1c.web;

import android.util.Log;
import com.pereginiak.gateway1c.Constants;
import com.pereginiak.gateway1c.HealthChecker;
import fi.iki.elonen.NanoHTTPD;

import java.util.List;
import java.util.Map;

public class WebServer extends NanoHTTPD {

    private WebServerListener webServerListener;

    private static final String GET_REQUEST = "/get";
    private static final String PUT_REQUEST = "/put";
    private static final String STATUS_REQUEST = "/status";
    private static final String URL_PARAM_ID = "id";

    private static final String RESPONSE_OK = "OK";
    private static final String RESPONSE_ERR = "ERROR:bad request";

    private static final String TAG = "WebServer";

    public WebServer(int port, WebServerListener webServerListener) {
        super(port);
        this.webServerListener = webServerListener;
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        Log.i(TAG, "request:" + uri);

        Response response;
        switch (uri) {
            case GET_REQUEST:
                response = processGetRequest();
                break;
            case PUT_REQUEST:
                response = processPutRequest(session);
                break;
            case STATUS_REQUEST:
                response = processGetStatusRequest();
                break;
            default:
                response = newFixedLengthResponse(RESPONSE_ERR);
                break;

        }
        return response;
    }

    private Response processGetRequest() {
        String msg = webServerListener.getMessage();
        return newFixedLengthResponse(msg);
    }

    private Response processPutRequest(IHTTPSession session) {
        Map<String, List<String>> parameters = session.getParameters();
        Log.i(TAG, "parameters:" + parameters.toString());

        if (!parameters.isEmpty()) {
            List<String> messageList = parameters.get(URL_PARAM_ID);
            if (messageList != null) {
                String message = messageList.get(0);
                if (message != null && !message.isEmpty()) {
                    webServerListener.putMessage(message);

                    //TODO(kasian @2018-03-22): is 1C requires response?
                    return newFixedLengthResponse(RESPONSE_OK);
                }
            }
        }

        return newFixedLengthResponse(RESPONSE_ERR);
    }

    private Response processGetStatusRequest() {
        int status = Constants.RESPONSE_STATUS_OK;
        if (HealthChecker.isIpSetUp()) {
            if (!HealthChecker.isClientConnected(webServerListener)) {
                status = Constants.RESPONSE_STATUS_ERR_CLIENT;
            }
        } else {
            status = Constants.RESPONSE_STATUS_ERR_IP;
        }

        return newFixedLengthResponse(String.valueOf(status));
    }
}
