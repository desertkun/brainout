package com.desertkun.brainout.data.components;

import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.components.TeamComponent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.events.Event;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("TeamComponent")
@ReflectAlias("data.components.TeamComponentData")
public class TeamComponentData extends Component<TeamComponent>
{
    private Team team;

    public TeamComponentData(ComponentObject componentObject, TeamComponent contentComponent)
    {
        super(componentObject, contentComponent);
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

    public Team getTeam()
    {
        return team;
    }

    public void setTeam(Team team)
    {
        this.team = team;
    }
}
