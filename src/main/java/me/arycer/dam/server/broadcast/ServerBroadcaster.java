package me.arycer.dam.server.broadcast;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServerBroadcaster {
    private final int serverPort;
    private final ScheduledExecutorService executor;
    private boolean running = true;

    private static final String BROADCAST_ADDRESS = "255.255.255.255"; // Dirección de broadcast estándar
    private static final int BROADCAST_PORT = 4445;

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

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true); // Habilitar broadcast

            InetAddress broadcastAddress = InetAddress.getByName(BROADCAST_ADDRESS);
            String message = "CHAT_SERVER:" + serverPort;
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, broadcastAddress, BROADCAST_PORT);

            socket.send(packet);

            System.out.println("Mensaje de servidor enviado a la red local.");
        } catch (Exception e) {
            System.err.println("Error al enviar el mensaje de broadcast: " + e.getMessage());
        }
    }

    public void stopBroadcasting() {
        running = false;
        executor.shutdown();
    }
}