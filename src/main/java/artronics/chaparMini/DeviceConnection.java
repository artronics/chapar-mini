package artronics.chaparMini;

import artronics.chaparMini.exceptions.ChaparConnectionException;

public interface DeviceConnection
{
    void connect() throws ChaparConnectionException;

    void start();
}
