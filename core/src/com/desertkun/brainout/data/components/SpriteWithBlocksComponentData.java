package com.desertkun.brainout.data.components;

import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.components.SpriteWithBlocksComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("SpriteWithBlocksComponent")
@ReflectAlias("data.components.SpriteWithBlocksComponentData")
public class SpriteWithBlocksComponentData extends Component<SpriteWithBlocksComponent>
{
    public SpriteWithBlocksComponentData(ComponentObject componentObject,
                                         SpriteWithBlocksComponent contentComponent)
    {
        super(componentObject, contentComponent);
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    @Override
    public void init()
    {
        super.init();

        ActiveData activeData = ((ActiveData) getComponentObject());

        activeData.setLayer(getLayer());
        activeData.setzIndex(getContentComponent().getzIndex());
    }

    @Override
    public int getLayer()
    {
        SpriteWithBlocksComponent sp = getContentComponent();

        switch (sp.getBlocksLayer())
        {
            case Constants.Layers.BLOCK_LAYER_BACKGROUND:
                return Constants.Layers.ACTIVE_LAYER_1;
            case Constants.Layers.BLOCK_LAYER_FOREGROUND:
            default:
                return Constants.Layers.ACTIVE_LAYER_2;
        }
    }

    @Override
    public boolean hasUpdate()
    {
        return false;
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }
}
