package com.desertkun.brainout.data.components;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.content.components.ConnectionComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.SimpleEvent;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ConnectionComponent")
@ReflectAlias("data.components.ConnectionComponentData")
public class ConnectionComponentData extends Component<ConnectionComponent>
{
    public ConnectionComponentData(ComponentObject componentObject, ConnectionComponent connectionComponent)
    {
        super(componentObject, connectionComponent);
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }

    protected boolean isMatchNeighbor(Map map, int currentX, int currentY, int layer, int x, int y)
    {
        BlockData neighbor = BlockData.GetNeighbor(map, currentX, currentY, layer, x, y);

        if (neighbor == null)
            return false;

        ConnectionComponentData ccd = neighbor.getComponent(ConnectionComponentData.class);

        if (ccd == null)
            return false;

        ConnectionComponent cc = getContentComponent();

        return cc.getStickTo() != null && cc.getStickTo().indexOf(cc.getStickId(), false) >= 0;
    }

    public TextureRegion getRegion(Map map, int currentX, int currentY, int layer)
    {
        return getContentComponent().getRegion(
            currentX, currentY, getNeighbors(map, currentX, currentY, layer));
    }

    protected int getNeighbors(Map map, int currentX, int currentY, int layer)
    {
        int v = (isMatchNeighbor(map, currentX, currentY, layer, 0, 1) ? 1 : 0) +
                (isMatchNeighbor(map, currentX, currentY, layer, 1, 0) ? 2 : 0) +
                (isMatchNeighbor(map, currentX, currentY, layer, 0, -1) ? 4 : 0) +
                (isMatchNeighbor(map, currentX, currentY, layer, -1, 0) ? 8 : 0);

        if (getContentComponent().isNine())
        {
            v += (isMatchNeighbor(map, currentX, currentY, layer, 1, 1) ? 16 : 0) +
                (isMatchNeighbor(map, currentX, currentY, layer, 1, -1) ? 32 : 0) +
                (isMatchNeighbor(map, currentX, currentY, layer, -1, -1) ? 64 : 0) +
                (isMatchNeighbor(map, currentX, currentY, layer, -1, 1) ? 128 : 0);
        }

        return v;
    }

    @Override
    public boolean hasUpdate()
    {
        return false;
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }
}
