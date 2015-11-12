package artronics.chaparMini.connection;

import java.util.Hashtable;

public interface Connection
{
    int TIMEOUT = 2000;

    void stablishConnection();

    void open();

    void close();

    ConnectionStatusType getStatus();

    Hashtable<String,?> getConnections();
}
