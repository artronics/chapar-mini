package artronics.chaparMini.connection.serialPort;

import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class SerialRxEvent implements SerialPortEventListener
{
    private static final int MAX_PACKET_LENGTH = 255;

    private final InputStream input;
    private final BlockingQueue<List<Integer>> deviceRx;

    public SerialRxEvent(InputStream input,
                         BlockingQueue<List<Integer>> deviceRx)
    {
        this.input = input;
        this.deviceRx = deviceRx;
    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent)
    {
        if (serialPortEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                final byte[] buff = new byte[MAX_PACKET_LENGTH];
                final int length = input.read(buff, 0, MAX_PACKET_LENGTH);
                ArrayList<Integer> intBuff = new ArrayList<>(length);
                for (int i = 0; i < length; i++) {
                    //convert signed value to unsigned
                    intBuff.add(buff[i] & 0xFF);
                }

                deviceRx.add(intBuff);

            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
