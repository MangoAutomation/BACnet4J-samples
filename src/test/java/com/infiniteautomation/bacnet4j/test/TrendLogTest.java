package com.infiniteautomation.bacnet4j.test;

import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.exception.BACnetServiceException;
import com.serotonin.bacnet4j.obj.BACnetObject;
import com.serotonin.bacnet4j.type.constructed.DateTime;
import com.serotonin.bacnet4j.type.constructed.LogRecord;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.constructed.StatusFlags;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.Boolean;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.Real;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;

public class TrendLogTest {
    public static void main(String[] args) throws BACnetServiceException {
        LocalDevice localDevice = null;
        BACnetObject obj = new BACnetObject(ObjectType.trendLog, 0);
        obj.writeProperty(PropertyIdentifier.objectName, new CharacterString("My trend log"));
        obj.writeProperty(PropertyIdentifier.enable, new Boolean(true));
        // Define other properties as necessary.

        SequenceOf<LogRecord> buffer = new SequenceOf<LogRecord>();
        obj.writeProperty(PropertyIdentifier.logBuffer, buffer);
        obj.writeProperty(PropertyIdentifier.recordCount, new UnsignedInteger(0));

        // Add records to the buffer.
        LogRecord record = new LogRecord(new DateTime(), new Real(123), new StatusFlags(false, false, false, false));
        buffer.add(record);
        obj.writeProperty(PropertyIdentifier.recordCount,
                new UnsignedInteger(((UnsignedInteger) obj.getProperty(PropertyIdentifier.recordCount)).intValue() + 1));
        // Update totalRecordCount too, if required.
    }
}
