package com.desertkun.brainout.data.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.BoundingBoxComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.inspection.InspectableGetter;
import com.desertkun.brainout.inspection.InspectableSetter;
import com.desertkun.brainout.inspection.PropertyKind;
import com.desertkun.brainout.inspection.PropertyValue;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("BoundingBoxComponent")
@ReflectAlias("data.components.BoundingBoxComponentData")
public class BoundingBoxComponentData extends Component<BoundingBoxComponent> implements Json.Serializable
{
    private final ActiveData activeData;

    private float height;
    private float width;

    public BoundingBoxComponentData(ActiveData activeData,
                                    BoundingBoxComponent contentComponent)
    {
        super(activeData, contentComponent);

        this.activeData = activeData;

        this.width = contentComponent.getWidth();
        this.height = contentComponent.getHeight();
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

    public boolean  test(float x, float y)
    {
        float mx = activeData.getX(), my = activeData.getY(), w = width / 2.0f, h = height / 2.0f;
        return (x >= mx - w) && (x <= mx + w) && (y >= my - h) && (y <= my + h);
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }

    @Override
    public void write(Json json)
    {
        json.writeValue("_w", width);
        json.writeValue("_h", height);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        this.width = jsonData.getFloat("_w");
        this.height = jsonData.getFloat("_h");
    }

    @InspectableGetter(name = "width", kind = PropertyKind.string, value = PropertyValue.vFloat)
    public float getWidth()
    {
        return width;
    }

    @InspectableGetter(name = "height", kind = PropertyKind.string, value = PropertyValue.vFloat)
    public float getHeight()
    {
        return height;
    }

    @InspectableSetter(name = "width")
    public void setWidth(float width)
    {
        this.width = width;
    }

    @InspectableSetter(name = "height")
    public void setHeight(float height)
    {
        this.height = height;
    }
}
