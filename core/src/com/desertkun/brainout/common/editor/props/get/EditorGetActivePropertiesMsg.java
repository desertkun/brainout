package com.desertkun.brainout.common.editor.props.get;

import com.desertkun.brainout.data.active.ActiveData;

public class EditorGetActivePropertiesMsg extends EditorGetPropertiesMsg
{
    public int activeId;
    public String d;

    public EditorGetActivePropertiesMsg() {}
    public EditorGetActivePropertiesMsg(ActiveData activeData)
    {
        this.activeId = activeData.getId();
        this.d = activeData.getDimension();
    }
}
