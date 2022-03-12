package com.desertkun.brainout.client.states;

import com.badlogic.gdx.Gdx;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.common.msg.server.NotifyMsg;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.events.NotifyEvent;
import com.desertkun.brainout.gs.EndGameState;
import com.desertkun.brainout.playstate.ClientPSEndGame;

public class CSEndGame extends ControllerState
{
    private final ClientPSEndGame clientPSEndGame;
    private final CSGame csGame;

    public CSEndGame(ClientPSEndGame clientPSEndGame, CSGame csGame)
    {
        this.clientPSEndGame = clientPSEndGame;
        this.csGame = csGame;
    }

    @SuppressWarnings("unused")
    public boolean received(final NotifyMsg notifyMsg)
    {
        Gdx.app.postRunnable(() -> BrainOut.EventMgr.sendEvent(NotifyEvent.obtain(notifyMsg.notifyAward,
                notifyMsg.amount, notifyMsg.reason,
                notifyMsg.method, notifyMsg.data)));

        return true;
    }

    @Override
    public ID getID()
    {
        return ID.endGame;
    }

    @Override
    public void init()
    {
        BrainOutClient.getInstance().switchState(new EndGameState(clientPSEndGame));

        Gdx.app.postRunnable(() -> BrainOutClient.MusicMng.playMusic("music-endgame"));
    }

    @Override
    public void release()
    {
        //
    }

    public CSGame getCsGame()
    {
        return csGame;
    }
}
