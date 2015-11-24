package artronics.chaparMini.exceptions;

public class DeviceConnectionException extends RuntimeException
{
    public DeviceConnectionException()
    {
    }

    public DeviceConnectionException(String message)
    {
        super(message);
    }

    public DeviceConnectionException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public DeviceConnectionException(Throwable cause)
    {
        super(cause);
    }

    public DeviceConnectionException(String message, Throwable cause, boolean enableSuppression,
                                     boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
