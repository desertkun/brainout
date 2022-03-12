package com.desertkun.brainout.online;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.Skin;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.content.consumable.DecayConsumableContent;
import com.desertkun.brainout.content.consumable.Walkietalkie;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.upgrades.Upgrade;
import com.desertkun.brainout.data.components.ServerWeaponComponentData;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.data.instrument.WeaponData;
import org.json.JSONArray;
import org.json.JSONObject;

public class FreePlayContainer
{
    private Array<ContainerItem> items;
    private boolean saveEmpty;

    public FreePlayContainer()
    {
        items = new Array<>();
    }

    public void addItem(ContainerItem item)
    {
        items.add(item);
    }

    public Array<ContainerItem> getItems()
    {
        return items;
    }

    public abstract static class ContainerItem
    {
        public int quality;

        public ContainerItem(int quality)
        {
            this.quality = quality;
        }

        public abstract void write(JSONObject o);
        public abstract boolean read(JSONObject o);
        public abstract boolean isValid();
    }

    public void setSaveEmpty(boolean saveEmpty)
    {
        this.saveEmpty = saveEmpty;
    }

    public boolean isSaveEmpty()
    {
        return saveEmpty;
    }

    public static class ConsumableContainerItem extends ContainerItem
    {
        public ConsumableContent cnt;
        public int amount;

        public ConsumableContainerItem(int quality) { super(quality);}
        public ConsumableContainerItem(ConsumableContent cnt, int amount, int quality)
        {
            super(quality);
            this.amount = amount;
            this.cnt = cnt;
        }


        @Override
        public void write(JSONObject o)
        {
            if (amount > 1)
            {
                o.put("a", amount);
            }
            o.put("c", cnt.getID());
        }

        @Override
        public boolean isValid()
        {
            return cnt != null;
        }

        @Override
        public boolean read(JSONObject o)
        {
            amount = o.optInt("a", 1);

            String c = o.optString("c");
            if (c == null)
                return false;

            cnt = BrainOut.ContentMgr.get(c, ConsumableContent.class);
            return cnt != null;
        }
    }

    public static class DecayConsumableContainerItem extends ContainerItem
    {
        public DecayConsumableContent cnt;
        public int uses;

        public DecayConsumableContainerItem(int quality) { super(quality);}
        public DecayConsumableContainerItem(DecayConsumableContent cnt, int uses, int quality)
        {
            super(quality);
            this.uses = uses;
            this.cnt = cnt;
        }


        @Override
        public void write(JSONObject o)
        {
            o.put("u", uses);
            o.put("c", cnt.getID());
        }

        @Override
        public boolean isValid()
        {
            return cnt != null;
        }

        @Override
        public boolean read(JSONObject o)
        {
            uses = o.optInt("u", 1);

            String c = o.optString("c");
            if (c == null)
                return false;

            cnt = BrainOut.ContentMgr.get(c, DecayConsumableContent.class);
            return cnt != null;
        }
    }

    public static class InstrumentContainerItem extends ContainerItem
    {
        public InstrumentInfo instrument;
        public int rounds, chambered;

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

        public int getChambered()
        {
            return chambered;
        }

        public int getRounds()
        {
            return rounds;
        }

        public boolean isAttached()
        {
            return rounds >= 0;
        }

        public IntMap<Magazine> getMagazines()
        {
            return magazines;
        }

        public InstrumentContainerItem(int quality)
        {
            super(quality);
            instrument = new InstrumentInfo();
        }

        public InstrumentContainerItem(InstrumentData instrumentData, int quality)
        {
            super(quality);

            instrument = instrumentData.getInfo();

            ServerWeaponComponentData wp = instrumentData.getComponent(ServerWeaponComponentData.class);

            if (wp != null)
            {
                ServerWeaponComponentData.Slot primarySlot = wp.getSlot(Constants.Properties.SLOT_PRIMARY);

                if (primarySlot != null)
                {
                    chambered = primarySlot.getChambered();
                    rounds = primarySlot.getRounds();

                    if (primarySlot.hasMagazineManagement())
                    {
                        magazines = new IntMap<Magazine>();

                        for (IntMap.Entry<ServerWeaponComponentData.Slot.Magazine> magazine : primarySlot.getMagazines())
                        {
                            magazines.put(magazine.key, new Magazine(magazine.value.rounds, magazine.value.quality));
                        }
                    }
                }
            }
        }

