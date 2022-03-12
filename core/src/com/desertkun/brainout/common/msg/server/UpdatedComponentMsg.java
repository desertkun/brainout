package com.desertkun.brainout.common.msg.server;

import com.badlogic.gdx.utils.Json;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.ActiveComponent;
import com.desertkun.brainout.data.components.base.Component;

public class UpdatedComponentMsg
{
    public String data;
    public String clazz;
    public int id;
    public int d;

    public UpdatedComponentMsg() {}
    public UpdatedComponentMsg(ActiveData data, Component component)
    {
        this.clazz = BrainOut.R.getClassName(component.getComponentClass());
        this.data = BrainOut.R.JSON.toJson(component);
        this.id = data.getId();
        this.d = data.getDimensionId();
    }
}
