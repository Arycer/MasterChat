package me.arycer.dam.server.broadcast;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServerBroadcaster {
    private final int serverPort;
    private final ScheduledExecutorService executor;
    private boolean running = true;

    private static final String MULTICAST_ADDRESS = "230.0.0.0";
    private static final int MULTICAST_PORT = 4445;

    public ServerBroadcaster(int serverPort) {
        this.serverPort = serverPort;
        this.executor = Executors.newSingleThreadScheduledExecutor();
    }

    public void startBroadcasting() {
        executor.scheduleAtFixedRate(this::broadcast, 0, 5, TimeUnit.SECONDS);
    }

    private void broadcast() {
        if (!running) {
            stopBroadcasting();
            return;
        }

        try (MulticastSocket socket = new MulticastSocket()) {
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);

            String message = "CHAT_SERVER:" + serverPort;
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, MULTICAST_PORT);

            socket.send(packet);

            System.out.println("Mensaje de servidor enviado al grupo multicast.");
        } catch (Exception e) {
            System.err.println("Error al enviar el mensaje multicast: " + e.getMessage());
        }
    }

    public void stopBroadcasting() {
        running = false;
        executor.shutdown();
    }
}