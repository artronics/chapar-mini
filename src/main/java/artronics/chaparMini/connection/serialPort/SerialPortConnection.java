package artronics.chaparMini.connection.serialPort;


import artronics.chaparMini.connection.Connection;
import artronics.chaparMini.connection.ConnectionStatusType;
import artronics.chaparMini.exceptions.DeviceConnectionException;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static artronics.chaparMini.connection.ConnectionStatusType.*;

public class SerialPortConnection implements Connection
{
    private final static List<Integer> POISON_PILL = new ArrayList<>();

    private static final String COM_PORT = "/dev/tty.usbserial-AH00WG8Y";
    private static final int BAUDRATE = 115200;

    private final BlockingQueue<List<Integer>> deviceRx = new LinkedBlockingQueue<>();
    private final BlockingQueue<List<Integer>> deviceTx = new LinkedBlockingQueue<>();

    private final Hashtable<String, CommPortIdentifier> ports = new Hashtable<>();
    private ConnectionStatusType status = CLOSED;

    private CommPortIdentifier commPortIdentifier;
    private SerialPort serialPort;

    private InputStream input = null;
    private OutputStream output = null;
    private final Runnable transmitter = new Runnable()
    {
        @Override
        public void run()
        {
            try {
                while (true) {
                    List<Integer> msg = deviceTx.take();

                    if (msg == POISON_PILL) {
                        break;
                    }

                    for (int i = 0; i < msg.size(); i++) {
                        output.write(msg.get(i));
                    }
                }

            }catch (InterruptedException e) {
                e.printStackTrace();
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
    private SerialPortEventListener serialPortEventListener =
            new SerialRxEvent(input, deviceRx);

    public SerialPortConnection(InputStream input, OutputStream output)
    {
        this.input = input;
        this.output = output;

        getAllPorts();
    }

    public SerialPortConnection()
    {
        getAllPorts();
    }

    private void getAllPorts()
    {
        Enumeration portsEnum = CommPortIdentifier.getPortIdentifiers();

        while (portsEnum.hasMoreElements()) {
            CommPortIdentifier port = (CommPortIdentifier) portsEnum.nextElement();
            this.ports.put(port.getName(), port);
        }
    }

    @Override
    public void establishConnection() throws ConnectException
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
            throw new DeviceConnectionException("Can not open the connection");
        }

        if (serialPort != null) {
            this.serialPort = serialPort;
            initEventListenersAndIO();

        }else
            throw new DeviceConnectionException("Can not open the connection");
    }

    @Override
    public void start()
    {
        Thread tx = new Thread(transmitter, "deviceTX");
        tx.start();
    }

    @Override
    public void close()
    {
        try {

            serialPort.close();
            input.close();
            output.close();

            deviceTx.add(POISON_PILL);

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
            input = serialPort.getInputStream();
            output = serialPort.getOutputStream();
            serialPort.addEventListener(new SerialRxEvent(input, deviceRx));
            serialPort.notifyOnDataAvailable(true);

        }catch (TooManyListenersException e) {
            e.printStackTrace();
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

    @Override
    public BlockingQueue<List<Integer>> getDeviceRx()
    {
        return deviceRx;
    }

    @Override
    public BlockingQueue<List<Integer>> getDeviceTx()
    {
        return deviceTx;
    }
}
