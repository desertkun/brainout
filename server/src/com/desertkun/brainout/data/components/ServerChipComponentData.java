package com.desertkun.brainout.data.components;

import com.desertkun.brainout.content.components.ServerChipComponent;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.instrument.ChipData;
import com.desertkun.brainout.events.Event;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.esotericsoftware.minlog.Log;

@Reflect("ServerChipComponent")
@ReflectAlias("data.components.ServerChipComponentData")
public class ServerChipComponentData extends Component<ServerChipComponent>
{
    private final ChipData chipData;
    private ServerChipSpawnerComponentData spawner;

    public ServerChipComponentData(ChipData chipData,
                                   ServerChipComponent contentComponent)
    {
        super(chipData, contentComponent);

        this.chipData = chipData;
    }

    public ServerChipSpawnerComponentData getSpawner()
    {
        return spawner;
    }

    public void setSpawner(ServerChipSpawnerComponentData spawner)
    {
        this.spawner = spawner;
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    @Override
    public boolean hasUpdate()
    {
        return false;
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }

    @Override
    public void init()
    {
        super.init();

        Log.info("New chip!");
    }

    public ChipData getChipData()
    {
        return chipData;
    }

    @Override
    public void release()
    {
        super.release();

        Log.info("Chip destroyed!");
    }
}
