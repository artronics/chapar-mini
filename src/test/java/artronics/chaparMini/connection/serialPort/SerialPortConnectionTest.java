package artronics.chaparMini.connection.serialPort;

import artronics.chaparMini.connection.Connection;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class SerialPortConnectionTest
{
    InputStream input = null;
    OutputStream output = null;
    SerialPortEventListener mockEventListener = new MockSerialPortEventListener();

    BlockingQueue<List<Integer>> deviceRx;
    BlockingQueue<List<Integer>> deviceTx;

    @Spy
    Connection connection = new SerialPortConnection(input, output);

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        Mockito.doNothing().when(connection).open();

        connection.establishConnection();
        connection.open();
        connection.start();

        deviceRx = connection.getDeviceRx();
        deviceTx = connection.getDeviceTx();
    }

    @Test
    public void it_should_send_msg_in_tx_queue()
    {
        deviceTx.add(Arrays.asList(122, 2, 3, 126));
    }
}

class MockSerialPortEventListener implements SerialPortEventListener
{

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent)
    {

    }
}