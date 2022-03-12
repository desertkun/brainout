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
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.utils.ContentImage;

@Reflect("content.quest.task.TakeOutRandomItem")
public class ClientTakeOutRandomItem extends TakeOutRandomItem implements ClientTask
{
    private int random = -1;

    @Override
    public void renderIcon(WidgetGroup to)
    {
        ConsumableContent item = getItem(BrainOutClient.ClientController.getMyAccount());
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
    protected long getCurrentTime()
    {
        return BrainOutClient.ClientController.getServerTime();
    }

    @Override
    protected int getDateStableRandom(String account)
    {
        if (random != -1)
            return random;

        random = super.getDateStableRandom(account);
        return random;
    }

    @Override
    public String getLocalizedName()
    {
        ConsumableContent item = getItem(BrainOutClient.ClientController.getMyAccount());
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

        return L.get("QUEST_TASK_TAKE_ITEM", title, String.valueOf(getTarget(
            BrainOutClient.ClientController.getMyAccount())));
    }

    @Override
    public String getShortLocalizedName()
    {
        ConsumableContent item = getItem(BrainOutClient.ClientController.getMyAccount());
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

        ConsumableContent it = getItem(BrainOutClient.ClientController.getMyAccount());
        if (it == null)
            return false;

        return item.getContent().getID().equals(it.getID());
    }
}
