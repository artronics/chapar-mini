package artronics.chaparMini;

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
        receivedBuffer.add(event.getPacket());
    }

    @Override
    public void run()
    {

    }
}
