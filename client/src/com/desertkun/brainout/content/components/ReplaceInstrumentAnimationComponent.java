package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.components.interfaces.UpgradeComponent;
import com.desertkun.brainout.data.components.InstrumentAnimationComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ReplaceInstrumentAnimationComponent")
public class ReplaceInstrumentAnimationComponent extends ContentComponent implements UpgradeComponent
{
    private InstrumentAnimationStates states;

    public ReplaceInstrumentAnimationComponent()
    {
        states = new InstrumentAnimationStates();
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
    public void read(Json json, JsonValue jsonData)
    {
        JsonValue replace = jsonData.get("replace");
        states.read(json, replace);
    }

    @Override
    public void upgrade(InstrumentData instrumentData)
    {
        InstrumentAnimationComponentData iac =
            instrumentData.getComponentWithSubclass(InstrumentAnimationComponentData.class);

        if (iac != null)
        {
            iac.getStates().update(states);
        }
    }

    public InstrumentAnimationStates getStates()
    {
        return states;
    }

    @Override
    public boolean pre()
    {
        return false;
    }
}
