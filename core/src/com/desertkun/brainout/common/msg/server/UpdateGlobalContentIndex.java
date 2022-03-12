package com.desertkun.brainout.common.msg.server;

import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.content.Content;

public class UpdateGlobalContentIndex
{
    public String[] content;

    public UpdateGlobalContentIndex() {}
    public UpdateGlobalContentIndex(Array<Content> content)
    {
        this.content = new String[content.size];
        for (int i = 0; i < content.size; i++)
        {
            this.content[i] = content.get(i).getID();
        }
    }
}
