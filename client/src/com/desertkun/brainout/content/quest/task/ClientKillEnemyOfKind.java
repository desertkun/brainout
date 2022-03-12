package com.desertkun.brainout.content.quest.task;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.content.components.IconComponent;
import com.desertkun.brainout.content.components.ItemComponent;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.utils.ContentImage;

@Reflect("content.quest.task.KillEnemyOfKind")
public class ClientKillEnemyOfKind extends KillEnemyOfKind implements ClientTask
{
    private String playerTitle;
    private String playerIcon;

    @Override
    public void renderIcon(WidgetGroup to)
    {
        Image iconImage = new Image(BrainOutClient.Skin, playerIcon);
        iconImage.setTouchable(Touchable.disabled);
        iconImage.setScaling(Scaling.none);
        iconImage.setFillParent(true);

        to.addActor(iconImage);
    }

    @Override
    protected void readTask(JsonValue jsonData)
    {
        super.readTask(jsonData);

        playerTitle = jsonData.getString("playerTitle");
        playerIcon = jsonData.getString("playerIcon");
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
    public boolean hasLocalizedName()
    {
        return true;
    }

    @Override
    public String getLocalizedName()
    {
        return L.get("QUEST_TASK_KILL_ENEMY_OF_KIND", L.get(playerTitle), String.valueOf(getTarget(
            BrainOutClient.ClientController.getMyAccount()
        )));
    }

    @Override
    public String getShortLocalizedName()
    {
        return L.get(playerTitle);
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
