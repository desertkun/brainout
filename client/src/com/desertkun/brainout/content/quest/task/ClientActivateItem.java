package com.desertkun.brainout.content.quest.task;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.quest.task.ActivateItem")
public class ClientActivateItem extends ActivateItem implements ClientTask
{
    private String icon;

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
        return icon != null;
    }

    @Override
    protected void readTask(JsonValue jsonData)
    {
        super.readTask(jsonData);

        this.icon = jsonData.getString("icon", null);
    }

    @Override
    public void renderIcon(WidgetGroup to)
    {
        if (icon != null)
        {
            TextureRegion bigIcon = BrainOutClient.getRegion(icon);

            if (bigIcon != null)
            {
                Image iconImage = new Image(bigIcon);
                iconImage.setTouchable(Touchable.disabled);
                iconImage.setScaling(Scaling.none);
                iconImage.setFillParent(true);

                to.addActor(iconImage);
            }
        }
    }

    @Override
    public boolean hasProgress()
    {
        return getTarget(BrainOutClient.ClientController.getMyAccount()) > 1;
    }

    @Override
    public boolean showInSummaryScreen()
    {
        return true;
    }

    @Override
    public boolean isItemTaskRelated(ConsumableItem item)
    {
        return false;
    }
}
