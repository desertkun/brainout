package com.desertkun.brainout.menu.widgets;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;

public class Widgets extends Stage
{
    private Array<Widget> widgets;

    public Widgets()
    {
        widgets = new Array<>();
    }

    public void addWidget(Widget widget)
    {
        addActor(widget);
        widget.setStage(this);
        widgets.add(widget);

        widget.init();
    }

    public Array<Widget> getWidgets()
    {
        return widgets;
    }

    public void removeWidget(Widget widget)
    {
        widget.setStage(null);
        widget.release();
        widget.remove();

        widgets.removeValue(widget, true);
    }

    public void removeAll()
    {
        for (Widget widget : widgets)
        {
            widget.setStage(null);
            widget.release();
            widget.remove();
        }

        widgets.clear();
    }

    public void release()
    {
        for (Widget widget : widgets)
        {
            widget.release();
        }

        widgets.clear();
    }

    public void render()
    {
        draw();
    }

    public void update(float dt)
    {
        act(dt);
    }
}
