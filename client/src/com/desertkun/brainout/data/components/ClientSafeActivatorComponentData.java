package com.desertkun.brainout.data.components;

import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.common.msg.client.ActivateActiveMsg;
import com.desertkun.brainout.content.components.ClientActivatorConditionComponent;
import com.desertkun.brainout.content.components.ClientActiveActivatorComponent;
import com.desertkun.brainout.content.components.ClientSafeActivatorComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.menu.impl.SafeEnterDigitsMenu;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ClientSafeActivatorComponent")
@ReflectAlias("data.components.ClientSafeActivatorComponentData")
public class ClientSafeActivatorComponentData extends ClientActiveActivatorComponentData<ClientSafeActivatorComponent>
{
    public ClientSafeActivatorComponentData(ActiveData activeData,
                                            ClientSafeActivatorComponent activatorComponent)
    {
        super(activeData, activatorComponent);
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }

    @Override
    public boolean activate(PlayerData playerData)
    {
        if (!test(playerData))
            return false;

        BrainOutClient.getInstance().topState().pushMenu(new SafeEnterDigitsMenu(
                getContentComponent().getDigits(),
                getContentComponent().getEmptyDigit(),
                getContentComponent().getBeep(),
                code -> BrainOutClient.ClientController.sendTCP(
                    new ActivateActiveMsg(getActiveData().getId(), code))
        ));

        return true;
    }
}
