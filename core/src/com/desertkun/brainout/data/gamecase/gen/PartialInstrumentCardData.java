package com.desertkun.brainout.data.gamecase.gen;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.gamecase.gen.ContentCard;
import com.desertkun.brainout.content.gamecase.gen.PartialInstrumentCard;
import com.desertkun.brainout.data.gamecase.CaseData;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.gamecase.gen.PartialInstrumentCardData")
public class PartialInstrumentCardData extends ContentCardData
{
    private int parts;

    public PartialInstrumentCardData(PartialInstrumentCard card, CaseData caseData, String dimension)
    {
        super(card, caseData, dimension);
    }

    public int getParts()
    {
        return parts;
    }

    public void setParts(int parts)
    {
        this.parts = parts;
    }

    @Override
    public void write(Json json)
    {
        super.write(json);

        json.writeValue("parts", parts);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        parts = jsonData.getInt("parts", 1);
    }
}
