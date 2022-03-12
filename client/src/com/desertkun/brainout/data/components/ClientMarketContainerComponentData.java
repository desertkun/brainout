package com.desertkun.brainout.data.components;


import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.active.PersonalContainer;
import com.desertkun.brainout.content.components.ClientMarketContainerComponent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.interfaces.WithTag;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ClientMarketContainerComponent")
@ReflectAlias("data.components.ClientMarketContainerComponentData")
public class ClientMarketContainerComponentData extends Component<ClientMarketContainerComponent> implements WithTag
{
    public ClientMarketContainerComponentData(ComponentObject componentObject,
                                              ClientMarketContainerComponent contentComponent)
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
        return WithTag.TAG(Constants.ActiveTags.MARKET_CONTAINER);
    }
}
