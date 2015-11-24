package artronics.chaparMini;

import artronics.chaparMini.broker.MessageToPacketConvertor;
import artronics.chaparMini.connection.Connection;
import artronics.chaparMini.connection.ConnectionStatusType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class ChaparTest
{
    private final static int START_BYTE = MessageToPacketConvertor.START_BYTE;
    private final static int STOP_BYTE = MessageToPacketConvertor.STOP_BYTE;

    MockConnection mockConnection = new MockConnection();
    //    @Spy
//    SerialPortConnection portConn = new SerialPortConnection();
    DeviceConnection chapar = new Chapar(mockConnection);
//    DeviceConnection chapar = new Chapar(portConn);

    BlockingQueue<List<Integer>> chaparRx;
    BlockingQueue<List<Integer>> chaparTx;

    BlockingQueue<List<Integer>> deviceRx;
    BlockingQueue<List<Integer>> deviceTx;

    FakePacketFactory factory = new FakePacketFactory();
    List<Integer> actPacket;
    List<Integer> expPacket;


    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);

        chaparRx = chapar.getRxQueue();
        chaparTx = chapar.getTxQueue();

        deviceRx = mockConnection.getDeviceRx();
        deviceTx = mockConnection.getDeviceTx();

//        Mockito.doNothing().when(portConn).open();

        chapar.connect();
        chapar.start();
    }

    @Test
    public void it_should_send_msg() throws InterruptedException
    {
        actPacket = factory.createRawDataPacket();
        chaparTx.add(actPacket);

        Thread.sleep(300);
        List<List<Integer>> messages;
        messages = mockConnection.getMessages();

        assertThat(messages.size(), equalTo(1));

        expPacket = addStartAndStopByte(actPacket);
        assertEquals(expPacket, actPacket);
    }

    private List<Integer> addStartAndStopByte(List<Integer> msg)
    {
        List<Integer> packet = new ArrayList<>(msg);
        packet.add(0, START_BYTE);
        packet.add(STOP_BYTE);

        return packet;
    }
}

class MockConnection implements Connection
{
    List<List<Integer>> messages = new ArrayList<>();
    private BlockingQueue<List<Integer>> deviceRx = new LinkedBlockingQueue<>();
    private BlockingQueue<List<Integer>> deviceTx = new LinkedBlockingQueue<>();
    private final Runnable transmitter = new Runnable()
    {
        @Override
        public void run()
        {
            try {
                while (true) {
                    List<Integer> msg = deviceTx.take();
                    messages.add(msg);
                }

            }catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    public List<List<Integer>> getMessages()
    {
        return messages;
    }

    @Override
    public void establishConnection() throws ConnectException
    {

    }

    @Override
    public void open() throws ConnectException
    {
        Thread tx = new Thread(transmitter, "deviceTX");
        tx.start();
    }

    @Override
    public void start()
    {

    }

    @Override
    public void close()
    {

    }

    public BlockingQueue<List<Integer>> getDeviceRx()
    {
        return deviceRx;
    }

    public BlockingQueue<List<Integer>> getDeviceTx()
    {
        return deviceTx;
    }

    @Override
    public ConnectionStatusType getStatus()
    {
        return null;
    }

    @Override
    public Hashtable<String, ?> getConnections()
    {
        return null;
    }
}