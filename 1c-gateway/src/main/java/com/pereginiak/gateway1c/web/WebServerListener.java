package com.pereginiak.gateway1c.web;

import java.util.EventListener;

public interface WebServerListener extends EventListener {
    String getMessage();

    void putMessage(String message);

    boolean isClientConnected();
}
