package com.desertkun.brainout.online;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.OrderedMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.OwnableContent;
import com.desertkun.brainout.content.Layout;
import com.desertkun.brainout.content.instrument.Weapon;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

public class UserProfile
{
    protected ObjectMap<String, Integer> items;
    protected ObjectMap<String, Float> stats;
    protected ObjectMap<String, String> selectedSlots;
    protected ObjectSet<String> badges;
    protected ObjectSet<String> favorites;
    protected ObjectMap<Integer, Trophy> trophies;
    protected String name;
    protected String avatar;
    protected String clanAvatar;
    protected String clanId;
    protected String currency;
    protected ObjectMap<String, Limits> limits;
    protected String layout;

    private long lastDailyClaim;
    private long deactivated;

    public class Limits
    {
        public long time;
        public int amount;
    }

    private boolean loaded;

    public static final int MAX_TROPHIES = 12;

    public UserProfile()
    {
        this.items = new ObjectMap<>();
        this.stats = new ObjectMap<>();
        this.badges = new ObjectSet<>();
        this.favorites = new ObjectSet<>();
        this.trophies = new ObjectMap<>();
        this.selectedSlots = new ObjectMap<>();
        this.limits = new ObjectMap<>();

        this.name = "Player";
        this.avatar = "";
        this.clanAvatar = "";
        this.clanId = "";
        this.currency = "";
        this.lastDailyClaim = 0;
        this.deactivated = 0;
        this.layout = "layout-1";

        loaded = false;
    }

    public void write(JSONObject data)
    {
        data.put("name", name);
        data.put("avatar", avatar);
        data.put("clan-avatar", clanAvatar);
        data.put("clan-id", clanId);
        data.put("currency", currency);
        data.put("layout", layout);

        writeExt(data);
    }

    public void writeExt(JSONObject ext)
    {
        JSONObject items = new JSONObject();
        ext.put("items", items);

        for (ObjectMap.Entry<String, Integer> entry : this.items)
        {
            items.put(entry.key, entry.value);
        }

        JSONObject stats = new JSONObject();
        ext.put("stats", stats);

        for (ObjectMap.Entry<String, Float> entry : this.stats)
        {
            stats.put(entry.key, (double)entry.value);
        }

        JSONObject slots = new JSONObject();
        ext.put("slots", slots);

        for (ObjectMap.Entry<String, String> entry : selectedSlots)
        {
            slots.put(entry.key, entry.value);
        }

        JSONArray badges = new JSONArray();
        ext.put("badges", badges);

        for (String badge : this.badges)
        {
            badges.put(badge);
        }

        JSONArray favorites = new JSONArray();
        ext.put("favorites", favorites);

        for (String favorite : this.favorites)
        {
            favorites.put(favorite);
        }

        JSONObject trophies = new JSONObject();
        ext.put("trophies", trophies);

        for (int i = 0; i < MAX_TROPHIES; i++)
        {
            Trophy trophy = this.trophies.get(i);

            if (trophy != null)
            {
                try
                {
                    if (trophy.isValid())
                    {
                        trophies.put(String.valueOf(i), trophy.write());
                    }
                }
                catch (Exception ignored)
                {
                    trophies.put(String.valueOf(i), "");
                }
            }
            else
            {
                trophies.put(String.valueOf(i), "");
            }
        }

        for (ObjectMap.Entry<Integer, Trophy> entry : this.trophies)
        {
            try
            {
                if (entry.value.isValid())
                {
                    trophies.put(String.valueOf(entry.key), entry.value.write());
                }
            }
            catch (Exception ignored)
            {

            }
        }

        ext.put("last-daily-claim", getLastDailyClaim());
        ext.put("deactivated", getDeactivated());

        JSONObject limits = new JSONObject();
        ext.put("limits", limits);

        for (ObjectMap.Entry<String, Limits> entry : this.limits)
        {
            long now = System.currentTimeMillis() / 1000L;

            if (now > entry.value.time)
                continue;

            JSONObject limit = new JSONObject();

            limit.put("time", entry.value.time);
            limit.put("amount", entry.value.amount);

            limits.put(entry.key, limit);
        }
    }

    public String getName()
    {
        return name;
    }

