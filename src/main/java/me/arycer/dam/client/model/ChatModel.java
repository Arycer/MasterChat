package me.arycer.dam.client.model;

import io.netty.channel.ChannelHandlerContext;
import me.arycer.dam.client.io.Server;
import me.arycer.dam.client.io.ServerManager;
import me.arycer.dam.client.ws.LocalWebSocketServer;
import me.arycer.dam.shared.protocol.Message;
import me.arycer.dam.shared.utils.MessageUtils;

import java.util.ArrayList;
import java.util.List;

public class ChatModel implements ChatListener {
    private final List<String> connectedUsers = new ArrayList<>();
    private ChannelHandlerContext ctx;
    private LocalWebSocketServer webSocketServer;

    public void setChannelHandlerContext(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public void setWebSocketServer(LocalWebSocketServer webSocketServer) {
        this.webSocketServer = webSocketServer;
    }

    @Override
    public void sendMessage(Message message) {
        if (ctx != null) {
            ctx.writeAndFlush(MessageUtils.serialize(message) + "\n");
        }
    }

    @Override
    public void sendChatMessage(String username, String content) {
        Message message = new Message();
        message.setType("public_message");
        message.setSender(username);
        message.setContent(content);
        sendMessage(message);
    }

    @Override
    public void sendPrivateMessage(String username, String receiver, String content) {
        Message message = new Message();
        message.setType("private_message");
        message.setSender(username);
        message.setReceiver(receiver);
        message.setContent(content);
        sendMessage(message);
    }

    public void updateUserList(List<String> users) {
        this.connectedUsers.clear();
        this.connectedUsers.addAll(users);
        if (webSocketServer != null) {
            Message msg = new Message();
            msg.setType("user_list");
            msg.setContent(String.join(",", users));
            webSocketServer.sendMessageToElectron(msg);
        }
    }

    public void updateChat(String sender, String message) {
        if (webSocketServer != null) {
            Message msg = new Message();
            msg.setType("chat");
            msg.setSender(sender);
            msg.setContent(message);
            webSocketServer.sendMessageToElectron(msg);
        }
    }

    @Override
    public List<String> getConnectedUsers() {
        return connectedUsers;
    }

    @Override
    public void addServer(String content) {
        // format: username:address:port
        String[] parts = content.split(":");
        String username = parts[0];
        String address = parts[1];
        int port = Integer.parseInt(parts[2]);

        ServerManager.INSTANCE.addServer(username, address, port);

        if (webSocketServer != null) {
            webSocketServer.sendServerList();
        }
    }

    @Override
    public void editServer(String receiver, String content) {
        int index = Integer.parseInt(receiver);
        // format: username:address:port
        String[] parts = content.split(":");
        String username = parts[0];
        String address = parts[1];
        int port = Integer.parseInt(parts[2]);

        ServerManager.INSTANCE.editServer(index, username, address, port);

        if (webSocketServer != null) {
            webSocketServer.sendServerList();
        }
    }

    @Override
    public void deleteServer(String receiver) {
        int index = Integer.parseInt(receiver);
        System.out.println("Deleting server: " + index);
        ServerManager.INSTANCE.deleteServer(index);

        if (webSocketServer != null) {
            webSocketServer.sendServerList();
        }
    }

    @Override
    public void connectServer(String receiver, Runnable onConnect) {
        int index = Integer.parseInt(receiver);
        try {
            Server server = ServerManager.INSTANCE.getServer(index);
            if (server == null) {
                sendError("server_not_found");
                return;
            }

            ServerManager.INSTANCE.connect(server, this, onConnect);
        } catch (InterruptedException e) {
            sendError("connect_failed");
            System.err.println("Error connecting to server: " + e.getMessage());
        }
    }

    @Override
    public void connectServer(Server server, Runnable onConnect) {
        try {
            ServerManager.INSTANCE.connect(server, this, onConnect);
        } catch (InterruptedException e) {
            sendError("connect_failed");
            System.err.println("Error connecting to server: " + e.getMessage());
        }
    }

    @Override
    public void disconnect() {
        ServerManager.INSTANCE.disconnect();
    }

    public void updatePrivateChat(String sender, String receiver, String content) {
        if (webSocketServer != null) {
            Message msg = new Message();
            msg.setType("private_chat");
            msg.setSender(sender);
            msg.setReceiver(receiver);
            msg.setContent(content);
            webSocketServer.sendMessageToElectron(msg);
        }
    }

    public void sendError(String error) {
        if (webSocketServer != null) {
            Message msg = new Message();
            msg.setType("error");
            msg.setContent(error);
            webSocketServer.sendMessageToElectron(msg);
        }
    }

    public void sendAcceptConnection(String username) {
        if (webSocketServer != null) {
            Message msg = new Message();
            msg.setType("session");
            msg.setReceiver(username);
            webSocketServer.sendMessageToElectron(msg);
        }
    }

    public void clearUsername() {
        if (webSocketServer != null) {
            webSocketServer.clearUsername();
        }
    }
}