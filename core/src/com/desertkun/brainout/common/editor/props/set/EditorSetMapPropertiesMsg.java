package com.desertkun.brainout.common.editor.props.set;

import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.common.editor.EditorProperty;

public class EditorSetMapPropertiesMsg extends EditorSetPropertiesMsg
{
    public String d;

    public EditorSetMapPropertiesMsg() {}
    public EditorSetMapPropertiesMsg(String dimension, Array<EditorProperty> properties)
    {
        super(properties);

        this.d = dimension;
    }
}
