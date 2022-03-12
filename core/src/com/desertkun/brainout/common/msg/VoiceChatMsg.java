package com.desertkun.brainout.common.msg;

public class VoiceChatMsg implements UdpMessage
{
    public int id;
    public int object;
    public int d;
    public short[] data;
    public float volume;

    public VoiceChatMsg() {}
    public VoiceChatMsg(int id, int object, int dimension, short[] data, float volume)
    {
        this.id = id;
        this.object = object;
        this.d = dimension;
        this.data = data;
        this.volume = volume;
    }

    public VoiceChatMsg(short[] data, float volume)
    {
        this.id = -1;
        this.object = -1;
        this.d = -1;
        this.data = data;
        this.volume = volume;
    }
}
