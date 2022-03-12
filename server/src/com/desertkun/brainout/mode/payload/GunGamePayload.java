package com.desertkun.brainout.mode.payload;

import com.desertkun.brainout.client.Client;

public class GunGamePayload extends ModePayload
{
    private int rank;
    private int kills;

    public GunGamePayload(Client playerClient, int rank)
    {
        super(playerClient);

        this.rank = rank;
    }

    public int getRank()
    {
        return rank;
    }

    public boolean promote(boolean knife)
    {
        if (knife)
        {
            rank++;
            kills = 0;
            return true;
        }
        else
        {
            kills++;

            if (kills >= 2)
            {
                rank++;
                kills = 0;
                return true;
            }
        }

        return false;
    }

    public boolean demote()
    {
        if (rank <= 0)
            return false;

        rank--;
        kills = 1;

        return true;
    }

    @Override
    public void init()
    {

    }

    @Override
    public void release()
    {

    }
}
