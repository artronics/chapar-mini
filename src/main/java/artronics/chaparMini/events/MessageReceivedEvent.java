package artronics.chaparMini.events;

import java.util.List;

public class MessageReceivedEvent extends BaseEvent
{
    public MessageReceivedEvent(List<Integer> packet)
    {
        super(packet);
    }
}
