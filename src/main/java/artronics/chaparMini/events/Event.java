package artronics.chaparMini.events;

import com.google.common.eventbus.EventBus;

public class Event
{
    private static final EventBus MAIN_BUS = new EventBus();

    protected Event()
    {
    }

    public static EventBus mainBus()
    {
        return MAIN_BUS;
    }
}
