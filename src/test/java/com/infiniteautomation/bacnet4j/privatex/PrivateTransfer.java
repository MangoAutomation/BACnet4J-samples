package com.infiniteautomation.bacnet4j.privatex;

import java.util.HashMap;
import java.util.Map;

import com.infiniteautomation.bacnet4j.test.DecodingTest;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.apdu.APDU;
import com.serotonin.bacnet4j.apdu.ConfirmedRequest;
import com.serotonin.bacnet4j.enums.MaxApduLength;
import com.serotonin.bacnet4j.enums.MaxSegments;
import com.serotonin.bacnet4j.service.VendorServiceKey;
import com.serotonin.bacnet4j.service.confirmed.ConfirmedPrivateTransferRequest;
import com.serotonin.bacnet4j.service.confirmed.ConfirmedRequestService;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.SequenceDefinition;
import com.serotonin.bacnet4j.type.SequenceDefinition.ElementSpecification;
import com.serotonin.bacnet4j.type.constructed.Sequence;
import com.serotonin.bacnet4j.type.constructed.ServicesSupported;
import com.serotonin.bacnet4j.type.primitive.OctetString;
import com.serotonin.bacnet4j.type.primitive.Real;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;
import com.serotonin.bacnet4j.util.sero.ByteQueue;

public class PrivateTransfer {
    static final SequenceDefinition def1 = new SequenceDefinition( //
            new ElementSpecification("value1", Real.class, false, false) //
            , new ElementSpecification("value2", OctetString.class, false, false) //
    );

    static final SequenceDefinition def2 = new SequenceDefinition( //
            new ElementSpecification("ui", UnsignedInteger.class, false, true) //
    );

    public static void main(String[] args) throws Exception {
        LocalDevice.vendorServiceRequestResolutions.put(new VendorServiceKey(25, 8), def1);
        LocalDevice.vendorServiceRequestResolutions.put(new VendorServiceKey(8, 21), def2);

        encoding();
        //        decodingFull();
        decodingFail();
    }

    static void encoding() throws Exception {
        Map<String, Encodable> values = new HashMap<String, Encodable>();
        values.put("value1", new Real(72.4f));
        values.put("value2", new OctetString(new byte[] { 0x16, 0x49 }));
        Sequence parameters = new Sequence(def1, values);

        ConfirmedRequestService service = new ConfirmedPrivateTransferRequest(new UnsignedInteger(25),
                new UnsignedInteger(8), parameters);
        APDU pdu = new ConfirmedRequest(false, false, false, MaxSegments.UNSPECIFIED, MaxApduLength.UP_TO_1024,
                (byte) 85, (byte) 0, 0, service);

        ByteQueue queue = new ByteQueue();
        pdu.write(queue);
        System.out.println(queue);
    }

    static void decodingFull() throws Exception {
        byte[] b = DecodingTest.toBytes("[0,4,55,12,9,19,19,8,2e,44,42,90,cc,cd,62,16,49,2f]");
        ByteQueue queue = new ByteQueue(b);

        ServicesSupported ss = new ServicesSupported();
        ss.setAll(true);
        ConfirmedRequest req = (ConfirmedRequest) APDU.createAPDU(ss, queue);
        req.parseServiceData();

        ConfirmedPrivateTransferRequest cptr = (ConfirmedPrivateTransferRequest) req.getServiceRequest();
        Sequence sequence = (Sequence) cptr.getServiceParameters();
        System.out.println(sequence.getValues());
    }

    static void decodingFail() throws Exception {
        byte[] b = DecodingTest.toBytes("[0,4,55,12,9,19,19,9,2e,44,42,90,cc,cd,62,16,49,2f]");
        ByteQueue queue = new ByteQueue(b);

        ServicesSupported ss = new ServicesSupported();
        ss.setAll(true);
        ConfirmedRequest req = (ConfirmedRequest) APDU.createAPDU(ss, queue);
        req.parseServiceData();

        ConfirmedPrivateTransferRequest cptr = (ConfirmedPrivateTransferRequest) req.getServiceRequest();
        Sequence sequence = (Sequence) cptr.getServiceParameters();
        System.out.println(sequence.getValues());
    }
}
