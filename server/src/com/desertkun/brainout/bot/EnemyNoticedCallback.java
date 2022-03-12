package com.desertkun.brainout.bot;

import com.desertkun.brainout.data.active.ActiveData;

public interface EnemyNoticedCallback
{
    // return true if you wish for this task to be pop'ed
    boolean noticed(TaskStack stack, ActiveData enemy);
}

