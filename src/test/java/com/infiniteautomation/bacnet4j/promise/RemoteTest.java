package com.infiniteautomation.bacnet4j.promise;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.ServiceFuture;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.service.acknowledgement.ReadPropertyAck;
import com.serotonin.bacnet4j.service.acknowledgement.ReadPropertyMultipleAck;
import com.serotonin.bacnet4j.service.confirmed.ReadPropertyMultipleRequest;
import com.serotonin.bacnet4j.service.confirmed.ReadPropertyRequest;
import com.serotonin.bacnet4j.service.confirmed.WritePropertyMultipleRequest;
import com.serotonin.bacnet4j.service.confirmed.WritePropertyRequest;
import com.serotonin.bacnet4j.transport.DefaultTransport;
import com.serotonin.bacnet4j.transport.Transport;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.PropertyReference;
import com.serotonin.bacnet4j.type.constructed.PropertyValue;
import com.serotonin.bacnet4j.type.constructed.ReadAccessResult;
import com.serotonin.bacnet4j.type.constructed.ReadAccessResult.Result;
import com.serotonin.bacnet4j.type.constructed.ReadAccessSpecification;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.constructed.WriteAccessSpecification;
import com.serotonin.bacnet4j.type.enumerated.EngineeringUnits;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.Real;
import com.serotonin.bacnet4j.util.DiscoveryUtils;
import com.serotonin.bacnet4j.util.RequestUtils;

public class RemoteTest {
    static final Logger LOG = LoggerFactory.getLogger(Transport.class);

    static LocalDevice localDevice;
    static RemoteDevice d;

    public static void main(String[] args) throws Exception {
        localDevice = new LocalDevice(1, new DefaultTransport(new IpNetwork()));
        localDevice.initialize();
        try {
            d = DiscoveryUtils.discoverDevice(localDevice, 1969);
            if (d == null)
                throw new RuntimeException("Remote device not found");

            for (int i = 0; i < 20; i++) {
                readRequest();
                segmentedResponse();
                writeRequest();
                segmentedRequest();
                segmentedRequestAndResponse();
            }
        }
        finally {
            localDevice.terminate();
        }
    }

    static void readRequest() throws Exception {
        // Send a read request to the remote device.
        ServiceFuture future = localDevice.send(d, new ReadPropertyRequest(new ObjectIdentifier(ObjectType.analogInput,
                1), PropertyIdentifier.units));
        ReadPropertyAck ack = future.get();
        Assert.assertEquals(EngineeringUnits.percentObscurationPerFoot, ack.getValue());
    }

    static void segmentedResponse() throws Exception {
        // Send an object list request to the remote device.
        List<ReadAccessSpecification> specs = new ArrayList<ReadAccessSpecification>();
        specs.add(new ReadAccessSpecification(d.getObjectIdentifier(), PropertyIdentifier.objectList));
        ServiceFuture future = localDevice.send(d, new ReadPropertyMultipleRequest(
                new SequenceOf<ReadAccessSpecification>(specs)));
        ReadPropertyMultipleAck ack = future.get();

        Assert.assertEquals(1, ack.getListOfReadAccessResults().getCount());
        ReadAccessResult readResult = ack.getListOfReadAccessResults().get(1);
        Assert.assertEquals(d.getObjectIdentifier(), readResult.getObjectIdentifier());
        Assert.assertEquals(1, readResult.getListOfResults().getCount());
        Result result = readResult.getListOfResults().get(1);
        Assert.assertEquals(PropertyIdentifier.objectList, result.getPropertyIdentifier());
        @SuppressWarnings("unchecked")
        SequenceOf<ObjectIdentifier> idList = (SequenceOf<ObjectIdentifier>) result.getReadResult().getDatum();
        //        System.out.println(idList);
        Assert.assertEquals(1010, idList.getCount());
        Assert.assertEquals(d.getObjectIdentifier(), idList.get(1));
    }

    static void writeRequest() throws Exception {
        ObjectIdentifier ai1 = new ObjectIdentifier(ObjectType.analogInput, 1);

        // Set the object to something else.
        localDevice.send(
                d,
                new WritePropertyRequest(ai1, PropertyIdentifier.units, null,
                        EngineeringUnits.percentObscurationPerMeter, null)).get();

        // Verify
        Encodable value = RequestUtils.readProperty(localDevice, d, ai1, PropertyIdentifier.units, null);
        Assert.assertEquals(EngineeringUnits.percentObscurationPerMeter, value);

        // Set it back
        localDevice.send(
                d,
                new WritePropertyRequest(ai1, PropertyIdentifier.units, null,
                        EngineeringUnits.percentObscurationPerFoot, null)).get();

        // Verify
        value = RequestUtils.readProperty(localDevice, d, ai1, PropertyIdentifier.units, null);
        Assert.assertEquals(EngineeringUnits.percentObscurationPerFoot, value);
    }

    static void segmentedRequest() throws Exception {
        List<PropertyValue> propertyValues = new ArrayList<PropertyValue>();
        propertyValues.add(new PropertyValue(PropertyIdentifier.presentValue, new Real(2.28F)));
        propertyValues.add(new PropertyValue(PropertyIdentifier.units, EngineeringUnits.btus));
        SequenceOf<PropertyValue> propertyValuesSeq = new SequenceOf<PropertyValue>(propertyValues);
        List<WriteAccessSpecification> specs = new ArrayList<WriteAccessSpecification>();
        for (int i = 0; i < 1000; i++)
            specs.add(new WriteAccessSpecification(new ObjectIdentifier(ObjectType.analogValue, i), propertyValuesSeq));
        localDevice.send(d, new WritePropertyMultipleRequest(new SequenceOf<WriteAccessSpecification>(specs))).get();
    }

    static void segmentedRequestAndResponse() throws Exception {
        List<PropertyReference> propertyReferences = new ArrayList<PropertyReference>();
        propertyReferences.add(new PropertyReference(PropertyIdentifier.presentValue));
        propertyReferences.add(new PropertyReference(PropertyIdentifier.units));
        SequenceOf<PropertyReference> propertyReferenceSeq = new SequenceOf<PropertyReference>(propertyReferences);

        List<ReadAccessSpecification> specs = new ArrayList<ReadAccessSpecification>();
        for (int i = 0; i < 1000; i++)
            specs.add(new ReadAccessSpecification(new ObjectIdentifier(ObjectType.analogValue, i), propertyReferenceSeq));

        ServiceFuture future = localDevice.send(d, new ReadPropertyMultipleRequest(
                new SequenceOf<ReadAccessSpecification>(specs)));
        ReadPropertyMultipleAck ack = future.get();

        Assert.assertEquals(1000, ack.getListOfReadAccessResults().getCount());
        //        ReadAccessResult readResult = ack.getListOfReadAccessResults().get(1);
        //        Assert.assertEquals(d.getObjectIdentifier(), readResult.getObjectIdentifier());
        //        Assert.assertEquals(1, readResult.getListOfResults().getCount());
        //        Result result = readResult.getListOfResults().get(1);
        //        Assert.assertEquals(PropertyIdentifier.objectList, result.getPropertyIdentifier());
        //        @SuppressWarnings("unchecked")
        //        SequenceOf<ObjectIdentifier> idList = (SequenceOf<ObjectIdentifier>) result.getReadResult().getDatum();
        //        //        System.out.println(idList);
        //        Assert.assertEquals(1010, idList.getCount());
        //        Assert.assertEquals(d.getObjectIdentifier(), idList.get(1));
    }
}
