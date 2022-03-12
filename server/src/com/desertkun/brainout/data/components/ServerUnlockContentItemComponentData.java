package com.desertkun.brainout.data.components;

import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.OwnableContent;
import com.desertkun.brainout.content.components.ServerUnlockContentItemComponent;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.online.ClientProfile;
import com.desertkun.brainout.reflection.Reflect;

@Reflect("data.components.ServerUnlockContentItemComponentData")
public class ServerUnlockContentItemComponentData extends
        ServerItemComponentData<ServerUnlockContentItemComponent>
{
    private final ItemData itemData;
    private EarnedCallback callback;

    public interface EarnedCallback
    {
        void contentEarned(OwnableContent content, PlayerClient playerClient);
    }

    public ServerUnlockContentItemComponentData(ItemData itemData,
                                                ServerUnlockContentItemComponent itemComponent)
    {
        super(itemData, itemComponent);

        this.itemData = itemData;
        this.callback = callback;
    }

    public void setCallback(EarnedCallback callback)
    {
        this.callback = callback;
    }

    protected void contentEarned(OwnableContent content, PlayerClient playerClient)
    {
        if (callback != null)
        {
            callback.contentEarned(content, playerClient);
            callback = null;
        }
    }

    @Override
    protected boolean earn(Client client)
    {
        boolean good = false;

        for (ConsumableRecord record : itemData.getRecords().getData().values())
        {
            Content content = record.getItem().getContent();

            if (record.getItem().getContent() instanceof OwnableContent)
            {
                OwnableContent ownableContent = ((OwnableContent) content);

                good = true;

                if (client instanceof PlayerClient)
                {
                    PlayerClient playerClient = ((PlayerClient) client);

                    playerClient.gotOwnable(ownableContent, "pickup",
                        ClientProfile.OnwAction.owned, record.getAmount());

                    contentEarned(ownableContent, playerClient);
                }
            }
        }

        return good;
    }
}
