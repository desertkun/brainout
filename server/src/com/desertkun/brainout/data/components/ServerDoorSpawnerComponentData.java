package com.desertkun.brainout.data.components;

import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.components.ServerDoorSpawnerComponent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.interfaces.WithTag;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ServerDoorSpawnerComponent")
@ReflectAlias("data.components.ServerDoorSpawnerComponentData")
public class ServerDoorSpawnerComponentData extends Component<ServerDoorSpawnerComponent> implements WithTag
{
    public ServerDoorSpawnerComponentData(ComponentObject componentObject,
                                          ServerDoorSpawnerComponent contentComponent)
    {
        super(componentObject, contentComponent);
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
    public boolean onEvent(Event event)
    {
        return false;
    }

    @Override
    public int getTags()
    {
        return WithTag.TAG(Constants.ActiveTags.EXIT_DOOR);
    }
}
