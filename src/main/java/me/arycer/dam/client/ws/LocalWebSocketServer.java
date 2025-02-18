package me.arycer.dam.client.ws;

import me.arycer.dam.client.io.Server;
import me.arycer.dam.client.io.ServerManager;
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

        Message test = new Message();
        test.setType("test_ping");
        System.out.println("Sending test ping");
        conn.send(MessageUtils.serialize(test));

        if (username == null) {
            this.sendServerList();
        } else {
            Message session = new Message();
            session.setType("session");
            session.setReceiver(username);
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
            case "chat_message" -> chatListener.sendChatMessage(username, msg.getContent());
            case "private_message" -> chatListener.sendPrivateMessage(username, msg.getReceiver(), msg.getContent());
            case "add_server" -> chatListener.addServer(msg.getContent());
            case "edit_server" -> chatListener.editServer(msg.getReceiver(), msg.getContent());
            case "delete_server" -> chatListener.deleteServer(msg.getReceiver());
            case "test-pong" -> System.out.println("Received test pong");
            case "connect_server" -> {
                System.out.println("Conectando a servidor: " + msg.getReceiver());

                if (username != null) {
                    return;
                }

                Server server = ServerManager.INSTANCE.getServer(Integer.parseInt(msg.getReceiver()));
                if (server == null) {
                    return;
                }

                chatListener.connectServer(msg.getReceiver(), () -> {
                    System.out.println("Conectado a servidor: " + server.getName());

                    this.username = server.getName();
                    Message connect = new Message();
                    connect.setType("connect");
                    connect.setSender(username);
                    chatListener.sendMessage(connect);
                });
            }
            case "disconnect" -> {
                chatListener.disconnect();
                this.username = null;

                ServerManager.INSTANCE.loadServers();
                sendServerList();
            }
            case "direct_connect" -> {
                // formato: username:ip:port
                String[] parts = msg.getContent().split(":");
                String username = parts[0];
                String address = parts[1];
                int port = Integer.parseInt(parts[2]);

                if (this.username != null) {
                    return;
                }

                Server server = new Server(username, address, port);

                chatListener.connectServer(server, () -> {
                    System.out.println("Conectado a servidor: " + server.getName());
                    this.username = server.getName();
                    Message connect = new Message();
                    connect.setType("connect");
                    connect.setSender(username);
                    chatListener.sendMessage(connect);
                });
            }
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

    @Override
    public void onClosing(WebSocket conn, int code, String reason, boolean remote) {
        super.onClosing(conn, code, reason, remote);
        System.out.println("Cerrando conexión: " + conn.getRemoteSocketAddress());
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

    public void sendServerList() {
        // format: username:address:port|username:address:port...
        StringBuilder servers = new StringBuilder();

        if (ServerManager.INSTANCE.getCurrentServers().isEmpty()) {
            Message message = new Message();
            message.setType("server_list");
            message.setContent("");
            sendMessageToElectron(message);

            return;
        }

        for (Server currentServer : ServerManager.INSTANCE.getCurrentServers()) {
            String username = currentServer.getName();
            String address = currentServer.getAddress();
            int port = currentServer.getPort();

            servers.append(username).append(":").append(address).append(":").append(port).append("|");
        }

        servers.deleteCharAt(servers.length() - 1);

        Message message = new Message();
        message.setType("server_list");
        message.setContent(servers.toString());

        sendMessageToElectron(message);
    }

    public void sendServerDiscoveryList(String serverList) {
        Message message = new Message();
        message.setType("discovery_list");
        message.setContent(serverList);

        sendMessageToElectron(message);
    }
}