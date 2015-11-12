package artronics.chaparMini;

import artronics.chaparMini.exceptions.ChaparConnectionException;

import java.util.LinkedList;

public class Chapar implements Runnable
{
    private final LinkedList<Integer> receivedBuffer;
    private final LinkedList<Integer> transmitBuffer;

    public Chapar(LinkedList<Integer> receivedBuffer,
                  LinkedList<Integer> transmitBuffer)
    {
        this.receivedBuffer = receivedBuffer;
        this.transmitBuffer = transmitBuffer;
    }

    public void connect() throws ChaparConnectionException
    {

    }

    @Override
    public void run()
    {

    }
}
