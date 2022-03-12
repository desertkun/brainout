package com.desertkun.brainout.content.shop;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.content.active.Player;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.instrument.Weapon;
import com.desertkun.brainout.content.upgrades.UpgradableProperty;
import com.desertkun.brainout.content.upgrades.Upgrade;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.online.UserProfile;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.shop.PlayerSlotItem")
public class PlayerSlotItem extends ConsumableSlotItem
{
    private Player player;
    private int additionalClip;
    private Array<Bullet> clipExcept;
    private Array<Weapon> weaponExcept;
    private Slot slotFrom;

    public PlayerSlotItem()
    {
        clipExcept = new Array<>();
        weaponExcept = new Array<>();
    }

    public class PlayerSlotSelection extends ConsumableSelection
    {
        public PlayerSlotItem getItem()
        {
            return ((PlayerSlotItem) super.getItem());
        }

        @Override
        public void apply(ShopCart shopCart, PlayerData playerData, UserProfile profile, Slot slot, Selection selection)
        {
            PlayerOwnerComponent ownerComponent = playerData.getComponent(PlayerOwnerComponent.class);

            Selection slotFromSelection = shopCart.getItem(slotFrom);

            if (slotFromSelection instanceof InstrumentSlotItem.InstrumentSelection)
            {
                InstrumentSlotItem.InstrumentSelection asInstrument =
                        ((InstrumentSlotItem.InstrumentSelection) slotFromSelection);

                Instrument instrument = asInstrument.getInfo().instrument;

                if (instrument instanceof Weapon)
                {
                    Weapon asWeapon = ((Weapon) instrument);

                    if (!weaponExcept.contains(asWeapon, true))
                    {
                        if (additionalClip != 0)
                        {
                            Weapon.WeaponProperties primary = asWeapon.getPrimaryProperties();

                            float am = primary.getClipSize();

                            for (ObjectMap.Entry<String, Upgrade> entry : asInstrument.getInfo().upgrades)
                            {
                                Upgrade upgrade = entry.value;
                                Upgrade.UpgradeProperty property = upgrade.getProperty("clip-size");
                                if (property != null)
                                {
                                    am = property.applyFloat(am);
                                }
                            }

                            int amount = (int) am * additionalClip;

                            UpgradableProperty bulletProperty = new UpgradableProperty(
                                    primary.property(Constants.Properties.BULLET),
                                    asInstrument.getInfo().upgrades.values());

                            Bullet bullet = bulletProperty.asContent(Bullet.class);
                            if (bullet != null && !clipExcept.contains(bullet, true))
                            {
                                ownerComponent.getConsumableContainer().putConsumable(
                                        amount,
                                        bullet.acquireConsumableItem()
                                );
                            }
                        }
                    }
                }
            }

            super.apply(shopCart, playerData, profile, slot, selection);
        }
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        player = ((Player) BrainOut.ContentMgr.get(jsonData.getString("player")));

        additionalClip = jsonData.getInt("additionalClip", 0);

        if (jsonData.has("clipExcept"))
        {
            for (JsonValue value : jsonData.get("clipExcept"))
            {
                clipExcept.add(BrainOut.ContentMgr.get(value.asString(), Bullet.class));
            }
        }

        if (jsonData.has("weaponExcept"))
        {
            for (JsonValue value : jsonData.get("weaponExcept"))
            {
                weaponExcept.add(BrainOut.ContentMgr.get(value.asString(), Weapon.class));
            }
        }

        if (jsonData.has("slotFrom"))
        {
            slotFrom = ((Slot) BrainOut.ContentMgr.get(jsonData.getString("slotFrom")));
        }
    }

    public Player getPlayer()
    {
        return player;
    }

    @Override
    public Selection getSelection()
    {
        return new PlayerSlotSelection();
    }
}
