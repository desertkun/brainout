package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.ActiveDamageComponentData;
import com.desertkun.brainout.data.components.EnemyDetectorComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.EnemyDetectorComponent")
public class EnemyDetectorComponent extends DetectorComponent
{
    @Override
    public EnemyDetectorComponentData getComponent(ComponentObject componentObject)
    {
        return new EnemyDetectorComponentData((ActiveData)componentObject, this);
    }
}
