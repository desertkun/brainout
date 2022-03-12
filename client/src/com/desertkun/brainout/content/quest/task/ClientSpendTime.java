package com.desertkun.brainout.content.quest.task;

import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.quest.task.SpendTime")
public class ClientSpendTime extends SpendTime implements ClientTask
{
    @Override
    public void renderIcon(WidgetGroup to)
    {
        Image iconImage = new Image(BrainOutClient.Skin, "stats-time-spent");
        iconImage.setTouchable(Touchable.disabled);
        iconImage.setScaling(Scaling.none);
        iconImage.setFillParent(true);
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
    public String getLocalizedName()
    {
        return L.get("QUEST_TASK_SPEND_TIME",
            String.valueOf(getTarget(BrainOutClient.ClientController.getMyAccount())));
    }

    @Override
    public String getShortLocalizedName()
    {
        return L.get("QUEST_TASK_SPEND_TIME",
            String.valueOf(getTarget(BrainOutClient.ClientController.getMyAccount())));
    }

    @Override
    public boolean showInSummaryScreen()
    {
        return false;
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
        return false;
    }
}
