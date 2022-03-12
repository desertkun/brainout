package com.desertkun.brainout.content.active;

import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.SpawnPointData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.active.SpawnPoint")
public class SpawnPoint extends Active
{
    @Override
    public SpawnPointData getData(String dimension)
    {
        return new SpawnPointData(this, dimension);
    }

    @Override
    public boolean isEditorSelectable()
    {
        return true;
    }
}