        @Override
        public void write(JSONObject o)
        {
            o.put("c", instrument.instrument.getID());

            ObjectMap<String, Upgrade> u = instrument.upgrades;
            if (u.size > 0)
            {
                JSONObject upgrades = new JSONObject();
                for (ObjectMap.Entry<String, Upgrade> entry : u)
                {
                    upgrades.put(entry.key, entry.value.getID());
                }
                o.put("u", upgrades);
            }

            Skin skin = instrument.skin;
            if (skin != instrument.instrument.getDefaultSkin())
            {
                o.put("s", skin.getID());
            }

            if (rounds != 0)
            {
                o.put("r", rounds);
            }

            if (chambered != 0)
            {
                o.put("ch", chambered);
            }

            if (magazines != null)
            {
                JSONObject m = new JSONObject();

                for (IntMap.Entry<Magazine> entry : magazines)
                {
                    JSONArray a = new JSONArray();
                    a.put(entry.value.amount);
                    a.put(entry.value.quality);

                    m.put(String.valueOf(entry.key), a);
                }

                o.put("m", m);
            }
        }

        @Override
        public boolean isValid()
        {
            return instrument.instrument != null;
        }

        @Override
        public boolean read(JSONObject o)
        {
            String i = o.optString("c");
            if (i == null)
                return false;

            instrument.instrument = BrainOut.ContentMgr.get(i, Instrument.class);
            if (instrument.instrument == null)
                return false;

            JSONObject u = o.optJSONObject("u");
            if (u != null)
            {
                for (String key : u.keySet())
                {
                    String v = u.optString(key);
                    if (v == null)
                        continue;
                    Upgrade up = BrainOut.ContentMgr.get(v, Upgrade.class);
                    if (up == null)
                        continue;
                    instrument.upgrades.put(key, up);
                }
            }

            String s = o.optString("s");
            if (s != null && !"".equals(s))
            {
                instrument.skin = BrainOut.ContentMgr.get(s, Skin.class);
                if (instrument.skin == null)
                    instrument.skin = instrument.instrument.getDefaultSkin();
            }
            else
            {
                instrument.skin = instrument.instrument.getDefaultSkin();
            }

            rounds = o.optInt("r", 0);
            chambered = o.optInt("ch", 0);

            JSONObject m = o.optJSONObject("m");
            if (m != null)
            {
                magazines = new IntMap<>();

                for (String key : m.keySet())
                {
                    JSONArray a = m.optJSONArray(key);

                    if (a == null)
                        continue;

                    try
                    {
                        int id = Integer.parseInt(key);
                        magazines.put(id, new Magazine(a.optInt(0), a.optInt(1)));
                    }
                    catch (NumberFormatException ignored)
                    {}

                }
            }

            return true;
        }
    }

    public static class WalkietalkieContainerItem extends ContainerItem
    {
        public Walkietalkie cnt;
        public int frequency;

        public WalkietalkieContainerItem(int quality) { super(quality);}
        public WalkietalkieContainerItem(Walkietalkie cnt, int frequency, int quality)
        {
            super(quality);
            this.cnt = cnt;
            this.frequency = frequency;
        }

        @Override
        public void write(JSONObject o)
        {
            o.put("f", frequency);
            o.put("c", cnt.getID());
        }

        @Override
        public boolean isValid()
        {
            return cnt != null;
        }

        @Override
        public boolean read(JSONObject o)
        {
            String c = o.optString("c");
            if (c == null)
                return false;

            frequency = o.optInt("f", Walkietalkie.getRandomFrequency());

            cnt = BrainOut.ContentMgr.get(c, Walkietalkie.class);
            return cnt != null;
        }
    }

    public void write(JSONArray o)
    {
        for (ContainerItem item : items)
        {
            JSONObject entry = new JSONObject();
            item.write(entry);
            o.put(entry);
        }
    }

    public void read(JSONArray o)
    {
        items.clear();

        for (int i = 0, t = o.length(); i < t; i++)
        {
            JSONObject entry = o.optJSONObject(i);
            String c = entry.optString("c");
            if (c == null)
                continue;
            Content cnt = BrainOut.ContentMgr.get(c, Content.class);
            if (cnt == null)
                continue;

            int quality = entry.optInt("q", -1);

            ContainerItem item;
            if (cnt instanceof Instrument)
            {
                item = new InstrumentContainerItem(quality);
            }
            else if (cnt instanceof DecayConsumableContent)
            {
                item = new DecayConsumableContainerItem(quality);
            }
            else if (cnt instanceof Walkietalkie)
            {
                item = new WalkietalkieContainerItem(quality);
            }
            else if (cnt instanceof ConsumableContent)
            {
                item = new ConsumableContainerItem(quality);
            }
            else
            {
                continue;
            }

            if (item.read(entry))
            {
                items.add(item);
            }
        }
    }

    public void clear()
    {
        items.clear();
    }
}
