/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.bacnet4j;
import java.net.DatagramSocket;
import java.util.List;

import org.junit.Test;

import com.infiniteautomation.bacnet4j.util.IpRelay;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.event.DeviceEventAdapter;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.npdu.ip.IpNetworkBuilder;
import com.serotonin.bacnet4j.service.acknowledgement.ReadPropertyAck;
import com.serotonin.bacnet4j.service.confirmed.ReadPropertyRequest;
import com.serotonin.bacnet4j.service.unconfirmed.WhoIsRequest;
import com.serotonin.bacnet4j.transport.DefaultTransport;
import com.serotonin.bacnet4j.transport.Transport;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.util.RequestUtils;

/**
 * Example of how to 
 * @author Terry Packer
 *
 */
public class BacnetTest {

    @Test
    public void testListener() {
            IpNetwork network = new IpNetworkBuilder().
                withBroadcast("192.168.1.255", 24).
                withPort(9001).
            build();
            Transport transport = new DefaultTransport(network);
            LocalDevice localDevice = new LocalDevice(1234, transport);
            
            IpNetwork network2 = new IpNetworkBuilder().
                    withBroadcast("192.168.1.255", 24).
                    withPort(9002).
                build();
                Transport transport2 = new DefaultTransport(network2);
            LocalDevice localDevice2 = new LocalDevice(12345, transport2);
            
            DatagramSocket localOut = new DatagramSocket();
            DatagramSocket localIn = new DatagramSocket();
            IpRelay relay = new IpRelay(localIn, localOut, network.getSocket());
            
            try {
                localDevice.initialize();
                localDevice2.initialize();
                localDevice.getEventHandler().addListener(new Listener(localDevice));
                localDevice.sendGlobalBroadcast(new WhoIsRequest());

                Thread.sleep(200000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                localDevice.terminate();
            }        
    }
    

    class Listener extends DeviceEventAdapter {
        
        private LocalDevice me;
        
        public Listener(LocalDevice ld) {
            this.me = ld;
        }
        
        @Override
        public void iAmReceived(final RemoteDevice d) {
            System.out.println("IAm received from " + d);
            System.out.println("Segmentation: " + d.getSegmentationSupported());
            //d.setSegmentationSupported(Segmentation.noSegmentation);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        getExtendedDeviceInformation(d);
                        System.out.println("Done getting extended information");

                        List<?> oids = ((SequenceOf) RequestUtils.sendReadPropertyAllowNull(me, d,
                                d.getObjectIdentifier(), PropertyIdentifier.objectList)).getValues();
                        System.out.println(oids);
                    } catch (BACnetException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        
        void getExtendedDeviceInformation(RemoteDevice d) throws BACnetException {
            ObjectIdentifier oid = d.getObjectIdentifier();

            // Get the device's supported services
            System.out.println("protocolServicesSupported");
            ReadPropertyAck ack = me.send(d, new ReadPropertyRequest(oid, PropertyIdentifier.protocolServicesSupported)).get();
            //d.setServicesSupported((ServicesSupported) ack.getValue());

            System.out.println("objectName");
            ack = me.send(d, new ReadPropertyRequest(oid, PropertyIdentifier.objectName)).get();
            // d.setName(ack.getValue().toString());

            System.out.println("protocolVersion");
            ack = me.send(d, new ReadPropertyRequest(oid, PropertyIdentifier.protocolVersion)).get();
            //d.setProtocolVersion((UnsignedInteger) ack.getValue());

            //        System.out.println("protocolRevision");
            //        ack = localDevice.send(d, new ReadPropertyRequest(oid, PropertyIdentifier.protocolRevision)).get();
            //        d.setProtocolRevision((UnsignedInteger) ack.getValue());
        }
    }

}
