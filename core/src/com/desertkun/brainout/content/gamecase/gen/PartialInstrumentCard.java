package com.desertkun.brainout.content.gamecase.gen;

import com.desertkun.brainout.data.gamecase.CaseData;
import com.desertkun.brainout.data.gamecase.gen.CardData;
import com.desertkun.brainout.data.gamecase.gen.PartialInstrumentCardData;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.gamecase.gen.PartialInstrumentCard")
public class PartialInstrumentCard extends ContentCard
{
    @Override
    public CardData getCard(CaseData caseData, String dimension)
    {
        return new PartialInstrumentCardData(this, caseData, dimension);
    }
}
