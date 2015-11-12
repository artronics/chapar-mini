package artronics.chaparMini.events;

import java.util.List;

public class BaseEvent
{
    private List<Integer> packet;

    public BaseEvent()
    {
    }

    public BaseEvent(List<Integer> packet)
    {
        this.packet = packet;
    }

    public List<Integer> getPacket()
    {
        return packet;
    }

    public void setPacket(List<Integer> packet)
    {
        this.packet = packet;
    }
}
