package com.desertkun.brainout.online;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.math.MathUtils;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import org.anthillplatform.runtime.services.GameService;
import org.json.JSONObject;

public class RoomSettings
{
    private StringOption mode = new StringOption();
    private StringOption map = new StringOption();
    private BooleanOption keepMode = new BooleanOption();
    private BooleanOption disableBuilding = new BooleanOption();

    private int level = -1;
    private int subscribers = -1;

    private boolean myLevelOnly = true;
    private boolean ignoreMyLevelOnly = false;
    private int levelGap = 10;
    private boolean showFull = false;
    private boolean conflict = false;
    private String state = "active";
    private String warmup = null;
    private String newbie = null;
    private String party = null;
    private String region = null;
    private String preset = null;
    private String zone = null;

    public static class BooleanOption
    {
        public static BooleanOption YES = new BooleanOption();
        public static BooleanOption NO = new BooleanOption();
        public static BooleanOption UNSET = new BooleanOption();

        static
        {
            YES.define(true);
            NO.define(false);
        }

        private boolean defined = false;
        private boolean value = false;

        public boolean getValue()
        {
            return value;
        }

        public boolean isDefined()
        {
            return defined;
        }

        public void undefine()
        {
            this.defined = false;
            this.value = false;
        }

        public void define(boolean value)
        {
            this.defined = true;
            this.value = value;
        }

        @Override
        public String toString()
        {
            if (isDefined())
            {
                return getValue() ? L.get("MENU_YES") : L.get("MENU_NO");
            }

            return L.get("MENU_UNSET");
        }

        public void write(GameService.RoomSettings filer, String key)
        {
            if (isDefined())
            {
                filer.add(key, getValue());
            }
        }

        public void write(GameService.RoomsFilter filer, String key)
        {
            if (isDefined())
            {
                filer.putEquals(key, getValue());
            }
        }

        public void read(JSONObject obj, String key)
        {
            if (obj.has(key))
            {
                define(obj.getBoolean(key));
            }
            else
            {
                undefine();
            }
        }
    }

    public static class StringOption
    {
        private boolean defined = false;
        private String value = null;

        public String getValue()
        {
            return value;
        }

        public boolean isDefined()
        {
            return defined;
        }

        public void undefine()
        {
            this.defined = false;
            this.value = null;
        }

        public void define(String value)
        {
            this.defined = true;
            this.value = value;
        }

        public void write(GameService.RoomSettings settings, String key)
        {
            if (isDefined())
            {
                settings.add(key, getValue());
            }
        }

        public void write(GameService.RoomsFilter filer, String key)
        {
            if (isDefined())
            {
                filer.putEquals(key, getValue());
            }
        }

        public void read(JSONObject obj, String key)
        {
            if (obj.has(key))
            {
                define(obj.getString(key));
            }
            else
            {
                undefine();
            }
        }
    }

    public StringOption getMode()
    {
        return mode;
    }

    public StringOption getMap()
    {
        return map;
    }

    public BooleanOption getKeepMode()
    {
        return keepMode;
    }

    public BooleanOption getDisableBuilding()
    {
        return disableBuilding;
    }

    public void init(UserProfile userProfile, boolean newbies)
    {
        if (newbies)
        {
            newbie = userProfile.getLevel(Constants.User.LEVEL, 0) <= 25 ? "true" : "false";
        }
    }

    public void write(GameService.RoomsFilter filter)
    {
        getMode().write(filter, "mode");
        getMap().write(filter, "map");
        getKeepMode().write(filter, "keep-mode");
        getDisableBuilding().write(filter, "disable-building");

        filter.putEquals("state", state);

        if (warmup != null)
        {
            filter.putEquals("warmup", warmup);
        }

        if (newbie != null)
        {
            filter.putEquals("newbie", newbie);
        }

        if (levelGap < 100 && myLevelOnly && !ignoreMyLevelOnly && level > 0)
        {
            filter.putBetween("level", level - levelGap, level + levelGap);
        }
        else
        {
            if (levelGap < 100 && level > Constants.Other.LEVEL_PROTECT)
            {
                filter.putGreater("level", Constants.Other.LEVEL_PROTECT);
            }
        }

        if (subscribers > 0)
        {
            filter.putGreater("subscribers", subscribers);
        }

        if (party != null)
        {
            filter.putEquals("party", party);
        }

        if (zone != null)
        {
            filter.putEquals("zone", zone);
        }

        if (preset != null)
        {
                filter.putEquals("preset", preset);
        }
    }

