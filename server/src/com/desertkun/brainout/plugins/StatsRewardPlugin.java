package com.desertkun.brainout.plugins;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.msg.server.ChatMsg;
import com.desertkun.brainout.content.OwnableContent;
import com.desertkun.brainout.content.gamecase.Case;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.EventReceiver;
import com.desertkun.brainout.events.PlayerWonEvent;
import com.desertkun.brainout.online.ClientProfile;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.server.ServerConstants;
import com.desertkun.brainout.utils.ItemsFilters;
import com.desertkun.brainout.utils.StatsFilters;

@Reflect("plugins.StatsRewardPlugin")
public class StatsRewardPlugin extends Plugin implements EventReceiver
{
    private String rewardContent;
    private StatsFilters statsFilters;
    private ItemsFilters itemsFilters;
    private int minPlayers;

    public StatsRewardPlugin()
    {
        statsFilters = new StatsFilters();
        itemsFilters = new ItemsFilters();
    }

    @Override
    public void init()
    {
        super.init();

        BrainOutServer.EventMgr.subscribe(Event.ID.playerWon, this);
    }

    @Override
    public void release()
    {
        super.release();

        BrainOutServer.EventMgr.unsubscribe(Event.ID.playerWon, this);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        rewardContent = jsonData.getString("reward");
        statsFilters.read(json, jsonData.get("stats-filters"));
        itemsFilters.read(json, jsonData.get("items-filters"));
        minPlayers = jsonData.getInt("min-players", 3);
    }

    public OwnableContent getContent()
    {
        return ((OwnableContent) BrainOut.ContentMgr.get(rewardContent));
    }

    @Override
    public boolean onEvent(Event event)
    {
        if (event.getID() == Event.ID.playerWon)
        {
            playerWon(((PlayerWonEvent) event).client);
        }

        return false;
    }

    private void playerWon(Client client)
    {
        if (!BrainOut.OnlineEnabled())
            return;

        if (BrainOutServer.Controller.getClients().size < minPlayers)
            return;

        if (!(client instanceof PlayerClient))
            return;

        PlayerClient playerClient = ((PlayerClient) client);

        if (playerClient.getProfile() == null)
            return;

        if (!statsFilters.checkFilters(playerClient.getProfile()))
            return;

        if (!itemsFilters.checkFilters(playerClient.getProfile()))
            return;

        reward(playerClient);

        playerClient.getProfile().setDirty();
    }

    private void reward(PlayerClient client)
    {
        if (getContent() instanceof Case)
        {
            Case asCase = ((Case) getContent());

            if (!asCase.applicable(client.getProfile()))
            {
                return;
            }
        }

        client.gotOwnable(getContent(), "reward-plugin", ClientProfile.OnwAction.owned, 1);

        BrainOutServer.Controller.getClients().sendTCP(
            new ChatMsg("{MP_SERVER}", client.getName() + " {MP_PLAYER_CONTENT_OWNED} {" +
                    getContent().getTitle().getID() + "}", "server",
                ServerConstants.Chat.COLOR_IMPORTANT, -1)
        );
    }
}
