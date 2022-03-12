package com.desertkun.brainout.mode;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.client.RemoteClient;
import com.desertkun.brainout.common.ReflectionReceiver;
import com.desertkun.brainout.common.msg.ModeMessage;
import com.desertkun.brainout.common.msg.server.ModeUpdatedMsg;
import com.desertkun.brainout.content.shop.ShopCart;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.interfaces.Spawnable;
import com.desertkun.brainout.events.SimpleEvent;
import com.desertkun.brainout.gs.ActionPhaseState;
import com.desertkun.brainout.menu.impl.ActionPhaseMenu;
import com.desertkun.brainout.menu.impl.SpawnMenu;

public abstract class ClientRealization<G extends GameMode> extends GameModeRealization<G>
{
    private final ReflectionReceiver receiver;
    protected Table stats;
    protected Table topStats;
    protected Json json;

    public ClientRealization(G gameMode)
    {
        super(gameMode);

        this.receiver = new ReflectionReceiver();
        this.json = new Json();

        BrainOut.R.tag(json);
    }

    public boolean received(ModeMessage modeMessage)
    {
        return receiver.received(modeMessage, this);
    }

    public void showSpawnMenu(ActionPhaseState state, ShopCart shopCart,
                              SpawnMenu.Spawn spawn,
                              Spawnable lastSpawnPoint)
    {
        state.pushMenu(new SpawnMenu(shopCart, spawn, lastSpawnPoint));
    }

    @SuppressWarnings("unused")
    public boolean received(final ModeUpdatedMsg updatedMsg)
    {
        Gdx.app.postRunnable(() ->
        {
            getGameMode().read(json, new JsonReader().parse(updatedMsg.data));
            ((ClientRealization) getGameMode().getRealization()).updated();

            BrainOutClient.EventMgr.sendDelayedEvent(SimpleEvent.obtain(SimpleEvent.Action.modeUpdated));
        });

        return true;
    }

    public boolean forcePlayerList()
    {
        return false;
    }

    public boolean doDisplayPlayerBadge(PlayerData playerData)
    {
        return true;
    }

    protected void updated() {}

    public Class<? extends ClientMap> getMapClass()
    {
        return ClientMap.class;
    }

    public void init(ActionPhaseMenu menu)
    {
        //
    }

    public boolean isEnemies(RemoteClient a, RemoteClient b)
    {
        return false;
    }

    @Override
    public boolean isEnemies(int ownerA, int ownerB)
    {
        RemoteClient a = BrainOutClient.ClientController.getRemoteClients().get(ownerA);
        RemoteClient b = BrainOutClient.ClientController.getRemoteClients().get(ownerB);

        if (a == null || b == null)
            return true;

        return isEnemies(a, b) || getGameMode().isEnemies(a.getTeam(), b.getTeam());
    }

    public void postRender() {}

    public boolean canDropConsumable(ConsumableRecord record)
    {
        return true;
    }
}
