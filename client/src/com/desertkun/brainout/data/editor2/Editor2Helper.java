package com.desertkun.brainout.data.editor2;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Disposable;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.client.states.CSGame;
import com.desertkun.brainout.data.Editor2Map;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.data.interfaces.RenderUpdatable;
import com.desertkun.brainout.editor2.Editor2Menu;
import com.desertkun.brainout.menu.Menu;

public class Editor2Helper implements RenderUpdatable, Disposable
{
    private final String dimension;

    public Editor2Helper(String dimension)
    {
        this.dimension = dimension;
    }

    public String getDimension()
    {
        return dimension;
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        Editor2Map map = Map.Get(getDimension(), Editor2Map.class);

        if (map == null || map.isPreviewMode())
            return;

        Menu topMenu = BrainOutClient.getInstance().topState().topMenu();

        if (topMenu instanceof Editor2Menu)
        {
            ((Editor2Menu) topMenu).getCurrentMode().render(batch, context);
        }
    }

    @Override
    public boolean hasRender()
    {
        return true;
    }

    @Override
    public int getZIndex()
    {
        return 1;
    }

    @Override
    public int getLayer()
    {
        return 0;
    }

    @Override
    public void dispose()
    {
    }

    @Override
    public void update(float dt)
    {
        Menu topMenu = BrainOutClient.getInstance().topState().topMenu();

        if (topMenu instanceof Editor2Menu)
        {
            ((Editor2Menu) topMenu).getCurrentMode().update(dt);
        }

        CSGame csGame = BrainOutClient.ClientController.getState(CSGame.class);
        if (csGame != null)
        {
            PlayerData playerData = csGame.getPlayerData();
            if (playerData != null)
            {
                Editor2Map.StartLocation.set(playerData.getX(), playerData.getY());
            }
        }
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }
}
