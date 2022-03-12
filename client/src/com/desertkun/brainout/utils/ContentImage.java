package com.desertkun.brainout.utils;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.Shader;
import com.desertkun.brainout.content.Skin;
import com.desertkun.brainout.content.components.AnimatedIconComponent;
import com.desertkun.brainout.content.components.ClientItemDynamicIconComponent;
import com.desertkun.brainout.content.components.IconComponent;
import com.desertkun.brainout.content.components.InstrumentIconComponent;
import com.desertkun.brainout.content.consumable.impl.DecayConsumableItem;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.instrument.InstrumentSkin;
import com.desertkun.brainout.content.shop.InstrumentSlotItem;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.menu.ui.InstrumentIcon;
import com.desertkun.brainout.menu.ui.ShaderedActor;

public class ContentImage
{
    public static Actor RenderImage(Content content, Table renderTo, int amount)
    {
        if (content == null)
            return null;

        if (content.hasComponent(IconComponent.class))
        {
            IconComponent iconComponent = content.getComponent(IconComponent.class);

            ClientItemDynamicIconComponent dyn = content.getComponent(ClientItemDynamicIconComponent.class);

            TextureAtlas.AtlasRegion sprite = null;

            if (dyn != null)
            {
                for (Integer range : dyn.getRanges())
                {
                    if (amount >= range)
                    {
                        sprite = BrainOutClient.getRegion(dyn.getPrefix() + range);

                        if (sprite != null)
                        {
                            break;
                        }
                    }
                }

                if (sprite == null)
                {
                    sprite = BrainOutClient.getRegion(dyn.getFallback());
                }
            }

            if (sprite == null)
            {
                sprite = iconComponent.getIcon("big-icon", null);
            }

            if (sprite != null)
            {
                Image iconImage = new Image(sprite);
                iconImage.setScaling(Scaling.none);

                renderTo.add(iconImage).expand().fill().row();
                return iconImage;
            }
        }

        if (content.hasComponent(InstrumentIconComponent.class))
        {
            InstrumentIconComponent icon = content.getComponent(InstrumentIconComponent.class);
            return RenderInstrument(renderTo, icon.getInfo());
        }

        if (BrainOut.R.instanceOf(InstrumentSlotItem.class, content))
        {
            InstrumentSlotItem isi = ((InstrumentSlotItem) content);

            return RenderInstrument(renderTo, isi.getInstrument(), isi.getDefaultSkin());
        }

        if (BrainOut.R.instanceOf(InstrumentSkin.class, content))
        {
            InstrumentSkin skin = ((InstrumentSkin) content);
            if (!skin.isPreferIcon())
            {
                InstrumentSlotItem slotItem = skin.getSlotItem();

                if (slotItem != null)
                {
                    return RenderInstrument(renderTo, slotItem.getInstrument(), skin);
                }
                return null;
            }
        }

        if (content.hasComponent(IconComponent.class))
        {
            IconComponent iconComponent = content.getComponent(IconComponent.class);

            TextureAtlas.AtlasRegion iconRegion = iconComponent.getIcon();

            if (iconRegion == null)
            {
                throw new RuntimeException("Icon " + iconComponent.getItemName() + " cannot be found on content " +
                    content.getID());
            }

            Image iconImage = new Image(iconRegion);
            iconImage.setScaling(Scaling.none);

            renderTo.add(iconImage).expand().fill().row();
            return iconImage;
        }

        if (content.hasComponent(AnimatedIconComponent.class))
        {
            AnimatedIconComponent iconComponent = content.getComponent(AnimatedIconComponent.class);

            Image iconImage = new Image();
            iconComponent.setupImage(iconImage);
            iconImage.setScaling(Scaling.none);

            renderTo.add(iconImage).expand().fill().row();
            return iconImage;
        }

        return null;
    }

