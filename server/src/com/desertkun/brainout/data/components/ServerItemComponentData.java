package com.desertkun.brainout.data.components;

import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.enums.NotifyAward;
import com.desertkun.brainout.common.enums.NotifyMethod;
import com.desertkun.brainout.common.enums.NotifyReason;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.components.ServerItemComponent;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.shop.Slot;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.data.interfaces.WithTag;
import com.desertkun.brainout.events.DestroyEvent;
import com.desertkun.brainout.events.EarnEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.ServerRealization;
import com.desertkun.brainout.online.ClientProfile;
import com.desertkun.brainout.online.Trophy;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ServerItemComponent")
@ReflectAlias("data.components.ServerItemComponentData")
public class ServerItemComponentData<T extends ServerItemComponent> extends Component<T>
        implements WithTag
{
    private final ItemData itemData;
    private float time;
    private boolean done;
    private String target;
    private boolean isAlreadyEarned;

    public ServerItemComponentData(ItemData itemData, T itemComponent)
    {
        super(itemData, itemComponent);

        this.time = itemComponent.getTimeToLive();
        this.itemData = itemData;
        this.done = false;
        this.target = itemComponent.getTarget();
        this.isAlreadyEarned = false;
    }

    @Override
    public void init()
    {
        super.init();

        for (ObjectMap.Entry<ConsumableContent, Integer> entry : getContentComponent().getLoadWith())
        {
            ConsumableItem item = entry.key.acquireConsumableItem();
            itemData.getRecords().putConsumable(entry.value, item);
        }
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        if (!done)
        {
            if (time > 0)
            {
                time -= dt;

                if (time <= 0)
                {
                    BrainOutServer.PostRunnable(() ->
                            BrainOut.EventMgr.sendEvent(getComponentObject(), DestroyEvent.obtain()));

                    done = true;
                }
            }
        }
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case earn:
            {
                if (isAlreadyEarned)
                    return false;

                EarnEvent earnEvent = ((EarnEvent) event);
                Client client = BrainOutServer.Controller.getClients().get(earnEvent.playerData.getOwnerId());

                return earn(client);
            }
        }

        return false;
    }

    protected boolean earn(Client client)
    {
        InstrumentConsumableItem instrument = null;

        for (ObjectMap.Entry<Integer, ConsumableRecord> entry : itemData.getRecords().getData())
        {
            ConsumableRecord record = entry.value;

            if (record.getItem() instanceof InstrumentConsumableItem)
            {
                instrument = ((InstrumentConsumableItem) record.getItem());

                PlayerOwnerComponent poc = client.getPlayerData().getComponent(PlayerOwnerComponent.class);
                Content content = instrument.getContent();

                //Check if the slot is busy to avoid taking two weapons at once
                if (content instanceof Instrument)
                {
                    Slot slot = ((Instrument) content).getSlot();

                    if (poc.getInstrumentForSlot(slot) != null)
                    {
                        return false;
                    }
                }
            }

            ConsumableRecord r = client.addConsumable(record.getAmount(), record.getItem(), record.getQuality());
            isAlreadyEarned = true;
            if (r != null)
            {
                r.setTag(record.getTag());
            }
        }

        if (instrument != null && itemData.hasProperty("killer"))
        {
            String killer = itemData.getProperty("killer");
            try
            {
                int killerId = Integer.valueOf(killer);
                Client victim = BrainOutServer.Controller.getClients().get(itemData.getOwnerId());
                if (itemData.getComponent(ServerChipItemComponentData.class) != null)
                {
                    return true;
                }

                if (killerId == client.getId() && victim != null)
                {
                    addTrophy(client, victim, instrument);
                }
            }
            catch (NumberFormatException ignored) {}
        }

        return true;
    }

    private void addTrophy(Client killer, Client victim, InstrumentConsumableItem instrument)
    {
        if (killer instanceof PlayerClient && victim instanceof PlayerClient)
        {
            if (killer == victim)
            {
                return;
            }

            GameMode gameMode = BrainOutServer.Controller.getGameMode();

            if (gameMode == null || !(gameMode.isEnemies(killer.getTeam(), victim.getTeam())
                || ((ServerRealization) gameMode.getRealization()).isEnemies(killer, victim)))
            {
                return;
            }

            PlayerClient playerKiller = ((PlayerClient) killer);
            PlayerClient playerVictim = ((PlayerClient) victim);

            if (playerVictim.getAccount() == null)
            {
                return;
            }

            ClientProfile clientProfile = playerKiller.getProfile();

            InstrumentInfo info = instrument.getInstrumentData().getInfo();

            int base = BrainOutServer.Settings.getPrice("trophy-base");
            int upgrade = BrainOutServer.Settings.getPrice("trophy-upgrade");
            int skin = 0;

            if (info.skin != info.instrument.getDefaultSkin())
            {
                skin = BrainOutServer.Settings.getPrice("trophy-skin");
            }

            Trophy trophy = new Trophy();
            trophy.setOwner(victim.getName(), playerVictim.getAccount(),
                victim.getLevel(Constants.User.LEVEL, 1));

            trophy.setInfo(info);

            trophy.setXp(base + upgrade * info.upgrades.size + skin);

            if (clientProfile.addTrophy(trophy))
            {
                playerKiller.addStat("trophies-picked", 1);

                clientProfile.addBadge(trophy.getBadgeId());

                playerKiller.notify(NotifyAward.trophy, 1, NotifyReason.trophyEarned,
                    NotifyMethod.message, null);
            }
        }
    }



    @Override
    public int getTags()
    {
        return WithTag.TAG(Constants.ActiveTags.ITEM);
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    public String getTarget()
    {
        return target;
    }

    public void setTarget(String target)
    {
        this.target = target;
    }
}
