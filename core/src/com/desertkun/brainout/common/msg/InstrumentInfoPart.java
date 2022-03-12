package com.desertkun.brainout.common.msg;

import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.content.upgrades.Upgrade;
import com.desertkun.brainout.data.instrument.InstrumentInfo;

public class InstrumentInfoPart
{
    public static class InstrumentUpgrade
    {
        public String key;
        public String upgrade;
    }

    public String instrument;
    public String skin;
    public InstrumentUpgrade[] upgrades;

    public InstrumentInfoPart() {}
    public InstrumentInfoPart(InstrumentInfo info)
    {
        this.instrument = info.instrument.getID();
        this.skin = info.skin.getID();
        this.upgrades = new InstrumentUpgrade[info.upgrades.size];

        int i = 0;

        for (ObjectMap.Entry<String, Upgrade> entry : info.upgrades)
        {
            InstrumentUpgrade u = new InstrumentUpgrade();
            u.key = entry.key;
            u.upgrade = entry.value.getID();

            this.upgrades[i] = u;
            i++;
        }
    }
}
