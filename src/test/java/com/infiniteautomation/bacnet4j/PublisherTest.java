package com.infiniteautomation.bacnet4j;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.transport.DefaultTransport;
import com.serotonin.bacnet4j.transport.Transport;
import com.serotonin.bacnet4j.util.DiscoveryUtils;

public class PublisherTest {
    static LocalDevice localDevice;

    public static void main(String[] args) throws Exception {
        IpNetwork network = new IpNetwork();
        Transport transport = new DefaultTransport(network);
        //        //        transport.setTimeout(15000);
        //        //        transport.setSegTimeout(15000);
        localDevice = new LocalDevice(1234, transport);
        try {
            localDevice.initialize();
            //            localDevice.getEventHandler().addListener(new Listener());
            //            localDevice.sendGlobalBroadcast(new WhoIsRequest());

            //            RemoteDevice d = localDevice.findRemoteDevice(1968, 10000);
            //            System.out.println(d);
            //            System.out.println(d.getMaxAPDULengthAccepted());
            //            System.out.println(d.getSegmentationSupported());
            //            System.out.println(d.getVendorId());
            //            System.out.println(d.getVendorName());
            //            System.out.println(d.getServicesSupported());

            //            RemoteDevice d = localDevice.findRemoteDevice(new Address("192.168.0.122", 0xBAC0), null, 1968);
            //            System.out.println(d);
            //            System.out.println(d.getMaxAPDULengthAccepted());
            //            System.out.println(d.getSegmentationSupported());
            //            System.out.println(d.getVendorId());
            //            System.out.println(d.getVendorName());
            //            System.out.println(d.getServicesSupported());

            RemoteDevice d = DiscoveryUtils.discoverDevice(localDevice, 1968);
            System.out.println(d);
            System.out.println(d.getMaxAPDULengthAccepted());
            System.out.println(d.getSegmentationSupported());
            System.out.println(d.getVendorId());
            System.out.println(d.getVendorName());
            System.out.println(d.getServicesSupported());

            //            RemoteDevice d = localDevice.findRemoteDevice(new Address("192.168.0.122", 0xBAC0), null, 1968);
            //            //            RemoteDevice d = localDevice.findRemoteDevice(new Address("192.168.0.91", 0xBAC0), null, 1968);
            //
            //            ObjectIdentifier oid = new ObjectIdentifier(ObjectType.analogInput, 1);
            //            PropertyReferences refs = new PropertyReferences();
            //            refs.add(oid, PropertyIdentifier.all);
            //            PropertyValues e = RequestUtils.readProperties(localDevice, d, refs, null);
            //            System.out.println(e);

            //Thread.sleep(200000);
        }
        finally {
            localDevice.terminate();
        }
    }
}
