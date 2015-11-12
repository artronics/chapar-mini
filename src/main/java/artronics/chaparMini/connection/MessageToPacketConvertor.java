package artronics.chaparMini.connection;

import java.util.List;

public interface MessageToPacketConvertor
{
    List<List<Integer>> generatePackets(List receivedData);
}
