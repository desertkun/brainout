package com.desertkun.brainout.server.console;

import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.active.RealEstateItem;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.content.consumable.impl.RealEstateItemConsumableItem;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.online.PlayerRights;
import com.desertkun.brainout.utils.MarketUtils;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.services.MarketService;

public class Give extends ConsoleCommand
{
    @Override
    public int requiredArgs()
    {
        return 1;
    }

    @Override
    public String execute(String[] args, Client client)
    {
        String id = args[1];

        Content content = BrainOutServer.ContentMgr.get(id);

        if (content != null)
        {
            int amount = 1;
            int quality = -1;

            if (args.length >= 3)
            {
                try
                {
                    amount = Integer.valueOf(args[2]);
                }
                catch (NumberFormatException e)
                {
                    return "Bad format";
                }
            }

            if (args.length >= 4)
            {
                try
                {
                    quality = Integer.valueOf(args[3]);
                }
                catch (NumberFormatException e)
                {
                    return "Bad quality";
                }

                if (quality < 0 || quality > 100)
                {
                    return "Bad quality";
                }
            }

            if (client instanceof PlayerClient)
            {
                PlayerClient player = ((PlayerClient) client);

                if (player.getPlayerData() == null)
                    return "Player not spawned";

                PlayerOwnerComponent ownerComponent =
                    player.getPlayerData().getComponent(PlayerOwnerComponent.class);

                String dimension = player.getPlayerData().getDimension();

                if (content instanceof RealEstateItem)
                {
                    if (System.getenv("VALPHA") == null)
                    {
                        return "This is not possible here.";
                    }

                    MarketService marketService = MarketService.Get();
                    ConsumableRecord r = new ConsumableRecord(
                        new RealEstateItemConsumableItem(((RealEstateItem) content)), amount, -1);

                    MarketService.MarketItemEntry rr = MarketUtils.ConsumableRecordToMarketEntry(r);
                    if (rr == null)
                    {
                        return "Ugh I don't know about this.";
                    }

                    marketService.updateMarketItem("freeplay", rr.name, rr.payload, rr.amount, player.getAccessToken(),
                        (request, result) -> BrainOutServer.PostRunnable(() ->
                    {
                        if (result == Request.Result.success)
                        {
                            player.sendChat("Okay done.");
                        }
                        else
                        {
                            player.sendChat("Couldn't do that: " + result.toString());
                        }
                    }));

                    return "Trying.";
                }
                else if (content instanceof Instrument)
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
                        ((ConsumableContent) content).acquireConsumableItem(), quality);
                }

                player.sendConsumable();
            }
            else
            {
                return "Not a player";
            }

            return "Done";
        }

        return "No such content.";
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
