package com.desertkun.brainout.vote;

import com.desertkun.brainout.L;

public class CVEndGame extends SimpleClientVote
{
    @Override
    public String getTitle()
    {
        return L.get("MENU_VOTE_SKIP_THIS_GAME");
    }
}
