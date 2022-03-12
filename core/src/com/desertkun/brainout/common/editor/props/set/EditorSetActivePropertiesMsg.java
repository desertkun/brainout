package com.desertkun.brainout.common.editor.props.set;

import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.common.editor.EditorProperty;
import com.desertkun.brainout.data.active.ActiveData;

public class EditorSetActivePropertiesMsg extends EditorSetPropertiesMsg
{
    public int activeId;
    public String d;

    public EditorSetActivePropertiesMsg() {}
    public EditorSetActivePropertiesMsg(ActiveData activeData, Array<EditorProperty> properies)
    {
        super(properies);

        this.activeId = activeData.getId();
        this.d = activeData.getDimension();
    }
}
