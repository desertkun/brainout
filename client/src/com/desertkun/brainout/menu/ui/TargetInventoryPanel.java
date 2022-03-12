package com.desertkun.brainout.menu.ui;

import com.desertkun.brainout.data.active.ActiveData;

public class TargetInventoryPanel extends InventoryPanel
{
    protected ActiveData placeInto;

    public TargetInventoryPanel(InventoryDragAndDrop dragAndDrop)
    {
        super(dragAndDrop);
    }

    @Override
    public ActiveData getPlaceInto()
    {
        return placeInto;
    }
}
