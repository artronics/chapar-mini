package artronics.chaparMini.connection;


import java.util.List;

public class MessageToPacketConvertorImpl implements MessageToPacketConvertor
{
    private int thisPacketExpectedSize = 0;
    private boolean isStarted = false;


    @Override
    public List<List<Integer>> generatePackets(List receivedData)
    {
        return createPackets(receivedData);
    }

    protected List<List<Integer>> createPackets(List receivedData)
    {
        return null;
    }
}
