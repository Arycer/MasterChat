package me.arycer.dam.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import me.arycer.dam.server.model.User;
import me.arycer.dam.shared.protocol.Message;
import me.arycer.dam.shared.utils.MessageUtils;

import java.util.HashMap;
import java.util.Map;

public class ChatServerHandler extends SimpleChannelInboundHandler<String> {
    private static final Map<String, User> connectedUsers = new HashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        Message message = MessageUtils.deserialize(msg);

        switch (message.getType()) {
            case "connect" -> handleUserConnection(ctx, message);
            case "public_message" -> handlePublicMessage(message);
            case "private_message" -> handlePrivateMessage(ctx, message);
            case "disconnect" -> handleUserDisconnection(message);
            default -> System.out.println("Mensaje desconocido: " + msg);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String username = getUserByCtx(ctx);
        if (username != null) {
            connectedUsers.remove(username);
            System.out.println("Usuario desconectado: " + username);
            broadcastUserList();
        }
        super.channelInactive(ctx);
    }

    private String getUserByCtx(ChannelHandlerContext ctx) {
        for (Map.Entry<String, User> entry : connectedUsers.entrySet()) {
            if (entry.getValue().getCtx().equals(ctx)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void handleUserConnection(ChannelHandlerContext ctx, Message message) {
        String username = message.getSender();

        if (connectedUsers.containsKey(username)) {
            Message errorMessage = new Message();
            errorMessage.setType("err_already_exising_username");
            ctx.writeAndFlush(MessageUtils.serialize(errorMessage) + "\n");
        } else {
            connectedUsers.put(username, new User(username, ctx));
            System.out.println("Usuario conectado: " + username);
            broadcastUserList();

            Message acceptMessage = new Message();
            acceptMessage.setType("accept_connection");
            acceptMessage.setReceiver(username);
            ctx.writeAndFlush(MessageUtils.serialize(acceptMessage) + "\n");
        }
    }

    private void handlePublicMessage(Message message) {
        System.out.println("Mensaje público de " + message.getSender() + ": " + message.getContent());
        broadcastMessage(message);
    }

    private void handlePrivateMessage(ChannelHandlerContext ctx, Message message) {
        String receiver = message.getReceiver();
        if (connectedUsers.containsKey(receiver)) {
            User receiverUser = connectedUsers.get(receiver);
            receiverUser.getCtx().writeAndFlush(MessageUtils.serialize(message) + "\n");
            ctx.writeAndFlush(MessageUtils.serialize(message) + "\n");
        } else {
            System.out.println("Usuario " + receiver + " no encontrado.");
        }
    }

    private void handleUserDisconnection(Message message) {
        String username = message.getSender();
        connectedUsers.remove(username);
        System.out.println("Usuario desconectado: " + username);
        broadcastUserList();
    }

    private void broadcastMessage(Message message) {
        for (User user : connectedUsers.values()) {
            user.getCtx().writeAndFlush(MessageUtils.serialize(message) + "\n");
        }
    }

    private void broadcastUserList() {
        Message userListMessage = new Message();
        userListMessage.setType("user_list");
        userListMessage.setSender("server");
        userListMessage.setContent(String.join(",", connectedUsers.keySet()));
        broadcastMessage(userListMessage);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}