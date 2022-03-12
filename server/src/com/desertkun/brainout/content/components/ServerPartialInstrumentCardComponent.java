package com.desertkun.brainout.content.components;

import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.content.ContentLockTree;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.shop.InstrumentSlotItem;
import com.desertkun.brainout.data.gamecase.gen.CardData;
import com.desertkun.brainout.data.gamecase.gen.ContentCardData;
import com.desertkun.brainout.data.gamecase.gen.PartialInstrumentCardData;
import com.desertkun.brainout.online.UserProfile;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ServerPartialInstrumentCardComponent")
public class ServerPartialInstrumentCardComponent extends ServerContentCardComponent
{
    @Override
    public void apply(PlayerClient client, UserProfile profile, CardData cardData)
    {
        if (!(cardData instanceof ContentCardData))
            return;

        PartialInstrumentCardData contentCardData = ((PartialInstrumentCardData) cardData);

        InstrumentSlotItem instrumentSlot = ((InstrumentSlotItem) contentCardData.getCardContent());
        ContentLockTree.LockItem lockItem = instrumentSlot.getLockItem();

        if (lockItem == null)
        {
            return;
        }

        Instrument instrument = instrumentSlot.getInstrument();
        int need = lockItem.getParam();

        if (profile != null)
        {
            float stat = profile.addStat(instrument.getPartsStat(), contentCardData.getAmount(), true);
            contentCardData.setParts(((int) stat));

            client.resourceEvent(1,
                "card",
                cardData.getCaseData().getCase().getID(),
                ((ContentCardData) cardData).getCard().getID());

            client.resourceEvent(contentCardData.getAmount(),
                "item",
                "case-card",
                contentCardData.getCardContent().getID());
        }
    }
}
