package com.desertkun.brainout.common.msg.client;

import com.desertkun.brainout.common.msg.UdpMessage;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.interfaces.LaunchData;

public class ThrowableLaunchMsg implements UdpMessage
{
    public float x;
    public float y;
    public float angle;
    public int recordId;

    public ThrowableLaunchMsg() {}
    public ThrowableLaunchMsg(LaunchData launchData, ConsumableRecord record)
    {
        this.x = launchData.getX();
        this.y = launchData.getY();
        this.angle = launchData.getAngle();

        this.recordId = record.getId();
    }
}
