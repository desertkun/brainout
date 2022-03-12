package com.desertkun.brainout.vote;

public class ServerVotes extends Votes
{
    public static ServerVote NewVote(ID id, String data)
    {
        switch (id)
        {
            case endgame:
            {
                return new SVEndGame(data);
            }
        }

        return null;
    }
}
