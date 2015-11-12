package artronics.chaparMini.broker;

import java.util.List;

public interface MessageToPacketConvertor
{
    int START_BYTE = 122;
    int STOP_BYTE = 126;

    List<List<Integer>> generatePackets(List receivedData);
}
