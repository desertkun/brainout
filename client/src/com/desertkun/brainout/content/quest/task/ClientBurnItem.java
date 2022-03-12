package com.desertkun.brainout.content.quest.task;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.components.IconComponent;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.utils.ContentImage;

@Reflect("content.quest.task.BurnItem")
public class ClientBurnItem extends BurnItem implements ClientTask
{
    @Override
    public void renderIcon(WidgetGroup to)
    {
        if (getItems().size == 0)
            return;
        
        Content item = getItems().first();
        
        if (item == null)
            return;

        IconComponent iconComponent = item.getComponent(IconComponent.class);

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

                if (getItems().size > 1)
                {
                    SequenceAction seq = Actions.sequence();

                    for (Content content : getItems())
                    {
                        IconComponent ii = content.getComponent(IconComponent.class);
                        if (ii == null)
                            continue;

                        TextureRegion bi = ii.getIcon("big-icon", null);
                        if (bi == null)
                            continue;

                        seq.addAction(Actions.delay(1.0f));
                        seq.addAction(Actions.run(() -> iconImage.setDrawable(new TextureRegionDrawable(bi))));
                    }

                    iconImage.addAction(Actions.repeat(RepeatAction.FOREVER, seq));
                }

                return;
            }

        }

        if (item instanceof Instrument)
        {
            Table holder = new Table();
            holder.setTouchable(Touchable.disabled);
            holder.setFillParent(true);

            Instrument instrument = ((Instrument) item);
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

        if (getItems().size > 1)
        {
            SequenceAction seq = Actions.sequence();

            for (Content content : getItems())
            {
                IconComponent ii = content.getComponent(IconComponent.class);
                if (ii == null)
                    continue;

                TextureRegion bi = ii.getIcon("icon", null);
                if (bi == null)
                    continue;

                seq.addAction(Actions.delay(1.0f));
                seq.addAction(Actions.run(() -> iconImage.setDrawable(new TextureRegionDrawable(bi))));
            }

            iconImage.addAction(Actions.repeat(RepeatAction.FOREVER, seq));
        }

        to.addActor(iconImage);
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
    public boolean showInSummaryScreen()
    {
        return true;
    }

    @Override
    public String getLocalizedName()
    {
        if (getItems().size == 0)
            return "";

        Content item = getItems().first();

        if (item == null)
            return "";
        
        String title = item.getTitle().get();

        if (item instanceof Instrument)
        {
            Instrument instrument = ((Instrument) item);
            if (instrument.getSlotItem() != null)
            {
                title = instrument.getSlotItem().getTitle().get();
            }
        }

        return L.get("QUEST_TASK_BURN_ITEM", title, String.valueOf(getTarget(
            BrainOutClient.ClientController.getMyAccount()
        )));
    }

    @Override
    public String getShortLocalizedName()
    {
        if (getItems().size == 0)
            return "";

        Content item = getItems().first();

        if (item == null)
            return "";

        String title = item.getTitle().get();

        if (item instanceof Instrument)
        {
            Instrument instrument = ((Instrument) item);
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

        return getItems().contains(item.getContent(), true);
    }
}
