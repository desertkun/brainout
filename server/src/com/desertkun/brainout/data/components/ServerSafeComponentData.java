package com.desertkun.brainout.data.components;

import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.msg.server.LaunchEffectMsg;
import com.desertkun.brainout.content.components.ServerSafeComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.active.PortalData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.interfaces.WithTag;
import com.desertkun.brainout.events.ActivateActiveEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.esotericsoftware.minlog.Log;

@Reflect("ServerSafeComponent")
@ReflectAlias("data.components.ServerSafeComponentData")
public class ServerSafeComponentData extends Component<ServerSafeComponent> implements WithTag
{
    private final PortalData portal;
    private String code;

    public ServerSafeComponentData(PortalData portal,
                                   ServerSafeComponent contentComponent)
    {
        super(portal, contentComponent);

        this.portal = portal;
        this.code = "1111";
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
                enter(ev.client, ev.playerData, ev.payload);
                return true;
            }
        }

        return false;
    }

    private void enter(Client client, PlayerData playerData, String payload)
    {
        if (!(client instanceof PlayerClient))
            return;

        PlayerClient playerClient = ((PlayerClient) client);

        String tag = portal.tag;

        if (tag.isEmpty())
            return;

        ActiveData otherPortal_ = null;

        for (Map map : Map.All())
        {
            otherPortal_ = map.getActiveForTag(Constants.ActiveTags.PORTAL, activeData ->
            {
                if (activeData == portal)
                    return false;

                if (!(activeData instanceof PortalData))
                    return false;

                PortalData portalData = ((PortalData) activeData);

                return portalData.tag.equals(tag);
            });

            if (otherPortal_ != null)
                break;
        }

        final ActiveData otherPortal = otherPortal_;

        if (otherPortal == null)
        {
            if (Log.ERROR) Log.error("Portal not found!");
            return;
        }

        if (!payload.equals(code))
        {
            String denied = getContentComponent().getDeniedEffect();

            if (!denied.isEmpty())
            {
                BrainOutServer.Controller.getClients().sendUDP(
                    new LaunchEffectMsg(portal.getDimension(), portal.getX(), portal.getY(), denied));

                return;
            }
        }

        float enterTime = 0.5f;

        String effect = getContentComponent().getActivateEffect();

        if (!effect.isEmpty())
        {
            BrainOutServer.Controller.getClients().sendUDP(
                new LaunchEffectMsg(portal.getDimension(), portal.getX(), portal.getY(), effect));
        }

        ServerSafeComponentData otherPortalComponent = otherPortal.getComponent(ServerSafeComponentData.class);

        if (otherPortalComponent != null)
        {
            if (!otherPortalComponent.getContentComponent().getActivateEffect().isEmpty())
            {
                BrainOutServer.Controller.getClients().sendUDPExcept(
                    new LaunchEffectMsg(otherPortal.getDimension(),
                        otherPortal.getX(), otherPortal.getY(),
                        otherPortalComponent.getContentComponent().getActivateEffect()), playerClient);
            }
        }
        else
        {
            BrainOutServer.Controller.getClients().sendUDPExcept(
                new LaunchEffectMsg(otherPortal.getDimension(), otherPortal.getX(), otherPortal.getY(),
                    getContentComponent().getActivateEffect()), playerClient);
        }

        ActiveProgressComponentData progress = playerData.getComponent(ActiveProgressComponentData.class);

        if (progress == null)
            return;

        playerClient.setSafesOpenedThisGame(playerClient.getSafesOpenedThisGame() + 1);

        if (playerClient.getSafesOpenedThisGame() == 1)
        {
            playerClient.addStat("safes-opened", 1);
        }
        playerClient.enablePlayer(false);

        SimplePhysicsComponentData phy = playerData.getComponentWithSubclass(SimplePhysicsComponentData.class);

        if (phy != null)
        {
            phy.getSpeed().set(0, 0);
            playerClient.getServerPlayerController().sendPlayerData(false, 0);
        }


        Runnable finally_ = () ->
        {
            playerClient.enablePlayer(true);
        };

        progress.startCancellable(enterTime, () ->
        {
            playerClient.moveTo(otherPortal.getDimension(), otherPortal.getX(), otherPortal.getY());
            finally_.run();
        }, finally_);

    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    @Override
    public int getTags()
    {
        return WithTag.TAG(Constants.ActiveTags.PORTAL);
    }
}
