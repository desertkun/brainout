package com.desertkun.brainout.data.components;

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.common.enums.EntityReceived;
import com.desertkun.brainout.common.msg.server.ActiveReceivedConsumableMsg;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.content.components.AmmoResourceDispenserComponent;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.consumable.ConsumableContainer;
import com.desertkun.brainout.data.consumable.ConsumableRecord;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("AmmoResourceDispenserComponent")
@ReflectAlias("data.components.AmmoResourceDispenserComponentData")
public class AmmoResourceDispenserComponentData extends ResourceDispenserComponentData<AmmoResourceDispenserComponent>
{
    private ObjectMap<ConsumableContent, Integer> consumables;

    public AmmoResourceDispenserComponentData(ComponentObject componentObject,
        AmmoResourceDispenserComponent contentComponent)
    {
        super(componentObject, contentComponent);

        consumables = new ObjectMap<>();

        for (ObjectMap.Entry<ConsumableContent, AmmoResourceDispenserComponent.DispenserLimits>
            entry: contentComponent.getConsumables())
        {
            consumables.put(entry.key, entry.value.amount);
        }
    }

    @Override
    public String getResourceName()
    {
        return "ammo";
    }

    public int getAmount(ConsumableContent consumableContent)
    {
        Integer amount = consumables.get(consumableContent);

        if (amount == null)
        {
            return 0;
        }

        return amount;
    }

    public void widraw(ConsumableContent consumableContent, int amount)
    {
        consumables.put(consumableContent, getAmount(consumableContent) - amount);
    }

    public AmmoResourceDispenserComponent.DispenserLimits getLimits(ConsumableContent consumableContent)
    {
        return getContentComponent().getConsumables().get(consumableContent);
    }

    @Override
    protected void deliverResource(final ActiveData activeData)
    {
        PlayerOwnerComponent poc = activeData.getComponent(PlayerOwnerComponent.class);

        if (poc != null)
        {
            final ConsumableContainer cc = poc.getConsumableContainer();

            for (OrderedMap.Entry<ConsumableContent, Integer> entry: consumables)
            {
                final ConsumableRecord record = cc.getConsumable(entry.key);

                final ConsumableContent consumableContent = entry.key;
                int amount = getAmount(consumableContent);

                // check if we don't have some ammo, but also don't have a weapon, so don't deliver,
                // because we don't need that
                if (record == null)
                {
                    boolean foundOne = false;

                    search:
                        for (OrderedMap.Entry<Integer, ConsumableRecord> entryToCheck: cc.getData())
                        {
                            ConsumableRecord recordToCheck = entryToCheck.value;

                            if (recordToCheck.getItem() instanceof InstrumentConsumableItem)
                            {
                                InstrumentConsumableItem ici = ((InstrumentConsumableItem) recordToCheck.getItem());

                                ServerWeaponComponentData swc = ici.getInstrumentData().getComponent(ServerWeaponComponentData.class);

                                if (swc != null)
                                {
                                    for (ServerWeaponComponentData.Slot slot : swc.getSlots().values())
                                    {
                                        if (slot.getBullet() == consumableContent)
                                        {
                                            foundOne = true;
                                            break search;
                                        }
                                    }
                                }
                            }
                        }

                    if (!foundOne)
                    {
                        continue;
                    }
                }

                int myAmount = record != null ? record.getAmount() : 0;

                if (amount > 0)
                {
                    AmmoResourceDispenserComponent.DispenserLimits limits = getLimits(consumableContent);

                    if (myAmount < limits.minToHave)
                    {
                        final int toDeliver = Math.min(limits.minToHave - myAmount, limits.deliverAtTime);

                        widraw(consumableContent, toDeliver);

                        BrainOutServer.PostRunnable(() ->
                        {
                            cc.putConsumable(toDeliver, record != null ? record.getItem() :
                                consumableContent.acquireConsumableItem(), record != null ? record.getQuality() : -1);

                            Client client = BrainOutServer.Controller.getClients().getByActive(activeData);

                            if (client != null)
                            {
                                client.sendConsumable();
                            }

                            BrainOutServer.Controller.getClients().sendTCP(new ActiveReceivedConsumableMsg(activeData,
                                    EntityReceived.content, toDeliver));

                            resourceDelivered(activeData, toDeliver);
                        });
                    }
                }
            }
        }
    }
}
