package com.infiniteautomation.bacnet4j.ipv6;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class TestSender {
    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();

        send("T" + (System.currentTimeMillis() - start) + " ");

        int sleep = 290;
        for (int i = 0; i < 20; i++) {
            System.out.println("Sleeping for " + sleep + " seconds");
            Thread.sleep(1000 * sleep);
            send("T" + (System.currentTimeMillis() - start) + " ");
            sleep += 10;
        }
    }

    static void send(String msg) throws Exception {
        send("225.1.1.2", 0xBAC0, msg); // Broadcast
        send("192.168.0.103", 0xBAC0, msg); // Workstation

        //            send("FF03::BAC0", 0xBAC0, msg); // Broadcast
        //            send("fe80::e9f7:5494:48f5:9c86", 0xBAC0, msg); // Workstation
        //            send("fe80::ba27:ebff:fe08:6c87", 0xBAC0, msg); // Pi
    }

    static void send(String addr, int port, String msg) throws IOException {
        InetAddress ai = InetAddress.getByName(addr);
        byte[] b = (msg + " [" + addr + "]" + port).getBytes();
        DatagramPacket packet = new DatagramPacket(b, b.length, new InetSocketAddress(ai, port));
        DatagramSocket socket = new DatagramSocket();
        socket.send(packet);
        socket.close();
    }
}
