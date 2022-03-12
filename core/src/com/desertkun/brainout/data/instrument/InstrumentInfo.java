package com.desertkun.brainout.data.instrument;

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.common.msg.InstrumentInfoPart;
import com.desertkun.brainout.content.Skin;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.upgrades.Upgrade;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.instrument.InstrumentInfo")
public class InstrumentInfo
{
    public Instrument instrument;
    public Skin skin;
    public OrderedMap<String, Upgrade> upgrades;

    public InstrumentInfo()
    {
        this.instrument = null;
        this.skin = null;
        this.upgrades = new OrderedMap<>();
    }

    public InstrumentInfo(InstrumentInfo copyFrom)
    {
        this.instrument = copyFrom.instrument;
        this.skin = copyFrom.skin;
        this.upgrades = new OrderedMap<>(copyFrom.upgrades);
    }

    public boolean equals(InstrumentInfo info)
    {
        return info.instrument == instrument &&
            info.skin == skin;
    }

    public void parse(InstrumentInfoPart msg)
    {
        this.instrument = BrainOut.ContentMgr.get(msg.instrument, Instrument.class);
        this.skin = BrainOut.ContentMgr.get(msg.skin, Skin.class);
        for (InstrumentInfoPart.InstrumentUpgrade upgrade : msg.upgrades)
        {
            Upgrade u = BrainOut.ContentMgr.get(upgrade.upgrade, Upgrade.class);

            if (u == null)
                continue;

            upgrades.put(upgrade.key, u);
        }
    }
}
