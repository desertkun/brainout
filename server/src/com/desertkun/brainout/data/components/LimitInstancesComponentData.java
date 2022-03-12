package com.desertkun.brainout.data.components;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.components.LimitInstancesComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.interfaces.WithTag;
import com.desertkun.brainout.events.DestroyEvent;
import com.desertkun.brainout.events.Event;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("LimitInstancesComponent")
@ReflectAlias("data.components.LimitInstancesComponentData")
public class LimitInstancesComponentData extends Component<LimitInstancesComponent> implements WithTag
{
    private final String itemsClass;
    private final int instancesCount;
    private final ActiveData activeData;

    public LimitInstancesComponentData(ActiveData activeData,
                                       LimitInstancesComponent contentComponent)
    {
        super(activeData, contentComponent);

        itemsClass = contentComponent.getItemsClass();
        instancesCount = contentComponent.getInstancesCount();
        this.activeData = activeData;
    }

    public ActiveData getActiveData()
    {
        return activeData;
    }

    public String getItemsClass()
    {
        return itemsClass;
    }

    public int getInstancesCount()
    {
        return instancesCount;
    }

    @Override
    public void init()
    {
        super.init();

        Array<LimitInstancesComponentData> found = new Array<>();

        Map map = getMap();

        if (map == null)
            return;

        for (ActiveData activeData : map.getActivesForTag(Constants.ActiveTags.INSTANCE_LIMIT, false))
        {
            LimitInstancesComponentData lic = activeData.getComponent(LimitInstancesComponentData.class);

            if (lic != null)
            {
                if (lic != this && lic.getActiveData().getOwnerId() == getActiveData().getOwnerId() &&
                        lic.getItemsClass().equals(getItemsClass()))
                {
                    found.add(lic);
                }
            }
        }

        if (found.size >= getInstancesCount() && found.size > 0)
        {
            BrainOutServer.EventMgr.sendDelayedEvent(found.get(0).getActiveData(), DestroyEvent.obtain());
        }
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
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

    @Override
    public int getTags()
    {
        return WithTag.TAG(Constants.ActiveTags.INSTANCE_LIMIT);
    }
}
