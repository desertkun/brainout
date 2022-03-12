package com.desertkun.brainout.data.instrument;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.instrument.Chip;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.instrument.ChipData")
public class ChipData extends InstrumentData
{
    public ChipData(Chip chip, String dimension)
    {
        super(chip, dimension);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);
    }

    @Override
    public void write(Json json)
    {
        super.write(json);
    }
}
