package com.desertkun.brainout.client.states;

import com.badlogic.gdx.Gdx;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.common.msg.client.SimpleMsg;
import com.desertkun.brainout.common.msg.server.TeamChanged;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.active.PlayerData;

public class CSClientInit extends ControllerState
{
    private PlayerData playerData;

    public CSClientInit(PlayerData playerData)
    {
        this.playerData = playerData;
    }

    @Override
    public ID getID()
    {
        return ID.clientInit;
    }

    @SuppressWarnings("unused")
    public boolean received(final TeamChanged teamChanged)
    {
        Gdx.app.postRunnable(() -> clientInited(teamChanged.teamId));

        return true;
    }

    private void clientInited(String teamId)
    {
        Team team = ((Team) BrainOut.ContentMgr.get(teamId));
        switchTo(new CSGame(team, playerData));
    }

    @Override
    public void init()
    {
        getController().sendTCP(new SimpleMsg(SimpleMsg.Code.mapInited));
    }

    @Override
    public void release()
    {
        this.playerData = null;
    }
}
