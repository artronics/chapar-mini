package artronics.chaparMini;

import artronics.chaparMini.connection.Connection;
import artronics.chaparMini.connection.serialPort.SerialPortConnection;
import artronics.chaparMini.exceptions.ChaparConnectionException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class ChaparMain
{
    public static void main(String[] args) throws InterruptedException, ChaparConnectionException
    {
        DeviceConnectionConfig config = new DeviceConnectionConfig("/dev/tty.usbserial-AH00WG8Y");
        Connection connection = new SerialPortConnection();
        DeviceConnection chapar = new Chapar(connection,config,new ChaparPacketLogger());
        BlockingQueue rx = chapar.getChaparRxQueue();
        BlockingQueue tx = chapar.getChaparTxQueue();
        chapar.connect();
        chapar.start();
        FakeSdwnMsgFactory factory = new FakeSdwnMsgFactory();

        Thread.sleep(3000);

        List<Integer> dataPck = factory.createRawDataPacket(0, 30, 10);
        tx.add(dataPck);
        Thread.sleep(6000);

        List<Integer> opPck = factory.createRawOpenPathPacket(0, 30, Arrays.asList(0, 30));
        tx.add(opPck);
        Thread.sleep(1000);

        List<Integer> payload = Arrays.asList(0,1,2,3,4,5,6,7,8,9);
        int counter=0;
        for (int i = 0; i < 1000; i++) {
            payload.set(9,counter++);
            if (counter == 255)
                counter=0;

            List<Integer> dataPacket = factory.createRawDataPacket(0, 30, payload);
            List<Integer> packet = new ArrayList<>(dataPacket);
            tx.add(packet);
            dataPacket.clear();
            Thread.sleep(2000);
        }
    }
}
