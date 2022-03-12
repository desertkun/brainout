package com.desertkun.brainout.content.gamecase.gen;

import com.desertkun.brainout.content.OwnableContent;
import com.desertkun.brainout.data.gamecase.CaseData;
import com.desertkun.brainout.data.gamecase.gen.CardData;
import com.desertkun.brainout.data.gamecase.gen.ContentCardData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.gamecase.gen.ContentCard")
public class ContentCard extends Card
{
    private OwnableContent content;

    @Override
    public CardData getCard(CaseData caseData, String dimension)
    {
        return new ContentCardData(this, caseData, dimension);
    }

    @Override
    protected boolean needLocalizationCheck()
    {
        return false;
    }

    public OwnableContent getContent()
    {
        return content;
    }

    public void setContent(OwnableContent content)
    {
        this.content = content;
    }
}
