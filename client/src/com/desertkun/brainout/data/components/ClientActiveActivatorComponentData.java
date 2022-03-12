package com.desertkun.brainout.data.components;

import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.common.msg.client.ActivateActiveMsg;
import com.desertkun.brainout.content.components.ClientActivatorConditionComponent;
import com.desertkun.brainout.content.components.ClientActiveActivatorComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ClientActiveActivatorComponent")
@ReflectAlias("data.components.ClientActiveActivatorComponentData")
public class ClientActiveActivatorComponentData<T extends ClientActiveActivatorComponent> extends Component<T>
{
    private final ActiveData activeData;

    public ClientActiveActivatorComponentData(ActiveData activeData,
                                              T activatorComponent)
    {
        super(activeData, activatorComponent);

        this.activeData = activeData;
    }

    public ActiveData getActiveData()
    {
        return activeData;
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }

    public boolean activate(PlayerData playerData)
    {
        if (!test(playerData))
            return false;

        BrainOutClient.ClientController.sendTCP(new ActivateActiveMsg(activeData.getId(), ""));

        return true;
    }

    public boolean test(PlayerData currentPlayerData)
    {
        ClientActivatorConditionComponent condition =
                getComponentObject().getContent().getComponentFrom(ClientActivatorConditionComponent.class);

        return condition == null || condition.testCondition(currentPlayerData, getComponentObject());
    }

    public boolean dirty()
    {
        return false;
    }

    public String getFailedConditionLocalizedText()
    {
        ClientActivatorConditionComponent condition =
                getComponentObject().getContent().getComponentFrom(ClientActivatorConditionComponent.class);

        if (condition == null)
        {
            return "";
        }

        return condition.getFailedConditionLocalizedText();
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

    public String getActivateText()
    {
        return getContentComponent().getActivateText().get();
    }
}
