/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.bacnet4j.npdu.ip;

import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * @author Terry Packer
 *
 */
public class BacnetIpUtils {
    
    private BacnetIpUtils() { }

    /**
     * List all usable Interface addresses on the local machine.
     * 
     * Usable: is not loopback, is up, has broadcast address
     * 
     * @return
     * @throws SocketException
     */
    public static List<InterfaceAddress> listUsableBACnetInterfaces() throws SocketException {
        List<InterfaceAddress> usable = new ArrayList<>();
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
     
            if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                continue;
            }
            for(InterfaceAddress add : networkInterface.getInterfaceAddresses()) {
                if(add.getBroadcast() != null) {
                    usable.add(add);
                }
                    
            }
        }
        return usable;
    }
    
}
