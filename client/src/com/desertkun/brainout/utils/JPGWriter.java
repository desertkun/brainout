package com.desertkun.brainout.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class JPGWriter
{
    public static void Write(FileHandle handle, Pixmap pix) throws IOException
    {
        ImageIO.write(pixmapToBufferedImage(pix), "JPG", handle.file());
    }

    private static BufferedImage pixmapToBufferedImage(Pixmap p) {
        int w = p.getWidth();
        int h = p.getHeight();
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        int[] pixels = new int[w * h];
        for (int y=0; y<h; y++) {
            for (int x=0; x<w; x++) {
                //convert RGBA to RGB
                int value = p.getPixel(x, y);
                int R = ((value & 0xff000000) >>> 24);
                int G = ((value & 0x00ff0000) >>> 16);
                int B = ((value & 0x0000ff00) >>> 8);

                int i = x + (y * w);
                pixels[ i ] = (R << 16) | (G << 8) | B;
            }
        }
        img.setRGB(0, 0, w, h, pixels, 0, w);
        return img;
    }
}
