package artronics.chaparMini.events;

import java.util.List;

public class TransmitMessageEvent extends BaseEvent
{
    public TransmitMessageEvent(List<Integer> packet)
    {
        super(packet);
    }
}
