package com.infiniteautomation.bacnet4j.test;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.RemoteObject;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.npdu.ip.IpNetworkUtils;
import com.serotonin.bacnet4j.transport.DefaultTransport;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.util.PropertyValues;
import com.serotonin.bacnet4j.util.RequestUtils;

public class SegmentationTest {
    static LocalDevice localDevice;

    public static void main(String[] args) throws Exception {
        localDevice = new LocalDevice(1968, new DefaultTransport(new IpNetwork("255.255.255.255", 1234)));
        localDevice.initialize();

        try {
            RemoteDevice d = localDevice.findRemoteDevice(IpNetworkUtils.toAddress("192.168.0.103", 0xBAC0), 1969);
            d.setMaxReadMultipleReferences(2000);
            localDevice.addRemoteDevice(d);

            for (ObjectIdentifier oid : RequestUtils.getObjectList(localDevice, d))
                d.setObject(new RemoteObject(oid));
            System.out.println("Remote objects: " + d.getObjects().size());

            PropertyValues pvs = RequestUtils.readPresentValues(localDevice, d, null);
            System.out.println(pvs);
        }
        finally {
            localDevice.terminate();
        }
    }
}
