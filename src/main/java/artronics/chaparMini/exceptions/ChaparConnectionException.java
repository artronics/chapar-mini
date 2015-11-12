package artronics.chaparMini.exceptions;

public class ChaparConnectionException extends Exception
{
    public ChaparConnectionException()
    {
    }

    public ChaparConnectionException(String message)
    {
        super(message);
    }

    public ChaparConnectionException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ChaparConnectionException(Throwable cause)
    {
        super(cause);
    }

    public ChaparConnectionException(String message, Throwable cause, boolean enableSuppression,
                                     boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
