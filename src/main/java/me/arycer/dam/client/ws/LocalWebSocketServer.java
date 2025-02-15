package me.arycer.dam.client.ws;

import me.arycer.dam.client.model.ChatListener;
import me.arycer.dam.shared.protocol.Message;
import me.arycer.dam.shared.utils.MessageUtils;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

public class LocalWebSocketServer extends WebSocketServer {
    private final Set<WebSocket> connections = new HashSet<>();
    private final ChatListener chatListener;
    private String username = null;

    public LocalWebSocketServer(int port, ChatListener chatListener) {
        super(new InetSocketAddress(port));
        this.chatListener = chatListener;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        connections.add(conn);
        System.out.println("Nueva conexión desde: " + conn.getRemoteSocketAddress());

        if (username == null) {
            Message usernameRequest = new Message();
            usernameRequest.setType("username_request");
            conn.send(MessageUtils.serialize(usernameRequest));
        } else {
            Message session = new Message();
            session.setType("session");
            session.setContent(username);
            conn.send(MessageUtils.serialize(session));

            Message userList = new Message();
            userList.setType("user_list");
            userList.setContent(String.join(",", chatListener.getConnectedUsers()));
            conn.send(MessageUtils.serialize(userList));
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        connections.remove(conn);
        System.out.println("Conexión cerrada: " + conn.getRemoteSocketAddress());
    }

    // Mensajes recibidos del frontend react
    @Override
    public void onMessage(WebSocket conn, String message) {
        Message msg = MessageUtils.deserialize(message);

        switch (msg.getType()) {
            case "username_response" -> {
                if (username == null) {
                    String username = msg.getContent();
                    Message connect = new Message();
                    connect.setType("connect");
                    connect.setSender(username);
                    chatListener.sendMessage(connect);
                    this.username = username;
                }
            }
            case "chat_message" -> chatListener.sendChatMessage(username, msg.getContent());
            case "private_message" -> chatListener.sendPrivateMessage(username, msg.getReceiver(), msg.getContent());
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("Ocurrió un error en la conexión: " + ex);
    }

    @Override
    public void onStart() {
        System.out.println("Servidor WebSocket iniciado en el puerto " + getPort());
    }

    public void sendMessageToElectron(Message message) {
        for (WebSocket conn : connections) {
            if (conn.isOpen()) {
                conn.send(MessageUtils.serialize(message));
            }
        }
    }

    public void clearUsername() {
        this.username = null;
    }
}