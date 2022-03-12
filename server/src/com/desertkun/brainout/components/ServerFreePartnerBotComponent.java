package com.desertkun.brainout.components;

import com.desertkun.brainout.content.quest.Quest;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.mode.ServerFreeRealization;

public class ServerFreePartnerBotComponent extends Component
{
    private final String partyId;

    public ServerFreePartnerBotComponent(String partyId)
    {
        super(null, null);

        this.partyId = partyId;
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    public String getPartyId()
    {
        return partyId;
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
