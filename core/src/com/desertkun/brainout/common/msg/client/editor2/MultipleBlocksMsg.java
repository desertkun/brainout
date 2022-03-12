package com.desertkun.brainout.common.msg.client.editor2;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;
import com.desertkun.brainout.common.msg.ModeMessage;
import com.desertkun.brainout.content.block.Block;


public class MultipleBlocksMsg extends BlockMessage
{
    public int[] x;
    public int[] y;

    public String block;

    public static class Point
    {
        private final int x;
        private final int y;

        public Point(int x, int y)
        {
            this.x = x;
            this.y = y;
        }

        public int getY()
        {
            return y;
        }

        public int getX()
        {
            return x;
        }
    }

    public MultipleBlocksMsg() {}

    public MultipleBlocksMsg(String dimension, Block block, Queue<Point> points)
    {
        super(dimension);

        this.block = block != null ? block.getID() : null;

        this.x = new int[points.size];
        this.y = new int[points.size];

        int i = 0;

        for (Point point : points)
        {
            this.x[i] = point.x;
            this.y[i] = point.y;

            i++;
        }
    }
}
