package artronics.chaparMini;

import java.util.List;

public class ChaparPacketLogger implements PacketLogger
{
    @Override
    public String logPacket(List<Integer> packetContent)
    {
        String s = "";
        for (int data : packetContent) {
            s += String.format("%-3d", data);
            s += " ,";
        }
        return s;
    }
}
