package me.arycer.dam.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import me.arycer.dam.client.model.ChatModel;
import me.arycer.dam.client.network.ChatClientHandler;
import me.arycer.dam.client.ws.LocalWebSocketServer;

import java.io.IOException;
import java.net.ServerSocket;

public class ChatClient {
    private static final String HOST = "localhost";
    private static final int PORT = 8080;

    public static void main(String[] args) throws Exception {
        ChatModel model = new ChatModel();
        int webSocketPort = getAvailablePort();

        if (webSocketPort == -1) {
            System.err.println("No se pudo encontrar un puerto disponible para el servidor WebSocket");
            System.exit(1);
        }

        LocalWebSocketServer webSocketServer = new LocalWebSocketServer(webSocketPort, model);
        webSocketServer.start();
        model.setWebSocketServer(webSocketServer);

        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new DelimiterBasedFrameDecoder(32768, Unpooled.wrappedBuffer(new byte[]{'\n'})));
                            pipeline.addLast(new StringDecoder());
                            pipeline.addLast(new StringEncoder());
                            pipeline.addLast(new ChatClientHandler(model));
                        }
                    });

            ChannelFuture future = bootstrap.connect(HOST, PORT).sync();
            future.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
            webSocketServer.stop();
        }
    }

    private static int getAvailablePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            return -1;
        }
    }
}