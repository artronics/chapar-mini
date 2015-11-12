package artronics.chaparMini;

import artronics.chaparMini.connection.Connection;
import artronics.chaparMini.connection.serialPort.SerialPortConnection;
import artronics.chaparMini.exceptions.ChaparConnectionException;

import java.net.ConnectException;
import java.util.LinkedList;

public class Chapar implements Runnable
{
    private final LinkedList<Integer> receivedBuffer;
    private final LinkedList<Integer> transmitBuffer;

    private final Connection serialConnection = new SerialPortConnection();

    public Chapar(LinkedList<Integer> receivedBuffer,
                  LinkedList<Integer> transmitBuffer)
    {
        this.receivedBuffer = receivedBuffer;
        this.transmitBuffer = transmitBuffer;
        Log.CHAPAR.debug("kirrrrr");
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

    @Override
    public void run()
    {

    }
}
