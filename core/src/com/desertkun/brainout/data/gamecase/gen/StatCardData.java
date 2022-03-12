package com.desertkun.brainout.data.gamecase.gen;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.gamecase.gen.StatCard;

import com.desertkun.brainout.data.gamecase.CaseData;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.gamecase.gen.StatCardData")
public class StatCardData extends CardData<StatCard>
{
    private String stat;
    private int amount;

    public StatCardData(StatCard generator, CaseData caseData, String dimension)
    {
        super(generator, caseData, dimension);
    }

    @Override
    public void write(Json json)
    {
        super.write(json);

        json.writeValue("stat", stat);
        json.writeValue("amount", amount);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        this.stat = jsonData.getString("stat");
        this.amount = jsonData.getInt("amount");
    }

    public void setStat(String stat)
    {
        this.stat = stat;
    }

    public void setAmount(int amount)
    {
        this.amount = amount;
    }

    public String getStat()
    {
        return stat;
    }

    public int getAmount()
    {
        return amount;
    }
}
