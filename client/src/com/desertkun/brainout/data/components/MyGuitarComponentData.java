package com.desertkun.brainout.data.components;

import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.components.MyGuitarComponent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.GameControllerEvent;
import com.desertkun.brainout.menu.impl.PlayGuitarMenu;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("MyGuitarComponent")
@ReflectAlias("data.components.MyGuitarComponentData")
public class MyGuitarComponentData extends Component<MyGuitarComponent>
{
    public MyGuitarComponentData(ComponentObject componentObject, MyGuitarComponent contentComponent)
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
        switch (event.getID())
        {
            case gameController:
            {
                GameControllerEvent ev = ((GameControllerEvent) event);

                switch (ev.action)
                {
                    case activate:
                    {
                        BrainOutClient.getInstance().topState().pushMenu(new PlayGuitarMenu());

                        return true;
                    }
                }

                break;
            }
        }

        return false;
    }
}
