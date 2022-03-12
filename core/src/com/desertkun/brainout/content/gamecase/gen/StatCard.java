package com.desertkun.brainout.content.gamecase.gen;

import com.desertkun.brainout.data.gamecase.CaseData;
import com.desertkun.brainout.data.gamecase.gen.CardData;
import com.desertkun.brainout.data.gamecase.gen.StatCardData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.gamecase.gen.StatCard")
public class StatCard extends Card
{
    @Override
    public CardData getCard(CaseData caseData, String dimension)
    {
        return new StatCardData(this, caseData, dimension);
    }
}
