package com.desertkun.brainout.content.quest.task;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.L;
import com.desertkun.brainout.content.components.IconComponent;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.utils.ContentImage;

@Reflect("content.quest.task.EscortBot")
public class ClientEscortBot extends EscortBot implements ClientTask
{
    @Override
    public void renderIcon(WidgetGroup to)
    {
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
        String title = getName().get();
        String location = getLocation().get();

        return L.get("QUEST_TASK_ESCORT", title, location);
    }

    @Override
    public String getShortLocalizedName()
    {
        String title = getName().get();
        String location = getLocation().get();

        return L.get("QUEST_TASK_ESCORT", title, location);
    }

    @Override
    public boolean showInSummaryScreen()
    {
        return true;
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
        if (getRelatedItems() == null)
            return false;

        return getRelatedItems().contains(item.getContent().getID(), false);
    }
}
