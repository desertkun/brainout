package com.desertkun.brainout.data.physics;

import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.block.contact.CSTriangle;
import com.desertkun.brainout.content.block.contact.ContactShape;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.block.ConcreteBD;
import com.desertkun.brainout.data.containers.ChunkData;
import com.desertkun.brainout.events.PhysicChunkUpdatedEvent;

import java.lang.ref.WeakReference;

public class PhysicChunk implements Disposable
{
    private final WeakReference<ChunkData> chunk;
    private int idX;
    private int idY;
    private final int localX;
    private final int localY;
    private boolean dirty;
    private float counter;

    private boolean physicsGenerated;
    private Array<Body> bodies;

    private static boolean FLAGS[] =
        new boolean[Constants.Physics.PHYSIC_BLOCK_SIZE * Constants.Physics.PHYSIC_BLOCK_SIZE];

    public PhysicChunk(ChunkData chunk, int x, int y, int localX, int localY)
    {
        this.chunk = new WeakReference<>(chunk);
        this.idX = x;
        this.idY = y;
        this.localX = localX;
        this.localY = localY;
        this.counter = 0;

        this.physicsGenerated = false;
        this.dirty = false;

        this.bodies = new Array<>();
    }

    public void updateInfo(int x, int y)
    {
        this.idX = x;
        this.idY = y;
    }

    private void releasePhysics()
    {
        if (physicsGenerated)
        {
            doRelease();

            physicsGenerated = false;
        }
    }

    private void generatePhysics()
    {
        physicsGenerated = true;

        doGenerate();
    }

    public int getX()
    {
        return idX * Constants.Physics.PHYSIC_BLOCK_SIZE;
    }

    public int getY()
    {
        return idY * Constants.Physics.PHYSIC_BLOCK_SIZE;
    }

    private int getIndex(int x, int y)
    {
        return x + y * Constants.Physics.PHYSIC_BLOCK_SIZE;
    }

    private void setFlag(int x, int y, boolean value)
    {
        FLAGS[x + y * Constants.Physics.PHYSIC_BLOCK_SIZE] = value;
    }

    private boolean getFlag(int x, int y)
    {
        return FLAGS[x + y * Constants.Physics.PHYSIC_BLOCK_SIZE];
    }

    private boolean isTriangle(BlockData blockData)
    {
        if (blockData instanceof ConcreteBD)
        {
            ContactShape contactShape = ((ConcreteBD) blockData).getContactShape();

            if (contactShape instanceof CSTriangle)
            {
                return true;
            }
        }

        return false;
    }


