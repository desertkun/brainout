package com.desertkun.brainout.vote;

import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.playstate.PlayStateGame;
import com.desertkun.brainout.playstate.ServerPSGame;

public class SVEndGame extends SimpleServerVote
{
    public SVEndGame(String data)
    {
        super(data);
    }

    @Override
    public void apply(ApplyCallback applyCallback)
    {
        PlayState playState = BrainOutServer.Controller.getPlayState();

        if (!(playState instanceof ServerPSGame))
        {
            applyCallback.failed("MENU_GAME_IS_ABOUT_TO_END");
            return;
        }

        ServerPSGame serverPSGame = ((ServerPSGame) playState);

        GameMode mode = serverPSGame.getMode();

        if (mode.isAboutToEnd())
        {
            applyCallback.failed("MENU_GAME_IS_ABOUT_TO_END");
            return;
        }

        applyCallback.success();
    }

    @Override
    public void approved()
    {
        PlayState playState = BrainOutServer.Controller.getPlayState();

        if (playState instanceof ServerPSGame)
        {
            ServerPSGame game = ((ServerPSGame) playState);
            game.getGameResult().setTeamWon(null);
            game.getGameResult().setPlayerWon(-1);
            game.endGame();
        }
    }
}
