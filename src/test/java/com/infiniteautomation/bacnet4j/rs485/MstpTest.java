package com.infiniteautomation.bacnet4j.rs485;

import java.net.Socket;

import com.serotonin.bacnet4j.npdu.mstp.MasterNode;
import com.serotonin.bacnet4j.npdu.mstp.SlaveNode;

public class MstpTest {
    public static void main(String[] args) throws Exception {
        new MstpTest();
    }

    MstpTest() throws Exception {
        new HubServer();
        master((byte) 0, 3, false, 5);
        master((byte) 1, 3, true, 5);
        //        master((byte) 2, 3);
        //        master((byte) 3, 3);
        //        master((byte) 4, 3);
        //        master((byte) 10, 3);
        //        master((byte) 15, 3);
        //        master((byte) 20, 3);
        //        master((byte) 25, 3);
        //        master((byte) 30, 3);
        //        master((byte) 35, 3);
        //        master((byte) 40, 3);
        //        master((byte) 45, 3);
        //        master((byte) 50, 3);
        //        master((byte) 55, 3);
        //        master((byte) 70, 3);
        //        master((byte) 80, 3);
        //        master((byte) 110, 3);
        //        slave((byte) 5);
        //        slave((byte) 6);
        //        slave((byte) 7);
        //        slave((byte) 8);
        //        slave((byte) 9);
        //        slave((byte) 71);
        //        slave((byte) 72);
        //        slave((byte) 73);

        partyCrasher(5);
    }

    void master(byte station, int retryCount, boolean trace, int delay) throws Exception {
        // Set up the socket
        Socket socket = new Socket("localhost", 50505);

        MasterNode n = new MasterNode(socket.getInputStream(), socket.getOutputStream(), station, retryCount);
        n.initialize();
        n.setTrace(trace);
        Thread.sleep(delay);
    }

    void slave(byte station, boolean trace, int delay) throws Exception {
        // Set up the socket
        Socket socket = new Socket("localhost", 50505);
        SlaveNode n = new SlaveNode(socket.getInputStream(), socket.getOutputStream(), station);
        n.initialize();
        n.setTrace(trace);
        Thread.sleep(delay);
    }

    void partyCrasher(int delay) throws Exception {
        Socket socket = new Socket("localhost", 50505);
        new Thread(new PartyCrasher(socket)).start();
        Thread.sleep(delay);
    }

    class PartyCrasher implements Runnable {
        Socket socket;

        public PartyCrasher(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    // Write some noise into the output.
                    socket.getOutputStream().write(0);

                    // Dump the input.
                    while (socket.getInputStream().available() > 0)
                        socket.getInputStream().read();

                    Thread.sleep(15);
                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}