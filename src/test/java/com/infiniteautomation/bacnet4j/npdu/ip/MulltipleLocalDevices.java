/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.bacnet4j.npdu.ip;

import java.net.InterfaceAddress;
import java.util.List;

import org.junit.Assume;
import org.junit.Test;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.npdu.ip.IpNetworkBuilder;
import com.serotonin.bacnet4j.transport.DefaultTransport;
import com.serotonin.bacnet4j.transport.Transport;

/**
 * @author Terry Packer
 *
 */
public class MulltipleLocalDevices {
    
    
    @Test
    public void testMultipleLocalDevices() throws Exception {
        
        List<InterfaceAddress> usable = BacnetIpUtils.listUsableBACnetInterfaces();
        Assume.assumeTrue(usable.size() > 0);
        
        InterfaceAddress address = usable.get(0);
        String bindAddress = address.getAddress().toString().split("/")[1];
        String broadcastAddress = address.getBroadcast().toString().split("/")[1];
        
        //Configure the first network, ensure we set reuse address
        IpNetwork networkOne = new IpNetworkBuilder()
                .withLocalBindAddress(bindAddress)
                .withBroadcast(broadcastAddress, address.getNetworkPrefixLength())
                .withLocalNetworkNumber(1).withPort(9000).withReuseAddress(true).build();
        Transport transportOne = new DefaultTransport(networkOne);
        LocalDevice localDeviceOne = new LocalDevice(1, transportOne);
        
        
        
        IpNetwork networkTwo = new IpNetworkBuilder()
                .withLocalBindAddress(bindAddress)
                .withBroadcast(broadcastAddress, address.getNetworkPrefixLength())
                .withLocalNetworkNumber(1).withPort(9000).withReuseAddress(true).build();
        Transport transportTwo = new DefaultTransport(networkTwo);
        LocalDevice localDeviceTwo = new LocalDevice(2, transportTwo);
        
        localDeviceOne.initialize();
        localDeviceTwo.initialize();
        
    }

}
