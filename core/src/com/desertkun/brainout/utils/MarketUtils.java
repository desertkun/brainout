package com.desertkun.brainout.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.PlayerSkin;
import com.desertkun.brainout.content.active.Active;
import com.desertkun.brainout.content.active.RealEstateItem;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.content.components.FreePlayValuableComponent;
import com.desertkun.brainout.content.components.MaxWeightComponent;
import com.desertkun.brainout.content.consumable.*;
import com.desertkun.brainout.content.consumable.impl.*;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.instrument.InstrumentSkin;
import com.desertkun.brainout.content.upgrades.ExtendedStorage;
import com.desertkun.brainout.content.upgrades.Upgrade;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.instrument.WeaponData;
import com.desertkun.brainout.online.UserProfile;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.services.LoginService;
import org.anthillplatform.runtime.services.MarketService;
import org.json.JSONObject;

public class MarketUtils
{
    public static MarketService.MarketItemEntry ConsumableRecordToMarketEntry(ConsumableRecord record)
    {
        String name;
        int amount = record.getAmount();
        JSONObject payload = new JSONObject();

        ConsumableItem consumableItem = record.getItem();

        if (consumableItem instanceof RealEstateConsumableItem)
        {
            RealEstateConsumableItem rci = ((RealEstateConsumableItem) consumableItem);
            name = "realestate";
            payload.put("c", rci.getContent().getID());
            payload.put("l", rci.getLocation());
            payload.put("id", rci.getId());
        }
        else if (consumableItem instanceof RealEstateItemConsumableItem)
        {
            RealEstateItemConsumableItem rci = ((RealEstateItemConsumableItem) consumableItem);
            name = "rsitem";
            payload.put("c", rci.getContent().getID());
        }
        else if (consumableItem instanceof InstrumentConsumableItem)
        {
            InstrumentConsumableItem ici = ((InstrumentConsumableItem) consumableItem);
            name = "instrument";

            payload.put("c", ici.getInstrumentData().getInstrument().getID());


            InstrumentData instrumentData = ici.getInstrumentData();

            if (instrumentData.getInfo().skin != instrumentData.getInstrument().getDefaultSkin())
            {
                payload.put("s", instrumentData.getInfo().skin.getID());
            }

            if (ici.getInstrumentData() instanceof WeaponData)
            {
                WeaponData weaponData = ((WeaponData) ici.getInstrumentData());
                if (weaponData.getWeapon().getSlot() != null && weaponData.getWeapon().getSlotItem() != null)
                {
                    payload.put("ct", weaponData.getWeapon().getSlotItem().getCategory());
                }

                for (ObjectMap.Entry<String, Upgrade> entry : weaponData.getInfo().upgrades)
                {
                    payload.put(entry.key, entry.value.getID());
                }
            }
        }
        else if (consumableItem instanceof DefaultConsumableItem)
        {
            name = "consumable";
            payload.put("c", consumableItem.getContent().getID());

            if (consumableItem instanceof DecayConsumableItem)
            {
                payload.put("u", ((DecayConsumableItem)consumableItem).getUses());
            }

            FreePlayValuableComponent valuable = consumableItem.getContent().getComponent(FreePlayValuableComponent.class);
            if (valuable != null)
            {
                payload.put("k", "val");
            }
            else if (consumableItem.getContent() instanceof Bullet)
            {
                payload.put("k", "bull");
            }
            else if (consumableItem.getContent() instanceof Resource)
            {
                payload.put("k", "res");
            }
            else
            {
                payload.put("k", "itm");
            }
        }
        else if (consumableItem instanceof ArmorConsumableItem)
        {
            name = "armor";
            payload.put("c", consumableItem.getContent().getID());

            for (ObjectMap.Entry<String, Float> entry : ((ArmorConsumableItem) consumableItem).getProtect())
            {
                payload.put("p-" + entry.key, entry.value);
            }
        }
        else if (consumableItem instanceof WalkietalkieConsumableItem)
        {
            name = "radio";
            payload.put("c", consumableItem.getContent().getID());
            payload.put("f", ((WalkietalkieConsumableItem) consumableItem).getFrequency());
        }
        else if (consumableItem instanceof PlayerSkinConsumableItem)
        {
            name = "skin";
            payload.put("c", consumableItem.getContent().getID());
        }
        else
        {
            return null;
        }

        if (record.hasQuality())
        {
            payload.put("q", record.getQuality());
        }

        return new MarketService.MarketItemEntry(name, amount, payload);
    }

