package com.infiniteautomation.bacnet4j;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.npdu.ip.IpNetworkBuilder;
import com.serotonin.bacnet4j.service.confirmed.ConfirmedRequestService;
import com.serotonin.bacnet4j.service.confirmed.WritePropertyRequest;
import com.serotonin.bacnet4j.service.unconfirmed.WhoIsRequest;
import com.serotonin.bacnet4j.transport.DefaultTransport;
import com.serotonin.bacnet4j.type.constructed.ActionCommand;
import com.serotonin.bacnet4j.type.constructed.ActionList;
import com.serotonin.bacnet4j.type.constructed.DeviceObjectPropertyReference;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.notificationParameters.Extended;
import com.serotonin.bacnet4j.type.notificationParameters.Extended.Parameter;
import com.serotonin.bacnet4j.type.notificationParameters.NotificationParameters;
import com.serotonin.bacnet4j.type.primitive.Boolean;
import com.serotonin.bacnet4j.type.primitive.Null;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.Real;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;
import com.serotonin.bacnet4j.util.sero.ByteQueue;

public class EncodingTest {
    public static void main(String[] args) throws Exception {
        //        send();
        //        encode();
        parameters();
    }

    static void send() throws Exception {
        LocalDevice localDevice = new LocalDevice(123, new DefaultTransport(new IpNetworkBuilder().build()));

        localDevice.initialize();

        try {
            localDevice.sendGlobalBroadcast(new WhoIsRequest());
            Thread.sleep(1500);

            RemoteDevice d = localDevice.getRemoteDevices().get(0);

            ActionCommand ac = new ActionCommand( //
                    new ObjectIdentifier(ObjectType.device, 234), //
                    new ObjectIdentifier(ObjectType.analogValue, 0), //
                    PropertyIdentifier.presentValue, //
                    null, new Real(3.14F), null, null, Boolean.FALSE, Boolean.FALSE);
            ActionList al = new ActionList(new SequenceOf<ActionCommand>(ac));
            WritePropertyRequest req = new WritePropertyRequest( //
                    new ObjectIdentifier(ObjectType.command, 0), //
                    PropertyIdentifier.action, new UnsignedInteger(0), al, null);
            localDevice.send(d, req);
        }
        finally {
            localDevice.terminate();
        }
    }

    static void encode() throws Exception {
        ActionCommand ac = new ActionCommand( //
                new ObjectIdentifier(ObjectType.device, 234), //
                new ObjectIdentifier(ObjectType.analogInput, 0), //
                PropertyIdentifier.presentValue, //
                null, /* new Real(3.14F) */Null.instance, null, null, Boolean.FALSE, Boolean.FALSE);
        ActionList al = new ActionList(new SequenceOf<ActionCommand>(ac));
        WritePropertyRequest req = new WritePropertyRequest( //
                new ObjectIdentifier(ObjectType.command, 0), //
                PropertyIdentifier.action, new UnsignedInteger(1), al, null);

        ByteQueue queue = new ByteQueue();
        req.write(queue);

        System.out.println(queue);

        ConfirmedRequestService reqIn = ConfirmedRequestService
                .createConfirmedRequestService(WritePropertyRequest.TYPE_ID, queue);
        System.out.println(reqIn);
    }

    static void parameters() throws Exception {
        Parameter p1 = new Parameter(new DeviceObjectPropertyReference(new ObjectIdentifier(ObjectType.analogInput, 12),
                PropertyIdentifier.presentValue, null, new ObjectIdentifier(ObjectType.device, 234)));
        Parameter p2 = new Parameter(new Real(3.14F));

        SequenceOf<Parameter> parameters = new SequenceOf<Extended.Parameter>(p1, p2);
        Extended extended = new Extended(new UnsignedInteger(2), new UnsignedInteger(3), parameters);

        ByteQueue queue = new ByteQueue();
        extended.write(queue);

        Extended extended2 = (Extended) NotificationParameters.createNotificationParameters(queue);
        //        Extended extended2 = new Extended(queue);

        System.out.println(extended2.equals(extended));
    }
}
