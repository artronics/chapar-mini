package artronics.chaparMini;

import artronics.chaparMini.broker.MessageToPacketConvertor;
import artronics.chaparMini.broker.MessageToPacketConvertorImpl;
import artronics.chaparMini.connection.Connection;
import artronics.chaparMini.connection.serialPort.SerialPortConnection;
import artronics.chaparMini.events.Event;
import artronics.chaparMini.events.MessageReceivedEvent;
import artronics.chaparMini.exceptions.ChaparConnectionException;
import com.google.common.eventbus.Subscribe;

import java.net.ConnectException;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class Chapar implements Runnable
{
    private final BlockingQueue<List<Integer>> rxMessages;

    private final BlockingQueue<List<Integer>> txMessages;

    private final Connection serialConnection = new SerialPortConnection();

    private MessageToPacketConvertor convertor = new MessageToPacketConvertorImpl();

    public Chapar(BlockingQueue<List<Integer>> rxMessage, BlockingQueue<List<Integer>> txMessage)
    {
        this.rxMessages = rxMessage;
        this.txMessages = txMessage;

        Event.CHAPAR_BUS.register(this);
    }

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

    @Subscribe
    public void recievePacketHandler(MessageReceivedEvent event)
    {
        List<List<Integer>> messages = convertor.generatePackets(event.getPacket());

        for (List<Integer> msg : messages) {
            rxMessages.add(msg);
            for (Integer byt : msg) {
                System.out.print(byt + " ");
            }
            System.out.println();
        }
    }

    @Override
    public void run()
    {
        try {
            while (true) {
                while (!txMessages.isEmpty()) {
                    Event.CHAPAR_BUS.post(txMessages.take());
                }
            }

        }catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stop()
    {
    }
}