    public void write(GameService.RoomSettings settings)
    {
        getMode().write(settings, "mode");
        getMap().write(settings, "map");
        getKeepMode().write(settings, "keep-mode");
        getDisableBuilding().write(settings, "disable-building");

        settings.add("state", state);

        if (newbie != null)
        {
            settings.add("newbie", newbie);
        }

        if (warmup != null)
        {
            settings.add("warmup", warmup);
        }

        if (level > 0)
        {
            settings.add("level", level);
        }

        if (subscribers > 0)
        {
            settings.add("subscribers", subscribers);
        }

        if (party != null)
        {
            settings.add("party", party);
        }

        if (zone != null)
        {
            settings.add("zone", zone);
        }

        if (preset != null)
        {
            settings.add("preset", preset);
        }
    }

    public void read(JSONObject json)
    {
        getMode().read(json, "mode");
        getMap().read(json, "map");
        getKeepMode().read(json, "keep-mode");
        getDisableBuilding().read(json, "disable-building");

        if (json.has("newbie"))
        {
            setNewbie(json.optString("newbie", null));
        }

        if (json.has("level"))
        {
            setLevel(json.optInt("level", -1));
        }

        if (json.has("party"))
        {
            setParty(json.optString("party", null));
        }

        if (json.has("subscribers"))
        {
            setSubscribers(json.optInt("subscribers", -1));
        }

        if (json.has("state"))
        {
            setState(json.optString("state", null));
        }

        if (json.has("zone"))
        {
            setZone(json.optString("zone", null));
        }

        if (json.has("preset"))
        {
            setPreset(json.optString("preset", null));
        }
    }

    public String getRegion()
    {
        return region;
    }

    public boolean isShowFull()
    {
        return showFull;
    }

    public boolean isConflict()
    {
        return conflict;
    }

    public String getNewbie()
    {
        return newbie;
    }

    public void setNewbie(String newbie)
    {
        this.newbie = newbie;
    }

    public void setConflict(boolean conflict)
    {
        this.conflict = conflict;
    }

    public void setPreset(String preset)
    {
        this.preset = preset;
    }

    public void setRegion(String region)
    {
        setRegion(region, true);
    }

    public void setRegion(String region, boolean updatePreferences)
    {
        this.region = region;

        if (updatePreferences)
        {

            Preferences preferences = Gdx.app.getPreferences("region");
            if (region != null)
            {
                preferences.putString("region", region);
            } else
            {
                preferences.remove("region");
            }
            preferences.flush();
        }
    }

    public void setShowFull(boolean showFull)
    {
        this.showFull = showFull;
    }

    public boolean isMyLevelOnly()
    {
        return myLevelOnly;
    }

    public void setMyLevelOnly(boolean myLevelOnly)
    {
        this.myLevelOnly = myLevelOnly;
    }

    public void setIgnoreMyLevelOnly(boolean ignoreMyLevelOnly)
    {
        this.ignoreMyLevelOnly = ignoreMyLevelOnly;
    }

    public int getLevelGap()
    {
        return levelGap;
    }

    public void setLevelGap(int levelGap)
    {
        this.levelGap = levelGap;
    }

    public int getLevel()
    {
        return level;
    }

    public void setSubscribers(int subscribers)
    {
        this.subscribers = subscribers;
    }

    public int getSubscribers()
    {
        return subscribers;
    }

    public void setLevel(int level)
    {
        this.level = level;
    }

    public String getState()
    {
        return state;
    }

    public String getZone()
    {
        return zone;
    }

    public void setZone(String zone)
    {
        this.zone = zone;
    }

    public void setState(String state)
    {
        this.state = state;
    }

    public String getParty()
    {
        return party;
    }

    public String getPreset()
    {
        return preset;
    }

    public void setParty(String party)
    {
        this.party = party;
    }

    public String getWarmup()
    {
        return warmup;
    }

    public void setWarmup(String warmup)
    {
        this.warmup = warmup;
    }
}
