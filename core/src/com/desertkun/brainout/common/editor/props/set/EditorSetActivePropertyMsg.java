package com.desertkun.brainout.common.editor.props.set;

import com.desertkun.brainout.common.editor.EditorProperty;
import com.desertkun.brainout.common.msg.ModeMessage;
import com.desertkun.brainout.data.active.ActiveData;

public class EditorSetActivePropertyMsg implements ModeMessage
{
    public int activeId;
    public String d;
    public EditorProperty property;

    public EditorSetActivePropertyMsg() {}
    public EditorSetActivePropertyMsg(ActiveData activeData, EditorProperty property)
    {
        this.activeId = activeData.getId();
        this.d = activeData.getDimension();
        this.property = property;
    }
}
