package artronics.chaparMini.connection.serialPort;

import artronics.chapar.core.config.Config;
import artronics.chapar.core.config.ConfigType;

import java.io.Serializable;

public class SerialPortSetting implements Serializable
{
    private Integer startByte = Integer.parseInt(Config.get(ConfigType.START_BYTE));
    private Integer stopByte= Integer.parseInt(Config.get(ConfigType.STOP_BYTE));
    private Integer maxPacketLength= Integer.parseInt(Config.get(ConfigType.MAX_PACKET_LENGTH));
    private Integer baudrate= Integer.parseInt(Config.get(ConfigType.BAUDRATE));
    private String comPort = Config.get(ConfigType.COM_PORT);

    public SerialPortSetting()
    {
    }

    public SerialPortSetting(Integer startByte, Integer stopByte, Integer maxPacketLength,
                             Integer baudrate, String comPort)
    {
        this.startByte = startByte;
        this.stopByte = stopByte;
        this.maxPacketLength = maxPacketLength;
        this.baudrate = baudrate;
        this.comPort = comPort;
    }

    public String getComPort()
    {
        return comPort;
    }

    public void setComPort(String comPort)
    {
        this.comPort = comPort;
    }

    public Integer getMaxPacketLength()
    {
        return maxPacketLength;
    }

    public void setMaxPacketLength(Integer maxPacketLength)
    {
        this.maxPacketLength = maxPacketLength;
    }

    public Integer getStopByte()
    {
        return stopByte;
    }

    public void setStopByte(Integer stopByte)
    {
        this.stopByte = stopByte;
    }

    public Integer getStartByte()
    {
        return startByte;
    }

    public void setStartByte(Integer startByte)
    {
        this.startByte = startByte;
    }

    public Integer getBaudrate()
    {
        return baudrate;
    }

    public void setBaudrate(Integer baudrate)
    {
        this.baudrate = baudrate;
    }
}
