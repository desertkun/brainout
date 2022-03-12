package com.desertkun.brainout.content.components;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.L;
import com.desertkun.brainout.content.OwnableContent;
import com.desertkun.brainout.content.Shader;
import com.desertkun.brainout.content.Skin;
import com.desertkun.brainout.content.gamecase.Case;
import com.desertkun.brainout.content.gamecase.gen.Card;
import com.desertkun.brainout.content.gamecase.gen.ContentCard;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.instrument.InstrumentSkin;
import com.desertkun.brainout.content.shop.InstrumentSlotItem;
import com.desertkun.brainout.data.gamecase.gen.CardData;
import com.desertkun.brainout.data.gamecase.gen.ContentCardData;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.menu.ui.InstrumentIcon;

import com.desertkun.brainout.menu.ui.ShaderedActor;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ClientContentCardComponent")
public class ClientContentCardComponent extends ClientCardComponent
{
    @Override
    public void drawIcon(Table renderTo, CardData cardData)
    {
        if (!(cardData instanceof ContentCardData))
            return;

        ContentCardData contentCardData = ((ContentCardData) cardData);

        OwnableContent content = contentCardData.getCardContent();

        if (content.hasComponent(IconComponent.class))
        {
            IconComponent iconComponent = content.getComponent(IconComponent.class);

            TextureAtlas.AtlasRegion iconSprite = iconComponent.getIcon("big-icon", null);

            if (iconSprite != null)
            {
                Image iconImage = new Image(iconSprite);
                iconImage.setScaling(Scaling.none);

                renderTo.add(iconImage).expand().fill().row();
                return;
            }
        }

        if (BrainOut.R.instanceOf(InstrumentSlotItem.class, content))
        {
            InstrumentSlotItem isi = ((InstrumentSlotItem) content);

            renderInstrument(renderTo, isi.getInstrument(), isi.getDefaultSkin(), cardData);
            return;
        }

        if (BrainOut.R.instanceOf(InstrumentSkin.class, content))
        {
            InstrumentSkin skin = ((InstrumentSkin) content);
            InstrumentSlotItem slotItem = skin.getSlotItem();

            if (slotItem != null)
            {
                renderInstrument(renderTo, slotItem.getInstrument(), skin, cardData);
            }
            return;
        }

        if (content.hasComponent(IconComponent.class))
        {
            IconComponent iconComponent = content.getComponent(IconComponent.class);

            TextureAtlas.AtlasRegion icon = iconComponent.getIcon();
            if (icon != null)
            {
                Image iconImage = new Image(icon);
                iconImage.setScaling(Scaling.none);
                renderTo.add(iconImage).expand().fill().row();
            }
            return;
        }

        if (content.hasComponent(AnimatedIconComponent.class))
        {
            AnimatedIconComponent iconComponent = content.getComponent(AnimatedIconComponent.class);

            Image iconImage = new Image();
            iconImage.setScaling(Scaling.none);
            iconComponent.setupImage(iconImage);

            renderTo.add(iconImage).expand().fill().row();
        }
    }

    @Override
    public void drawIcon(Table renderTo, Card card)
    {
        if (!(card instanceof ContentCard))
            return;

        ContentCardComponent cmp = card.getComponentFrom(ContentCardComponent.class);

        OwnableContent content = cmp.getOwnableContent();

        if (content.hasComponent(IconComponent.class))
        {
            IconComponent iconComponent = content.getComponent(IconComponent.class);

            TextureAtlas.AtlasRegion iconSprite = iconComponent.getIcon("big-icon", null);

            if (iconSprite != null)
            {
                Image iconImage = new Image(iconSprite);
                iconImage.setScaling(Scaling.none);

                renderTo.add(iconImage).expand().fill().row();
                return;
            }
        }

        if (BrainOut.R.instanceOf(InstrumentSlotItem.class, content))
        {
            InstrumentSlotItem isi = ((InstrumentSlotItem) content);

            renderInstrument(renderTo, isi.getInstrument(), isi.getDefaultSkin(), card);
            return;
        }

        if (BrainOut.R.instanceOf(InstrumentSkin.class, content))
        {
            InstrumentSkin skin = ((InstrumentSkin) content);
            InstrumentSlotItem slotItem = skin.getSlotItem();

            if (slotItem != null)
            {
                renderInstrument(renderTo, slotItem.getInstrument(), skin, card);
            }
            return;
        }

        if (content.hasComponent(IconComponent.class))
        {
            IconComponent iconComponent = content.getComponent(IconComponent.class);

            Image iconImage = new Image(iconComponent.getIcon());
            iconImage.setScaling(Scaling.none);

            renderTo.add(iconImage).expand().fill().row();
            return;
        }

        if (content.hasComponent(AnimatedIconComponent.class))
        {
            AnimatedIconComponent iconComponent = content.getComponent(AnimatedIconComponent.class);

            Image iconImage = new Image();
            iconImage.setScaling(Scaling.none);
            iconComponent.setupImage(iconImage);

            renderTo.add(iconImage).expand().fill().row();
        }
    }

    public String getTitle(CardData cardData)
    {
        ContentCardData contentCardData = ((ContentCardData) cardData);
        OwnableContent cardContent = contentCardData.getCardContent();
        ContentCardComponent c = contentCardData.getCard().getComponentFrom(ContentCardComponent.class);
        int amount = c != null ? c.getAmount() : contentCardData.getAmount();

        if (BrainOut.R.instanceOf(Case.class, cardContent))
        {
            return "x" + amount;
        }

        return (amount > 1 ? "x" + amount + " ": "") + cardContent.getTitle().get();
    }

    @Override
    public String getDescription(CardData cardData)
    {
        ContentCardData contentCardData = ((ContentCardData) cardData);
        OwnableContent content = contentCardData.getCardContent();

        if (BrainOut.R.instanceOf(InstrumentSkin.class, content))
        {

            return L.get("CARD_TITLE_SKIN");
        }

        return super.getDescription(cardData);
    }

    protected void renderInstrument(Table renderTo, Instrument instrument, Skin skin, CardData cardData)
    {
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
        instrumentIcon.setBounds(0, 0, 192, 64);
        instrumentIcon.init();

        ShaderedActor sh = new ShaderedActor(instrumentIcon, blackShader);

        WidgetGroup root = new WidgetGroup();
        root.addActor(sh);

        InstrumentIcon orig = new InstrumentIcon(info, 2.0f * scale, true);
        orig.setBounds(0, 4, 192, 64);
        orig.init();

        root.addActor(orig);

        renderTo.add(root).size(196, 68).expand().row();
    }

    protected void renderInstrument(Table renderTo, Instrument instrument, Skin skin, Card cardData)
    {
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

        InstrumentIcon instrumentIcon = new InstrumentIcon(info, 1.0f * scale, true);
        instrumentIcon.setBounds(0, 0, 96, 32);
        instrumentIcon.init();

        ShaderedActor sh = new ShaderedActor(instrumentIcon, blackShader);

        WidgetGroup root = new WidgetGroup();
        root.addActor(sh);

        InstrumentIcon orig = new InstrumentIcon(info, 1.0f * scale, true);
        orig.setBounds(0, 2, 96, 32);
        orig.init();

        root.addActor(orig);

        renderTo.add(root).size(98, 34).expand().row();
    }
}
