package com.desertkun.brainout.common.msg.client;

import com.desertkun.brainout.content.Content;

public class SetFavoriteMsg
{
    public String content;
    public boolean fav;

    public SetFavoriteMsg(Content content, boolean fav)
    {
        this.content = content.getID();
        this.fav = fav;
    }

    public SetFavoriteMsg() {}
}
