package artronics.chaparMini.connection.serialPort;

import artronics.chapar.core.events.Event;
import artronics.chapar.core.events.MessageReceivedEvent;
import artronics.chapar.device.Connection;
import artronics.chapar.device.ConnectionStatusType;
import gnu.io.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.TooManyListenersException;

import static artronics.chapar.device.ConnectionStatusType.*;

public class SerialPortConnection implements Connection, SerialPortEventListener
{
    private final Hashtable<String, CommPortIdentifier> ports = new Hashtable<>();

    private SerialPortSetting setting;

    private ConnectionStatusType status = CLOSED;

    private CommPortIdentifier commPortIdentifier;

    private SerialPort serialPort;

    private InputStream input = null;

    private OutputStream output = null;


    public SerialPortConnection(SerialPortSetting setting)
    {
        this.setting = setting;

        Enumeration portsEnum = CommPortIdentifier.getPortIdentifiers();

        while (portsEnum.hasMoreElements()) {
            CommPortIdentifier port = (CommPortIdentifier) portsEnum.nextElement();
            this.ports.put(port.getName(), port);
        }

        Event.mainBus().register(this);
    }

    @Override
    public void stablishConnection()
    {
        try {
            if (setting == null | setting.getComPort() == null) {
                status = CONNECTION_FAILED;

                throw new ConnectException("setting is null or SerialPort name is not specified");
            }

            commPortIdentifier = findSerialPort(setting.getComPort());

        }catch (ConnectException e) {
            e.printStackTrace();
        }
    }

    private CommPortIdentifier findSerialPort(String comPort) throws ConnectException
    {
        CommPortIdentifier sinkPort = null;
        if (this.ports.containsKey(comPort)) {
            sinkPort = (CommPortIdentifier) this.ports.get(comPort);

            return sinkPort;
        }else {
            status = CONNECTION_FAILED;
            throw new ConnectException("There is no com port available with provided setting");
        }
    }

    @Override
    public void open()
    {
        status=CONNECTING;

        final CommPort commPort;
        SerialPort serialPort = null;

        try {
            commPort = commPortIdentifier.open("SinkPort", TIMEOUT);
            //the CommPort object can be casted to a SerialPort object
            serialPort = (SerialPort) commPort;

            serialPort.setSerialPortParams(setting.getBaudrate(),
                                           SerialPort.DATABITS_8,
                                           SerialPort.STOPBITS_1,
                                           SerialPort.PARITY_NONE);


        }catch (PortInUseException e) {
//            Log.main().error("Port in use. Make sure there is no other app using this com port.");
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }

        if (serialPort != null) {
            this.serialPort = serialPort;
            initEventListenersAndIO();
        }else
            throw new NullPointerException();
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

        status=CLOSED;
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
                Integer maxPacketLength = setting.getMaxPacketLength();
                final byte[] buff = new byte[maxPacketLength];
                final int length = input.read(buff, 0, maxPacketLength);
                ArrayList<Integer> intBuff = new ArrayList<>(maxPacketLength);
                for (int i = 0; i < length; i++) {
                    //convert signed value to unsigned
                    intBuff.add(buff[i] & 0xFF);
                }

                MessageReceivedEvent event = new MessageReceivedEvent(intBuff);
                Event.mainBus().post(event);

            }catch (IOException e) {
                e.printStackTrace();
            }
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
