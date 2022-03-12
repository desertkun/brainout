package com.desertkun.brainout.client.states;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.client.ClientController;
import com.desertkun.brainout.common.ReflectionReceiver;
import com.desertkun.brainout.common.msg.server.ServerDisconnect;
import com.desertkun.brainout.data.interfaces.RenderContext;

public abstract class ControllerState extends ReflectionReceiver
{
    public enum ID
    {
        connecting, error, packagesLoad, connected,
        mapDownload, mapLoad, game, clientInit,
        endGame, postGame, onlineInit, joinLobby, none,
        empty, multipleAccounts, loading, validate, findLobby, waitForUser,
        maintenance, reconnect, steamInit, steamUserInit, steamStats, joinRoom,
        getRegions, gameOutdated, freeplaySolo, quickPlay, privacy, getBlogUpdates,
        duel
    }

    public ControllerState()
    {
    }

    public ClientController getController()
    {
        return BrainOutClient.ClientController;
    }

    public abstract ID getID();

    public abstract void init();
    public abstract void release();

    public void update(float dt) {}
    public void render(Batch batch, RenderContext renderContext) {}
    public void postRender() {}
    public void preRender() {}

    protected void switchTo(ControllerState newState)
    {
        getController().setState(newState);
    }

    @SuppressWarnings("unused")
    public boolean received(ServerDisconnect msg)
    {
        getController().setDisconnectReason(msg.reason);

        return true;
    }
}
