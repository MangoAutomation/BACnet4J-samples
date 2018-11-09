/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.bacnet4j.npdu.ip;

import static org.junit.Assert.fail;

import java.net.InterfaceAddress;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assume;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.util.RequestUtils;

/**
 * Discover a device via WhoIs and interrogate it.
 * 
 * The better way is to use LocalDevice.startRemoteDeviceDiscovery(callback);
 * @see com.infiniteautomation.bacnet4j.npdu.ip.RemoteDiscoveryExample
 * 
 * @author Terry Packer
 *
 */
public class BacnetIpListenerDiscoveryExample {

    static final Logger LOG = LoggerFactory.getLogger(BacnetIpListenerDiscoveryExample.class);
    
    @Test
    public void testListener() throws SocketException {
        List<InterfaceAddress> usable = BacnetIpUtils.listUsableBACnetInterfaces();
        Assume.assumeTrue(usable.size() > 0);
        InterfaceAddress address = usable.get(0);
        String bindAddress = address.getAddress().toString().split("/")[1];
        String broadcastAddress = address.getBroadcast().toString().split("/")[1];
        IpNetwork network = new IpNetworkBuilder()
                .withLocalBindAddress(bindAddress)
                .withBroadcast(broadcastAddress, address.getNetworkPrefixLength())
                .withLocalNetworkNumber(1).withPort(9000).withReuseAddress(true).build();
        Transport transport = new DefaultTransport(network);
        LocalDevice localDevice = new LocalDevice(1, transport);

        IpNetwork network2 = new IpNetworkBuilder()
                .withLocalBindAddress(bindAddress)
                .withBroadcast(broadcastAddress, address.getNetworkPrefixLength())
                .withLocalNetworkNumber(1).withPort(9001).withReuseAddress(true).build();
        Transport transport2 = new DefaultTransport(network2);
        LocalDevice localDevice2 = new LocalDevice(2, transport2);

        try{ 
            localDevice.initialize();
            localDevice2.initialize();
        
            try (
                    IpBridge ldRelay = new IpBridge(8000, localDevice, localDevice2);
                    IpBridge ld2Relay = new IpBridge(8001, localDevice2, localDevice);
                ){ 
    
                ldRelay.start();
                ld2Relay.start();
                
                Listener listener = new Listener(localDevice);
                localDevice.getEventHandler().addListener(listener);
                
                //Send whois and discover device 2
                localDevice.sendGlobalBroadcast(new WhoIsRequest());
    
                int count = 0;
                while(!listener.isDone() || count < 20) {
                    Thread.sleep(100);
                    count++;
                }
                if(!listener.isDone())
                    fail("Didn't get all data from device 2");
            }
            
            //TODO Cache the remote device
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            localDevice.terminate();
            localDevice2.terminate();
        }      
    }
    
    /**
     * Listen for messages from Device 2
     * 
     * @author Terry Packer
     *
     */
    class Listener extends DeviceEventAdapter {
        
        private final AtomicBoolean done = new AtomicBoolean();
        private final LocalDevice me;
        
        public Listener(LocalDevice ld) {
            this.me = ld;
        }
        
        @Override
        public void iAmReceived(final RemoteDevice d) {
            LOG.info("IAm received from " + d);
            LOG.info("Segmentation: " + d.getSegmentationSupported());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        getExtendedDeviceInformation(d);
                        LOG.info("Done getting extended information");
                        Encodable e = RequestUtils.sendReadPropertyAllowNull(me, d,
                                d.getObjectIdentifier(), PropertyIdentifier.objectList);
                        List<?> objects = ((SequenceOf<?>)e).getValues();
                        String out = "Object list:";
                        for(Object o : objects)
                            out += " '" + o + "'";
                        LOG.info(out);
                    } catch (BACnetException e) {
                        fail(e.getMessage());
                    }finally {
                        done.set(true);
                    }
                }
            }).start();
        }
        
        /**
         * Get more information from the device. 
         * 
         * NOTE RequestUtils and Discovery Utils classes provide helpers for this, we do it the hard 
         * way here to be explicit.
         * 
         * @param d
         * @throws BACnetException
         */
        void getExtendedDeviceInformation(RemoteDevice d) throws BACnetException {
            ObjectIdentifier oid = d.getObjectIdentifier();

            // Get the device's supported services
            ReadPropertyAck ack = me.send(d, new ReadPropertyRequest(oid, PropertyIdentifier.protocolServicesSupported)).get();
            LOG.info("Protocol Services Supported: " + ack.getValue());
            d.setDeviceProperty(ack.getPropertyIdentifier(), ack.getValue());
            //d.setServicesSupported((ServicesSupported) ack.getValue());

            ack = me.send(d, new ReadPropertyRequest(oid, PropertyIdentifier.objectName)).get();
            LOG.info("Object Name: " + ack.getValue());
            d.setDeviceProperty(ack.getPropertyIdentifier(), ack.getValue());

            ack = me.send(d, new ReadPropertyRequest(oid, PropertyIdentifier.protocolVersion)).get();
            LOG.info("Protocol Version: " + ack.getValue());
            d.setDeviceProperty(ack.getPropertyIdentifier(), ack.getValue());

            ack = me.send(d, new ReadPropertyRequest(oid, PropertyIdentifier.protocolRevision)).get();
            LOG.info("Protocol Revision: " + ack.getValue());
            d.setDeviceProperty(ack.getPropertyIdentifier(), ack.getValue());
        }
        
        public boolean isDone() {
            return done.get();
        }
    }
    
}
