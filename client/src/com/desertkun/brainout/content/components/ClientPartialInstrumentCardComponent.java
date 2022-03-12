package com.desertkun.brainout.content.components;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.ContentLockTree;
import com.desertkun.brainout.content.OwnableContent;
import com.desertkun.brainout.content.Shader;
import com.desertkun.brainout.content.Skin;
import com.desertkun.brainout.content.gamecase.gen.Card;
import com.desertkun.brainout.content.gamecase.gen.ContentCard;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.data.gamecase.gen.CardData;
import com.desertkun.brainout.data.gamecase.gen.PartialInstrumentCardData;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.menu.ui.InstrumentIcon;
import com.desertkun.brainout.menu.ui.PartialShaderedActor;
import com.desertkun.brainout.menu.ui.ShaderedActor;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ClientPartialInstrumentCardComponent")
public class ClientPartialInstrumentCardComponent extends ClientContentCardComponent
{
    @Override
    public String getGroupTitle(CardData cardData)
    {
        PartialInstrumentCardData contentCardData = ((PartialInstrumentCardData) cardData);
        ContentLockTree.LockItem lockItem = contentCardData.getCardContent().getLockItem();

        if (lockItem == null)
        {
            return super.getGroupTitle(cardData);
        }
        else
        {
            return String.valueOf(contentCardData.getParts()) + " / " + lockItem.getParam();
        }
    }

    @Override
    public String getDescription(CardData cardData)
    {
        return cardData.getCard().getTitle().get();
    }

    @Override
    protected void renderInstrument(Table renderTo, Instrument instrument, Skin skin, CardData cardData)
    {
        PartialInstrumentCardData contentCardData = ((PartialInstrumentCardData) cardData);
        ContentLockTree.LockItem lockItem = contentCardData.getCardContent().getLockItem();

        Shader grayShader = ((Shader) BrainOut.ContentMgr.get("shader-grayed"));
        Shader blackShader = ((Shader) BrainOut.ContentMgr.get("shader-black"));


        InstrumentInfo info = new InstrumentInfo();
        info.instrument = instrument;
        info.skin = skin;

        float scale;

        InstrumentAnimationComponent iac =
                instrument.getComponentFrom(InstrumentAnimationComponent.class);

        if (iac != null)
        {
            scale = iac.getIconScale();
        }
        else
        {
            scale = 1.0f;
        }

        InstrumentIcon instrumentIcon = new InstrumentIcon(info, 2.0f * scale, true);
        instrumentIcon.setBounds(0, 4, 192, 64);
        instrumentIcon.init();

        InstrumentIcon shadow = new InstrumentIcon(info, 2.0f * scale, true);
        shadow.setBounds(0, 0, 192, 64);
        shadow.init();

        ShaderedActor shadowShader = new ShaderedActor(shadow, blackShader);

        WidgetGroup root = new WidgetGroup();
        root.addActor(shadowShader);

        if (lockItem != null)
        {
            PartialShaderedActor sh = new PartialShaderedActor(instrumentIcon, grayShader,
                    contentCardData.getParts(), lockItem.getParam());

            root.addActor(sh);
        }
        else
        {
            root.addActor(instrumentIcon);
        }

        renderTo.add(root).size(196, 68).expand().row();
    }

    @Override
    protected void renderInstrument(Table renderTo, Instrument instrument, Skin skin, Card card)
    {
        ContentCardComponent cmp = card.getComponentFrom(ContentCardComponent.class);
        OwnableContent content = cmp.getOwnableContent();

        ContentLockTree.LockItem lockItem = content.getLockItem();

        Shader grayShader = ((Shader) BrainOut.ContentMgr.get("shader-grayed"));
        Shader blackShader = ((Shader) BrainOut.ContentMgr.get("shader-black"));


        InstrumentInfo info = new InstrumentInfo();
        info.instrument = instrument;
        info.skin = skin;

        float scale;

        InstrumentAnimationComponent iac =
                instrument.getComponentFrom(InstrumentAnimationComponent.class);

        if (iac != null)
        {
            scale = iac.getIconScale();
        }
        else
        {
            scale = 1.0f;
        }

        InstrumentIcon instrumentIcon = new InstrumentIcon(info, scale, true);
        instrumentIcon.setBounds(0, 2, 96, 32);
        instrumentIcon.init();

        InstrumentIcon shadow = new InstrumentIcon(info, scale, true);
        shadow.setBounds(0, 0, 96, 32);
        shadow.init();

        ShaderedActor shadowShader = new ShaderedActor(shadow, blackShader);

        WidgetGroup root = new WidgetGroup();
        root.addActor(shadowShader);

        if (lockItem != null)
        {
            int parts =
                (int)(float)BrainOutClient.ClientController.getUserProfile().getStats().get(
                    instrument.getPartsStat(), 0.0f) + 1;

            PartialShaderedActor sh = new PartialShaderedActor(instrumentIcon, grayShader,
                parts, lockItem.getParam());

            root.addActor(sh);
        }
        else
        {
            root.addActor(instrumentIcon);
        }

        renderTo.add(root).size(98, 34).expand().row();
    }
}
