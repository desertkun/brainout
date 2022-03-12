package com.desertkun.brainout.client.states.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.states.CSClientInit;
import com.desertkun.brainout.client.states.CSError;
import com.desertkun.brainout.client.states.ControllerState;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.mode.ClientRealization;
import com.desertkun.brainout.playstate.PlayStateGame;

import java.io.InputStream;

public class CSMapLoad extends ControllerState
{
    private PlayerData playerData;

    @SuppressWarnings("unchecked")
    public CSMapLoad(InputStream inputStream)
    {
        PlayStateGame game = ((PlayStateGame) BrainOutClient.ClientController.getPlayState());

        ClientRealization clientRealization =
                ((ClientRealization) game.getMode().getRealization());
        Class<ClientMap> mapClass = clientRealization.getMapClass();

        Gdx.app.postRunnable(() -> loadMap(inputStream, mapClass));
    }

    private void loadMap(InputStream inputStream, Class<ClientMap> mapClass)
    {
        Map.Dispose();

        final Array<ClientMap> maps = BrainOut.loadMapsFromStream(inputStream, mapClass);

        Gdx.app.postRunnable(() -> mapsLoaded(maps));
    }

    @Override
    public ID getID()
    {
        return ID.mapLoad;
    }

    @Override
    public void init()
    {
    }

    private void mapsLoaded(Array<ClientMap> maps)
    {
        if (maps == null)
        {
            switchTo(new CSError(L.get("ERROR_MAP_LOAD")));
            return;
        }

        for (ClientMap map : maps)
        {
            map.init();
        }

        switchTo(new CSClientInit(playerData));
    }

    @Override
    public void release()
    {

    }

    public void setPlayerData(PlayerData playerData)
    {
        this.playerData = playerData;
    }
}
