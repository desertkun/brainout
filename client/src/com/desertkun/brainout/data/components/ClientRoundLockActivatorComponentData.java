package com.desertkun.brainout.data.components;

import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.common.msg.client.ActivateActiveMsg;
import com.desertkun.brainout.content.components.ClientRoundLockActivatorComponent;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.active.RoundLockSafeData;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.menu.impl.RoundLockCodeMenu;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ClientRoundLockActivatorComponent")
@ReflectAlias("data.components.ClientRoundLockActivatorComponentData")
public class ClientRoundLockActivatorComponentData extends
    ClientActiveActivatorComponentData<ClientRoundLockActivatorComponent>
{
    private final RoundLockSafeData safe;

    public ClientRoundLockActivatorComponentData(RoundLockSafeData safe,
                                                 ClientRoundLockActivatorComponent activatorComponent)
    {
        super(safe, activatorComponent);

        this.safe = safe;
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }

    @Override
    public boolean test(PlayerData playerData)
    {
        return safe.isLocked();
    }

    @Override
    public String getFailedConditionLocalizedText()
    {
        return "";
    }

    @Override
    public boolean activate(PlayerData playerData)
    {
        if (!safe.isLocked())
            return false;

        if (safe.isLocked())
        {
            BrainOutClient.getInstance().topState().pushMenu(new RoundLockCodeMenu(
                safe.getCode(), this::enter
            ));
        }

        return true;
    }

    private void enter()
    {
        BrainOutClient.ClientController.sendTCP(
            new ActivateActiveMsg(getActiveData().getId(), safe.getCode()));
    }
}
