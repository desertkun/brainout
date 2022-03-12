package com.desertkun.brainout.data.active;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.active.Active;
import com.desertkun.brainout.data.interfaces.WithTag;
import com.desertkun.brainout.inspection.InspectableProperty;
import com.desertkun.brainout.inspection.PropertyKind;
import com.desertkun.brainout.inspection.PropertyValue;

public class MarkerData extends PointData implements WithTag
{
    public MarkerData(Active active, String dimension)
    {
        super(active, dimension);
    }

    @InspectableProperty(name = "tag", kind = PropertyKind.string, value = PropertyValue.vString)
    public String tag;

    @Override
    public void write(Json json)
    {
        super.write(json);

        json.writeValue("tag", tag);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        tag = jsonData.getString("tag", "");
    }

    @Override
    public String getEditorTitle()
    {
        return tag;
    }

    @Override
    public int getTags()
    {
        return super.getTags() | WithTag.TAG(Constants.ActiveTags.MARKER);
    }
}
