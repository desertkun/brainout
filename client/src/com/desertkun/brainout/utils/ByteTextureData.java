package com.desertkun.brainout.utils;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/** A {@link TextureData} implementation which should be used to create float textures. */
public class ByteTextureData implements TextureData {

    int width = 0;
    int height = 0;
    boolean isPrepared = false;
    final ByteBuffer buffer;

    public ByteTextureData(int w, int h, ByteBuffer buffer)
    {
        this.width = w;
        this.height = h;
        this.buffer = buffer;
    }

    @Override
    public TextureDataType getType () {
        return TextureDataType.Custom;
    }

    @Override
    public boolean isPrepared () {
        return isPrepared;
    }

    @Override
    public void prepare () {
        if (isPrepared) throw new GdxRuntimeException("Already prepared");
        isPrepared = true;
    }

    @Override
    public void consumeCustomData (int target) {
        Gdx.gl.glTexImage2D(target, 0, GL20.GL_RGBA, width, height, 0, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, buffer);
    }

    @Override
    public Pixmap consumePixmap () {
        throw new GdxRuntimeException("This TextureData implementation does not return a Pixmap");
    }

    @Override
    public boolean disposePixmap () {
        throw new GdxRuntimeException("This TextureData implementation does not return a Pixmap");
    }

    @Override
    public int getWidth () {
        return width;
    }

    @Override
    public int getHeight () {
        return height;
    }

    @Override
    public Pixmap.Format getFormat () {
        return Pixmap.Format.RGBA8888;
    }

    @Override
    public boolean useMipMaps () {
        return false;
    }

    @Override
    public boolean isManaged () {
        return true;
    }

    public ByteBuffer getBuffer () {
        return buffer;
    }
}
