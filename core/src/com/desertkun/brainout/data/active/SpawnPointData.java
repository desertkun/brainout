package com.desertkun.brainout.data.active;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.SpawnTarget;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.active.SpawnPoint;
import com.desertkun.brainout.data.interfaces.Animable;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.data.interfaces.Spawnable;
import com.desertkun.brainout.data.interfaces.WithTag;
import com.desertkun.brainout.inspection.*;
import com.desertkun.brainout.utils.LocalizedString;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.active.SpawnPointData")
public class SpawnPointData extends PointData implements Animable, Spawnable
{
    private LocalizedString spawnName;

    public SpawnPointData(SpawnPoint active, String dimension)
    {
        super(active, dimension);

        spawnName = new LocalizedString();
        spawnRange = 0;

        setzIndex(2);
    }

    @Override
    public SpawnTarget getTarget()
    {
        return SpawnTarget.normal;
    }

    @InspectableProperty(name = "range", kind = PropertyKind.string, value = PropertyValue.vFloat)
    public float spawnRange;

    @InspectableGetter(name="name", kind= PropertyKind.string, value = PropertyValue.vString)
    public String getName()
    {
        return spawnName.getID();
    }

    @InspectableSetter(name="name")
    public void setName(String name)
    {
        spawnName.set(name);
    }

    @Override
    public void write(Json json)
    {
        super.write(json);

        json.writeValue("sname", spawnName.getID());
        json.writeValue("spawnRange", spawnRange);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        spawnName.set(jsonData.getString("sname", ""));
        spawnRange = jsonData.getFloat("spawnRange", 0);
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        super.render(batch, context);
    }

    @Override
    public boolean getFlipX()
    {
        return false;
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    @Override
    public boolean hasRender()
    {
        return true;
    }

    @Override
    public float getSpawnX()
    {
        return getX();
    }

    @Override
    public float getSpawnY()
    {
        return getY();
    }

    @Override
    public boolean canSpawn(Team teamFor)
    {
        return getTeam() == teamFor;
    }

    @Override
    public String toString()
    {
        return spawnName.get();
    }

    @Override
    public float getSpawnRange()
    {
        return spawnRange;
    }

    @Override
    public int getTags()
    {
        return super.getTags() | WithTag.TAG(Constants.ActiveTags.SPAWNABLE);
    }
}
