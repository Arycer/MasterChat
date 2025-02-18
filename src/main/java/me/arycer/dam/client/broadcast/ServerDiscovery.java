package me.arycer.dam.client.broadcast;

import me.arycer.dam.client.ws.LocalWebSocketServer;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.*;

public class ServerDiscovery {
    public static final ServerDiscovery INSTANCE = new ServerDiscovery();

    private final Map<String, Long> discoveredServers = new ConcurrentHashMap<>();
    private final ExecutorService executor;
    private final ScheduledExecutorService cleanupExecutor;
    private final ScheduledExecutorService sendServerListExecutor;
    private boolean running = true;
    private LocalWebSocketServer webSocketServer;
    private static final int TIMEOUT_MS = 15000; // 15 segundos de inactividad para borrar
    private String lastSentServerList = "";

    public ServerDiscovery() {
        this.executor = Executors.newSingleThreadExecutor();
        this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
        this.sendServerListExecutor = Executors.newSingleThreadScheduledExecutor();
    }

    public void startDiscovery() {
        executor.execute(this::discover);
        cleanupExecutor.scheduleAtFixedRate(this::cleanupServers, 5, 5, TimeUnit.SECONDS);
        sendServerListExecutor.scheduleAtFixedRate(this::sendServerList, 0, 5, TimeUnit.SECONDS);
    }

    private void discover() {
        try (DatagramSocket socket = new DatagramSocket(4445)) {  // Usar DatagramSocket para recibir en el mismo puerto
            socket.setBroadcast(true); // Permitir el uso de broadcast

            byte[] buffer = new byte[1024];

            while (running) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String message = new String(packet.getData(), 0, packet.getLength());
                if (message.startsWith("CHAT_SERVER:")) {
                    String ip = packet.getAddress().getHostAddress();
                    String port = message.split(":")[1];
                    String serverInfo = ip + ":" + port;

                    discoveredServers.put(serverInfo, System.currentTimeMillis());
                }
            }
        } catch (Exception e) {
            if (running) {
                System.err.println("Error al recibir el broadcast: " + e.getMessage());
            }
        }
    }

    private void sendServerList() {
        // Formato: ip:port|ip:port|ip:port
        String serverList = String.join("|", discoveredServers.keySet());
        if (webSocketServer != null && !serverList.equals(lastSentServerList)) {
            webSocketServer.sendServerDiscoveryList(serverList);
            lastSentServerList = serverList;
        }
    }

    private void cleanupServers() {
        long now = System.currentTimeMillis();
        discoveredServers.entrySet().removeIf(entry -> now - entry.getValue() > TIMEOUT_MS);
    }

    public void stopDiscovery() {
        running = false;
        executor.shutdown();
        cleanupExecutor.shutdown();
    }

    public void setWebSocketServer(LocalWebSocketServer webSocketServer) {
        this.webSocketServer = webSocketServer;
    }
}