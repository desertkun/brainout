package com.desertkun.brainout.data.components;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.msg.server.CustomPlayerAnimationMsg;
import com.desertkun.brainout.common.msg.server.LaunchEffectMsg;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.active.Active;
import com.desertkun.brainout.content.components.CampFireStarterComponent;
import com.desertkun.brainout.content.components.ServerCampfireActivatorComponent;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.content.consumable.impl.DecayConsumableItem;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.consumable.ConsumableContainer;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.events.ActivateActiveEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.FreePlayItemUsedEvent;
import com.desertkun.brainout.mode.payload.FreePayload;
import com.desertkun.brainout.mode.payload.ModePayload;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ServerCampfireActivatorComponent")
@ReflectAlias("data.components.ServerCampfireActivatorComponentData")
public class ServerCampfireActivatorComponentData extends Component<ServerCampfireActivatorComponent>
{
    public ServerCampfireActivatorComponentData(
        ComponentObject componentObject, ServerCampfireActivatorComponent contentComponent)
    {
        super(componentObject, contentComponent);
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    public boolean activate(Client client, PlayerData playerData)
    {
        if (!playerData.isAlive())
            return false;

        if (!getMap().getDimension().equals("default") &&
            !getMap().getDimension().equals("swamp2") &&
            !getMap().getDimension().equals("forest"))
        {
            return false;
        }

        if (playerData.getCurrentInstrument() != null && playerData.getCurrentInstrument().isForceSelect())
        {
            return false;
        }

        PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

        if (poc == null)
            return false;

        ConsumableContainer cnt = poc.getConsumableContainer();
        ConsumableRecord activator = null;

        for (ObjectMap.Entry<Integer, ConsumableRecord> entry : cnt.getData())
        {
            ConsumableRecord r = entry.value;

            if (r.getItem().getContent().hasComponent(CampFireStarterComponent.class))
            {
                activator = r;
                break;
            }
        }

        if (activator == null)
            return false;

        if (activator.getItem() instanceof DecayConsumableItem)
        {
            ((DecayConsumableItem) activator.getItem()).use(cnt, activator);
            if (client instanceof PlayerClient)
            {
                ((PlayerClient) client).sendConsumable(cnt);
            }
        }

        boolean success = MathUtils.random(100) <= activator.getQuality();

        ActiveProgressComponentData progress = playerData.getComponent(ActiveProgressComponentData.class);

        if (progress == null)
            return false;

        if (progress.isRunning())
            return false;

        if (client != null)
        {
            client.enablePlayer(false);
        }

        BrainOutServer.Controller.getClients().sendTCP(new CustomPlayerAnimationMsg(
            playerData, getContentComponent().getAnimation(), getContentComponent().getEffect()));

        Content finalActivator = activator.getItem().getContent();
        progress.startNonCancellable(getContentComponent().getTime(), () ->
        {
            if (client instanceof PlayerClient)
            {
                PlayerClient playerClient = ((PlayerClient) client);
                ModePayload payload = playerClient.getModePayload();
                if (payload instanceof FreePayload)
                {
                    FreePayload freePayload = ((FreePayload) payload);
                    freePayload.questEvent(FreePlayItemUsedEvent.obtain(playerClient, finalActivator, 1));
                }
            }

            if (client != null)
            {
                client.enablePlayer(true);
            }

            ActiveData me = ((ActiveData) getComponentObject());
            Map map = me.getMap();

            if (success && map.getActives().get(me.getId()) != null)
            {
                BrainOutServer.Controller.getClients().sendTCP(new LaunchEffectMsg(
                    me.getDimension(), me.getX(), me.getY(), getContentComponent().getSuccessEffect()));

                Active fire = BrainOutServer.ContentMgr.get(getContentComponent().getActive(), Active.class);
                ActiveData fireData = fire.getData(map.getDimension());
                fireData.setPosition(me.getX(), me.getY() - .5f);

                map.removeActive(me, true, true, false);
                map.addActive(map.generateServerId(), fireData, true, true);
            }
        });

        return true;
    }

    @Override
    public boolean hasUpdate()
    {
        return false;
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case activeActivateData:
            {
                ActivateActiveEvent ev = ((ActivateActiveEvent) event);
                return activate(ev.client, ev.playerData);
            }
        }

        return false;
    }
}
