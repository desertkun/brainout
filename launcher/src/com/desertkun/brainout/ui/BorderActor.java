package com.desertkun.brainout.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.desertkun.brainout.Launcher;

public class BorderActor extends Table
{
    private final Cell<Actor> cell;

    public BorderActor(Actor toInline)
    {
        this(toInline, 0);
    }

    public BorderActor(Actor toInline, float width)
    {
        setBackground(new NinePatchDrawable(Launcher.getNinePatch("form-default")));
        this.cell = add(toInline).pad(8);
        if (width != 0) cell.width(width);
    }

    public Cell getCell()
    {
        return cell;
    }
}
