package artronics.chaparMini.broker;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MessageToPacketConvertorImpl implements MessageToPacketConvertor
{
    private final LinkedList<Integer> dataQueue = new LinkedList<>();
    private final ArrayList<Integer> thisPacket = new ArrayList<>();
    private final List<List<Integer>> generatedPcks = new ArrayList<>();
    private int thisPacketExpectedSize = 0;
    private boolean isStarted = false;

    @Override
    public List<List<Integer>> generatePackets(List<Integer> receivedData)
    {
        return createPackets(receivedData);
    }

    protected List<List<Integer>> createPackets(List receivedData)
    {
        dataQueue.addAll(receivedData);
        while (!dataQueue.isEmpty()) {
            int thisData = dataQueue.removeFirst();

            if (thisData == START_BYTE
                    && !isStarted
                    && thisPacketExpectedSize == 0) {
                isStarted = Boolean.TRUE;

            }else if (isStarted) {
                if (thisPacketExpectedSize == 0) {
                    thisPacket.add(thisData);
                    thisPacketExpectedSize = thisData;

                }else if (thisPacket.size() < thisPacketExpectedSize) {
                    thisPacket.add(thisData);

                }else if (thisData == STOP_BYTE
                        && thisPacket.size() == thisPacketExpectedSize) {
                    ArrayList<Integer> newPacket = new ArrayList<>(thisPacket);
                    generatedPcks.add(newPacket);
                    thisPacket.clear();
                    isStarted = Boolean.FALSE;
                    thisPacketExpectedSize = 0;
                }
            }
        }

        List<List<Integer>> tmp = new ArrayList<>(generatedPcks);
        generatedPcks.clear();

        return tmp;
    }
}
