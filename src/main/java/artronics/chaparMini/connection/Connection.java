package artronics.chaparMini.connection;

import java.net.ConnectException;
import java.util.Hashtable;

public interface Connection
{
    int TIMEOUT = 2000;

    void stablishConnection() throws ConnectException;

    void open() throws ConnectException;

    void close();

    ConnectionStatusType getStatus();

    Hashtable<String,?> getConnections();
}
