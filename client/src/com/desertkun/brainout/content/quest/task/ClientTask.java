package com.desertkun.brainout.content.quest.task;

import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.desertkun.brainout.content.consumable.ConsumableItem;

public interface ClientTask
{
    boolean hasLocalizedName();
    boolean hasRichLocalization();
    String getLocalizedName();
    String getShortLocalizedName();
    boolean hasIcon();
    void renderIcon(WidgetGroup to);
    boolean hasProgress();
    boolean showInSummaryScreen();
    boolean isItemTaskRelated(ConsumableItem item);
}
