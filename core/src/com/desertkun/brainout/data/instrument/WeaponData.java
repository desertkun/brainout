package com.desertkun.brainout.data.instrument;

import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.content.instrument.Weapon;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.instrument.WeaponData")
public class WeaponData extends InstrumentData
{
    protected Weapon weapon;
    private ObjectMap<String, WeaponLoad> loads;

    public static class WeaponLoad implements Json.Serializable
    {
        public int amount;
        public int chambered;
        public int quality;

        public static class Magazine
        {
            public int amount;
            public int quality;

            public Magazine(int amount, int quality)
            {
                this.amount = amount;
                this.quality = quality;
            }
        }

        public IntMap<Magazine> magazines;

        @Override
        public void write(Json json)
        {
            json.writeValue("bulletA", amount);
            json.writeValue("ch", chambered);

            if (quality != -1)
            {
                json.writeValue("q", quality);
            }

            if (magazines != null)
            {
                json.writeObjectStart("mg");
                for (IntMap.Entry<Magazine> entry : magazines)
                {
                    if (entry.value.quality == -1)
                    {
                        json.writeValue(String.valueOf(entry.key), entry.value.amount);
                    }
                    else
                    {
                        json.writeArrayStart(String.valueOf(entry.key));
                        json.writeValue(entry.value.amount);
                        json.writeValue(entry.value.quality);
                        json.writeArrayEnd();
                    }
                }
                json.writeObjectEnd();
            }
        }

        public int getQuality()
        {
            return quality;
        }

        public int getRounds()
        {
            return amount;
        }

        public boolean hasMagazines()
        {
            return magazines != null;
        }

        public IntMap<Magazine> getMagazines()
        {
            return magazines;
        }

        public boolean isDetached()
        {
            return amount == -1;
        }

        public Magazine getMagazine(int mag)
        {
            if (magazines == null)
            {
                return null;
            }

            return magazines.get(mag, null);
        }

        public void clearMagazines()
        {
            if (magazines != null)
                magazines.clear();
        }

        public void setMagazine(int mag, int rounds, int quality)
        {
            if (magazines == null)
            {
                magazines = new IntMap<>();
            }

            magazines.put(mag, new Magazine(rounds, quality));
        }

        @Override
        public void read(Json json, JsonValue jsonData)
        {
            this.amount = jsonData.getInt("bulletA", 0);
            this.chambered = jsonData.getInt("ch", 0);
            this.quality = jsonData.has("q") ? jsonData.getInt("q") : -1;

            if (jsonData.has("mg"))
            {
                magazines = new IntMap<>();

                for (JsonValue value : jsonData.get("mg"))
                {
                    if (value.isNumber())
                    {
                        magazines.put(Integer.valueOf(value.name), new Magazine(value.asInt(), -1));
                    }
                    else
                    {
                        magazines.put(Integer.valueOf(value.name), new Magazine(value.getInt(0), value.getInt(1)));
                    }
                }
            }
        }
    }

    public WeaponData(Weapon instrument, String dimension)
    {
        super(instrument, dimension);

        weapon = instrument;
        loads = new ObjectMap<>();
    }

    public Weapon getWeapon()
    {
        return weapon;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        if (jsonData.has("loads"))
        {
            loads.clear();

            for (JsonValue value : jsonData.get("loads"))
            {
                WeaponLoad load = new WeaponLoad();
                load.read(json, value);
                loads.put(value.name(), load);
            }
        }
    }

    @Override
    public void write(Json json)
    {
        super.write(json);

        if (loads.size != 0)
        {
            json.writeObjectStart("loads");

            for (ObjectMap.Entry<String, WeaponLoad> load : loads)
            {
                json.writeValue(load.key, load.value);
            }

            json.writeObjectEnd();
        }
    }

    public WeaponLoad setLoad(String slot, int amount, int chambered)
    {
        WeaponLoad load = loads.get(slot);

        if (load == null)
        {
            load = new WeaponLoad();
            loads.put(slot, load);
        }

        load.amount = amount;
        load.chambered = chambered;

        return load;
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    public WeaponLoad getLoad(String slot)
    {
        return loads.get(slot);
    }
}
