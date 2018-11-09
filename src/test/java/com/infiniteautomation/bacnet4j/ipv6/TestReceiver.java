package com.infiniteautomation.bacnet4j.ipv6;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.nio.charset.Charset;

public class TestReceiver implements Runnable {
    public static void main(String[] args) throws Exception {
        //        new TestReceiver("FF03::BAC0", 0xBAC0);
        new TestReceiver("225.1.1.2", 0xBAC0);
    }

    private final String addr;
    private final int port;
    private final MulticastSocket socket;

    //    private final DatagramSocket socket;

    TestReceiver(String addr, int port) throws IOException {
        this.addr = addr;
        this.port = port;
        InetAddress ia = InetAddress.getByName(addr);
        socket = new MulticastSocket(new InetSocketAddress("::", port));
        //        socket = new DatagramSocket(port);
        socket.joinGroup(ia);

        new Thread(this).start();
    }

    public void close() {
        if (socket != null)
            socket.close();
    }

    @Override
    public void run() {
        try {
            byte[] buf = new byte[2048];
            DatagramPacket packet = new DatagramPacket(buf, 2048);
            while (true) {
                socket.receive(packet);

                String s = new String(buf, packet.getOffset(), packet.getLength(), Charset.forName("ASCII"));
                System.out.println(s + ", to m[" + addr + "]:" + port);

                //                byte[] msg = new byte[packet.getLength()];
                //                System.arraycopy(packet.getData(), packet.getOffset(), msg, 0, packet.getLength());
                //                sender.send((InetSocketAddress) packet.getSocketAddress(), msg);
            }
        }
        catch (IOException e) {
            if (!"socket closed".equals(e.getMessage()))
                e.printStackTrace();
        }
    }
}
