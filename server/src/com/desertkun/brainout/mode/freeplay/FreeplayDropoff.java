package com.desertkun.brainout.mode.freeplay;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.PlayerSkin;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.data.active.ItemData;
import org.json.JSONArray;
import org.json.JSONObject;

public class FreeplayDropoff
{
    private ObjectMap<String, Kind> kinds;

    public FreeplayDropoff(JSONObject from)
    {
        this.kinds = new ObjectMap<>();
        for (String key : from.keySet())
        {
            Kind kind = new Kind(from.optJSONArray(key));
            kinds.put(key, kind);
        }
    }

    public interface Generator
    {
        void generate(ItemData into);
    }

    public Array<Generator> generate(String kind)
    {
        return kinds.get(kind).generate();
    }

    private static class Kind
    {
        private Array<Array<OneOf>> spawns;

        private static class OneOf
        {
            private Array<FreeplayItems.TrackItem> items;

            public OneOf(JSONObject data)
            {
                this.items = new Array<>();
                JSONArray items = data.getJSONArray("items");

                for (int i = 0, t = items.length(); i < t; i++)
                {
                    JSONObject itm = items.getJSONObject(i);
                    String item = itm.getString("item");
                    Content content = BrainOutServer.ContentMgr.get(item, Content.class);

                    FreeplayItems.TrackItem trackItem;

                    if (content instanceof Instrument)
                    {
                        trackItem = new FreeplayItems.InstrumentTrackItem(itm, ((Instrument) content));
                    }
                    else if (content instanceof ConsumableContent)
                    {
                        trackItem = new FreeplayItems.ConsumableTrackItem(itm, ((ConsumableContent) content));
                    }
                    else if (content instanceof PlayerSkin)
                    {
                        trackItem = new FreeplayItems.SkinTrackItem(itm, ((PlayerSkin) content));
                    }
                    else
                    {
                        continue;
                    }

                    this.items.add(trackItem);
                }
            }

            public void generate(ItemData into)
            {
                for (FreeplayItems.TrackItem item : this.items)
                {
                    Array<ItemData> a = new Array<>();
                    a.add(into);
                    item.create(a, item.getAmount());
                }
            }
        }

        public Kind(JSONArray set)
        {
            this.spawns = new Array<>();

            for (int i = 0, t = set.length(); i < t; i++)
            {
                if (set.get(i) instanceof JSONObject)
                {
                    JSONObject o = set.getJSONObject(i);
                    OneOf oo = new OneOf(o);
                    Array<OneOf> ooo = new Array<>();
                    ooo.add(oo);
                    this.spawns.add(ooo);
                }
                else
                {
                    Array<OneOf> ooo = new Array<>();
                    JSONArray a = set.getJSONArray(i);
                    for (int k = 0, tt = a.length(); k < tt; k++)
                    {
                        JSONObject o = a.getJSONObject(k);
                        OneOf oo = new OneOf(o);
                        ooo.add(oo);
                    }
                    this.spawns.add(ooo);
                }
            }
        }

        public Array<Generator> generate()
        {
            Array<Generator> generators = new Array<>();

            for (Array<OneOf> spawn : this.spawns)
            {
                OneOf r = spawn.random();
                generators.add(r::generate);
            }

            return generators;
        }
    }
}
