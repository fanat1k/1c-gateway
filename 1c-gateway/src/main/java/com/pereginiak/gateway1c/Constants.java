package com.pereginiak.gateway1c;

public interface Constants {

    int WEB_SERVER_PORT = 8090;

    //TODO(kasian @2018-03-22): set to 5555!
    //private static final int SOCKET_SERVER_PORT = 5555;
    int SOCKET_SERVER_PORT = 1111;

    //TODO(kasian @2018-04-07): figure out what IP should be used?
    String IP_ADDRESS = "192.168.200.2";

    int RESPONSE_STATUS_OK = 0;
    int RESPONSE_STATUS_ERR_IP = 1;
    int RESPONSE_STATUS_ERR_CLIENT = 2;
}
