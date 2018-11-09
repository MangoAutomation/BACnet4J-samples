package com.infiniteautomation.bacnet4j.rs485;

import java.io.InputStream;
import java.io.OutputStream;

import com.infiniteautomation.bacnet4j.util.sero.SerialParameters;
import com.infiniteautomation.bacnet4j.util.sero.SerialPortProxy;
import com.infiniteautomation.bacnet4j.util.sero.SerialUtils;

public class PortTest {
    public static void main(String[] args) throws Exception {
        SerialParameters params = new SerialParameters();
        params.setCommPortId("COM4");
        params.setBaudRate(9600);
        params.setPortOwnerName("Testing");

        SerialPortProxy serialPort = SerialUtils.openSerialPort(params);
        InputStream in = serialPort.getInputStream();
        OutputStream out = serialPort.getOutputStream();

        while (true) {
            System.out.print(Integer.toString(in.read(), 16));
        }
    }
}
