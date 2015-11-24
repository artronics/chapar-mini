package artronics.chaparMini;

import artronics.chaparMini.broker.MessageToPacketConvertor;
import artronics.chaparMini.broker.MessageToPacketConvertorImpl;
import artronics.chaparMini.connection.Connection;
import artronics.chaparMini.exceptions.ChaparConnectionException;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Chapar implements DeviceConnection
{
    private final static int START_BYTE = MessageToPacketConvertor.START_BYTE;
    private final static int STOP_BYTE = MessageToPacketConvertor.STOP_BYTE;

    private final static List<Integer> POISON_PILL = new ArrayList<>();

    private final BlockingQueue<List<Integer>> rxQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<List<Integer>> txQueue = new LinkedBlockingQueue<>();

    private final BlockingQueue<List<Integer>> deviceRxQueue;
    private final BlockingQueue<List<Integer>> deviceTxQueue;

    private final Connection connection;
    private final Runnable txListener = new Runnable()
    {
        @Override
        public void run()
        {
            while (true) {
                try {

                    List<Integer> msg = new ArrayList<>(txQueue.take());

                    if (msg == POISON_PILL) {
                        closeConnection();
                        break;
                    }

                    msg.add(0, START_BYTE);
                    msg.add(STOP_BYTE);

                    deviceTxQueue.add(msg);

                    for (int data : msg)
                        System.out.print(data + " ,");
                    System.out.println();

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
                    List<Integer> msg = deviceRxQueue.take();
                    if (msg == POISON_PILL) {
                        closeConnection();
                        break;
                    }
                    List<List<Integer>> messages = convertor.generatePackets(msg);

                    for (List<Integer> msgI : messages) {
                        rxQueue.add(msgI);
                        for (int data : msgI)
                            System.out.print(data + " ,");
                        System.out.println();
                    }
                }
            }catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    };

    public Chapar(Connection connection)
    {
        this.connection = connection;
        this.deviceRxQueue = connection.getDeviceRx();
        this.deviceTxQueue = connection.getDeviceTx();
    }

    @Override
    public void connect() throws ChaparConnectionException
    {
        try {
            connection.establishConnection();
            connection.open();

        }catch (ConnectException e) {
            e.printStackTrace();
            throw new ChaparConnectionException("Can not connect to device", e);
        }
    }

    @Override
    public void start()
    {
        Thread chaparRxThr = new Thread(txListener, "ChaparTx");
        Thread chaparTxThr = new Thread(rxListener, "ChaparRx");
        chaparRxThr.start();
        chaparTxThr.start();
    }

    private void closeConnection()
    {
        connection.close();
    }

    @Override
    public BlockingQueue<List<Integer>> getRxQueue()
    {
        return rxQueue;
    }

    @Override
    public BlockingQueue<List<Integer>> getTxQueue()
    {
        return txQueue;
    }

    @Override
    public void stop()
    {
        txQueue.add(POISON_PILL);
    }
}
