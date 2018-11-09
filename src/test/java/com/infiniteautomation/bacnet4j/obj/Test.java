package com.infiniteautomation.bacnet4j.obj;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.event.DeviceEventAdapter;
import com.serotonin.bacnet4j.exception.BACnetErrorException;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.exception.ErrorAPDUException;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.service.unconfirmed.WhoIsRequest;
import com.serotonin.bacnet4j.transport.DefaultTransport;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.enumerated.BinaryPV;
import com.serotonin.bacnet4j.type.enumerated.ErrorClass;
import com.serotonin.bacnet4j.type.enumerated.ErrorCode;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.util.RequestUtils;

public class Test {
    static LocalDevice localDevice;

    static RemoteDevice d;

    public static void main(String[] args) throws Exception {
        localDevice = new LocalDevice(1234, new DefaultTransport(new IpNetwork()));
        localDevice.getEventHandler().addListener(new DeviceEventAdapter() {
            @Override
            public void iAmReceived(RemoteDevice d) {
                System.out.println("IAm: " + d);
            }
        });
        localDevice.initialize();

        try {
            localDevice.sendGlobalBroadcast(new WhoIsRequest());

            Thread.sleep(2000);

            d = localDevice.getRemoteDevice(76058);

            find();
        }
        finally {
            localDevice.terminate();
        }
    }

    static void find() throws BACnetException {
        SequenceOf<ObjectIdentifier> objList = RequestUtils.getObjectList(localDevice, d);
        for (ObjectIdentifier oid : objList) {
            CharacterString name = RequestUtils.getProperty(localDevice, d, oid, PropertyIdentifier.objectName);
            System.out.println(oid + ": " + name);

            try {
                if (oid.getObjectType().equals(ObjectType.analogValue)) {
                    //                    RequestUtils.writeProperty(localDevice, d, oid, PropertyIdentifier.highLimit, new Real(80));
                    //                    System.out.println("   Success");
                }
                else if (oid.getObjectType().equals(ObjectType.binaryValue)) {
                    //                  RequestUtils.writeProperty(localDevice, d, oid, PropertyIdentifier.alarmValue, BinaryPV.active);
                    //                  System.out.println("   Success");
                }
                else if (oid.getObjectType().equals(ObjectType.binaryInput)) {
                    RequestUtils.writeProperty(localDevice, d, oid, PropertyIdentifier.alarmValue, BinaryPV.active);
                    System.out.println("   Success");
                }
                else if (oid.getObjectType().equals(ObjectType.multiStateValue)) {
                    //                    RequestUtils.writeProperty(localDevice, d, oid, PropertyIdentifier.alarmValues,
                    //                            new SequenceOf<UnsignedInteger>(new UnsignedInteger(1)));
                    //                    System.out.println("   Success");
                }

                //                UnsignedInteger nc = RequestUtils
                //                        .getProperty(localDevice, d, oid, PropertyIdentifier.notificationClass);
                //                System.out.println("    " + nc);
            }
            catch (ErrorAPDUException e) {
                System.out.println("   Error: " + e.getMessage());
            }
            catch (BACnetErrorException e) {
                if (e.getError().getError().getErrorClass().equals(ErrorClass.property)
                        && e.getError().getError().getErrorCode().equals(ErrorCode.unknownProperty))
                    ;
                else
                    throw e;
            }
        }
    }
}
