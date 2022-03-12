package com.desertkun.brainout.vote;

import com.badlogic.gdx.utils.ObjectMap;

public class ClientVotes extends Votes
{
    private static ObjectMap<ID, ClientVote> votes = new ObjectMap<ID, ClientVote>();

    static
    {
        votes.put(ID.endgame, new CVEndGame());
    }

    public static ClientVote NewVote(ID id)
    {
        return votes.get(id);
    }

    public static ObjectMap<ID, ClientVote> GetVotes()
    {
        return votes;
    }
}
