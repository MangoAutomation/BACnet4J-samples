package com.infiniteautomation.bacnet4j.ipv6;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.serotonin.bacnet4j.util.sero.ThreadUtils;

public class TestEchoer implements Runnable {
    public static void main(String[] args) throws Exception {
        new TestEchoer("FF03::BAC0", 0xBAC0);
    }

    //    private final String addr;
    //    private final int port;
    private final MulticastSocket socket;
    private final Sender sender;
    private long bytesIn = 0;
    private long bytesOut = 0;

    TestEchoer(String addr, int port) throws IOException {
        //        this.addr = addr;
        //        this.port = port;
        InetAddress ia = InetAddress.getByName(addr);
        socket = new MulticastSocket(new InetSocketAddress("::", port));
        socket.joinGroup(ia);

        new Thread(this).start();
        sender = new Sender();
        new Thread(sender).start();
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

                bytesIn += packet.getLength();

                //String s = new String(buf, 0, packet.getLength(), Charset.forName("ASCII"));
                //System.out.println(s + ", to m[" + addr + "]:" + port);

                byte[] msg = new byte[packet.getLength()];
                System.arraycopy(packet.getData(), packet.getOffset(), msg, 0, packet.getLength());
                sender.send((InetSocketAddress) packet.getSocketAddress(), msg);

                System.out.println("Bytes in/out: " + bytesIn + "/" + bytesOut);

                // Reset the packet.
                packet.setData(buf);
            }
        }
        catch (IOException e) {
            if (!"socket closed".equals(e.getMessage()))
                e.printStackTrace();
        }
    }

    class ToSend {
        InetSocketAddress addr;
        byte[] data;
    }

    class Sender implements Runnable {
        Queue<ToSend> toSend = new ConcurrentLinkedQueue<ToSend>();
        Random random = new Random();

        void send(InetSocketAddress addr, byte[] data) {
            ToSend ts = new ToSend();
            ts.addr = addr;
            ts.data = data;
            toSend.add(ts);
            ThreadUtils.notifySync(toSend);
        }

        @Override
        public void run() {
            while (true) {
                ToSend ts = toSend.poll();
                if (ts == null)
                    ThreadUtils.waitSync(toSend, 200);
                else {
                    ThreadUtils.sleep(70 + random.nextInt(200));
                    sendPacket(ts.addr, ts.data);
                }
            }
        }
    }

    void sendPacket(InetSocketAddress addr, byte[] data) {
        try {
            DatagramPacket packet = new DatagramPacket(data, data.length, addr);
            socket.send(packet);
            bytesOut += data.length;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
