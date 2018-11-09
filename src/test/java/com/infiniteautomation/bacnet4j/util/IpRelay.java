/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.bacnet4j.util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 *  
 *  @author Terry Packer
 */
public class IpRelay extends Thread {
    
    final DatagramSocket listenAt;
    final DatagramSocket forwardTo;
    final DatagramSocket remoteNetwork;
    Thread thread;
    
    
    public IpRelay(final DatagramSocket listenAt, final DatagramSocket forwardTo, final DatagramSocket remoteNetwork) {
        this.listenAt = listenAt;
        this.forwardTo = forwardTo;
        this.remoteNetwork = remoteNetwork;
    }

    @Override
    public void run() {
        thread = Thread.currentThread();

        final DatagramPacket p = new DatagramPacket(new byte[1024], 1024);

        while (true) {
            try {
                listenAt.receive(p);

                // Determine where this came from.
                DatagramSocket from = null;
                if(remoteNetwork.getLocalAddress() == p.getAddress())
                    from = remoteNetwork;

                if (from == null)
                    System.out.println("Can't find from socket for address" + p.getAddress());
                else {
                    // Use that socket to send to individual addresses.
                    final DatagramPacket fwd = new DatagramPacket(p.getData(), p.getLength(),
                            forwardTo.getLocalSocketAddress());
                    from.send(fwd);
                }
            } catch (final SocketException e) {
                if ("Socket closed".equalsIgnoreCase(e.getMessage()))
                    break;

                // ignore
                e.printStackTrace();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void close() throws Exception {
        listenAt.close();
        if (thread != null)
            thread.join();
    }
}