    private void doGenerate()
    {
        ChunkData chunk = this.chunk.get();

        if (chunk == null)
            return;

        World world = getWorld();

        if (world == null)
            return;

        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.StaticBody;

        for (int j = 0; j < Constants.Physics.PHYSIC_BLOCK_SIZE; j++)
        {
            int lY = localY * Constants.Physics.PHYSIC_BLOCK_SIZE + j;
            int gY = idY * Constants.Physics.PHYSIC_BLOCK_SIZE + j;

            Array<BlockData> blocks = chunk.getLayer(Constants.Layers.BLOCK_LAYER_FOREGROUND);

            if (blocks == null)
                continue;

            for (int i = 0, index = chunk.getIndex(localX * Constants.Physics.PHYSIC_BLOCK_SIZE, lY);
                 i < Constants.Physics.PHYSIC_BLOCK_SIZE; i++, index++)
            {
                int gX = idX * Constants.Physics.PHYSIC_BLOCK_SIZE + i;

                BlockData blockData = blocks.get(index);

                if (blockData != null && !blockData.getCreator().doGeneratePhysics())
                {
                    setFlag(i, j, false);
                    continue;
                }

                if (isTriangle(blockData))
                {
                    byte mask = blockData.calculateNeighborMask(
                        chunk.getMap(), gX, gY, Constants.Layers.BLOCK_LAYER_FOREGROUND);

                    switch (mask)
                    {
                        // ╔
                        case 6:
                        //  ╝
                        case 9:
                        {
                            Body box = world.createBody(def);

                            EdgeShape poly = new EdgeShape();
                            poly.set(-0.5f * Constants.Physics.SCALE, -0.5f * Constants.Physics.SCALE,
                                      0.5f * Constants.Physics.SCALE, 0.5f * Constants.Physics.SCALE);

                            box.setTransform(((float)gX + 0.5f) * Constants.Physics.SCALE,
                                             ((float)gY + 0.5f) * Constants.Physics.SCALE, 0);

                            box.createFixture(poly, 1.0f);
                            poly.dispose();

                            bodies.add(box);

                            setFlag(i, j, false);
                            continue;
                        }
                        // ╚
                        case 3:
                        //  ╗
                        case 12:
                        {
                            Body box = world.createBody(def);

                            EdgeShape poly = new EdgeShape();
                            poly.set(0.5f * Constants.Physics.SCALE, -0.5f * Constants.Physics.SCALE,
                                    -0.5f * Constants.Physics.SCALE, 0.5f * Constants.Physics.SCALE);

                            box.setTransform(((float)gX + 0.5f) * Constants.Physics.SCALE,
                                             ((float)gY + 0.5f) * Constants.Physics.SCALE, 0);

                            box.createFixture(poly, 1.0f);
                            poly.dispose();

                            bodies.add(box);

                            setFlag(i, j, false);
                            continue;
                        }
                    }

                }

                // flag the blocks is good for creating physics
                setFlag(i, j, blockData != null && !blockData.isFixture()
                        && blockData.getCreator().hasContact());
            }
        }


        for (int j = 0; j < Constants.Physics.PHYSIC_BLOCK_SIZE; j++)
        {
            int gY = idY * Constants.Physics.PHYSIC_BLOCK_SIZE + j;

            for (int i = 0; i < Constants.Physics.PHYSIC_BLOCK_SIZE; i++)
            {
                int gX = idX * Constants.Physics.PHYSIC_BLOCK_SIZE + i;

                if (getFlag(i, j))
                {
                    // detect the rectangle sizes
                    int rW = 1, rH = 1;

                    for (int rX = i + 1, index = getIndex(i + 1, j);
                         rX < Constants.Physics.PHYSIC_BLOCK_SIZE; rX++, index++)
                    {
                        if (FLAGS[index])
                        {
                            rW += 1;
                            FLAGS[index] = false;
                        }
                        else
                        {
                            break;
                        }
                    }

                    for (int rY = j + 1; rY < Constants.Physics.PHYSIC_BLOCK_SIZE; rY++)
                    {
                        boolean failed = false;

                        for (int rX = 0, index = getIndex(i, rY); rX < rW; rX++, index++)
                        {
                            if (!FLAGS[index])
                            {
                                failed = true;
                                break;
                            }
                        }

                        if (failed)
                        {
                            break;
                        }
                        else
                        {
                            for (int rX = 0, index = getIndex(i, rY); rX < rW; rX++, index++)
                            {
                                FLAGS[index] = false;
                            }

                            rH += 1;
                        }
                    }

                    float rWf = (float)rW / 2.0f, rHf = (float)rH / 2.0f;

                    Body box = world.createBody(def);

                    PolygonShape poly = new PolygonShape();
                    poly.setAsBox(rWf * Constants.Physics.SCALE, rHf * Constants.Physics.SCALE);

                    box.setTransform(((float)gX + rWf) * Constants.Physics.SCALE,
                                     ((float)gY + rHf) * Constants.Physics.SCALE, 0);

                    box.createFixture(poly, 1.0f);
                    poly.dispose();

                    bodies.add(box);
                }
            }
        }
    }

    private World getWorld()
    {
        ChunkData chunkData = chunk.get();

        if (chunkData == null)
            return null;

        Map map = chunkData.getMap();

        if (map == null)
            return null;

        return map.getPhysicWorld();
    }

    private void doRelease()
    {
        World world = getWorld();

        if (world != null)
        {
            for (Body body : bodies)
            {
                world.destroyBody(body);
            }
        }

        bodies.clear();
    }

    public void init()
    {
        generatePhysics();
    }

    public void setDirty()
    {
        this.dirty = true;
    }

    @Override
    public void dispose()
    {
        releasePhysics();
    }

    public void update(float dt)
    {
        counter -= dt;

        if (counter <= 0)
        {
            counter = 0.25f;

            if (dirty)
            {
                dirty = false;

                releasePhysics();
                generatePhysics();

                BrainOut.EventMgr.sendEvent(PhysicChunkUpdatedEvent.obtain(this));
            }
        }
    }
}