    public void read(JSONObject object)
    {
        clear();

        this.name = object.optString("name", object.optString("nickname", "Anonymous"));
        this.avatar = object.optString("avatar", "");
        this.clanAvatar = object.optString("clan-avatar", "");
        this.clanId = object.optString("clan-id", "");
        this.currency = object.optString("currency", "USD");
        this.lastDailyClaim = object.optLong("last-daily-claim", 0);
        this.deactivated = object.optLong("deactivated", 0);
        this.layout = object.optString("layout", "layout-1");

        readExt(object);

        loaded = true;
    }

    public void readExt(JSONObject ext)
    {
        if (ext.has("items"))
        {
            JSONObject items = (JSONObject)ext.get("items");

            Iterator keys = items.keys();
            while (keys.hasNext())
            {
                Object itemKey = keys.next();

                String item = itemKey.toString();
                Object itemValue = items.get(item);

                if (itemValue instanceof Integer)
                {
                    Integer integer = ((Integer) itemValue);

                    addItem(item, integer);
                }
            }
        }

        if (ext.has("slots"))
        {
            JSONObject slots = (JSONObject)ext.get("slots");

            Iterator keys = slots.keys();
            while (keys.hasNext())
            {
                Object itemKey = keys.next();
                String key = itemKey.toString();

                setSelection(key, slots.getString(key));
            }

        }

        if (ext.has("stats"))
        {
            JSONObject stats = (JSONObject)ext.get("stats");

            Iterator keys = stats.keys();
            while (keys.hasNext())
            {
                Object itemKey = keys.next();

                String item = itemKey.toString();
                Object itemValue = stats.get(item);

                if (itemValue instanceof Integer)
                {
                    Integer integer = ((Integer) itemValue);
                    addStat(item, integer, false);
                }
                else
                if (itemValue instanceof Double)
                {
                    Double d = ((Double) itemValue);
                    addStat(item, (float)(double)d, false);
                }
            }
        }

        if (ext.has("badges"))
        {
            this.badges.clear();

            JSONArray badges = ext.getJSONArray("badges");

            for (int i = 0, r = badges.length(); i < r; i++)
            {
                this.badges.add(badges.getString(i));
            }
        }

        if (ext.has("favorites"))
        {
            this.favorites.clear();

            JSONArray favorites = ext.getJSONArray("favorites");

            for (int i = 0, r = favorites.length(); i < r; i++)
            {
                this.favorites.add(favorites.getString(i));
            }
        }

        if (ext.has("trophies"))
        {
            this.trophies.clear();

            JSONObject trophies = (JSONObject)ext.get("trophies");

            Iterator keys = trophies.keys();
            while (keys.hasNext())
            {
                Object itemKey = keys.next();
                String key = itemKey.toString();
                Integer id = Integer.valueOf(key);

                JSONObject ob = trophies.optJSONObject(key);

                if (ob == null)
                    continue;

                Trophy trophy = new Trophy();
                trophy.read(ob);

                if (trophy.getInfo().instrument == null || trophy.getInfo().skin == null ||
                    trophy.getInfo().upgrades == null)
                        continue;

                trophy.setIndex(id);

                this.trophies.put(id, trophy);
            }

        }


        if (ext.has(Constants.User.LEVEL))
        {
            getStats().put(Constants.User.LEVEL, (float)ext.getInt(Constants.User.LEVEL));
        }

        if (!getStats().containsKey("level"))
        {
            getStats().put(Constants.User.LEVEL, 1.0f);
        }

        if (ext.has("limits"))
        {
            JSONObject limits = (JSONObject)ext.get("limits");

            for (String limitId : limits.keySet())
            {
                JSONObject limitValue = limits.optJSONObject(limitId);

                if (limitValue != null)
                {
                    Limits limit = new Limits();

                    limit.time = limitValue.optLong("time");
                    limit.amount = limitValue.optInt("amount");

                    this.limits.put(limitId, limit);
                }
            }
        }
    }

    public void addItem(String item, int amount)
    {
        Integer have = items.get(item);

        if (have == null)
        {
            items.put(item, amount);
        }
        else
        {
            items.put(item, have + amount);
        }
    }

    public void addItem(OwnableContent item, int amount)
    {
        addItem(item.getID(), amount);
    }

