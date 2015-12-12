package artronics.chaparMini;


public class DeviceConnectionConfig
{
    String connectionString;

    public DeviceConnectionConfig(String connectionString)
    {
        this.connectionString = connectionString;
    }

    public String getConnectionString()
    {
        return connectionString;
    }
}
