package me.arycer.dam.client.model;

import me.arycer.dam.client.io.Server;
import me.arycer.dam.shared.protocol.Message;

import java.util.List;

public interface ChatListener {
    void sendMessage(Message connect);
    void sendChatMessage(String username, String content);
    void sendPrivateMessage(String username, String receiver, String content);
    List<String> getConnectedUsers();
    void addServer(String content);
    void editServer(String receiver, String content);
    void deleteServer(String receiver);
    void connectServer(String receiver, Runnable onConnect);
    void connectServer(Server server, Runnable onConnect);
    void disconnect();
}