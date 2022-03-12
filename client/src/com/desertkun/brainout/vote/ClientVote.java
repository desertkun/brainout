package com.desertkun.brainout.vote;

public abstract class ClientVote extends Vote
{
    public abstract String getTitle();

    public interface SelectedCallback
    {
        void selected(String data);
    }

    public abstract void show(SelectedCallback callback);
}
