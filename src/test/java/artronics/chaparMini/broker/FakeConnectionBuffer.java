package artronics.chaparMini.broker;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FakeConnectionBuffer
{
    private static final int START_BYTE = 122;
    private static final int STOP_BYTE = 126;

    private List<Integer> firstOne;
    private List<Integer> secondOne;
    private List<Integer> twoPackets;
    private List<Integer> giberish;
    private List<Integer> withGibberish;
    private List<Integer> withStartStop;
    private List<Integer> withStartStop_anotherPacket;
    private List<Integer> firstHalf;
    private List<Integer> secondHalf;
    private List<Integer> fullHalf;
    private List<Integer> mixed;

    private List<List<Integer>> allBuffers = new ArrayList<>();

    public FakeConnectionBuffer()
    {
        Integer[] streamOfBytes =
                {
                        12, 34, 87, 89,//some giberish ;0-4
                        START_BYTE, 2, 23, STOP_BYTE, //first :4-8
                        678, 85, 73, 75, //again some giberish :8-12
                        START_BYTE, 2, 39, STOP_BYTE, //second :12-16
                        // an appropriate packet with stop and start bytes as data
                        START_BYTE, 3, START_BYTE, STOP_BYTE, STOP_BYTE, //16-21
                        START_BYTE, 2, 23, STOP_BYTE, //another packet after unibo, same as
                        // firstPacket 21-25
                        START_BYTE, 6, 45, 9, //A half packet 25-29
                        53, 74, 54, STOP_BYTE, //another half 29-33
                        //Notice that unibo does not validate your packet
                        //if your packet is longer that length your packet is
                        //malformed. if packet is shorter that length
                        //convertor conside stop byte as data.
                        START_BYTE, 1, 45, STOP_BYTE //Notice length is less than actual length
                };

        //for those packets which we want to assert we need to remove start and stop bytes
        //for first, second, and fullhalf i changed the index range. for others
        //see removeStartStop inside MessageToPacketConvertorImplTest method
        Integer[] giberishArr = Arrays.copyOfRange(streamOfBytes, 0, 4);
        Integer[] firstOneArr = Arrays.copyOfRange(streamOfBytes, 4, 8);
        Integer[] secondOneArr = Arrays.copyOfRange(streamOfBytes, 12, 16);
        Integer[] twoPacketsArr = Arrays.copyOfRange(streamOfBytes, 4, 16);
        Integer[] withGibberishArr = Arrays.copyOfRange(streamOfBytes, 0, 16);
        Integer[] withStartStopArr = Arrays.copyOfRange(streamOfBytes, 16, 21);
        Integer[] withStrStp_packet = Arrays.copyOfRange(streamOfBytes, 16, 25);
        Integer[] firstHalfArr = Arrays.copyOfRange(streamOfBytes, 25, 29);
        Integer[] secondHalfArr = Arrays.copyOfRange(streamOfBytes, 29, 33);
        Integer[] fullHalfArr = Arrays.copyOfRange(streamOfBytes, 25, 33);

        //These are the with start and stop
        Integer[] firstOneBuff = Arrays.copyOfRange(streamOfBytes, 4, 8);
        Integer[] secondOneBuff = Arrays.copyOfRange(streamOfBytes, 12, 16);


        firstOne = Arrays.asList(firstOneArr);
        secondOne = Arrays.asList(secondOneArr);
        twoPackets = Arrays.asList(twoPacketsArr);
        giberish = Arrays.asList(giberishArr);
        withGibberish = Arrays.asList(withGibberishArr);
        withStartStop = Arrays.asList(withStartStopArr);
        withStartStop_anotherPacket = Arrays.asList(withStrStp_packet);
        firstHalf = Arrays.asList(firstHalfArr);
        secondHalf = Arrays.asList(secondHalfArr);
        fullHalf = Arrays.asList(fullHalfArr);
        mixed = Arrays.asList(streamOfBytes);

        //all buffers consists of a list of pure data inside buffer.
        //I add them here to make it seperate and clear.
        //Well, it's supposed to make things easier.
        // now i need another test for this shit LOOL
        allBuffers.add(giberish);

        allBuffers.add(Arrays.asList(firstOneBuff));
        allBuffers.add(Arrays.asList(secondOneBuff));

        allBuffers.add(twoPackets);
        allBuffers.add(withGibberish);
        allBuffers.add(withStartStop);
        allBuffers.add(withStartStop_anotherPacket);
        allBuffers.add(firstHalf);
        allBuffers.add(secondHalf);
        allBuffers.add(fullHalf);
    }

    public List<List<Integer>> getAllBuffres()
    {
        return allBuffers;
    }

    //<editor-fold desc="getters">
    public List<Integer> getFirstOne()
    {
        return firstOne;
    }

    public List<Integer> getSecondOne()
    {
        return secondOne;
    }

    public List<Integer> getTwoPackets()
    {
        return twoPackets;
    }

    public List<Integer> getGiberish()
    {
        return giberish;
    }

    public List<Integer> getWithGibberish()
    {
        return withGibberish;
    }

    public List<Integer> getWithStartStop()
    {
        return withStartStop;
    }

    public List<Integer> getWithStartStop_anotherPacket()
    {
        return withStartStop_anotherPacket;
    }

    public List<Integer> getFirstHalf()
    {
        return firstHalf;
    }

    public List<Integer> getSecondHalf()
    {
        return secondHalf;
    }

    public List<Integer> getFullHalf()
    {
        return fullHalf;
    }

    public List<Integer> getMixed()
    {
        return mixed;
    }
    //</editor-fold>
}
