package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.ServerFreeplayDriveAwayActivatorComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ServerFreeplayDriveAwayActivatorComponent")
public class ServerFreeplayDriveAwayActivatorComponent extends ContentComponent
{
    private String event;
    private String generator;
    private String drive;
    private String trunk;
    private String[] driveAnimations;

    @Override
    public ServerFreeplayDriveAwayActivatorComponentData getComponent(ComponentObject componentObject)
    {
        return new ServerFreeplayDriveAwayActivatorComponentData(componentObject, this);
    }

    public ServerFreeplayDriveAwayActivatorComponent()
    {
        event = "";
        generator = "";
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        this.event = jsonData.getString("event", "");
        this.generator = jsonData.getString("generator", "");
        this.drive = jsonData.getString("drive");
        this.trunk = jsonData.getString("trunk");

        if (jsonData.has("driveAnimations"))
        {
            JsonValue d = jsonData.get("driveAnimations");
            driveAnimations = new String[d.size];
            int i = 0;
            for (JsonValue value : d)
            {
                driveAnimations[i] = value.asString();
                i++;
            }
        }
    }

    public String getTrunk()
    {
        return trunk;
    }

    public String getDrive()
    {
        return drive;
    }

    public String[] getDriveAnimations()
    {
        return driveAnimations;
    }

    public String getEvent()
    {
        return event;
    }

    public String getGenerator()
    {
        return generator;
    }
}
