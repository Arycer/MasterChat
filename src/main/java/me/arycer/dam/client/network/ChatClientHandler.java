package me.arycer.dam.client.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import me.arycer.dam.client.model.ChatModel;
import me.arycer.dam.shared.protocol.Message;
import me.arycer.dam.shared.utils.MessageUtils;

import java.util.Arrays;

public class ChatClientHandler extends SimpleChannelInboundHandler<String> {
    private final ChatModel model;

    public ChatClientHandler(ChatModel model) {
        this.model = model;
    }

    // Mensajes recibidos del servidor Netty
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        Message message = MessageUtils.deserialize(msg);

        switch (message.getType()) {
            case "public_message" -> model.updateChat(message.getSender(), message.getContent());
            case "private_message" -> model.updatePrivateChat(message.getSender(), message.getReceiver(), message.getContent());
            case "user_list" -> {
                model.updateUserList(Arrays.asList(message.getContent().split(",")));
                System.out.println("Lista de usuarios actualizada: " + message.getContent());
            }
            case "err_already_exising_username" -> {
                model.clearUsername();
                model.sendError("already_existing_username");
            }
            case "accept_connection" -> model.sendAcceptConnection(message.getReceiver());
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        model.setChannelHandlerContext(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}