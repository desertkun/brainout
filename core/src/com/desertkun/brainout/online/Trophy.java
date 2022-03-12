package com.desertkun.brainout.online;

import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.Skin;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.upgrades.Upgrade;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.data.interfaces.WithBadge;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

public class Trophy implements WithBadge
{
    private int index;
    private String ownerName;
    private String ownerId;
    private int ownerLevel;
    private int xp;
    
    private InstrumentInfo info;
    
    public Trophy()
    {
        this.ownerName = null;
        this.ownerId = null;
        this.ownerLevel = 1;
        this.info = new InstrumentInfo();
        this.xp = 0;
        this.index = 0;
    }
    
    public void read(JSONObject from)
    {
        ownerName = from.optString("ownerName");
        ownerId = from.optString("ownerId");
        ownerLevel = from.optInt("ownerLevel", 1);
        xp = from.optInt("xp", 0);
        
        info.instrument = ((Instrument) BrainOut.ContentMgr.get(from.getString("instrument")));
        info.skin = ((Skin) BrainOut.ContentMgr.get(from.getString("skin")));

        info.upgrades.clear();

        JSONObject upgrades = from.getJSONObject("upgrades");

        Iterator keys = upgrades.keys();
        while (keys.hasNext())
        {
            String key = keys.next().toString();
            Upgrade upgrade = ((Upgrade) BrainOut.ContentMgr.get(upgrades.getString(key)));

            info.upgrades.put(key, upgrade);
        }
    }
    
    public JSONObject write()
    {
        JSONObject to = new JSONObject();

        to.put("ownerName", ownerName);
        to.put("ownerId", ownerId);
        to.put("ownerLevel", ownerLevel);
        to.put("xp", xp);
        
        to.put("instrument", info.instrument.getID());
        to.put("skin", info.skin.getID());

        JSONObject upgrades = new JSONObject();
        for (ObjectMap.Entry<String, Upgrade> upgrade : info.upgrades)
        {
            if (upgrade == null || upgrade.key == null || upgrade.value == null)
                continue;

            upgrades.put(upgrade.key, upgrade.value.getID());
        }
        
        to.put("upgrades", upgrades);

        return to;
    }

    public String getOwnerName()
    {
        return ownerName;
    }

    public String getOwnerId()
    {
        return ownerId;
    }

    public int getOwnerLevel()
    {
        return ownerLevel;
    }

    public InstrumentInfo getInfo()
    {
        return info;
    }

    public void setOwner(String name, String id, int level)
    {
        this.ownerName = name;
        this.ownerId = id;
        this.ownerLevel = level;
    }

    public void setInfo(InstrumentInfo info)
    {
        this.info = info;
    }

    public int getXp()
    {
        return xp;
    }

    public void setXp(int xp)
    {
        this.xp = xp;
    }

    public void setIndex(int index)
    {
        this.index = index;
    }

    public int getIndex()
    {
        return index;
    }

    public String getBadgeId()
    {
        return "trophy-" + index;
    }

    @Override
    public boolean hasBadge(UserProfile profile, Involve involve)
    {
        return profile.hasBadge(getBadgeId());
    }

    public boolean isValid()
    {
        return info != null && info.instrument != null && info.skin != null;
    }
}
