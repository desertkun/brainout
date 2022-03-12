package com.desertkun.brainout.data.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.block.Block;
import com.desertkun.brainout.content.components.TargetBlocksSpawnerComponent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.interfaces.DoNotSyncToClients;
import com.desertkun.brainout.data.interfaces.WithTag;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.inspection.InspectableGetter;
import com.desertkun.brainout.inspection.InspectableSetter;
import com.desertkun.brainout.inspection.PropertyKind;
import com.desertkun.brainout.inspection.PropertyValue;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("TargetBlocksSpawnerComponent")
@ReflectAlias("data.components.TargetBlocksSpawnerComponentData")
public class TargetBlocksSpawnerComponentData extends Component<TargetBlocksSpawnerComponent>
    implements Json.Serializable, WithTag, DoNotSyncToClients
{
    private String group;

    public TargetBlocksSpawnerComponentData(ComponentObject componentObject,
                                            TargetBlocksSpawnerComponent contentComponent)
    {
        super(componentObject, contentComponent);

        group = "";
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
    public void write(Json json)
    {
        json.writeValue("gr", group);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        group = jsonData.getString("gr");
    }

    @InspectableGetter(name = "targetGroup", kind = PropertyKind.string, value = PropertyValue.vString)
    public String getTargetGroup()
    {
        return group;
    }

    @InspectableSetter(name = "targetGroup")
    public void setTargetGroup(String group)
    {
        this.group = group;
    }

    public Block getOpenedBlock()
    {
        return getContentComponent().getOpened();
    }

    public Block getClosedBlock()
    {
        return getContentComponent().getClosed();
    }

    @Override
    public int getTags()
    {
        return WithTag.TAG(Constants.ActiveTags.TARGET_SPAWNER);
    }
}
