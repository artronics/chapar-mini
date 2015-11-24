package artronics.chaparMini.broker;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class MessageToPacketConvertorTest
{
    MessageToPacketConvertor convertor = new MessageToPacketConvertorImpl();
    List<List<Integer>> act = new ArrayList<>();
    List<Integer> expOnePck = new ArrayList<>();
    List<List<Integer>> expPcks = new ArrayList<>();

    FakeConnectionBuffer fakeBuffers = new FakeConnectionBuffer();
    List<Integer> goodPacket;

    List<Integer> firstOne = fakeBuffers.getFirstOne();
    List<Integer> secondOne = fakeBuffers.getSecondOne();
    List<Integer> twoPackets = fakeBuffers.getTwoPackets();
    List<Integer> withGibberish = fakeBuffers.getWithGibberish();
    List<Integer> withStartStop = fakeBuffers.getWithStartStop();
    List<Integer> withStartStop_anotherPacket = fakeBuffers.getWithStartStop_anotherPacket();
    List<Integer> firstHalf = fakeBuffers.getFirstHalf();
    List<Integer> secondHalf = fakeBuffers.getSecondHalf();
    List<Integer> fullHalf = fakeBuffers.getFullHalf();
    List<Integer> mixed = fakeBuffers.getMixed();

    private static List removeStartStop(List<Integer> packet)
    {
        List exp = packet.subList(1, packet.size() - 1);
        return exp;
    }

    @Before
    public void setUp() throws Exception
    {

    }

    @Test
    public void it_should_return_empty_list_if_there_is_no_packet_ready(){
        act = convertor.generatePackets(Arrays.asList(122,12,22));

        assertNotNull(act);
        assertThat(act.size(),equalTo(0));
    }

    @Test
    public void it_should_return_a_full_packet_as_it_is()
    {
        act = convertor.generatePackets(firstOne);
        expOnePck = removeStartStop(firstOne);

        assertEquals(expOnePck, act.get(0));
    }

    @Test
    public void As_long_as_queue_has_data_it_should_create_packets()
    {
        act = convertor.generatePackets(twoPackets);
        //first test the size
        assertThat(act.size(), equalTo(2));

        assertEquals(act.get(0), removeStartStop(firstOne));
        assertEquals(act.get(1), removeStartStop(secondOne));
    }

    @Test
    public void It_sould_ignore_un_approprite_bytes_before_start_and_after_stop()
    {
        act = convertor.generatePackets(withGibberish);

        assertThat(act.size(), equalTo(2));

        assertEquals(act.get(0), removeStartStop(firstOne));
        assertEquals(act.get(1), removeStartStop(secondOne));
    }

    @Test
    public void If_data_consists_of_start_and_stop_bytes_it_should_not_fuck_up()
    {
        act = convertor.generatePackets(withStartStop);

        assertThat(act.size(), equalTo(1));
        assertEquals(act.get(0), removeStartStop(withStartStop));

        //now lets add another packet to end of unibo
        act = convertor.generatePackets(withStartStop_anotherPacket);

        assertThat(act.size(), equalTo(2));
        assertEquals(act.get(0), removeStartStop(withStartStop));
        assertEquals(act.get(1), removeStartStop(firstOne));
    }

    @Test
    public void It_should_keep_track_of_previous_bytes()
    {
        //if a buffer consists of half of a message and another
        //received buffer consists of second half. unibo should create
        //a full packet.

        act = convertor.generatePackets(firstHalf);
        assertThat(act.size(), equalTo(0));

        act = convertor.generatePackets(secondHalf);
        assertThat(act.size(), equalTo(1));

        assertEquals(act.get(0), removeStartStop(fullHalf));
    }

    @Test
    public void Now_everything_together()
    {
        //now we send whole data and check if convertor gets us correct number of packets
        int totalNumberOfPackets = 6;
        act = convertor.generatePackets(mixed);

        assertThat(act.size(), equalTo(totalNumberOfPackets));
    }
}