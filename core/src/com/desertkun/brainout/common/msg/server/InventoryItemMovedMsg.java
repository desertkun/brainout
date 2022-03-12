package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.data.active.PlayerData;

public class InventoryItemMovedMsg
{
    public int object;
    public int d;
    public String content;

    public InventoryItemMovedMsg() {}
    public InventoryItemMovedMsg(PlayerData playerData, Content content)
    {
        this.object = playerData.getId();
        this.d = playerData.getDimensionId();
        this.content = content.getID();
    }
}
