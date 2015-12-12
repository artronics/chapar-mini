package artronics.chaparMini;

import artronics.chaparMini.broker.MessageToPacketConvertor;
import artronics.chaparMini.connection.Connection;
import artronics.chaparMini.connection.ConnectionStatusType;
import artronics.chaparMini.connection.serialPort.SerialPortConnection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;

public class ChaparTest
{
    //number of iteration for multiple send and receive.
    private final static int MAX_IT = 20;

    private final static int START_BYTE = MessageToPacketConvertor.START_BYTE;
    private final static int STOP_BYTE = MessageToPacketConvertor.STOP_BYTE;

    MockConnection mockConnection = new MockConnection();

    InputStream input;
    ByteArrayOutputStream output = new ByteArrayOutputStream();

    @Spy
    SerialPortConnection portConn = new SerialPortConnection(input, output);

    //    DeviceConnection chapar = new Chapar(mockConnection);
    DeviceConnection chapar;

    BlockingQueue<List<Integer>> chaparRx;
    BlockingQueue<List<Integer>> chaparTx;

    BlockingQueue<List<Integer>> deviceRx;
    BlockingQueue<List<Integer>> deviceTx;

    FakePacketFactory factory = new FakePacketFactory();
    List<Integer> actPacket;
    List<Integer> expPacket = new ArrayList<>();


    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);


        DeviceConnectionConfig config = new DeviceConnectionConfig("someString");
        chapar = new Chapar(portConn,config,new MockPacketLogger());

        chaparRx = chapar.getChaparRxQueue();
        chaparTx = chapar.getChaparTxQueue();

        deviceRx = portConn.getDeviceRx();
        deviceTx = portConn.getDeviceTx();

        Mockito.doNothing().when(portConn).open();
        Mockito.doNothing().when(portConn).establishConnection(any(String.class));

        chapar.connect();
        chapar.start();
    }

    @Test
    public void it_should_send_msg() throws InterruptedException
    {
        actPacket = factory.createRawDataPacket();
        chaparTx.add(actPacket);
        Thread.sleep(100);

        readFromBuffer();
        assertEquals(expPacket, actPacket);
    }

    @Test
    public void it_should_send_msg_if_there_are_more_than_one_msg_in_queue() throws
            InterruptedException
    {
        List<List<Integer>> actPackets = new ArrayList<>();
        actPacket = new ArrayList<>();

        //actPacket is a list consist of multiple packets. we
        //build this array by appending so we can assert with one statement.
        for (int i = 0; i < MAX_IT; i++) {
            //every time change the content of packet so we can detect errors.
            List<Integer> partialPck = new ArrayList<>(new ArrayList<>(Arrays.asList(2, i)));

            //append partial packet(which is well-constructed packet) to actPacket
            actPacket.addAll(addStartAndStopByte(partialPck));

            chaparTx.add(partialPck);
        }

        Thread.sleep(100);

        //Here buffer consists of all packets
        readFromBuffer();
        assertEquals(expPacket, actPacket);
    }

    @Test
    public void it_should_receive_msg() throws InterruptedException
    {
        actPacket = addStartAndStopByte(factory.createRawDataPacket());
        deviceRx.add(actPacket);

        expPacket = chaparRx.take();

        assertEquals(expPacket, removeStartAndStopByte(actPacket));
    }

    @Test
    public void it_should_receive_msg_if_there_are_more_than_one_msg_in_queue() throws
            InterruptedException
    {
        actPacket = new ArrayList<>();
        List<List<Integer>> actPackets = new ArrayList<>();

        for (int i = 0; i < MAX_IT; i++) {
            //every time change the content of packet so we can detect errors.
            List<Integer> partialPck = new ArrayList<>(new ArrayList<>(Arrays.asList(2, i)));

            //append partial packet(which is well-constructed packet) to actPacket
            actPackets.add(partialPck);

            deviceRx.add(addStartAndStopByte(partialPck));
        }

        Thread.sleep(200);

        Assert.assertThat(chaparRx.size(), equalTo(MAX_IT));

        for (int i = 0; i < MAX_IT; i++) {
            assertEquals(chaparRx.take(), actPackets.get(i));
        }
    }


    private List<Integer> removeStartAndStopByte(List<Integer> actPacket)
    {
        actPacket.remove(0);
        actPacket.remove(actPacket.size() - 1);
        return actPacket;
    }

    private void readFromBuffer()
    {
        int len = output.size();
        byte[] bytes = output.toByteArray();

        for (int i = 0; i < len; i++) {
            expPacket.add(bytes[i] & 0xFF);
        }
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
    public void establishConnection(String connectionString) throws ConnectException
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