package artronics.chaparMini;

import artronics.chaparMini.broker.MessageToPacketConvertor;
import artronics.chaparMini.broker.MessageToPacketConvertorImpl;
import artronics.chaparMini.connection.Connection;
import artronics.chaparMini.connection.serialPort.SerialPortConnection;
import artronics.chaparMini.events.Event;
import artronics.chaparMini.events.MessageReceivedEvent;
import artronics.chaparMini.events.TransmitMessageEvent;
import artronics.chaparMini.exceptions.ChaparConnectionException;
import com.google.common.eventbus.Subscribe;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Chapar implements DeviceConnection
{
    private final static List<Integer> POISON_PILL = new ArrayList<>();

    private final BlockingQueue<List<Integer>> rxMessages = new LinkedBlockingQueue<>();

    private final BlockingQueue<List<Integer>> txMessages = new LinkedBlockingQueue<>();

    private final Connection serialConnection = new SerialPortConnection();

    private MessageToPacketConvertor convertor = new MessageToPacketConvertorImpl();


    public Chapar()
    {
        Event.CHAPAR_BUS.register(this);
    }

    @Override
    public void connect() throws ChaparConnectionException
    {
        try {
            serialConnection.stablishConnection();
            serialConnection.open();

        }catch (ConnectException e) {
            e.printStackTrace();
            throw new ChaparConnectionException("Can not connect to device", e);
        }
    }

    @Override
    public void start()
    {

        Thread chaparThr = new Thread(
                new Runnable()
                {
                    @Override
                    public void run()
                    {
                        while (true) {
                            try {

                                List<Integer> msg = txMessages.take();

                                if (msg == POISON_PILL) {
                                    closeConnection();
                                    break;
                                }

                                TransmitMessageEvent event = new TransmitMessageEvent(msg);
                                Event.CHAPAR_BUS.post(event);

                            }catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                , "Chapar"); //Thread name

        chaparThr.start();
    }

    private void closeConnection()
    {
        serialConnection.close();
    }

    public BlockingQueue<List<Integer>> getRxMessages()
    {
        return rxMessages;
    }

    public BlockingQueue<List<Integer>> getTxMessages()
    {
        return txMessages;
    }

    @Subscribe
    public void recievePacketHandler(MessageReceivedEvent event)
    {
        List<List<Integer>> messages = convertor.generatePackets(event.getPacket());

        for (List<Integer> msg : messages) {
            rxMessages.add(msg);
        }
    }

    public void stop()
    {
        txMessages.add(POISON_PILL);
    }
}
