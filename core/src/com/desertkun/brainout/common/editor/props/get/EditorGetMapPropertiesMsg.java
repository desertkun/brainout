package com.desertkun.brainout.common.editor.props.get;

public class EditorGetMapPropertiesMsg extends EditorGetPropertiesMsg
{
    public String d;

    public EditorGetMapPropertiesMsg() {}
    public EditorGetMapPropertiesMsg(String dimension)
    {
        this.d = dimension;
    }
}
