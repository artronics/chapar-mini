package artronics.chaparMini.connection;

import java.net.ConnectException;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public interface Connection
{
    int TIMEOUT = 2000;

    void establishConnection() throws ConnectException;

    void open() throws ConnectException;

    void start();

    void close();

    BlockingQueue<List<Integer>> getDeviceRx();

    BlockingQueue<List<Integer>> getDeviceTx();

    ConnectionStatusType getStatus();

    Hashtable<String,?> getConnections();
}
