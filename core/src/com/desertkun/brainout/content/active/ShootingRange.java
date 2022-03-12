package com.desertkun.brainout.content.active;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.Skin;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.upgrades.Upgrade;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.ShootingRangeData;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.active.ShootingRange")
public class ShootingRange extends Active
{
    public class WeaponPreset
    {
        public InstrumentInfo info;
        public ObjectMap<ConsumableContent, Integer> items;

        public WeaponPreset()
        {
            info = new InstrumentInfo();
            items = new ObjectMap<>();
        }
    }

    private ObjectMap<String, WeaponPreset> weapons;
    private String group;
    private int time;

    public ShootingRange()
    {
        weapons = new ObjectMap<>();
    }

    @Override
    public ActiveData getData(String dimension)
    {
        return new ShootingRangeData(this, dimension);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        group = jsonData.getString("group");
        time = jsonData.getInt("time");

        if (jsonData.has("weapons"))
        {
            JsonValue weaponsValue = jsonData.get("weapons");

            for (JsonValue v : weaponsValue)
            {
                String id = v.name();

                Instrument instrument = BrainOut.ContentMgr.get(v.getString("weapon"), Instrument.class);

                if (instrument == null)
                    continue;

                Skin skin = BrainOut.ContentMgr.get(v.getString("skin"), Skin.class);

                if (skin == null)
                    continue;

                WeaponPreset preset = new WeaponPreset();

                preset.info.instrument = instrument;
                preset.info.skin = skin;

                if (v.has("upgrades"))
                {
                    for (JsonValue upgradeValue : v.get("upgrades"))
                    {
                        String slot = upgradeValue.name();
                        Upgrade upgrade = BrainOut.ContentMgr.get(upgradeValue.asString(), Upgrade.class);

                        if (upgrade == null)
                            continue;

                        preset.info.upgrades.put(slot, upgrade);
                    }
                }

                if (v.has("items"))
                {
                    for (JsonValue item : v.get("items"))
                    {
                        ConsumableContent cc = BrainOut.ContentMgr.get(item.name(), ConsumableContent.class);

                        if (cc == null)
                            continue;

                        preset.items.put(cc, item.asInt());
                    }
                }

                weapons.put(id, preset);
            }
        }
    }

    public ObjectMap<String, WeaponPreset> getWeapons()
    {
        return weapons;
    }

    public String getGroup()
    {
        return group;
    }

    public int getTime()
    {
        return time;
    }

    public boolean hasWeapon(String weapon)
    {
        return weapons.containsKey(weapon);
    }
}
