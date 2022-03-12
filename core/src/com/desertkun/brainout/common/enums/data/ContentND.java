package com.desertkun.brainout.common.enums.data;

import com.desertkun.brainout.content.Content;

public class ContentND extends NotifyData
{
    public String id;

    public ContentND() {}
    public ContentND(Content content)
    {
        this.id = content.getID();
    }
}
