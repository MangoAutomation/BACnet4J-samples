package com.infiniteautomation.bacnet4j.test;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.npdu.ip.IpNetworkUtils;
import com.serotonin.bacnet4j.obj.BACnetObject;
import com.serotonin.bacnet4j.transport.DefaultTransport;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.Address;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.Real;
import com.serotonin.bacnet4j.util.DiscoveryUtils;
import com.serotonin.bacnet4j.util.RequestUtils;

/**
 * Tests that the UDP too many segments bug for more than 7 segments is fixed.
 * 
 * @author japearson
 * 
 */
public class LargeSegmentTest {
    public static void main(String[] args) throws Exception {

        // Using a LocalDevice with 4000 objects
        int deviceWithObjectsId = 1968;
        int deviceReadId = deviceWithObjectsId + 1;
        final int numberOfObjectsToCreate = 4000;

        LocalDevice localDeviceWithLotsOfObjects = new LocalDevice(deviceWithObjectsId, new DefaultTransport(
                new IpNetwork("127.0.0.255")));

        for (int i = 1; i <= numberOfObjectsToCreate; i++) {
            BACnetObject av = new BACnetObject(
                    localDeviceWithLotsOfObjects.getNextInstanceObjectIdentifier(ObjectType.analogValue));
            av.writeProperty(PropertyIdentifier.objectName, new CharacterString("Test [" + i + "]"));
            av.writeProperty(PropertyIdentifier.relinquishDefault, new Real(i));
            localDeviceWithLotsOfObjects.addObject(av);
        }
        localDeviceWithLotsOfObjects.initialize();

        LocalDevice localDevice = new LocalDevice(deviceReadId, new DefaultTransport(new IpNetwork("127.0.0.255",
                IpNetwork.DEFAULT_PORT + 1)));
        localDevice.initialize();
        Address address = IpNetworkUtils.toAddress("127.0.0.1", IpNetwork.DEFAULT_PORT);
        RemoteDevice remoteDevice = localDevice.findRemoteDevice(address, deviceWithObjectsId);
        DiscoveryUtils.getExtendedDeviceInformation(localDevice, remoteDevice);

        try {
            // When querying the list of objects
            @SuppressWarnings("unchecked")
            SequenceOf<Encodable> list = (SequenceOf<Encodable>) RequestUtils.sendReadPropertyAllowNull(localDevice,
                    remoteDevice, remoteDevice.getObjectIdentifier(), PropertyIdentifier.objectList);

            // The device comes back in the object list, hence the extra 1
            final int expectedNumeberOfProperties = numberOfObjectsToCreate + 1;

            // We expect all the objects to come back
            if (list.getCount() == expectedNumeberOfProperties) {
                System.out.println("Correct number of objects returned!");
            }
            else {
                throw new IllegalStateException("Wrong nubmer of objects returned!");
            }
        }
        catch (BACnetException be) {
            System.out.println("Failed to get the objectList, most likely too many segments error.");
            be.printStackTrace();
        }
        finally {
            localDevice.terminate();
            localDeviceWithLotsOfObjects.terminate();
        }
    }
}
