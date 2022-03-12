package com.desertkun.brainout.content.shop;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.content.PlayerSkin;
import com.desertkun.brainout.content.active.Player;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.instrument.Weapon;
import com.desertkun.brainout.content.upgrades.Upgrade;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.PlayerAnimationComponentData;
import com.desertkun.brainout.online.UserProfile;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.shop.PlayerSkinSlotItem")
public class PlayerSkinSlotItem extends ConsumableSlotItem
{
    private PlayerSkin skin;

    public class PlayerSkinSlotSelection extends ConsumableSelection
    {
        public PlayerSkinSlotItem getItem()
        {
            return ((PlayerSkinSlotItem) super.getItem());
        }

        @Override
        public void apply(ShopCart shopCart, PlayerData playerData, UserProfile profile, Slot slot, Selection selection)
        {
            PlayerAnimationComponentData pac = playerData.getComponent(PlayerAnimationComponentData.class);

            if (pac != null)
            {
                pac.setSkin(skin);
            }

            super.apply(shopCart, playerData, profile, slot, selection);
        }
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        skin = BrainOut.ContentMgr.get(jsonData.getString("skin"), PlayerSkin.class);
    }

    @Override
    public void loadContent(AssetManager assetManager)
    {
        super.loadContent(assetManager);

        if (skin != null)
        {
            skin.setSlotItem(this);
        }
    }

    public PlayerSkin getSkin()
    {
        return skin;
    }

    @Override
    public Selection getSelection()
    {
        return new PlayerSkinSlotSelection();
    }
}
