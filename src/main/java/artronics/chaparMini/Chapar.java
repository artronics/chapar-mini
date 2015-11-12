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
import java.util.LinkedList;
import java.util.List;

public class Chapar implements Runnable
{
    private final LinkedList<List<Integer>> receivedBuffer;
    private final LinkedList<List<Integer>> transmitBuffer;

    private Connection serialConnection;

    private MessageToPacketConvertor convertor = new MessageToPacketConvertorImpl();

    public Chapar(LinkedList<List<Integer>> receivedBuffer,
                  LinkedList<List<Integer>> transmitBuffer)
    {
        this.receivedBuffer = receivedBuffer;
        this.transmitBuffer = transmitBuffer;

        this.serialConnection = new SerialPortConnection();

        Event.CHAPAR_BUS.register(this);
    }

    public void connect() throws ChaparConnectionException
    {
        try {
            serialConnection.stablishConnection();
            serialConnection.open();

        }catch (ConnectException e) {
            e.printStackTrace();
            throw new ChaparConnectionException("Can not connect to device",e);
        }
    }

    @Subscribe
    public void recievePacketHandler(MessageReceivedEvent event)
    {
        List<List<Integer>> messages = convertor.generatePackets(event.getPacket());

        for (List<Integer> msg : messages) {
            receivedBuffer.add(msg);
        }
    }

    @Override
    public void run()
    {
        while (!transmitBuffer.isEmpty()) {
            Event.CHAPAR_BUS.post(transmitBuffer.peekLast());
        }
    }
}
