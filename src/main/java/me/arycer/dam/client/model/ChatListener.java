package me.arycer.dam.client.model;

import me.arycer.dam.shared.protocol.Message;

import java.util.List;

public interface ChatListener {
    void sendMessage(Message connect);
    void sendChatMessage(String username, String content);
    void sendPrivateMessage(String username, String receiver, String content);
    List<String> getConnectedUsers();
}