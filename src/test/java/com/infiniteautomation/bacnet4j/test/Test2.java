package com.infiniteautomation.bacnet4j.test;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.event.DeviceEventAdapter;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.npdu.ip.IpNetworkUtils;
import com.serotonin.bacnet4j.service.acknowledgement.ReadPropertyAck;
import com.serotonin.bacnet4j.service.confirmed.ReadPropertyRequest;
import com.serotonin.bacnet4j.service.unconfirmed.WhoIsRequest;
import com.serotonin.bacnet4j.transport.DefaultTransport;
import com.serotonin.bacnet4j.transport.Transport;
import com.serotonin.bacnet4j.type.constructed.Address;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;

public class Test2 {
    public static void main(String[] args) throws Exception {
        Transport transport = new DefaultTransport(new IpNetwork());
        transport.addNetworkRouter(2001, IpNetworkUtils.toOctetString("192.168.0.68:47808"));
        LocalDevice localDevice = new LocalDevice(1968, transport);
        //        LocalDevice localDevice = new LocalDevice(1968, "93.93.233.191");
        //        localDevice.setPort(47809);

        localDevice.getEventHandler().addListener(new Listener());
        localDevice.initialize();

        try {
            localDevice.sendGlobalBroadcast(new WhoIsRequest());
            Thread.sleep(2000);

            // Read
            // ReadPropertyRequest read = new ReadPropertyRequest(new ObjectIdentifier(ObjectType.analogInput, 243),
            // PropertyIdentifier.presentValue);

            //        getObjectList(localDevice, "192.168.0.68", 0xBAC0, 101);
            //            getObjectList(localDevice, "192.168.0.68", 76058, new Address(2001, new byte[] { 0x3a }));
            getObjectList(localDevice, new Address(2001, new byte[] { 0x3a }), 76058);

            //getObjectList(localDevice, "206.210.100.135", 0xBAC0, 1011);
            // getObjectList(localDevice, "10.174.1.128", 0xBAC0, 57);
            // getObjectList(localDevice, "10.174.1.132", 0xBAC0, 59);
            // getObjectList(localDevice, "10.174.1.137", 0xBAC0, 63);
        }
        finally {
            localDevice.terminate();
        }
    }

    private static void getObjectList(LocalDevice localDevice, String ip, int deviceId) throws Exception {
        getObjectList(localDevice, IpNetworkUtils.toAddress(ip, 0xBAC0), deviceId);
    }

    private static void getObjectList(LocalDevice localDevice, Address to, int deviceId) throws Exception {
        ReadPropertyRequest read = new ReadPropertyRequest(new ObjectIdentifier(ObjectType.device, deviceId),
                PropertyIdentifier.objectList);
        ReadPropertyAck ack = (ReadPropertyAck) localDevice.send(to, read).get();

        System.out.println("IP: " + to.getDescription());
        SequenceOf<ObjectIdentifier> oids = (SequenceOf<ObjectIdentifier>) ack.getValue();
        for (ObjectIdentifier oid : oids)
            System.out.println("    " + oid);
    }

    static class Listener extends DeviceEventAdapter {
        @Override
        public void iAmReceived(RemoteDevice d) {
            System.out.println("IAm received" + d);
        }
    }
}
