package me.arycer.dam.client.io;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerManager {
    public static final ServerManager INSTANCE = new ServerManager();

    private static final Path SERVERS_FILE_PATH = Paths.get(System.getProperty("user.home"), "MasterChat/servers.json");
    private final List<Server> currentServers = new ArrayList<>();
    private final Gson gson = new Gson();
    private EventLoopGroup group;
    private ExecutorService executorService;

    public void loadServers() {
        currentServers.clear();
        try {
            if (Files.exists(SERVERS_FILE_PATH)) {
                try (FileReader reader = new FileReader(SERVERS_FILE_PATH.toFile())) {
                    Type listType = new TypeToken<List<Server>>() {}.getType();
                    currentServers.addAll(gson.fromJson(reader, listType));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading servers" + e.getMessage());
        }
    }
    
    public void saveServers() {
        try {
            Files.createDirectories(SERVERS_FILE_PATH.getParent());
            try (FileWriter writer = new FileWriter(SERVERS_FILE_PATH.toFile())) {
                gson.toJson(this.currentServers, writer);
            }
        } catch (IOException e) {
            System.err.println("Error saving servers" + e.getMessage());
        }
    }

    public List<Server> getCurrentServers() {
        return currentServers;
    }

    public void addServer(String username, String address, int port) {
        currentServers.add(new Server(username, address, port));
        saveServers();
    }

    public void editServer(int index, String username, String address, int port) {
        if (index < 0 || index >= currentServers.size()) {
            return;
        }

        Server currentServer = currentServers.get(index);
        if (currentServer != null) {
            currentServer.setName(username);
            currentServer.setAddress(address);
            currentServer.setPort(port);
            currentServers.set(index, currentServer);
            saveServers();
        }
    }

    public void deleteServer(int index) {
        if (index < 0 || index >= currentServers.size()) {
            return;
        }

        currentServers.remove(index);
        saveServers();
    }

    public Server getServer(int index) {
        if (index < 0 || index >= currentServers.size()) {
            return null;
        }

        return currentServers.get(index);
    }

    public void connect(Server server, ChatModel model, Runnable onConnect) throws InterruptedException {
        group = new NioEventLoopGroup();
        executorService = Executors.newSingleThreadExecutor();

        executorService.execute(() -> {
            try {
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
                                    pipeline.addLast(new ChatClientHandler(model, onConnect));
                                }
                            });

                    ChannelFuture future = bootstrap.connect(server.getAddress(), server.getPort()).sync();
                    future.channel().closeFuture().sync();
                } finally {
                    group.shutdownGracefully();
                }
            } catch (InterruptedException e) {
                System.err.println("Error connecting to server: " + e.getMessage());
            }
        });
    }

    public void disconnect() {
        if (group != null) {
            group.shutdownGracefully();
        }

        if (executorService != null) {
            executorService.shutdown();
        }
    }
}