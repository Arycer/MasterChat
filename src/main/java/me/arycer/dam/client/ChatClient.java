package me.arycer.dam.client;

import me.arycer.dam.client.io.ServerManager;
import me.arycer.dam.client.model.ChatModel;
import me.arycer.dam.client.ws.LocalWebSocketServer;

import java.io.IOException;
import java.net.ServerSocket;

public class ChatClient {
    public static void main(String[] args) throws Exception {
        ChatModel model = new ChatModel();
        int webSocketPort = getAvailablePort();

        if (webSocketPort == -1) {
            System.err.println("No se pudo encontrar un puerto disponible para el servidor WebSocket");
            System.exit(1);
        }

        ServerManager.INSTANCE.loadServers();

        LocalWebSocketServer webSocketServer = new LocalWebSocketServer(webSocketPort, model);

        webSocketServer.start();
        model.setWebSocketServer(webSocketServer);
    }

    private static int getAvailablePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            return -1;
        }
    }
}