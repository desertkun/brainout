package com.desertkun.brainout.data.containers;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.data.interfaces.Renderable;
import com.desertkun.brainout.data.interfaces.Updatable;

public abstract class CameraDataContainer<T extends Updatable & Renderable> extends DataContainer<T>
{
    private OrthographicCamera camera;
    private boolean flipY;

    public CameraDataContainer()
    {
        camera = new OrthographicCamera();
        flipY = false;
    }

    public void setFlipY(boolean flipY)
    {
        this.flipY = flipY;
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        camera.setToOrtho(flipY, context.width, context.height);

        updateCamera(context);

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        context.camera = camera;

        super.render(batch, context);
    }

    public abstract void updateCamera(RenderContext context);

    public OrthographicCamera getCamera()
    {
        return camera;
    }
}
