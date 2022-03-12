package com.desertkun.brainout.common.msg.client.editor2;

import com.badlogic.gdx.utils.ObjectSet;
import com.desertkun.brainout.common.msg.ModeMessage;
import com.desertkun.brainout.data.active.ActiveData;

public class CopyObjectsMsg implements ModeMessage
{
    public String d;
    public int[] o;
    public int x, y;

    public CopyObjectsMsg() {}
    public CopyObjectsMsg(String dimension, ObjectSet<ActiveData> objects, int x, int y)
    {
        this.d = dimension;
        this.o = new int[objects.size];

        int i = 0;
        for (ActiveData object : objects)
        {
            o[i] = object.getId();
            i++;
        }

        this.x = x;
        this.y = y;
    }
}
