package com.desertkun.brainout.content.quest.task;

import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.shop.InstrumentSlotItem;
import com.desertkun.brainout.content.shop.SlotItem;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.managers.ContentManager;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.utils.ContentImage;

@Reflect("content.quest.task.PaintItemOfCategory")
public class ClientPaintItemOfCategory extends PaintItemOfCategory implements ClientTask
{
    private String categoryName;

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
        return L.get("QUEST_TASK_PAINT_ITEM_OF_CATEGORY", L.get(categoryName), String.valueOf(getTarget(
            BrainOutClient.ClientController.getMyAccount()
        )));
    }

    @Override
    public String getShortLocalizedName()
    {
        return L.get(categoryName);
    }

    @Override
    protected void readTask(JsonValue jsonData)
    {
        super.readTask(jsonData);

        categoryName = jsonData.getString("categoryName");
    }

    @Override
    public boolean hasIcon()
    {
        return true;
    }

    @Override
    public void renderIcon(WidgetGroup to)
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

        Image iconImage = new Image(BrainOutClient.Skin, "icon-item-paint");
        iconImage.setTouchable(Touchable.disabled);
        iconImage.setScaling(Scaling.none);
        iconImage.setFillParent(true);
        iconImage.getColor().a = 0.6f;
        to.addActor(iconImage);
    }

    @Override
    public boolean showInSummaryScreen()
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

        if (item.getContent().getID().equals("consumable-item-paint"))
            return true;

        /*
        if (item instanceof InstrumentConsumableItem)
        {
            InstrumentConsumableItem ici = ((InstrumentConsumableItem) item);
            InstrumentData instrumentData = ici.getInstrumentData();
            if (instrumentData != null)
            {
                SlotItem slot = instrumentData.getInstrument().getSlotItem();
                if (slot != null)
                {
                    return slot.getCategory().equals(getCategory());
                }
            }
        }
         */

        return false;
    }
}
