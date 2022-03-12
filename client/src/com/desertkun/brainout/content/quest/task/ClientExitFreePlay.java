package com.desertkun.brainout.content.quest.task;

import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.quest.task.ExitFreePlay")
public class ClientExitFreePlay extends ExitFreePlay implements ClientTask
{
    @Override
    public void renderIcon(WidgetGroup to)
    {
    }

    @Override
    public boolean hasLocalizedName()
    {
        return false;
    }

    @Override
    public boolean hasRichLocalization()
    {
        return false;
    }

    @Override
    public String getLocalizedName()
    {
        return null;
    }

    @Override
    public String getShortLocalizedName()
    {
        return null;
    }

    @Override
    public boolean hasIcon()
    {
        return false;
    }

    @Override
    public boolean hasProgress()
    {
        return false;
    }

    @Override
    public boolean isItemTaskRelated(ConsumableItem item)
    {
        return false;
    }

    @Override
    public boolean showInSummaryScreen()
    {
        return true;
    }
}
