package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.components.interfaces.UpgradeComponent;
import com.desertkun.brainout.data.components.AnimationComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.Slot;
import com.esotericsoftware.spine.attachments.Attachment;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ReplaceSlotComponent")
public class ReplaceSlotComponent extends ContentComponent implements UpgradeComponent
{
    private ObjectMap<String, String> replace;
    private Class<? extends AnimationComponentData> classToReplace;

    public ReplaceSlotComponent()
    {
        this.replace = new ObjectMap<String, String>();
        this.classToReplace = AnimationComponentData.class;
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

    @SuppressWarnings("unchecked")
    @Override
    public void read(Json json, JsonValue jsonData)
    {
        if (jsonData.has("replace"))
        {
            for (JsonValue r: jsonData.get("replace"))
            {
                replace.put(r.name(), r.asString());
            }
        }

        if (jsonData.has("classToReplace"))
        {
            classToReplace = BrainOut.R.forName(jsonData.getString("classToReplace"));
        }
    }

    @Override
    public void upgrade(InstrumentData instrumentData)
    {
        AnimationComponentData ac = instrumentData.getComponent(classToReplace);

        if (ac != null)
        {
            upgradeSkeleton(ac.getSkeleton());
        }
    }

    public ObjectMap<String, String> getReplace()
    {
        return replace;
    }

    public void upgradeSkeleton(Skeleton skeleton)
    {
        ObjectMap<String, String> array = replace;

        for (ObjectMap.Entry<String, String> entry : array)
        {
            UpgradeSkeleton(skeleton, entry.key, entry.value);
        }
    }
    public static void UpgradeSkeleton(Skeleton skeleton, String key, String value)
    {
        Slot slot = skeleton.findSlot(key);
        if (slot != null)
        {
            Attachment attachment = skeleton.getAttachment(key, value);
            if (attachment != null)
            {
                slot.setAttachment(attachment);
            }
        }
    }

    @Override
    public boolean pre()
    {
        return false;
    }
}
