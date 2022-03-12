package com.desertkun.brainout.data.components;

import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.content.components.DropOffComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.consumable.ConsumableContainer;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.mode.freeplay.FreeplayDropoff;

public class DropOffComponentData extends Component<DropOffComponent>
{
    private static Array<DropOff> tmp = new Array<>();

    private final ActiveData activeData;
    private final Array<DropOff> dropOffs;
    private float cnt;

    public static class DropOff
    {
        private float position;
        private Array<FreeplayDropoff.Generator> generators;

        public DropOff(float position, Array<FreeplayDropoff.Generator> generators)
        {
            this.position = position;
            this.generators = generators;
        }

        public float getPosition()
        {
            return position;
        }

        public Array<FreeplayDropoff.Generator> getGenerators()
        {
            return generators;
        }
    }

    public DropOffComponentData(ActiveData activeData, DropOffComponent contentComponent)
    {
        super(activeData, contentComponent);

        this.activeData = activeData;
        this.dropOffs = new Array<>();
    }

    public void addDropOff(DropOff dropOff)
    {
        this.dropOffs.add(dropOff);
    }

    @Override
    public void update(float dt)
    {
        cnt -= dt;

        if (cnt > 0)
            return;

        cnt = 0.1f;

        float x = activeData.getX();

        for (DropOff off : dropOffs)
        {
            if (x < off.getPosition())
            {
                tmp.add(off);

                BrainOutServer.PostRunnable(() -> drop(off));
            }
        }

        if (tmp.size > 0)
        {
            for (DropOff off : tmp)
            {
                dropOffs.removeValue(off, true);
            }

            tmp.clear();
        }

    }

    private void drop(DropOff off)
    {
        Map map = getMap();

        ItemData itemData = getContentComponent().getItem().getData(getComponentObject().getDimension());
        itemData.setPosition(activeData.getX(), activeData.getY());

        for (FreeplayDropoff.Generator generator : off.generators)
        {
            generator.generate(itemData);
        }

        map.addActive(map.generateServerId(), itemData, true);
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }
}
