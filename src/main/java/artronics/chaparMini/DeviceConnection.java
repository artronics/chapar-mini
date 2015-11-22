package artronics.chaparMini;

import artronics.chaparMini.exceptions.ChaparConnectionException;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public interface DeviceConnection
{
    void connect() throws ChaparConnectionException;

    void start();

    void stop();

    BlockingQueue<List<Integer>> getTxQueue();

    BlockingQueue<List<Integer>> getRxQueue();
}
