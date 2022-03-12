package com.desertkun.brainout.data.components;

import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.msg.server.CustomPlayerAnimationMsg;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.content.components.ServerFreeplayGeneratorComponent;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.data.active.FreeplayGeneratorData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.events.ActivateActiveEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ServerFreeplayGeneratorComponent")
@ReflectAlias("data.components.ServerFreeplayGeneratorComponentData")
public class ServerFreeplayGeneratorComponentData extends Component<ServerFreeplayGeneratorComponent>
{
    public ServerFreeplayGeneratorComponentData(
        ComponentObject componentObject,
        ServerFreeplayGeneratorComponent contentComponent)
    {
        super(componentObject, contentComponent);
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case activeActivateData:
            {
                ActivateActiveEvent ev = ((ActivateActiveEvent) event);
                activate(ev.client, ev.playerData);
                return true;
            }
        }

        return false;
    }

    private FreeplayGeneratorData getGenerator()
    {
        return ((FreeplayGeneratorData) getComponentObject());
    }

    private void activate(Client client, PlayerData playerData)
    {
        if (!(client instanceof PlayerClient))
            return;

        if (getGenerator().isEmpty())
        {
            if (playerData.getCurrentInstrument() == null)
                return;

            if (playerData.getCurrentInstrument().getInstrument() != getGenerator().getPetrol())
                return;

            refill(playerData);
        }
        else
        {
            if (getGenerator().isWorking())
                return;

            startup(playerData);
        }
    }

    private void startup(PlayerData playerData)
    {
        getGenerator().activate();
    }

    private void refill(PlayerData playerData)
    {
        PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

        if (poc == null)
            return;

        int ownerId = playerData.getOwnerId();

        ConsumableRecord record = poc.getCurrentInstrumentRecord();
        ConsumableItem item = record.getItem();

        if (!(item instanceof InstrumentConsumableItem))
            return;

        if (playerData.getCurrentInstrument() != null && playerData.getCurrentInstrument().isForceSelect())
        {
            return;
        }

        InstrumentConsumableItem ici = ((InstrumentConsumableItem) item);

        if (ici.getInstrumentData() != playerData.getCurrentInstrument())
            return;

        Client client = BrainOutServer.Controller.getClients().get(ownerId);

        if (!(client instanceof PlayerClient))
            return;

        PlayerClient playerClient = ((PlayerClient) client);

        BrainOutServer.Controller.getClients().sendTCP(new CustomPlayerAnimationMsg(
            playerData,
            getGenerator().getGenerator().getRefillAnimation(),
            getGenerator().getGenerator().getRefillEffect()));

        ActiveProgressComponentData progress = playerData.getComponent(ActiveProgressComponentData.class);

        if (progress == null)
            return;

        playerClient.enablePlayer(false);

        SimplePhysicsComponentData phy = playerData.getComponentWithSubclass(SimplePhysicsComponentData.class);

        if (phy != null)
        {
            phy.getSpeed().set(0, 0);
            playerClient.getServerPlayerController().sendPlayerData(false, 0);
        }

        Runnable finally_ = () ->
        {
            getGenerator().addFuel();
            poc.removeConsumable(record);
            playerClient.enablePlayer(true);

            ServerPlayerControllerComponentData ctl = playerClient.getServerPlayerController();

            if (ctl != null)
            {
                ctl.selectFirstInstrument(poc);
                ctl.updateAttachments();
                ctl.consumablesUpdated();
            }
        };

        playerClient.moveTo(getGenerator().getDimension(), getGenerator().getX(), getGenerator().getY());

        progress.startNonCancellable(getGenerator().getGenerator().getRefillTime(), finally_);
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
}
