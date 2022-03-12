package com.desertkun.brainout.common.editor.props.set;

import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.common.editor.EditorProperty;
import com.desertkun.brainout.common.msg.ModeMessage;

public class EditorSetPropertiesMsg implements ModeMessage
{
    public EditorProperty[] properties;

    public EditorSetPropertiesMsg() {}
    public EditorSetPropertiesMsg(Array<EditorProperty> properties)
    {
        this.properties = properties.toArray(EditorProperty.class);
    }
}
