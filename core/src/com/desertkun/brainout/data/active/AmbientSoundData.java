package com.desertkun.brainout.data.active;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.active.AmbientSound;
import com.desertkun.brainout.inspection.InspectableProperty;
import com.desertkun.brainout.inspection.PropertyKind;
import com.desertkun.brainout.inspection.PropertyValue;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.active.AmbientSoundData")
public class AmbientSoundData extends PointData
{
    @InspectableProperty(name = "sound", kind = PropertyKind.string, value = PropertyValue.vString)
    public String sound;

    public AmbientSoundData(AmbientSound ambientSound, String dimension)
    {
        super(ambientSound, dimension);

        sound = ambientSound.getSoundEffect();
    }

    public String getSound()
    {
        return sound;
    }

    @Override
    public void write(Json json)
    {
        super.write(json);

        json.writeValue("sname", sound);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        sound = jsonData.getString("sname", ((AmbientSound) getContent()).getSoundEffect());
    }
}
