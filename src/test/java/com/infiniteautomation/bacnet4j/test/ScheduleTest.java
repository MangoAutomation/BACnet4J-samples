package com.infiniteautomation.bacnet4j.test;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.exception.BACnetException;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.npdu.ip.IpNetworkUtils;
import com.serotonin.bacnet4j.transport.DefaultTransport;
import com.serotonin.bacnet4j.transport.Transport;
import com.serotonin.bacnet4j.type.AmbiguousValue;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.PropertyValue;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.Primitive;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;
import com.serotonin.bacnet4j.util.RequestUtils;

public class ScheduleTest {
    static LocalDevice localDevice;

    public static void main(String[] args) throws Exception {
        IpNetwork network = new IpNetwork();
        Transport transport = new DefaultTransport(network);
        localDevice = new LocalDevice(1234, transport);

        try {
            localDevice.initialize();
            run();
        }
        finally {
            localDevice.terminate();
        }
    }

    static void run() throws BACnetException {
        RemoteDevice brock = localDevice.findRemoteDevice(IpNetworkUtils.toAddress("108.9.141.98", 0xbac0), 10000);
        getSchedule(brock, 1);
        //        getSchedule(brock, 2);
        //        getSchedule(brock, 3);

        //        RemoteDevice vico = localDevice.findRemoteDevice(new Address(2001, (byte) 58), new OctetString(
        //                "192.168.0.68:47808"), 76058);
        //        getSchedule(vico, 78);
        //        setSchedule2(vico, 78);
        //        getSchedule(vico, 78);
    }

    static void getSchedule(RemoteDevice d, int id) throws BACnetException {
        ObjectIdentifier oid = new ObjectIdentifier(ObjectType.schedule, id);

        PropertyValue pv;

        //        pv = new PropertyValue(PropertyIdentifier.presentValue, null);

        //        pv = new PropertyValue(PropertyIdentifier.outOfService,
        //                new com.serotonin.bacnet4j.type.primitive.Boolean(false));

        //        Date date = new Date(new GregorianCalendar(2012, Calendar.SEPTEMBER, 24));
        //        Date unspecified = new Date(-1, Month.valueOf(0), -1, DayOfWeek.valueOf(0));
        //        pv = new PropertyValue(PropertyIdentifier.effectivePeriod, new DateRange(date, unspecified));

        //        CalendarEntry ce = new CalendarEntry(new Date(new GregorianCalendar(2013, Calendar.SEPTEMBER, 24)));
        //        List<TimeValue> timeValues = new ArrayList<TimeValue>();
        //        timeValues.add(new TimeValue(new Time(12, 0, 0, 0), new UnsignedInteger(13)));
        //        SpecialEvent se = new SpecialEvent(ce, new SequenceOf<TimeValue>(timeValues), new UnsignedInteger(15));
        //        pv = new PropertyValue(PropertyIdentifier.exceptionSchedule, new UnsignedInteger(2), se, null);

        pv = new PropertyValue(PropertyIdentifier.exceptionSchedule, new UnsignedInteger(0), new UnsignedInteger(0),
                null);

        //        pv = new PropertyValue(PropertyIdentifier.weeklySchedule, null);
        //        [DailySchedule [daySchedule=[TimeValue [time=7:0:0.0, value=1], TimeValue [time=16:0:0.0, value=2]]], DailySchedule [daySchedule=[TimeValue [time=7:0:0.0, value=1], TimeValue [time=16:0:0.0, value=2]]], DailySchedule [daySchedule=[TimeValue [time=7:0:0.0, value=1], TimeValue [time=16:0:0.0, value=2]]], DailySchedule [daySchedule=[TimeValue [time=7:0:0.0, value=1], TimeValue [time=16:0:0.0, value=2]]], DailySchedule [daySchedule=[TimeValue [time=7:0:0.0, value=1], TimeValue [time=16:0:0.0, value=2]]], DailySchedule [daySchedule=[TimeValue [time=7:0:0.0, value=1], TimeValue [time=16:0:0.0, value=2]]], DailySchedule [daySchedule=[TimeValue [time=7:0:0.0, value=1], TimeValue [time=16:0:0.0, value=2]]]]

        //        System.out.println(get(d, oid, pv));
        setAndCheck(d, oid, pv);

        //        PropertyReferences refs = new PropertyReferences();
        //        refs.add(oid, PropertyIdentifier.effectivePeriod, PropertyIdentifier.weeklySchedule,
        //                PropertyIdentifier.scheduleDefault, PropertyIdentifier.priorityForWriting,
        //                PropertyIdentifier.outOfService, PropertyIdentifier.statusFlags, PropertyIdentifier.exceptionSchedule,
        //                PropertyIdentifier.listOfObjectPropertyReferences);
        //        PropertyValues values = RequestUtils.readProperties(localDevice, d, refs, null);
    }

    static void setSchedule2(RemoteDevice d, int id) throws BACnetException {
        setAndCheck(d, new ObjectIdentifier(ObjectType.schedule, id), new PropertyValue(
                PropertyIdentifier.outOfService, new com.serotonin.bacnet4j.type.primitive.Boolean(true)));
    }

    static void setAndCheck(RemoteDevice d, ObjectIdentifier oid, PropertyValue pv) throws BACnetException {
        System.out.println("Before: " + get(d, oid, pv));
        set(d, oid, pv);
        System.out.println("After: " + get(d, oid, pv));
    }

    static Encodable get(RemoteDevice d, ObjectIdentifier oid, PropertyValue pv) throws BACnetException {
        //        Encodable e = RequestUtils.readProperty(localDevice, d, oid, pv.getPropertyIdentifier(),
        //                pv.getPropertyArrayIndex());
        Encodable e = RequestUtils.readProperty(localDevice, d, oid, pv.getPropertyIdentifier(), null);
        if (e instanceof AmbiguousValue)
            e = ((AmbiguousValue) e).convertTo(Primitive.class);
        return e;
    }

    static void set(RemoteDevice d, ObjectIdentifier oid, PropertyValue pv) throws BACnetException {
        RequestUtils.writeProperty(localDevice, d, oid, pv);
    }
}
