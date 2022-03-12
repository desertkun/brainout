package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.msg.server.CustomPlayerAnimationMsg;
import com.desertkun.brainout.common.msg.server.LaunchEffectMsg;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.ActiveProgressComponentData;
import com.desertkun.brainout.data.components.FreeplayPlayerComponentData;
import com.desertkun.brainout.data.components.HealthComponentData;
import com.desertkun.brainout.data.components.PlayerBoostersComponentData;
import com.desertkun.brainout.events.FreePlayItemUsedEvent;
import com.desertkun.brainout.mode.payload.FreePayload;
import com.desertkun.brainout.mode.payload.ModePayload;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ServerBoosterActivatorComponent")
public class ServerBoosterActivatorComponent extends ServerItemActivatorComponent
{
    private String effect;
    private String animation;

    public ServerBoosterActivatorComponent()
    {
    }

    @Override
    public boolean activate(PlayerClient playerClient, PlayerData playerData, int quality)
    {
        BoosterActivatorComponent booster = getContent().getComponent(BoosterActivatorComponent.class);

        if (!playerData.isAlive())
            return false;

        if (playerData.isWounded())
            return false;

        if (playerData.getCurrentInstrument() != null && playerData.getCurrentInstrument().isForceSelect())
        {
            return false;
        }

        ActiveProgressComponentData progress = playerData.getComponent(ActiveProgressComponentData.class);

        if (progress == null)
            return false;

        boolean canAddUpHunger = false;

        HealthComponentData health = playerData.getComponent(HealthComponentData.class);

        if (health == null)
            return false;

        FreeplayPlayerComponentData fp = playerData.getComponent(FreeplayPlayerComponentData.class);

        if (fp != null)
        {
            canAddUpHunger = booster.getHunger(quality) > 0 && !fp.isHungerMax();

            if (booster.isFixBones() && fp.hasBonesBroken())
            {
                canAddUpHunger = true;
            }

            if (booster.getRadio(quality) > 0 && fp.getRadio() > 0)
            {
                canAddUpHunger = true;
            }

            if (booster.getTemp(quality) > 0 && !fp.isTemperatureMax())
            {
                canAddUpHunger = true;
            }

            if (!fp.isThirstMax() && booster.getThirst(quality) > 0)
            {
                canAddUpHunger = true;
            }

            if (!health.isMaxHealth() && booster.getHealth(quality) > 0)
            {
                canAddUpHunger = true;
            }
        }

        PlayerBoostersComponentData bst = playerData.getComponent(PlayerBoostersComponentData.class);

        boolean haveAll = true;

        for (ObjectMap.Entry<String, BoosterActivatorComponent.BoosterActivator> entry : booster.getBoosters())
        {
            if (bst.hasBooster(entry.key))
                continue;

            haveAll = false;
            break;
        }

        if (haveAll && !canAddUpHunger)
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

            freePayload.questEvent(FreePlayItemUsedEvent.obtain(playerClient,
                getContent(), 1));

            playerClient.enablePlayer(true);

            if (fp != null)
            {
                if (booster.isFixBones())
                {
                    fp.setBonesBroken(false);
                }

                float t_ = booster.getTemp(quality);
                if (t_ > 0)
                {
                    fp.setTemperature(fp.getTemperature() + t_);
                }
                fp.refillThirst(booster.getThirst(quality));
                fp.refillHunger(booster.getHunger(quality));
                float r_ = booster.getRadio(quality);
                if (r_ > 0)
                {
                    fp.removeRadio(r_);
                }
                fp.sync();
            }

            float h_ = booster.getHealth(quality);
            if (h_ > 0)
            {
                health.addHealth(h_);
            }

            health.updated(playerData);

            for (ObjectMap.Entry<String, BoosterActivatorComponent.BoosterActivator> entry : booster.getBoosters())
            {
                bst.setBooster(entry.key, entry.value.value, booster.getBoosterDuration(entry.value.duration, quality));
            }
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
