package com.infiniteautomation.bacnet4j;
import java.util.ArrayList;
import java.util.List;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.npdu.ip.IpNetworkUtils;
import com.serotonin.bacnet4j.npdu.mstp.MstpNetworkUtils;
import com.serotonin.bacnet4j.transport.DefaultTransport;
import com.serotonin.bacnet4j.transport.Transport;
import com.serotonin.bacnet4j.type.constructed.Address;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.util.DiscoveryUtils;
import com.serotonin.bacnet4j.util.PropertyValues;
import com.serotonin.bacnet4j.util.RequestUtils;

public class FindDevice {
    static LocalDevice localDevice;

    public static void main(String[] args) throws Exception {
        test2();
        //        test1();
    }

    static void test2() throws Exception {
        IpNetwork network = new IpNetwork("255.255.255.255", 12345);
        Transport transport = new DefaultTransport(network);
        localDevice = new LocalDevice(1234, transport);
        try {
            localDevice.initialize();
            RemoteDevice d = localDevice.findRemoteDevice(
                    new Address(0, IpNetworkUtils.toOctetString("127.0.0.1:47808")), 31415);
            System.out.println(d);
            DiscoveryUtils.getExtendedDeviceInformation(localDevice, d);
            System.out.println(RequestUtils.getObjectList(localDevice, d));

            ObjectIdentifier ai3 = new ObjectIdentifier(ObjectType.analogInput, 3);
            List<ObjectIdentifier> oids = new ArrayList<ObjectIdentifier>();
            oids.add(ai3);
            for (int i = 0; i < 10; i++) {
                PropertyValues pvs = RequestUtils.readOidPresentValues(localDevice, d, oids, null);
                System.out.println(pvs.get(ai3, PropertyIdentifier.presentValue));
                Thread.sleep(5000);
            }
        }
        finally {
            localDevice.terminate();
        }
    }

    static void test1() throws Exception {
        IpNetwork network = new IpNetwork();
        Transport transport = new DefaultTransport(network);
        //        transport.setTimeout(15000);
        //        transport.setSegTimeout(15000);
        transport.addNetworkRouter(36, IpNetworkUtils.toOctetString("89.101.141.54:47808"));

        localDevice = new LocalDevice(1234, transport);
        try {
            localDevice.initialize();

            //            Address address = new Address(50, new OctetString(new byte[] { 0, 0, (byte) 0xc3, 0x52 }));
            //            OctetString linkService = new OctetString("216.138.232.134:47808");
            //            RemoteDevice d = localDevice.findRemoteDevice(address, linkService, 50002);

            RemoteDevice d = localDevice.findRemoteDevice(MstpNetworkUtils.toAddress(36, (byte) 1), 1001);
            System.out.println(d);

            Thread.sleep(5000);
            DiscoveryUtils.getExtendedDeviceInformation(localDevice, d);

            Thread.sleep(5000);
            System.out.println(RequestUtils.getObjectList(localDevice, d));

            //            Thread.sleep(200000);
        }
        finally {
            localDevice.terminate();
        }
    }
}
