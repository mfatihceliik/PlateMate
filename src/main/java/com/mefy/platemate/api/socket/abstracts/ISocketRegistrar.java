package com.mefy.platemate.api.socket.abstracts;

import com.corundumstudio.socketio.SocketIOServer;

/**
 * Her socket handler'ın implemente ettiği temel interface.
 * Handler kendi event'lerini kendisi register eder — Single Responsibility.
 */
public interface ISocketRegistrar {
    void registerEvents(SocketIOServer server);
}
