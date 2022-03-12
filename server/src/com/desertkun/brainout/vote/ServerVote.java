package com.desertkun.brainout.vote;

public abstract class ServerVote extends Vote
{
    private String data;

    public ServerVote(String data)
    {
        this.data = data;
    }

    public interface ApplyCallback
    {
        void success();
        void failed(String reason);
    }

    public String getData()
    {
        return data;
    }

    public abstract void apply(ApplyCallback applyCallback);

    public abstract void approved();
}
