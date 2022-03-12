package com.desertkun.brainout.common.enums.data;

public class DuelResultsND extends NotifyData
{
    public int yourDeaths;
    public int enemyDeaths;
    public int deathsRequired;

    public DuelResultsND() {}
    public DuelResultsND(int your, int enemy, int deathsRequired)
    {
        this.yourDeaths = your;
        this.enemyDeaths = enemy;
        this.deathsRequired = deathsRequired;
    }
}
