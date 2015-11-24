package artronics.chaparMini;

public enum SdwnPacketType
{
    DATA(0),
    MALFORMED(1),
    REPORT(2),
    BEACON(3),
    RULE_REQUEST(128),
    RULE_RESPONSE(4),
    OPEN_PATH(5);

    private int value;

    SdwnPacketType(int value)
    {
        this.value = value;
    }

    public int getValue()
    {
        return value;
    }
}
