package com.desertkun.brainout.data.interfaces;

import com.badlogic.gdx.utils.Json;
import com.desertkun.brainout.data.Data;

public interface ComponentWritable
{
    void write(Json json, Data.ComponentWriter componentWriter, int owner);
}
