package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.msg.server.CustomPlayerAnimationMsg;
import com.desertkun.brainout.common.msg.server.LaunchEffectMsg;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.ActiveProgressComponentData;
import com.desertkun.brainout.data.components.ServerFreeplayPlayerComponentData;
import com.desertkun.brainout.events.FreePlayItemActivatedEvent;
import com.desertkun.brainout.events.FreePlayItemUsedEvent;
import com.desertkun.brainout.mode.payload.FreePayload;
import com.desertkun.brainout.mode.payload.ModePayload;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ServerBleedingItemActivatorComponent")
public class ServerBleedingItemActivatorComponent extends ServerItemActivatorComponent
{
    private String effect;
    private String animation;

    @Override
    public boolean activate(PlayerClient playerClient, PlayerData playerData, int quality)
    {
        if (!playerData.isAlive())
            return false;

        ActiveProgressComponentData progress = playerData.getComponent(ActiveProgressComponentData.class);

        if (progress == null)
            return false;

        if (playerData.getCurrentInstrument() != null && playerData.getCurrentInstrument().isForceSelect())
        {
            return false;
        }

        ServerFreeplayPlayerComponentData fp = playerData.getComponent(ServerFreeplayPlayerComponentData.class);

        if (fp == null)
            return false;

        if (!fp.isBleeding())
            return false;

        if (progress.isRunning())
            return false;

        ModePayload payload = playerClient.getModePayload();
        if (!(payload instanceof FreePayload))
            return false;

        playerClient.enablePlayer(false);

        if (this.animation != null)
        {
            BrainOutServer.Controller.getClients().sendTCP(new CustomPlayerAnimationMsg(
                    playerData, animation, effect));
        }
        else
        {
            if (this.effect != null)
            {
                BrainOutServer.Controller.getClients().sendTCP(new LaunchEffectMsg(
                    playerData.getDimension(), playerData.getX(), playerData.getY(), effect
                ));
            }
        }

        FreePayload freePayload = ((FreePayload) payload);

        progress.startNonCancellable(getTime(), () ->
        {
            playerClient.addStat(getContent() + "-used", 1);

            if (getContent() instanceof ConsumableContent)
            {
                freePayload.questEvent(FreePlayItemUsedEvent.obtain(playerClient,
                    ((ConsumableContent) getContent()), 1));
            }

            playerClient.enablePlayer(true);
            fp.stopBleeding();
        });

        return true;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        this.effect = jsonData.getString("effect", null);
        this.animation = jsonData.getString("animation", null);
    }
}