    public void setStatTo(String stat, int amount, boolean notify)
    {
        float oldAmount = stats.get(stat, 0.0f);
        stats.put(stat, Math.max(oldAmount, amount));

        if (notify)
        {
            statUpdated(stat, amount, amount - oldAmount);
        }
    }

    public float addStat(String stat, float amount, boolean notify)
    {
        Float have = stats.get(stat);
        float a;

        if (have == null)
        {
            a = amount;
            stats.put(stat, a);
        }
        else
        {
            a = have + amount;
            stats.put(stat, a);
        }

        if (notify)
        {
            statUpdated(stat, a, amount);
        }

        return a;
    }

    public float setStat(String stat, float amount)
    {
        stats.put(stat, Math.max(amount, 0.0f));
        return amount;
    }

    public float descreaseStat(String stat, float amount)
    {
        Float have = stats.get(stat);
        float a;

        if (have == null)
        {
            return 0;
        }
        else
        {
            a = have - amount;
            stats.put(stat, Math.max(a, 0.0f));
        }

        return a;
    }

    public void statUpdated(String stat, float newValue, float added) {}

    public void clear()
    {
        items.clear();
        stats.clear();
        selectedSlots.clear();
        limits.clear();
    }

    public ObjectMap<String, Integer> getItems()
    {
        return items;
    }

    public <T extends OwnableContent> int getAmountOf(Class<T> classOf)
    {
        int amount = 0;

        for (ObjectMap.Entry<String, Integer> item : items)
        {
            String key = item.key;
            Content content = BrainOut.ContentMgr.get(key);

            if (BrainOut.R.instanceOf(classOf, content))
            {
                amount += item.value;
            }
        }

        return amount;
    }

    public <T extends OwnableContent> OrderedMap<T, Integer> getItemsOf(Class<T> classOf)
    {
        OrderedMap<T, Integer> map = new OrderedMap<>();

        for (ObjectMap.Entry<String, Integer> item : items)
        {
            String key = item.key;
            Content content = BrainOut.ContentMgr.get(key);

            if (BrainOut.R.instanceOf(classOf, content))
            {
                //noinspection unchecked
                map.put((T)content, item.value);
            }
        }

        return map;
    }


    public ObjectMap<String, Float> getStats()
    {
        return stats;
    }

    public ObjectSet<String> getBadges()
    {
        return badges;
    }

    public boolean hasBadge(String id)
    {
        return badges.contains(id);
    }

    public void addBadge(String id)
    {
        badges.add(id);
    }

    public void removeBadge(String id)
    {
        badges.remove(id);
    }

    public void addFavorite(String id)
    {
        favorites.add(id);
    }

    public void removeFavorite(String id)
    {
        favorites.remove(id);
    }

    public boolean isFavorite(String id)
    {
        return favorites.contains(id);
    }

    public int getInt(String stat, int defaultValue)
    {
        return (int)(float)getStats().get(stat, (float)defaultValue);
    }

    public boolean hasStat(String stat)
    {
        return getStats().containsKey(stat);
    }

    public void setInt(String stat, int value)
    {
        getStats().put(stat, (float)value);
    }

    public boolean hasItem(OwnableContent item)
    {
        return hasItem(item, true);
    }

    public boolean hasItem(OwnableContent item, boolean lockCheck)
    {
        if (item == null)
        {
            return false;
        }

        if (lockCheck && !item.isLocked(this) && item.isFree())
        {
            return true;
        }

        return itemsHave(item) > 0;
    }

    public int itemsHave(OwnableContent item)
    {
        Integer have = items.get(item.getID());
        return have != null ? have : 0;
    }

    public int getLevel(String kind, int def)
    {
        return getInt(kind, def);
    }

    public int getLevel(String kind)
    {
        return getLevel(kind, 1);
    }

    public ObjectMap<String, String> getSelectedSlots()
    {
        return selectedSlots;
    }

    public void setSelection(String item, String value)
    {
        if (value == null)
        {
            selectedSlots.remove(item);
        }
        else
        {
            selectedSlots.put(item, value);
        }
    }

    public void removeSelection(String item)
    {
        selectedSlots.put(item, "");
    }

