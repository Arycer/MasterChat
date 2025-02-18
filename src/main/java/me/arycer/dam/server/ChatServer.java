package me.arycer.dam.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import me.arycer.dam.server.broadcast.ServerBroadcaster;
import me.arycer.dam.server.handler.ChatServerHandler;

public class ChatServer {
    public static void main(String[] args) throws Exception {
        String portEnv = System.getenv("SERVER_PORT");
        int port = 8080; // Valor predeterminado

        // Si la variable de entorno está definida, úsala
        if (portEnv != null && !portEnv.isEmpty()) {
            try {
                port = Integer.parseInt(portEnv);
            } catch (NumberFormatException e) {
                System.err.println("El valor de SERVER_PORT no es un número válido. Usando el puerto predeterminado 8080.");
            }
        }

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBroadcaster broadcaster = new ServerBroadcaster(port);
        broadcaster.startBroadcasting(); // Iniciar broadcast

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new DelimiterBasedFrameDecoder(32768, Unpooled.wrappedBuffer(new byte[]{'\n'})));
                            pipeline.addLast(new StringDecoder());
                            pipeline.addLast(new StringEncoder());
                            pipeline.addLast(new ChatServerHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture future = bootstrap.bind(port).sync();
            System.out.println("Servidor de chat iniciado en el puerto " + port);
            future.channel().closeFuture().sync();
        } finally {
            broadcaster.stopBroadcasting();
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}