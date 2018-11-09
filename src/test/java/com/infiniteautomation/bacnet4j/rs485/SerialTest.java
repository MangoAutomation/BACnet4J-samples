package com.infiniteautomation.bacnet4j.rs485;

import com.infiniteautomation.bacnet4j.util.sero.SerialParameters;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.event.DeviceEventAdapter;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.npdu.mstp.MasterNode;
import com.serotonin.bacnet4j.npdu.mstp.MstpNetwork;
import com.serotonin.bacnet4j.service.unconfirmed.WhoIsRequest;
import com.serotonin.bacnet4j.transport.DefaultTransport;
import com.serotonin.bacnet4j.transport.Transport;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.util.RequestUtils;

/**
 * This class tests the MS/TP code using an RS-485 network accessed via COM4.
 * 
 * @author Matthew
 * 
 */
public class SerialTest {
    static LocalDevice localDevice;

    public static void main(String[] args) throws Exception {
        SerialParameters params = new SerialParameters();
        params.setCommPortId("COM4");
        params.setBaudRate(9600);
        params.setPortOwnerName("Testing");

        MasterNode master = new MasterNode(params, (byte) 0x4, 2);
        MstpNetwork network = new MstpNetwork(master);
        Transport transport = new DefaultTransport(network);
        localDevice = new LocalDevice(1234, transport);
        localDevice.getEventHandler().addListener(new Listener());

        localDevice.initialize();

        localDevice.sendGlobalBroadcast(new WhoIsRequest());

        network.sendTestRequest((byte) 8);
    }

    static class Listener extends DeviceEventAdapter {
        @Override
        public void iAmReceived(RemoteDevice d) {
            System.out.println("Received IAm from " + d);

            try {
                System.out.println(RequestUtils.sendReadPropertyAllowNull(localDevice, d, d.getObjectIdentifier(),
                        PropertyIdentifier.objectList));
            }
            catch (BACnetException e) {
                e.printStackTrace();
            }
        }
    }
}
