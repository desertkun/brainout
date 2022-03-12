package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.utils.ColorNinePatchDrawable;

public class BorderActor extends Table
{
    private Cell<Actor> cell;

    public BorderActor(Actor toInline)
    {
        this(toInline, 0, "form-default", Color.WHITE);
    }

    public BorderActor(Actor toInline, String ninePatch)
    {
        this(toInline, 0, ninePatch, Color.WHITE);
    }

    public BorderActor(Actor toInline, float width)
    {
        this(toInline, width, "form-default", Color.WHITE);
    }

    public BorderActor(Actor toInline, float width, String ninePatch)
    {
        this(toInline, width, ninePatch, Color.WHITE);
    }

    public BorderActor(Actor toInline, float width, String ninePatch, Color color)
    {
        setBackground(new ColorNinePatchDrawable(BrainOutClient.getNinePatch(ninePatch), color));

        if (toInline != null)
        {
            this.cell = add(toInline);
            if (width != 0) cell.width(width);
        }
    }

    public Cell getCell()
    {
        return cell;
    }
}