    public static void RenderUsesAndAmount(ConsumableRecord record, Table renderTo)
    {
        int amount = record.getAmount();
        int maxUses = 0;
        int leftUses = 0;

        if (record.getItem() instanceof DecayConsumableItem)
        {
            DecayConsumableItem decayItem = (DecayConsumableItem) record.getItem();
            maxUses = decayItem.getMax();
            leftUses = decayItem.getUses();
        }

        if (maxUses > 0 || record.hasQuality())
        {
            HorizontalGroup usageCharges = new HorizontalGroup();
            usageCharges.space(2);

            String full;

            if (record.hasQuality())
            {
                full = "icon-item-condition";
            }
            else
            {
                full = "icon-item-full";
            }

            if (maxUses == 0 && record.hasQuality())
            {
                maxUses = 1;
                leftUses = 1;
            }

            for (int i = 0; i < maxUses; i++)
            {
                Image usesCharge;

                if (i < leftUses)
                    usesCharge = new Image(BrainOutClient.Skin, full);
                else
                    usesCharge = new Image(BrainOutClient.Skin, "icon-item-empty");

                if (record.hasQuality())
                {
                    usesCharge.setColor(
                        MathUtils.lerp(1.0f, 0.f, (float)record.getQuality() / 100.0f),
                        MathUtils.lerp(0.0f, 1.f, (float)record.getQuality() / 100.0f),
                        0.f, 1.0f
                    );
                }

                usageCharges.addActor(usesCharge);
            }

            usageCharges.align(Align.left | Align.bottom);
            usageCharges.setFillParent(true);
            usageCharges.pad(6);

            renderTo.addActor(usageCharges);
        }

        if (amount > 1)
        {
            Container labelContainer = new Container();
            labelContainer.pad(4);
            labelContainer.setFillParent(true);
            labelContainer.align(Align.right | Align.bottom);

            String amountTitle = String.valueOf(amount);
            Label amountLabel = new Label(amountTitle, BrainOutClient.Skin, "title-yellow");

            labelContainer.setActor(amountLabel);

            renderTo.addActor(labelContainer);
        }
    }

    public static Image RenderStatImage(String stat, int amount, Table renderTo)
    {
        String icon = null;

        switch (stat)
        {
            case "valuables":
            {
                icon = "icon-valuables";
                break;
            }
            case Constants.User.GEARS:
            {
                switch (amount)
                {
                    case 2:
                    case 4:
                    case 6:
                    {
                        icon = "icon-reward-gears-" + amount;
                        break;
                    }
                    case 10:
                    case 15:
                    {
                        icon = "icon-gears-store-15";
                        break;
                    }
                    default:
                    {
                        icon = "icon-reward-gears-6";
                    }
                }
                break;
            }
            case Constants.User.NUCLEAR_MATERIAL:
            {
                switch (amount)
                {
                    case 1:
                    case 2:
                    case 3:
                    {
                        icon = "icon-nuclear-material-big-" + amount;
                        break;
                    }
                    case 400:
                        icon = "daily-case";
                        break;
                    case 600:
                        icon = "icon-nuclear-material-big-600";
                        break;
                    case 1000:
                        icon = "icon-nuclear-material-big-1000";
                        break;
                    default:
                    {
                        icon = "icon-nuclear-material-big-3";
                    }
                }
                break;
            }
            case Constants.User.SCORE:
            {
                icon = "icon-score";
                break;
            }
            case Constants.User.TECH_SCORE:
            {
                icon = "icon-tech-level-3";
                break;
            }
            case Constants.User.SKILLPOINTS:
            {
                icon = "skillpoints-big";
                break;
            }
            case "ru":
            {
                if (amount > 5000)
                {
                    icon = "icon-ru-5000";
                } else if (amount > 1000)
                {
                    icon = "icon-ru-1000";
                } else if (amount > 500)
                {
                    icon = "icon-ru-500";
                } else if (amount > 100)
                {
                    icon = "icon-ru-100";
                } else if (amount > 50)
                {
                    icon = "icon-ru-50";
                } else {
                    icon = "icon-ru-10";
                }

                break;
            }
        }

        if (icon != null)
        {
            TextureRegion region = BrainOutClient.getRegion(icon);

            if (region != null)
            {
                Image image = new Image(region);
                image.setScaling(Scaling.none);

                renderTo.add(image).minSize(region.getRegionWidth(), region.getRegionHeight())
                    .expand().fill().row();
                return image;
            }
        }

        return null;
    }


    public static Actor RenderInstrument(Table renderTo, InstrumentInfo info)
    {
        return RenderInstrument(renderTo, info, false);
    }

    public static Actor RenderInstrument(Table renderTo, InstrumentInfo info, boolean unloaded)
    {
        Shader blackShader = ((Shader) BrainOut.ContentMgr.get("shader-black"));

        InstrumentIcon instrumentIcon = new InstrumentIcon(info, 1.0f, true);
        instrumentIcon.setBounds(0, 0, 192, 64);
        instrumentIcon.init();

        ShaderedActor sh = new ShaderedActor(instrumentIcon, blackShader);

        WidgetGroup root = new WidgetGroup();
        root.addActor(sh);

        InstrumentIcon orig = new InstrumentIcon(info, 1.0f, true);
        orig.setBounds(0, 4, 192, 64);
        orig.init();

        if (unloaded)
        {
            orig.disableAttachment("clip");
        }

        root.addActor(orig);

        renderTo.add(root).size(196, 68).expand().row();

        return root;
    }

    public static Actor RenderInstrument(Table renderTo, Instrument instrument, Skin skin)
    {
        InstrumentInfo info = new InstrumentInfo();
        info.instrument = instrument;
        info.skin = skin;

        return RenderInstrument(renderTo, info);
    }
}
