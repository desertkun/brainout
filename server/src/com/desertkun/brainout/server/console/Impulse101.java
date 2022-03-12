package com.desertkun.brainout.server.console;

import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.online.PlayerRights;

public class Impulse101 extends ConsoleCommand
{
    private static ObjectMap<String, Integer> ContentToGive = new ObjectMap<>();

    static
    {
        ContentToGive.put("weapon-m4a1", 1);
        ContentToGive.put("bullet-5.56x45", 200);
        ContentToGive.put("instrument-binoculars", 1);
        ContentToGive.put("weapon-magnum", 1);
        ContentToGive.put("bullet-357-magnum", 20);
        ContentToGive.put("instrument-grenade-he", 4);
    }

    @Override
    public int requiredArgs()
    {
        return 0;
    }

    @Override
    public String execute(String[] args, Client client)
    {
        if (client instanceof PlayerClient)
        {
            PlayerClient player = ((PlayerClient) client);

            if (player.getPlayerData() == null)
                return "Player not spawned";

            for (ObjectMap.Entry<String, Integer> entry : ContentToGive)
            {
                String id = entry.key;
                int amount = entry.value;

                Content content = BrainOutServer.ContentMgr.get(id);

                if (content != null)
                {
                    PlayerOwnerComponent ownerComponent =
                            player.getPlayerData().getComponent(PlayerOwnerComponent.class);

                    String dimension = player.getPlayerData().getDimension();

                    if (content instanceof Instrument)
                    {
                        Instrument instrument = ((Instrument) content);
                        InstrumentData instrumentData = instrument.getData(dimension);
                        instrumentData.setSkin(instrument.getDefaultSkin());

                        ownerComponent.getConsumableContainer().putConsumable(amount,
                                new InstrumentConsumableItem(instrumentData, dimension));
                    }
                    else if (content instanceof ConsumableContent)
                    {
                        ownerComponent.getConsumableContainer().putConsumable(amount,
                                ((ConsumableContent) content).acquireConsumableItem());
                    }
                }
            }

            player.sendConsumable();
        }


        return "Done";
    }

    @Override
    public boolean isRightsValid(Client asker, Client forClient, PlayerRights rights)
    {
        switch (rights)
        {
            case admin:
            case mod:
                return true;
            default:
                return false;
        }
    }
}
