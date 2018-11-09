package com.infiniteautomation.bacnet4j.ipv6;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;

public class IList {
    public static void main(String[] args) throws Exception {
        for (NetworkInterface iface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            for (InetAddress addr : Collections.list(iface.getInetAddresses())) {
                if (!addr.isLoopbackAddress())
                    System.out
                            .println(addr + ", ll=" + addr.isLinkLocalAddress() + ", sl=" + addr.isSiteLocalAddress());
            }
        }
    }
}
