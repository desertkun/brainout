package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.components.interfaces.UpgradeComponent;
import com.desertkun.brainout.data.components.WeaponAnimationComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.instrument.InstrumentData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.SuppressBulletAttachmentsComponent")
public class SuppressBulletAttachmentsComponent extends ContentComponent implements UpgradeComponent
{
    private Array<String> suppress;

    public SuppressBulletAttachmentsComponent()
    {
        suppress = new Array<String>();
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
        JsonValue suppress = jsonValue.get("suppress");

        if (suppress.isArray())
        {
            for (JsonValue value : suppress)
            {
                this.suppress.add(value.asString());
            }
        }
        else
        {
            this.suppress.add(suppress.asString());
        }
    }

    @Override
    public void upgrade(InstrumentData instrumentData)
    {
        WeaponAnimationComponentData cwd = instrumentData.getComponent(WeaponAnimationComponentData.class);

        if (cwd != null)
        {
            cwd.suppressAttachments(suppress);
        }
    }

    @Override
    public boolean pre()
    {
        return false;
    }
}
