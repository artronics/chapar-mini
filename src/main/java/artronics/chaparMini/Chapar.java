package artronics.chaparMini;

import artronics.chaparMini.broker.MessageToPacketConvertor;
import artronics.chaparMini.broker.MessageToPacketConvertorImpl;
import artronics.chaparMini.connection.Connection;
import artronics.chaparMini.connection.serialPort.SerialPortConnection;
import artronics.chaparMini.exceptions.ChaparConnectionException;
import org.apache.log4j.Logger;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Chapar implements DeviceConnection
{
    private final static Logger log = Logger.getLogger(DeviceConnection.class);

    private final static int START_BYTE = MessageToPacketConvertor.START_BYTE;
    private final static int STOP_BYTE = MessageToPacketConvertor.STOP_BYTE;
    private final static List<Integer> POISON_PILL = new ArrayList<>();

    private final BlockingQueue<List<Integer>> chaparRxQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<List<Integer>> chaparTxQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<List<Integer>> deviceRxQueue;
    private final BlockingQueue<List<Integer>> deviceTxQueue;

    private final DeviceConnectionConfig connectionConfig;

    private final Connection connection;
    private PacketLogger packetLogger;
    private final Runnable txListener = new Runnable()
    {
        @Override
        public void run()
        {
            while (true) {
                try {
                    final List<Integer> msg = chaparTxQueue.take();

                    if (msg == POISON_PILL) {
                        closeConnection();
                        break;
                    }

                    if (packetLogger != null) {
                        log.debug(packetLogger.logPacket(msg));
                    }

                    msg.add(0, START_BYTE);
                    msg.add(STOP_BYTE);

                    deviceTxQueue.add(msg);

                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };
    private MessageToPacketConvertor convertor = new MessageToPacketConvertorImpl();
    private final Runnable rxListener = new Runnable()
    {
        @Override
        public void run()
        {
            try {
                while (true) {
                    final List<Integer> msg = deviceRxQueue.take();

                    if (msg == POISON_PILL) {
                        closeConnection();
                        break;
                    }

                    final List<List<Integer>> messages = convertor.generatePackets(msg);

                    for (final List<Integer> msgI : messages) {
                        if (packetLogger != null) {
                            log.debug(packetLogger.logPacket(msgI));
                        }

                        chaparRxQueue.add(msgI);
                    }
                }
            }catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    public Chapar(Connection connection,
                  DeviceConnectionConfig connectionConfig,
                  PacketLogger packetLogger
                  )
    {
        this.connection = connection;
        this.deviceRxQueue = connection.getDeviceRx();
        this.deviceTxQueue = connection.getDeviceTx();

        this.connectionConfig = connectionConfig;

        this.packetLogger = packetLogger;
    }

    public Chapar(DeviceConnectionConfig connectionConfig)
    {
        this.connectionConfig = connectionConfig;

        this.connection = new SerialPortConnection();

        this.deviceRxQueue = connection.getDeviceRx();
        this.deviceTxQueue = connection.getDeviceTx();

        this.packetLogger = new ChaparPacketLogger();
    }

    @Override
    public void connect() throws ChaparConnectionException
    {
        String connectionString = connectionConfig.getConnectionString();

        if (connectionString == null) {
            throw new ChaparConnectionException("ConnectionString is null");
        }

        try {
            log.debug("Try to connect to device with ConnectionString: "+connectionString);
            connection.establishConnection(connectionString);
            connection.open();
            connection.start();

        }catch (ConnectException e) {
            e.printStackTrace();
            log.fatal("stop device");
            log.debug("List of available com ports:");
            for (String con:connection.getConnections().keySet())
                log.debug(con);
            throw new ChaparConnectionException("Can not connect to device", e);
        }
    }

    @Override
    public void start()
    {
        Thread chaparRxThr = new Thread(txListener, "Chapar TX");
        Thread chaparTxThr = new Thread(rxListener, "Chapar RX");
        chaparRxThr.start();
        chaparTxThr.start();
    }

    private void closeConnection()
    {
        connection.close();
    }

    @Override
    public BlockingQueue<List<Integer>> getChaparRxQueue()
    {
        return chaparRxQueue;
    }

    @Override
    public BlockingQueue<List<Integer>> getChaparTxQueue()
    {
        return chaparTxQueue;
    }

    @Override
    public void stop()
    {
        chaparTxQueue.add(POISON_PILL);
    }

    public void setPacketLogger(PacketLogger packetLogger)
    {
        this.packetLogger = packetLogger;
    }
}
