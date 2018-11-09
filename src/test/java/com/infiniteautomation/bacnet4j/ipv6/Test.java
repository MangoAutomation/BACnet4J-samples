package com.infiniteautomation.bacnet4j.ipv6;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.util.Collections;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.event.DeviceEventAdapter;
import com.serotonin.bacnet4j.npdu.NPCI;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.npdu.ip.IpNetworkUtils;
import com.serotonin.bacnet4j.npdu.ipv6.Ipv6Network;
import com.serotonin.bacnet4j.service.acknowledgement.ReadPropertyAck;
import com.serotonin.bacnet4j.service.confirmed.ReadPropertyRequest;
import com.serotonin.bacnet4j.service.unconfirmed.WhoIsRequest;
import com.serotonin.bacnet4j.transport.DefaultTransport;
import com.serotonin.bacnet4j.type.constructed.Address;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.util.sero.ByteQueue;
import com.serotonin.bacnet4j.util.sero.ThreadUtils;

public class Test {
    // udp.port eq 47808

    public static void main(String[] args) throws Exception {
        test5();
        //        test4();
        //        test3();
        //        test2();
        //        test1();
    }

    static void test5() throws Exception {
        LocalDevice localDevice = new LocalDevice(456, new DefaultTransport(new Ipv6Network("FF03::BAC0")));
        localDevice.getEventHandler().addListener(new DeviceEventAdapter() {
            @Override
            public void iAmReceived(RemoteDevice d) {
                System.out.println("Received IAm from " + d);
            }
        });

        try {
            localDevice.initialize();
            localDevice.sendGlobalBroadcast(new WhoIsRequest());

            ThreadUtils.sleep(3000);

            RemoteDevice d = localDevice.getRemoteDevice(76058);

            ReadPropertyRequest req = new ReadPropertyRequest(new ObjectIdentifier(ObjectType.device, 76058),
                    PropertyIdentifier.objectList, null);
            ReadPropertyAck ack = localDevice.send(d, req).get();
            System.out.println(ack);
        }
        finally {
            localDevice.terminate();
        }
    }

    static void test4() throws Exception {
        LocalDevice localDevice = new LocalDevice(456, new DefaultTransport(new IpNetwork()));
        localDevice.getEventHandler().addListener(new DeviceEventAdapter() {
            @Override
            public void iAmReceived(RemoteDevice d) {
                System.out.println("Received IAm from " + d);
            }
        });

        try {
            localDevice.initialize();
            localDevice.sendGlobalBroadcast(new WhoIsRequest());

            ThreadUtils.sleep(3000);
            // Received IAm from RemoteDevice(instanceNumber=1236, address=Address [networkNumber=0, macAddress=[c0,a8,0,67,ba,c0]])
            // Received IAm from RemoteDevice(instanceNumber=101, address=Address [networkNumber=0, macAddress=[c0,a8,0,44,ba,c0]])
            // Received IAm from RemoteDevice(instanceNumber=76058, address=Address [networkNumber=2001, macAddress=[3a]])

            RemoteDevice d = localDevice.getRemoteDevice(76058);

            ReadPropertyRequest req = new ReadPropertyRequest(new ObjectIdentifier(ObjectType.device, 76058),
                    PropertyIdentifier.objectList, null);
            ReadPropertyAck ack = localDevice.send(d, req).get();
            System.out.println(ack);
        }
        finally {
            localDevice.terminate();
        }
    }

    static void test3() throws Exception {
        // 6.4.19 --> 6.4.20
        Address dest = IpNetworkUtils.toAddress("255.255.255.255", 47808);

        ByteQueue queue = new ByteQueue();
        queue.push(0x81);
        queue.push(0xb);

        // NPCI
        ByteQueue postLength = new ByteQueue();
        NPCI npci = new NPCI(dest, null, false, 0x12, 0);
        npci.write(postLength);

        //         Length
        queue.pushU2B(queue.size() + postLength.size() + 2);

        // Combine the queues
        queue.push(postLength);

        InetSocketAddress isa = IpNetworkUtils.getInetSocketAddress(dest.getMacAddress());
        byte[] msg = queue.popAll();
        DatagramPacket packet = new DatagramPacket(msg, msg.length, isa);
        DatagramSocket socket = new DatagramSocket();
        socket.send(packet);
        socket.close();
    }

    static void test2() throws Exception {
        // 6.4.1 --> 6.4.2, 6.4.3
        Address dest = IpNetworkUtils.toAddress("255.255.255.255", 47808);
        //        Address dest = new Address("192.168.255.255", 47808);

        ByteQueue queue = new ByteQueue();
        queue.push(0x81);
        queue.push(0xb);

        // NPCI
        ByteQueue postLength = new ByteQueue();
        NPCI npci = new NPCI(dest, null, false, 0, 0);
        npci.write(postLength);

        //         Length
        queue.pushU2B(queue.size() + postLength.size() + 2);

        // Combine the queues
        queue.push(postLength);

        InetSocketAddress isa = IpNetworkUtils.getInetSocketAddress(dest.getMacAddress());
        byte[] msg = queue.popAll();
        DatagramPacket packet = new DatagramPacket(msg, msg.length, isa);
        DatagramSocket socket = new DatagramSocket();
        socket.send(packet);
        socket.close();
    }

    static void test1() throws Exception {
        for (NetworkInterface iface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            if (iface.isLoopback() || iface.isPointToPoint() || iface.isVirtual() || !iface.supportsMulticast()
                    || !iface.isUp())
                continue;

            System.out.println(iface + ", " + iface.isLoopback() + ", " + iface.isPointToPoint() + ", "
                    + iface.isVirtual() + ", " + iface.supportsMulticast());
            for (InetAddress addr : Collections.list(iface.getInetAddresses())) {
                //                if (!addr.isLoopbackAddress() && addr.isLinkLocalAddress())
                //                if (!addr.isLoopbackAddress())
                System.out.println("   " + addr);
                //                    result.add(getAddress(addr));
            }
        }
    }
}
