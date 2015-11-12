package artronics.chaparMini.connection.serialPort;


import artronics.chaparMini.broker.MessageToPacketConvertor;
import artronics.chaparMini.connection.Connection;
import artronics.chaparMini.connection.ConnectionStatusType;
import artronics.chaparMini.events.Event;
import artronics.chaparMini.events.MessageReceivedEvent;
import artronics.chaparMini.events.TransmitMessageEvent;
import com.google.common.eventbus.Subscribe;
import gnu.io.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.util.*;

import static artronics.chaparMini.connection.ConnectionStatusType.*;

public class SerialPortConnection implements Connection, SerialPortEventListener
{
    private final static int START_BYTE = MessageToPacketConvertor.START_BYTE;
    private final static int STOP_BYTE = MessageToPacketConvertor.STOP_BYTE;
    private static final String COM_PORT = "/dev/tty.usbserial-AH00WG8Y";
    private static final int MAX_PACKET_LENGTH = 255;
    private static final int BAUDRATE = 115200;

    private final Hashtable<String, CommPortIdentifier> ports = new Hashtable<>();

    private ConnectionStatusType status = CLOSED;

    private CommPortIdentifier commPortIdentifier;

    private SerialPort serialPort;

    private InputStream input = null;

    private OutputStream output = null;


    public SerialPortConnection()
    {

        Enumeration portsEnum = CommPortIdentifier.getPortIdentifiers();

        while (portsEnum.hasMoreElements()) {
            CommPortIdentifier port = (CommPortIdentifier) portsEnum.nextElement();
            this.ports.put(port.getName(), port);
        }

        Event.CHAPAR_BUS.register(this);
    }

    @Override
    public void stablishConnection() throws ConnectException
    {
        commPortIdentifier = findSerialPort(COM_PORT);
    }

    private CommPortIdentifier findSerialPort(String comPort) throws ConnectException
    {
        CommPortIdentifier sinkPort = null;
        if (this.ports.containsKey(comPort)) {
            sinkPort = this.ports.get(comPort);

            return sinkPort;
        }else {
            status = CONNECTION_FAILED;
            throw new ConnectException("There is no com port available with provided setting");
        }
    }

    @Override
    public void open() throws ConnectException
    {
        status = CONNECTING;

        final CommPort commPort;
        SerialPort serialPort = null;

        try {
            commPort = commPortIdentifier.open("SinkPort", TIMEOUT);
            //the CommPort object can be casted to a SerialPort object
            serialPort = (SerialPort) commPort;

            serialPort.setSerialPortParams(BAUDRATE,
                                           SerialPort.DATABITS_8,
                                           SerialPort.STOPBITS_1,
                                           SerialPort.PARITY_NONE);


        }catch (Exception e) {
            e.printStackTrace();
            throw new ConnectException("Can not open the connection");
        }

        if (serialPort != null) {
            this.serialPort = serialPort;
            initEventListenersAndIO();
        }else
            throw new ConnectException("Can not open the connection");
    }

    @Override
    public void close()
    {
        try {

            serialPort.close();
            input.close();
            output.close();

        }catch (IOException e) {
            e.printStackTrace();
        }

        status = CLOSED;
    }

    @Override
    public String toString()
    {
        return commPortIdentifier.getName();
    }

    private void initEventListenersAndIO()
    {
        try {

            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
            input = serialPort.getInputStream();
            output = serialPort.getOutputStream();

        }catch (TooManyListenersException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void serialEvent(SerialPortEvent serialPortEvent)
    {
        if (serialPortEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                final byte[] buff = new byte[MAX_PACKET_LENGTH];
                final int length = input.read(buff, 0, MAX_PACKET_LENGTH);
                ArrayList<Integer> intBuff = new ArrayList<>(length);
                for (int i = 0; i < length; i++) {
                    //convert signed value to unsigned
                    intBuff.add(buff[i] & 0xFF);
                }

                MessageReceivedEvent event = new MessageReceivedEvent(intBuff);
                Event.CHAPAR_BUS.post(event);

            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Subscribe
    public void transmitMessageHandler(TransmitMessageEvent event)
    {
        List<Integer> msg = event.getPacket();
        msg.add(0, START_BYTE);
        msg.add(STOP_BYTE);

        try {
            for (int i = 0; i < msg.size(); i++) {
                output.write(msg.get(i));
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Hashtable<String, CommPortIdentifier> getConnections()
    {
        return this.ports;
    }

    @Override
    public ConnectionStatusType getStatus()
    {
        return status;
    }

}
