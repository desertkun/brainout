package com.desertkun.brainout.content.quest.task;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.components.IconComponent;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.shop.InstrumentSlotItem;
import com.desertkun.brainout.content.shop.SlotItem;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.utils.ContentImage;

@Reflect("content.quest.task.KillWith")
public class ClientKillWith extends KillWith implements ClientTask
{
    private String categoryName;

    @Override
    public void renderIcon(WidgetGroup to)
    {
        if (getWeapon() == null)
        {
            if (getCategory() != null)
            {
                Array<Content> insOfCategory = BrainOut.ContentMgr.queryContent(Instrument.class, check ->
                {
                    Instrument i = ((Instrument) check);
                    InstrumentSlotItem slot = i.getSlotItem();
                    if (slot == null || slot.getCategory() == null)
                        return false;
                    return slot.getCategory().equals(getCategory());
                });

                Table holder = new Table();
                holder.setTouchable(Touchable.disabled);
                holder.setFillParent(true);

                holder.addAction(Actions.repeat(RepeatAction.FOREVER,
                Actions.sequence(
                    Actions.run(() ->
                    {
                        holder.clearChildren();
                        Instrument instrument = ((Instrument) insOfCategory.random());
                        ContentImage.RenderInstrument(holder, instrument, instrument.getDefaultSkin());
                    }), Actions.delay(1.0f)
                )));

                to.addActor(holder);
            }

            return;
        }

        IconComponent iconComponent = getWeapon().getComponent(IconComponent.class);

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

        Table holder = new Table();
        holder.setTouchable(Touchable.disabled);
        holder.setFillParent(true);

        Instrument instrument = getWeapon();
        ContentImage.RenderInstrument(holder, instrument, instrument.getDefaultSkin());

        to.addActor(holder);
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
        if (getWeapon() != null)
        {
            String title = getWeapon().getTitle().get();
            if (getWeapon().getSlotItem() != null)
            {
                title = getWeapon().getSlotItem().getTitle().get();
            }

            return L.get("QUEST_TASK_KILL_WITH", title, String.valueOf(getTarget(
                BrainOutClient.ClientController.getMyAccount()
            )));
        }

        if (categoryName != null)
        {
            return L.get("QUEST_TASK_KILL_WITH", L.get(categoryName), String.valueOf(getTarget(
                BrainOutClient.ClientController.getMyAccount()
            )));
        }

        return "";
    }

    @Override
    public String getShortLocalizedName()
    {
        if (getWeapon() != null)
        {
            String title = getWeapon().getTitle().get();
            if (getWeapon().getSlotItem() != null)
            {
                title = getWeapon().getSlotItem().getTitle().get();
            }

            return title;
        }

        if (categoryName != null)
        {
            return L.get(categoryName);
        }

        return "";
    }

    @Override
    protected void readTask(JsonValue jsonData)
    {
        super.readTask(jsonData);

        categoryName = jsonData.getString("categoryName", null);
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
        /*
        if (isComplete(BrainOutClient.ClientController.getUserProfile(), BrainOutClient.ClientController.getMyAccount()))
            return false;

        if (item instanceof InstrumentConsumableItem)
        {
            InstrumentConsumableItem ici = ((InstrumentConsumableItem) item);
            InstrumentData instrumentData = ici.getInstrumentData();
            if (instrumentData != null)
            {
                SlotItem slot = instrumentData.getInstrument().getSlotItem();
                if (slot != null)
                {
                    if (getCategory() != null)
                    {
                        return slot.getCategory().equals(getCategory());
                    }
                }

                return getWeapon() == instrumentData.getInstrument();
            }
        }
        */

        return false;
    }
}
