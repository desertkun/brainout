package com.desertkun.brainout.utils;

public class DistanceUtils
{
    public static float PointToLineDistance(float x, float y, float x1, float y1, float x2, float y2) {

        float A = x - x1; // position of point rel one end of line
        float B = y - y1;
        float C = x2 - x1; // vector along line
        float D = y2 - y1;
        float E = -D; // orthogonal vector

        float dot = A * E + B * C;
        float len_sq = E * E + C * C;

        return (float) Math.abs(dot / Math.sqrt(len_sq));
    }
}
