package com.desertkun.brainout.data.components;

import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.content.active.Item;
import com.desertkun.brainout.content.components.DropComponent;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.events.DestroyEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("DropComponent")
@ReflectAlias("data.components.DropComponentData")
public class DropComponentData extends Component<DropComponent>
{
    public DropComponentData(ComponentObject componentObject, DropComponent dropComponent)
    {
        super(componentObject, dropComponent);
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case destroy:
            {
                destroy(((DestroyEvent) event));

                break;
            }
        }

        return false;
    }

    protected void destroy(DestroyEvent e)
    {
        if (e.info == null || e.info.instrument == null) return;

        // if we match the chance
        if (getContentComponent().checkChance(e.info.instrument.getID()))
        {
            Item dropItem = getContentComponent().getDropItem();

            Map map = getMap();

            if (map != null)
            {
                ItemData itemData = dropItem.getData(getComponentObject().getDimension());

                itemData.setPosition(e.x, e.y);

                itemData.getRecords().addRecord(new ConsumableRecord(
                    ((ConsumableContent) getComponentObject().getContent()).acquireConsumableItem(), 1, 0));

                map.addActive(map.generateServerId(),
                    itemData, true);
            }
        }
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    @Override
    public boolean hasUpdate()
    {
        return false;
    }
}
