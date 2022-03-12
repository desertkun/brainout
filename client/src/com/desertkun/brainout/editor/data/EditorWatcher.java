package com.desertkun.brainout.editor.data;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.active.PointData;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.data.interfaces.Watcher;

public class EditorWatcher extends PointData implements Watcher
{
    private Vector2 prevPos;
    private Vector2 pos;
    private float scale;

    public EditorWatcher(String dimention)
    {
        super(null, dimention);

        prevPos = new Vector2();
        pos = new Vector2();
        scale = 1;
    }

    @Override
    public void update(float dt)
    {
        //updateControls(dt);
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        /*
        if (map.getEditor().isRenderBlock())
        {
            if (x >= 0 && y >= 0 && x < getMap().getWidth() && y < getMap().getHeight())
            {
                batch.end();

                selected.begin(ShapeRenderer.ShapeType.Line);
                selected.setProjectionMatrix(getMap().getProjectionMatrix());

                selected.setColor(1, 1, 1, 1);
                selected.rect(getMap().getPos(getX()), getMap().getPos(getY()), 1, 1);
                selected.end();

                batch.begin();
            }
        }
        */
    }

    public void updateControls(float dt)
    {
        if (Gdx.input.isKeyPressed(ClientConstants.Keys.KEY_EDIT_MOVE_LEFT))
        {
            x -= Constants.Core.EDIT_MOVE_SPEED * dt;
        }

        if (Gdx.input.isKeyPressed(ClientConstants.Keys.KEY_EDIT_MOVE_RIGHT))
        {
            x += Constants.Core.EDIT_MOVE_SPEED * dt;
        }

        if (Gdx.input.isKeyPressed(ClientConstants.Keys.KEY_EDIT_MOVE_DOWN))
        {
            y -= Constants.Core.EDIT_MOVE_SPEED * dt;
        }

        if (Gdx.input.isKeyPressed(ClientConstants.Keys.KEY_EDIT_MOVE_UP))
        {
            y += Constants.Core.EDIT_MOVE_SPEED * dt;
        }

        ClientMap.getMouseScale(Gdx.input.getX() - BrainOutClient.getWidth() / 2, Gdx.input.getY() - BrainOutClient.getHeight() / 2, pos);

        x += pos.x;
        y -= pos.y;

        Gdx.input.setCursorPosition(BrainOutClient.getWidth() / 2, BrainOutClient.getHeight() / 2);

        /*
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT))
        {
            map.getEditor().placeBlock(map.getPos(getX()), map.getPos(getY()), Input.Buttons.LEFT);
        }
        else
        if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT))
        {
            map.getEditor().placeBlock(map.getPos(getX()), map.getPos(getY()), Input.Buttons.RIGHT);
        }
        else
        {
            map.getEditor().placeBlock(map.getPos(getX()), map.getPos(getY()), -1);
        }
        */
    }



    @Override
    public void write(Json json)
    {
        //
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        //
    }

    @Override
    public float getWatchX() {
        return getX();
    }

    @Override
    public float getWatchY() {
        return getY();
    }

    @Override
    public boolean allowZoom()
    {
        return false;
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    @Override
    public boolean hasRender()
    {
        return true;
    }

    @Override
    public float getScale()
    {
        return scale;
    }

    public void setScale(float scale)
    {
        this.scale = scale;
    }
}
