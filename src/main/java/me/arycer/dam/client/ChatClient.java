package me.arycer.dam.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import me.arycer.dam.client.model.ChatModel;
import me.arycer.dam.client.network.ChatClientHandler;
import me.arycer.dam.client.ws.LocalWebSocketServer;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

public class ChatClient {
    private static final String HOST = "localhost";
    private static final int PORT = 8080;

    public static void main(String[] args) throws Exception {
        ChatModel model = new ChatModel();
        int webSocketPort = getAvailablePort();

        LocalWebSocketServer webSocketServer = new LocalWebSocketServer(webSocketPort, model);
        webSocketServer.start();
        model.setWebSocketServer(webSocketServer);

        startElectronApp(webSocketPort);

        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new LineBasedFrameDecoder(8192));
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
            throw new RuntimeException("No se pudo encontrar un puerto disponible.", e);
        }
    }

    private static void startElectronApp(int webSocketPort) {
        try {
            String electronAppPath = "electron-app";
            File dir = new File(electronAppPath);

            ProcessBuilder processBuilder = new ProcessBuilder("npm", "start");
            processBuilder.environment().put("WS_PORT", String.valueOf(webSocketPort));
            processBuilder.directory(dir);

            processBuilder.redirectErrorStream(true);
            Process electronProcess = processBuilder.start();

            new Thread(() -> {
                try {
                    electronProcess.waitFor();
                    System.out.println("Terminando ejecución...");
                    System.exit(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

            System.out.println("Aplicación Electron iniciada en el puerto WebSocket: " + webSocketPort);
        } catch (IOException e) {
            System.err.println("Error al iniciar la aplicación Electron: " + e.getMessage());
        }
    }}