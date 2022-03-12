package com.desertkun.brainout.data.gamecase.gen;

import com.desertkun.brainout.content.gamecase.gen.Card;
import com.desertkun.brainout.data.Data;
import com.desertkun.brainout.data.gamecase.CaseData;

public abstract class CardData<T extends Card> extends Data
{
    private final CaseData caseData;

    public CardData(Card card, CaseData caseData, String dimension)
    {
        super(card, dimension);

        this.caseData = caseData;
    }

    public T getCard()
    {
        //noinspection unchecked
        return (T)getContent();
    }

    public CaseData getCaseData()
    {
        return caseData;
    }
}
