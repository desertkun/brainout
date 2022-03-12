package com.desertkun.brainout.content;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.utils.LocalizedString;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.Levels")
public class Levels extends Content
{
    private Array<Level> levels;
    private boolean beginFromZero;

    public class Level implements Json.Serializable
    {
        public final int number;
        public String icon;
        public LocalizedString name;
        public int score;
        public int skillpoints;
        public int gears;

        public Level nextLevel;
        public Level prevLevel;

        public Level(int number)
        {
            this.name = new LocalizedString();
            this.score = 0;
            this.icon = "";
            this.nextLevel = null;
            this.prevLevel = null;
            this.number = number;
        }

        public boolean hasNextLevel()
        {
            return this.score > 0;
        }

        @Override
        public void write(Json json)
        {

        }

        @Override
        public void read(Json json, JsonValue jsonData)
        {
            this.name.set(jsonData.getString("name", null));
            this.score = jsonData.getInt("score", 0);
            this.icon = jsonData.getString("icon", "");
            this.skillpoints = jsonData.getInt("skillpts", 1);
            this.gears = jsonData.getInt("gears", 0);
        }

        public void setNextLevel(Level nextLevel)
        {
            this.nextLevel = nextLevel;
        }

        public Level getNextLevel()
        {
            return nextLevel;
        }

        public Level getPrevLevel()
        {
            return prevLevel;
        }

        @Override
        public String toString()
        {
            int index = beginFromZero ? number - 1 : number;

            return index < 10 ? "0" + index : String.valueOf(index);
        }

        public String toShortString()
        {
            return Integer.toString(beginFromZero ? number - 1 : number);
        }


        public void setPrevLevel(Level prevLevel)
        {
            this.prevLevel = prevLevel;
        }
    }

    public Levels()
    {
        levels = new Array<Level>();
    }

    public Level getLevel(int level)
    {
        int index = level - 1;

        if (index <= 0) return levels.first();
        if (index >= levels.size) return levels.peek();
        return levels.get(index);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        beginFromZero = jsonData.getBoolean("beginFromZero", false);

        if (jsonData.has("levels"))
        {
            JsonValue levels = jsonData.get("levels");

            if (levels.isArray())
            {
                Level oldLevel = null;
                int i = 1;

                for (JsonValue level : levels)
                {
                    Level newLevel = new Level(i);
                    newLevel.read(json, level);

                    if (oldLevel != null)
                    {
                        oldLevel.setNextLevel(newLevel);
                    }
                    newLevel.setPrevLevel(oldLevel);

                    this.levels.add(newLevel);

                    oldLevel = newLevel;
                    i++;
                }
            }
        }
    }

    public Array<Level> getLevels()
    {
        return levels;
    }

    public Level getFirstLevel()
    {
        return levels.first();
    }
}