    public String getSelection(String item)
    {
        String selection = selectedSlots.get(item);

        if (selection != null && selection.isEmpty())
        {
            return null;
        }

        return selection;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public boolean isLoaded()
    {
        return loaded;
    }

    public boolean addTrophy(Trophy trophy)
    {
        if (trophies.size >= MAX_TROPHIES)
        {
            return false;
        }

        int index = -1;

        for (int i = 0; i < MAX_TROPHIES; i++)
        {
            if (!trophies.containsKey(i))
            {
                index = i;
                break;
            }
        }

        if (index >= 0)
        {
            trophy.setIndex(index);
            trophies.put(index, trophy);
            return true;
        }

        return false;
    }

    public void removeTrophy(int index)
    {
        trophies.remove(index);
    }

    public Trophy getTrophy(int index)
    {
        return trophies.get(index);
    }

    public ObjectMap<Integer, Trophy> getTrophies()
    {
        return trophies;
    }

    public String getLevelDimension()
    {
        int level = getLevel(Constants.User.LEVEL, 0);
        int level_group = (level / 5) * 5;

        return "level-" + level_group;
    }

    public String getTimeSpentDimension()
    {
        int hours_spent = (int)(getStats().get(Constants.Stats.TIME_SPENT, 0.0f) / 60);
        int time_group = (hours_spent / 5) * 5;

        return "time-spent-" + time_group;
    }

    public String getEfficiencyDimension()
    {
        float efficiency = MathUtils.clamp(getStats().get(Constants.Stats.EFFICIENCY, 1.0f), 0.0f, 2.0f);
        int efficiency_group = (int)(efficiency * 10);
        return "efficiency-" + (efficiency_group / 10) + "-" + (efficiency_group % 10);
    }

    public long getLastDailyClaim()
    {
        return lastDailyClaim;
    }

    public long getDeactivated()
    {
        return deactivated;
    }

    public void setLastDailyClaim(long lastDailyClaim)
    {
        this.lastDailyClaim = lastDailyClaim;
    }

    public void setDeactivated(long deactivated)
    {
        this.deactivated = deactivated;
    }

    public String getAvatar()
    {
        return avatar;
    }

    public String getCurrency()
    {
        return currency;
    }

    public String getClanAvatar()
    {
        return clanAvatar;
    }

    public String getClanId()
    {
        if (clanId == null)
            return "";

        return clanId;
    }

    public boolean isParticipatingClan()
    {
        return clanId != null && !clanId.isEmpty();
    }

    public void setClan(String clanId, String avatar)
    {
        this.clanId = clanId != null ? clanId : "";
        this.clanAvatar = avatar != null ? avatar : "";
    }

    public void leaveClan()
    {
        this.clanId = "";
        this.clanAvatar = "";
    }

    public boolean acquireLimit(String item, int max)
    {
        Limits limits = this.limits.get(item);

        long now = System.currentTimeMillis() / 1000L;

        if (limits != null && now > limits.time)
        {
            limits = null;
        }

        if (limits == null)
        {
            limits = new Limits();
            limits.time = now + 86400;
            limits.amount = max - 1;

            this.limits.put(item, limits);
            return true;
        }
        else
        {
            if (limits.amount <= 0)
                return false;

            limits.amount--;

            return true;
        }
    }

    public boolean checkLimit(String item)
    {
        Limits limits = this.limits.get(item);

        if (limits == null)
            return true;

        long now = System.currentTimeMillis() / 1000L;

        return now > limits.time || limits.amount > 0;
    }

    public boolean isDeactivated()
    {
        return deactivated > 0;
    }

    public Layout getLayout()
    {
        return BrainOut.ContentMgr.get(layout, Layout.class);
    }

    public void setLayout(Layout layout)
    {
        this.layout = layout.getID();
    }

    public void setLayout(String layout)
    {
        this.layout = layout;
    }

    public Weapon.ShootMode getPreferableShootMode(String weaponId)
    {
        String selection = getSelection("shoot-mode-" + weaponId);
        if (selection == null)
            return null;

        try
        {
            return Weapon.ShootMode.valueOf(selection);
        }
        catch (IllegalArgumentException ignored)
        {
            return null;
        }
    }

    public void setPreferableShooMode(String weaponId, Weapon.ShootMode shootMode)
    {
        setSelection("shoot-mode-" + weaponId, shootMode.toString());
    }
}
