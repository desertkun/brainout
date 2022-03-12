package com.desertkun.brainout.client;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.content.active.FreePlayPlayer;
import com.desertkun.brainout.content.active.Player;
import com.desertkun.brainout.content.shop.InstrumentSlotItem;
import com.desertkun.brainout.content.shop.PlayerSkinSlotItem;
import com.desertkun.brainout.content.shop.PlayerSlotItem;
import com.desertkun.brainout.content.shop.Slot;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.server.ServerController;

public class FreePlayBotClient extends BotClient
{
    public FreePlayBotClient(int id, ServerController serverController)
    {
        super(id, serverController);
    }

    @Override
    protected float spawnCheckTimer()
    {
        return MathUtils.random(30.f, 60.f);
    }

    @Override
    protected Player getSpawnPlayer()
    {
        return BrainOutServer.ContentMgr.get("player-fp-bot", FreePlayPlayer.class);
    }

    @Override
    protected String getBotContent()
    {
        return "sl-pl-freeplay-bot";
    }

    @Override
    protected boolean isMapOkayToSpawn(Map map)
    {
        return !map.isSafeMap();
    }

    @Override
    protected void setupShopCart()
    {
        getShopCart().clear();
        getShopCart().addDefaultItems();

        Slot playerSlot = BrainOutServer.ContentMgr.get("slot-player", Slot.class);
        PlayerSlotItem botSlotItem = BrainOutServer.ContentMgr.get(getBotContent(), PlayerSlotItem.class);

        if (playerSlot != null && botSlotItem != null)
        {
            getShopCart().selectItem(playerSlot, botSlotItem.getStaticSelection());
        }

        Slot playerSkinSlot = BrainOutServer.ContentMgr.get("slot-player-skin", Slot.class);
        PlayerSkinSlotItem playerSkin = getRandomPlayerSkin();
        if (playerSkin != null)
        {
            getShopCart().selectItem(playerSkinSlot, playerSkin.getStaticSelection());
        }
    }
}
