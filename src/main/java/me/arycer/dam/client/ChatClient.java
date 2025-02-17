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

public class ChatClient {
    private static final String HOST = "5.tcp.eu.ngrok.io";
    private static final int PORT = 18881;

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Uso: java -jar ChatClient.jar <puerto_websocket>");
            System.exit(1);
        }

        ChatModel model = new ChatModel();
        int webSocketPort = Integer.parseInt(args[0]);

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
}