package com.desertkun.brainout.data.components;

import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.msg.server.LaunchEffectMsg;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.content.components.ServerPortalComponent;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.ServerMap;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.active.PortalData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.containers.ChunkData;
import com.desertkun.brainout.data.interfaces.WithTag;
import com.desertkun.brainout.events.ActivateActiveEvent;
import com.desertkun.brainout.events.EnterPortalEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.esotericsoftware.minlog.Log;

@Reflect("ServerPortalComponent")
@ReflectAlias("data.components.ServerPortalComponentData")
public class ServerPortalComponentData extends Component<ServerPortalComponent> implements WithTag
{
    private final PortalData portal;
    private PortalData otherPortal;

    public ServerPortalComponentData(PortalData portal,
                                     ServerPortalComponent contentComponent)
    {
        super(portal, contentComponent);

        this.portal = portal;
    }

    @Override
    public boolean hasRender()
    {
        return false;
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
                enter(ev.client, ev.playerData);
                return true;
            }
        }

        return false;
    }

    public void enter(Client client, PlayerData playerData)
    {
        PlayerClient playerClient = client instanceof PlayerClient ? ((PlayerClient) client) : null;

        if (playerClient == null)
        {
            if (Log.INFO) Log.info("playerClient == null");
        }

        final PortalData otherPortal = findOtherPortal();

        if (otherPortal == null)
        {
            if (playerClient != null) playerClient.log("Portal not found!");
            return;
        }

        float enterTime = 0.5f;
        boolean locked = portal.isLocked();

        String effect = getContentComponent().getActivateEffect();
        boolean neededKey = false;

        if (locked)
        {
            ConsumableContent key = portal.getKey();

            PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

            if (poc == null)
            {
                if (playerClient != null) playerClient.log("Missing poc");
                return;
            }

            if (!poc.getConsumableContainer().hasConsumable(key))
            {
                if (playerClient != null) playerClient.log("Doesn't have key required: " + key.getID());
                return;
            }

            enterTime = 3.0f;
            neededKey = true;

            if (!getContentComponent().getLockedActivateEffect().isEmpty())
            {
                effect = getContentComponent().getLockedActivateEffect();
            }
        }

        if (allowEffect(portal.getDimension(), portal))
        {
            if (!effect.isEmpty())
            {
                BrainOutServer.Controller.getClients().sendUDP(
                    new LaunchEffectMsg(portal.getDimension(), portal.getX(), portal.getY(), effect));
            }
        }
        else
        {
            if (!effect.isEmpty() && playerClient != null)
            {
                playerClient.sendUDP(
                    new LaunchEffectMsg(portal.getDimension(), portal.getX(), portal.getY(), effect));
            }
        }

        ActiveProgressComponentData progress = playerData.getComponent(ActiveProgressComponentData.class);

        if (progress == null)
        {
            if (playerClient != null) playerClient.log("Missing progress");
            return;
        }

        if (progress.isRunning())
        {
            if (playerClient != null) playerClient.log("Progress is running");
            return;
        }

        {
            PlayerOwnerComponent own =
                    playerData.getComponent(PlayerOwnerComponent.class);

            if (own == null)
            {
                if (playerClient != null) playerClient.log("Missing poc2");
                return;
            }

            ServerPlayerControllerComponentData ctr =
                    playerData.getComponentWithSubclass(ServerPlayerControllerComponentData.class);

            if (ctr != null)
            {
                ctr.setEnabled(false);
            }

            own.setEnabled(false);
        }

        SimplePhysicsComponentData phy = playerData.getComponentWithSubclass(SimplePhysicsComponentData.class);

        if (phy != null)
        {
            phy.getSpeed().set(0, 0);

            ServerPlayerControllerComponentData playerController =
                playerData.getComponentWithSubclass(ServerPlayerControllerComponentData.class);

            if (playerController != null)
            {
                playerController.sendPlayerData(false, 0);
            }
        }

        if (locked && portal.isAllowSneak())
        {
            portal.setLocked(false);
            portal.updated();
        }

        if (neededKey && playerClient != null)
        {
            playerClient.addStat("enter-locked-doors", 1);
        }

        Runnable finally_ = () ->
        {
            {
                PlayerOwnerComponent own =
                        playerData.getComponent(PlayerOwnerComponent.class);

                if (own == null)
                {
                    if (playerClient != null) playerClient.log("Missing poc3");
                    return;
                }

                ServerPlayerControllerComponentData ctr =
                        playerData.getComponentWithSubclass(ServerPlayerControllerComponentData.class);

                if (ctr != null)
                {
                    ctr.setEnabled(true);
                }

                own.setEnabled(true);
            }

            if (locked && portal.isAllowSneak())
            {
                portal.setLocked(true);
                portal.updated();
            }

            ServerPortalComponentData otherPortalComponent = otherPortal.getComponent(ServerPortalComponentData.class);

            if (allowEffect(otherPortal.getDimension(), otherPortal))
            {
                if (otherPortalComponent != null)
                {
                    if (!otherPortalComponent.getContentComponent().getActivateEffect().isEmpty())
                    {
                        BrainOutServer.Controller.getClients().sendUDPExcept(
                            new LaunchEffectMsg(
                                otherPortal.getDimension(),
                                otherPortal.getX(), otherPortal.getY(),
                                otherPortalComponent.getContentComponent().getActivateEffect()), playerClient);
                    }
                } else
                {
                    BrainOutServer.Controller.getClients().sendUDPExcept(
                        new LaunchEffectMsg(
                            otherPortal.getDimension(),
                            otherPortal.getX(), otherPortal.getY(),
                            getContentComponent().getActivateEffect()), playerClient);
                }
            }

            BrainOutServer.EventMgr.sendDelayedEvent(EnterPortalEvent.obtain(
                playerData, otherPortal, portal
            ));

        };

        if (playerClient != null) playerClient.log("Entering portal");

        progress.startCancellable(enterTime, () ->
        {
            float x = otherPortal.getX(), y = otherPortal.getY();

            SimplePhysicsComponentData pcd = playerData.getComponentWithSubclass(SimplePhysicsComponentData.class);

            if (pcd != null)
            {
                y -= (3.0 - pcd.getSize().y) / 2.0f;
            }

            if (playerClient != null)
            {
                playerClient.moveTo(otherPortal.getDimension(), x, y);
            }
            else
            {
                playerData.setPosition(x, y);

                if (!otherPortal.getDimension().equals(playerData.getDimension()))
                {
                    Map map = Map.Get(otherPortal.getDimension());
                    int newId = map.generateServerId();
                    playerData.setDimension(newId, otherPortal.getDimension());
                }

            }

            HealthComponentData hcd = playerData.getComponent(HealthComponentData.class);
            if (hcd != null)
            {
                hcd.setImmortalTime(0.25f);
            }

            finally_.run();
        }, finally_);

    }

    @Override
    public void init()
    {
        super.init();

        otherPortal = findOtherPortal();
    }

    public PortalData getOtherPortal()
    {
        return otherPortal;
    }

    public PortalData findOtherPortal()
    {
        String tag = portal.tag;

        if (tag == null || tag.isEmpty())
            return null;

        PortalData otherPortal_;

        for (ObjectMap.Entry<String, Map> entry : new ObjectMap.Entries<>(Map.AllEntries()))
        {
            Map map = entry.value;

            otherPortal_ = (PortalData)map.getActiveForTag_(Constants.ActiveTags.PORTAL, activeData ->
            {
                if (activeData == portal)
                    return false;

                if (!(activeData instanceof PortalData))
                    return false;

                PortalData portalData = ((PortalData) activeData);

                return portalData.tag.equals(tag);
            });

            if (otherPortal_ != null)
                return otherPortal_;
        }

        return null;
    }

    private boolean allowEffect(String dimension, ActiveData at)
    {
        ServerMap map = Map.Get(dimension, ServerMap.class);

        if (map == null)
            return false;

        ChunkData chunk = map.getChunkAt((int)at.getX(), (int)at.getY());

        return chunk == null || !chunk.hasFlag(ChunkData.ChunkFlag.hideOthers);
    }

    @Override
    public int getTags()
    {
        return WithTag.TAG(Constants.ActiveTags.PORTAL);
    }
}
