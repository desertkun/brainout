package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.Skin;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.upgrades.Upgrade;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.InstrumentIconComponent")
public class InstrumentIconComponent extends ContentComponent
{
    private InstrumentInfo info;

    public InstrumentIconComponent()
    {
        info = new InstrumentInfo();
    }

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return null;
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonValue)
    {
        info.instrument = BrainOut.ContentMgr.get(jsonValue.getString("instrument"), Instrument.class);
        info.skin = BrainOut.ContentMgr.get(jsonValue.getString("skin", info.instrument.getDefaultSkin().getID()), Skin.class);

        if (jsonValue.has("upgrades"))
        {
            for (JsonValue value : jsonValue.get("upgrades"))
            {
                Upgrade u = BrainOut.ContentMgr.get(value.asString(), Upgrade.class);

                if (u == null)
                    continue;

                info.upgrades.put(value.name(), u);
            }
        }
    }

    public InstrumentInfo getInfo()
    {
        return info;
    }
}
