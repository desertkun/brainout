package com.desertkun.brainout.data.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.SpawnTarget;
import com.desertkun.brainout.content.components.SubPointComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.interfaces.WithTag;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.inspection.InspectableGetter;
import com.desertkun.brainout.inspection.InspectableSetter;
import com.desertkun.brainout.inspection.PropertyKind;
import com.desertkun.brainout.inspection.PropertyValue;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("SubPointComponent")
@ReflectAlias("data.components.SubPointComponentData")
public class SubPointComponentData extends Component<SubPointComponent>
    implements Json.Serializable, WithTag
{
    private float timer;
    private SpawnTarget target;

    public SubPointComponentData(ComponentObject componentObject, SubPointComponent contentComponent)
    {
        super(componentObject, contentComponent);

        this.timer = 0;

        if (contentComponent.getTarget() != null)
        {
            this.target = contentComponent.getTarget();
        }
        else
        {
            this.target = SpawnTarget.normal;
        }
    }

    public boolean isAvailable()
    {
        return timer <= 0;
    }

    public void take()
    {
        timer = getContentComponent().getTakeTime();
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

    public Vector2 getOffset()
    {
        return getContentComponent().getOffset();
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        timer -= dt;
    }

    @InspectableGetter(name = "target", kind = PropertyKind.select, value = PropertyValue.vEnum)
    public SpawnTarget getTarget()
    {
        return target;
    }

    @InspectableSetter(name = "target")
    public void setTarget(SpawnTarget target)
    {
        this.target = target;
    }

    @Override
    public int getTags()
    {
        return WithTag.TAG(Constants.ActiveTags.SPAWNABLE);
    }

    @Override
    public void write(Json json)
    {
        json.writeValue("target", target.toString());
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        if (jsonData.has("target") && getContentComponent().getTarget() == null)
            this.target = SpawnTarget.valueOf(jsonData.getString("target", SpawnTarget.normal.toString()));
    }
}
