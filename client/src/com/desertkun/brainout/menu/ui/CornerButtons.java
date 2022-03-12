package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class CornerButtons extends Table
{
    public enum Corner
    {
        topLeft,
        topRight,
        bottomLeft,
        bottomRight
    }

    private class CornerActor
    {
        public Actor actor;
        public Vector2 size;

        public CornerActor(Actor actor, int width, int height)
        {
            this.actor = actor;
            this.size = new Vector2(width, height);
        }
    }

    private CornerActor[] actors;

    public CornerButtons()
    {
        actors = new CornerActor[4];
    }

    public void setCorner(Corner corner, Actor actor, int width, int height)
    {
        actors[corner.ordinal()] = new CornerActor(actor, width, height);
    }

    private CornerActor getCorner(Corner corner)
    {
        return actors[corner.ordinal()];
    }

    public void init()
    {
        CornerActor topLeft = getCorner(Corner.topLeft);
        CornerActor topRight = getCorner(Corner.topRight);
        CornerActor bottomLeft = getCorner(Corner.bottomLeft);
        CornerActor bottomRight = getCorner(Corner.bottomRight);

        (topLeft != null ? add(topLeft.actor).size(topLeft.size.x, topLeft.size.y) :
                add()).expand().top().left().fill();
        (topRight != null ? add(topRight.actor).size(topRight.size.x, topRight.size.y) :
                add()).expand().fill().top().right().row();
        (bottomLeft != null ? add(bottomLeft.actor).size(bottomLeft.size.x, bottomLeft.size.y) :
                add()).expand().bottom().left().fill();
        (bottomRight != null ? add(bottomRight.actor).size(bottomRight.size.x, bottomRight.size.y) :
                add()).expand().bottom().right().fill().row();
    }
}
