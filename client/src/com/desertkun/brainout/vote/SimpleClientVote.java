package com.desertkun.brainout.vote;

public abstract class SimpleClientVote extends ClientVote
{
    @Override
    public void show(SelectedCallback callback)
    {
        callback.selected("");
    }
}
