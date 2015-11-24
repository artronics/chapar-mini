package artronics.chaparMini;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * It creates a fake SdwnPacket of all types The default convention for non-parameter methods is as
 * follow: src is always 30, dst is always 0, payload for data is from 0 to payload length and for
 * default length is 10,
 */
public class FakeSdwnMsgFactory
{
    List<Integer> packet = new ArrayList<>();
    List<Integer> header = new ArrayList<>();

    public static int getHighAddress(int address)
    {
        int addH = (address & 0x0000FF00) >> 8;

        return addH;
    }

    public static int getLowAddress(int address)
    {
        int addL = address & 0x000000FF;

        return addL;
    }

    private List<Integer> createHeader()
    {
        return createHeader(10, SdwnPacketType.DATA, 30, 0);
    }

    private List<Integer> createHeader(int len, SdwnPacketType type, int src, int dst)
    {
        Integer[] bytes = {
                len,//length
                1, //NetId
                getHighAddress(src),
                getLowAddress(src),

                getHighAddress(dst),
                getLowAddress(dst),
                type.getValue(),//type
                20, //MAX_TTL
                0,//next hop H
                0,//next hop L
        };
        header = Arrays.asList(bytes);

        return new ArrayList<>(header);
    }

    public List<Integer> createRawReportPacket(int src, int dst, List<Integer> neighbors)
    {
        return createRawReportPacket(src, dst, 1, 255, neighbors);
    }

    public List<Integer> createRawReportPacket(int src, int dst, int dis, int bat,
                                               List<Integer> neighbors)
    {
        List<Integer> header = createHeader(10 + 3 + neighbors.size(),
                                            SdwnPacketType.REPORT,
                                            src, dst);

        List<Integer> extra = new ArrayList<>();
        extra.add(dis);//distance
        extra.add(bat);//battery
        extra.add(neighbors.size() / 3);//number of neighbors

        packet = new ArrayList<>(header);
        packet.addAll(extra);
        packet.addAll(neighbors);

        return packet;
    }

    public List<Integer> createRawReportPacket()
    {
        List<Integer> neighbors = createNeighbors(35, 36, 37);

        return createRawReportPacket(30, 0, 1, 255, neighbors);
    }

    public List<Integer> createNeighbors(int... addrs)
    {
        List<Integer> neighbors = new ArrayList<>();
        Random rn = new Random();

        for (int addr : addrs) {
            neighbors.add(getHighAddress(addr));
            neighbors.add(getLowAddress(addr));
            neighbors.add(rn.nextInt(255)); //random rssi
        }

        return neighbors;
    }

    public List<Integer> createRawDataPacket(int src, int dst)
    {
        return createRawDataPacket(src, dst, 10);
    }

    public List<Integer> createRawDataPacket(int src, int dst, int payloadLen)
    {
        List<Integer> header = createHeader(10 + payloadLen,
                                            SdwnPacketType.DATA, src, dst);

        List<Integer> packet = new ArrayList<>(header);
        for (int i = 0; i < payloadLen; i++) {
            packet.add(i);
        }

        return new ArrayList<>(packet);
    }

    public List<Integer> createRawDataPacket()
    {
        return createRawDataPacket(30, 0, 10);
    }

    public List<Integer> createRawRuleRequestPacket(int src, int dst, int echoPayloadLen)
    {
        List<Integer> packet = createHeader(10 + echoPayloadLen,
                                            SdwnPacketType.RULE_REQUEST,
                                            src,
                                            dst);
        List<Integer> payload = new ArrayList<>();

        for (int i = 0; i < echoPayloadLen; i++) {
            payload.add(i);
        }

        packet.addAll(payload);

        return packet;
    }

    public List<Integer> createRawOpenPathPacket(int src, int dst, List<Integer> path)
    {
        List<Integer> packet = createHeader(10 + path.size() * 2,
                                            SdwnPacketType.OPEN_PATH,
                                            src,
                                            dst);
        List<Integer> payload = new ArrayList<>();

        for (int i = 0; i < path.size(); i++) {
            payload.add(i * 2, getHighAddress(path.get(i)));
            payload.add(i * 2 + 1, getLowAddress(path.get(i)));
        }

        packet.addAll(payload);

        return packet;
    }
}