    public static ConsumableRecord MarketObjectToConsumableRecord(String item, JSONObject payload, int amount)
    {
        ConsumableItem i;

        switch (item)
        {
            case "instrument":
            {
                String c = payload.optString("c");
                if (c == null)
                {
                    return null;
                }

                Instrument instrument = BrainOut.ContentMgr.get(c, Instrument.class);
                if (instrument == null)
                {
                    return null;
                }

                InstrumentData instrumentData = instrument.getData("default");

                String skin = payload.optString("s", null);
                if (skin != null)
                {
                    InstrumentSkin ss = BrainOut.ContentMgr.get(skin, InstrumentSkin.class);
                    instrumentData.setSkin(ss);
                }
                else
                {
                    instrumentData.setSkin(instrument.getDefaultSkin());
                }

                if (instrument.getSlotItem() != null && instrument.getSlotItem().getUpgrades() != null)
                {
                    for (ObjectMap.Entry<String, Array<Upgrade>> entry : instrument.getSlotItem().getUpgrades())
                    {
                        String has = payload.optString(entry.key, null);
                        if (has == null)
                            continue;

                        for (Upgrade upgrade : entry.value)
                        {
                            if (upgrade.getID().equals(has))
                            {
                                instrumentData.getInfo().upgrades.put(entry.key, upgrade);
                                break;
                            }
                        }
                    }
                }

                i = new InstrumentConsumableItem(instrumentData, "default");
                break;
            }
            case "consumable":
            {
                String c = payload.optString("c");
                if (c == null)
                {
                    return null;
                }

                ConsumableContent content;

                if (payload.has("u"))
                {
                    content = BrainOut.ContentMgr.get(c, DecayConsumableContent.class);
                    if (content == null)
                    {
                        return null;
                    }
                    i = content.acquireConsumableItem();
                    ((DecayConsumableItem)i).setUses(payload.getInt("u"));
                }
                else
                {
                    content = BrainOut.ContentMgr.get(c, ConsumableContent.class);
                    if (content == null)
                    {
                        return null;
                    }
                    i = content.acquireConsumableItem();
                }
                break;
            }
            case "armor":
            {
                String c = payload.optString("c");
                if (c == null)
                {
                    return null;
                }

                Armor content = BrainOut.ContentMgr.get(c, Armor.class);
                if (content == null)
                    return null;

                ArmorConsumableItem aci = content.acquireConsumableItem();

                for (String key : payload.keySet())
                {
                    if (!key.startsWith("p-"))
                        continue;

                    String param = key.substring(2);
                    aci.getProtect().put(param, payload.optFloat(key, 0));
                }

                i = aci;
                break;
            }
            case "skin":
            {
                String c = payload.optString("c");
                if (c == null)
                {
                    return null;
                }

                PlayerSkin content = BrainOut.ContentMgr.get(c, PlayerSkin.class);
                if (content == null)
                    return null;

                i = new PlayerSkinConsumableItem(content);
                break;
            }
            case "realestate":
            {
                String c = payload.optString("c");
                if (c == null)
                {
                    return null;
                }

                RealEstateContent content = BrainOut.ContentMgr.get(c, RealEstateContent.class);
                if (content == null)
                    return null;

                RealEstateConsumableItem rs = new RealEstateConsumableItem(content);
                i = rs;

                rs.setLocation(payload.optString("l"));
                rs.setId(payload.optString("id"));

                break;
            }
            case "rsitem":
            {
                String c = payload.optString("c");
                if (c == null)
                {
                    return null;
                }

                RealEstateItem content = BrainOut.ContentMgr.get(c, RealEstateItem.class);
                if (content == null)
                    return null;

                RealEstateItemConsumableItem rs = new RealEstateItemConsumableItem(content);
                i = rs;

                break;
            }
            case "radio":
            {
                String c = payload.optString("c");
                if (c == null)
                {
                    return null;
                }

                Walkietalkie content = BrainOut.ContentMgr.get(c, Walkietalkie.class);
                if (content == null)
                    return null;

                WalkietalkieConsumableItem w = new WalkietalkieConsumableItem(content);
                w.setFrequency(payload.optInt("f", Walkietalkie.getRandomFrequency()));
                i = w;
                break;
            }
            case "ru":
            default:
            {
                return null;
            }
        }

        ConsumableRecord record = new ConsumableRecord(i, amount, 0);

        if (payload.has("q"))
        {
            record.setQuality(payload.getInt("q"));
        }

        record.init();
        return record;
    }

    public interface GetRUCallback
    {
        void success(int amount);
        void error();
    }

    public static void GetMarketRU(GetRUCallback callback)
    {
        MarketService marketService = MarketService.Get();
        LoginService loginService = LoginService.Get();

        if (marketService != null && loginService != null)
        {
            marketService.getMarketItem("freeplay", "ru", new JSONObject(), loginService.getCurrentAccessToken(),
                (request, result, amount) ->
            {
                if (result == Request.Result.success || result == Request.Result.notFound)
                {
                    Gdx.app.postRunnable(() ->
                    {
                        callback.success(amount);
                    });
                }
                else
                {
                    callback.error();
                }
            });
        }
    }

    public static String GetMarketItemCategory(String itemName)
    {
        switch (itemName)
        {
            case "rsitem":
            {
                return "rs";
            }
            default:
            {
                return "default";
            }
        }
    }

    public static float GetMaxMarketWeightForPlayer(Active marketContainer, UserProfile userProfile, String category)
    {
        float maxWeight = 0;

        switch (category)
        {
            case "default":
            {
                if (marketContainer != null)
                {
                    MaxWeightComponent mx = marketContainer.getComponent(MaxWeightComponent.class);

                    if (mx != null)
                    {
                        maxWeight += mx.getMaxWeight();
                    }
                }
                break;
            }
            case "rs":
            {
                maxWeight += 500;

                break;
            }
        }

        int addWeight = 0;

        if (userProfile != null)
        {
            OrderedMap<ExtendedStorage, Integer> extStorages = userProfile.getItemsOf(ExtendedStorage.class);

            for (ExtendedStorage extStorage : extStorages.keys())
            {
                if (!category.equals(extStorage.getCategory()))
                    continue;

                if (addWeight < extStorage.getExtraWeight())
                    addWeight = extStorage.getExtraWeight();
            }
        }

        maxWeight += addWeight;

        return maxWeight;
    }
}
