/*-
 * ========================START=================================
 * Organization: Rafael Luis Ibasco
 * Project: GLCD Simulator
 * Filename: SerialByteListenerTask.java
 * 
 * ---------------------------------------------------------
 * %%
 * Copyright (C) 2018 - 2019 Rafael Luis Ibasco
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * =========================END==================================
 */
package com.ibasco.glcdemulator.net.serial;

import com.fazecast.jSerialComm.SerialPort;
import com.ibasco.glcdemulator.enums.SerialBaudRate;
import com.ibasco.glcdemulator.enums.SerialFlowControl;
import com.ibasco.glcdemulator.enums.SerialParity;
import com.ibasco.glcdemulator.enums.SerialStopBits;
import com.ibasco.glcdemulator.net.ByteListenerTask;
import com.ibasco.glcdemulator.net.ListenerOptions;
import com.ibasco.glcdemulator.services.SerialPortService;
import com.ibasco.glcdemulator.utils.ByteUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;

/**
 * A service task for receiving a byte stream via Serial communication interface.
 *
 * @author Rafael Ibasco
 */
public class SerialByteListenerTask extends ByteListenerTask {
    private static final Logger log = LoggerFactory.getLogger(SerialByteListenerTask.class);

    private int baudRate = SerialBaudRate.RATE_9600.toValue();

    private int parity = SerialParity.NONE.toValue();

    private int flowControl = SerialFlowControl.NONE.toValue();

    private int stopBits = SerialStopBits.ONE_STOP_BIT.toValue();

    private int dataBits = 8;

    private SerialPort serialPort;

    private static final byte MSG_REQ = 0x7B;

    private static final byte MSG_ACK = 0x7C;

    @Override
    protected void configure(ListenerOptions options) {
        log.debug("Processing serial options: {}", options);

        SerialPortService serialPortService = options.get(SerialListenerOptions.PORT_SERVICE);
        String portName = options.get(SerialListenerOptions.SERIAL_PORT_NAME);
        serialPort = serialPortService.findSerialPortByName(portName);
        baudRate = options.get(SerialListenerOptions.BAUD_RATE);
        parity = options.get(SerialListenerOptions.PARITY);
        flowControl = options.get(SerialListenerOptions.FLOW_CONTROL);
        stopBits = options.get(SerialListenerOptions.STOP_BITS);
        dataBits = options.get(SerialListenerOptions.DATA_BITS);

        if (serialPort == null) {
            String msg = "No serial ports found";
            log.warn(msg);
            throw new IllegalStateException(msg);
        }

        configurePort(serialPort);
    }

    private void configurePort(SerialPort port) {
        port.setComPortParameters(baudRate, dataBits, stopBits, parity);
        port.setFlowControl(flowControl);
        log.debug("Port settings: Baudrate={}, DataBits={}, StopBits={}, Parity={}, Flow Control={}", baudRate, dataBits, stopBits, parity, flowControl);
    }

    @Override
    protected void cleanup() {
        if (serialPort != null && serialPort.closePort()) {
            log.info("Serial port successfully closed");
        }
    }

    @Override
    protected String getName() {
        return "SERIAL-LISTENER";
    }

    @Override
    protected int calculateBufferSize() {
        return (getBuffer().getWidth() * getBuffer().getHeight()) / 8;
    }

    @Override
    protected void process() throws Exception {
        log.info("Starting Serial listen task");

        if (!serialPort.isOpen()) {
            if (!serialPort.isOpen()) {
                log.info("Opening serial port");
                if (!serialPort.openPort()) {
                    throw new IOException("Could not connect to serial device");
                }
            }
        }

        log.info("Connected to serial device");
        int size = calculateBufferSize();
        log.info("Calculated buffer size: " + size);
        boolean acknowledged = false;

        try (BufferedInputStream bis = new BufferedInputStream(serialPort.getInputStream(), size)) {
            log.info("Using serial port: {}", serialPort);
            reset();
            setConnected(true);
            while (!isCancelled()) {
                if (bis.available() > 0) {
                    byte data = (byte) bis.read();
                    if (!acknowledged) {
                        log.debug("Not acknowledged...Waiting: {}", ByteUtils.toHexString(data));
                        if (data == MSG_REQ) {
                            log.info("Received request from client. Sending ACK");
                            serialPort.writeBytes(new byte[]{MSG_ACK}, 1);
                            //Wait until we have received a response form the client
                            int bRead;
                            while ((bRead = bis.available()) <= 0) {
                                log.debug("Waiting for response..");
                                Thread.sleep(1000);
                            }
                            log.debug("Response received. Acknowledging (Data: {}, Bytes: {})", ByteUtils.toHexString((byte) bis.read()), bRead);
                            acknowledged = true;
                            reset();
                        }
                        continue;
                    }
                    processByte(data);
                }
            }
        }

        log.info("Exiting Serial listen task");
    }
}
