package com.desertkun.brainout.data.active;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.active.RoundLockSafe;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.active.RoundLockSafeData")
public class RoundLockSafeData extends ItemData
{
    private String code;
    private boolean locked;

    public RoundLockSafeData(RoundLockSafe safe, String dimension)
    {
        super(safe, dimension);

        code = "";
    }

    public String getCode()
    {
        return code;
    }

    @Override
    public void write(Json json)
    {
        super.write(json);

        json.writeValue("code", code);
        json.writeValue("locked", locked);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        code = jsonData.getString("code");
        locked = jsonData.getBoolean("locked", true);
    }

    public boolean isLocked()
    {
        return locked;
    }

    public void setLocked(boolean locked)
    {
        this.locked = locked;
    }

    public void setCode(String code)
    {
        this.code = code;
    }
}
