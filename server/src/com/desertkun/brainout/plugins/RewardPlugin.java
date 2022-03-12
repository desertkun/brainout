package com.desertkun.brainout.plugins;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.msg.server.ChatMsg;
import com.desertkun.brainout.content.OwnableContent;
import com.desertkun.brainout.content.components.ServerCaseComponent;
import com.desertkun.brainout.content.gamecase.Case;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.EventReceiver;
import com.desertkun.brainout.events.PlayerWonEvent;
import com.desertkun.brainout.online.ClientProfile;
import com.desertkun.brainout.server.ServerConstants;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("plugins.RewardPlugin")
public class RewardPlugin extends Plugin implements EventReceiver
{
    private String rewardContent, key;
    private int min, max, minPlayers;

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

        this.rewardContent = jsonData.getString("reward");
        this.key = jsonData.getString("key");
        this.min = jsonData.getInt("min", 3);
        this.max = jsonData.getInt("max", 5);
        this.minPlayers = jsonData.getInt("min-players", 3);
    }

    public OwnableContent getContent()
    {
        return ((OwnableContent) BrainOut.ContentMgr.get(rewardContent));
    }

    public int getPeriod()
    {
        return MathUtils.random(min, max);
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
        if (BrainOutServer.Controller.getClients().size < minPlayers)
            return;

        if (!(client instanceof PlayerClient))
            return;

        PlayerClient playerClient = ((PlayerClient) client);

        if (playerClient.getProfile() == null)
            return;

        int period = (int)playerClient.getStat(key, 0) - 1;

        if (period <= 0)
        {
            period = getPeriod();
            reward(playerClient);
        }

        playerClient.setStat(key, period);

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
