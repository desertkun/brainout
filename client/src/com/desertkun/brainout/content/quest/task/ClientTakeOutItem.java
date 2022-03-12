package com.desertkun.brainout.content.quest.task;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.content.components.IconComponent;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.utils.ContentImage;

@Reflect("content.quest.task.TakeOutItem")
public class ClientTakeOutItem extends TakeOutItem implements ClientTask
{
    @Override
    public void renderIcon(WidgetGroup to)
    {
        if (getItem() == null)
            return;


        IconComponent iconComponent = getItem().getComponent(IconComponent.class);

        if (iconComponent != null)
        {
            TextureRegion bigIcon = iconComponent.getIcon("big-icon", null);

            if (bigIcon != null)
            {
                Image iconImage = new Image(bigIcon);
                iconImage.setTouchable(Touchable.disabled);
                iconImage.setScaling(Scaling.none);
                iconImage.setFillParent(true);

                to.addActor(iconImage);
                return;
            }

        }

        if (getItem() instanceof Instrument)
        {
            Table holder = new Table();
            holder.setTouchable(Touchable.disabled);
            holder.setFillParent(true);

            Instrument instrument = ((Instrument) getItem());
            ContentImage.RenderInstrument(holder, instrument, instrument.getDefaultSkin());

            to.addActor(holder);
            return;
        }
        if (iconComponent == null)
            return;

        TextureRegion icon = iconComponent.getIcon("icon");

        if (icon == null)
            return;

        Image iconImage = new Image(icon);
        iconImage.setTouchable(Touchable.disabled);
        iconImage.setScaling(Scaling.none);
        iconImage.setFillParent(true);

        to.addActor(iconImage);
    }

    @Override
    public boolean showInSummaryScreen()
    {
        return true;
    }

    @Override
    public boolean hasLocalizedName()
    {
        return true;
    }

    @Override
    public boolean hasRichLocalization()
    {
        return false;
    }

    @Override
    public String getLocalizedName()
    {
        String title = getItem().getTitle().get();

        if (getItem() instanceof Instrument)
        {
            Instrument instrument = ((Instrument) getItem());
            if (instrument.getSlotItem() != null)
            {
                title = instrument.getSlotItem().getTitle().get();
            }
        }

        return L.get("QUEST_TASK_TAKE_ITEM", title, String.valueOf(getTarget(
            BrainOutClient.ClientController.getMyAccount()
        )));
    }

    @Override
    public String getShortLocalizedName()
    {
        String title = getItem().getTitle().get();

        if (getItem() instanceof Instrument)
        {
            Instrument instrument = ((Instrument) getItem());
            if (instrument.getSlotItem() != null)
            {
                title = instrument.getSlotItem().getTitle().get();
            }
        }

        return title;
    }

    @Override
    public boolean hasIcon()
    {
        return true;
    }

    @Override
    public boolean hasProgress()
    {
        return true;
    }

    @Override
    public boolean isItemTaskRelated(ConsumableItem item)
    {
        if (isComplete(BrainOutClient.ClientController.getUserProfile(), BrainOutClient.ClientController.getMyAccount()))
            return false;

        return item.getContent() == getItem();
    }
}
