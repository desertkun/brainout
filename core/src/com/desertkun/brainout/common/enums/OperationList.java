package com.desertkun.brainout.common.enums;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.BrainOut;

public class OperationList
{
    private Array<Operation> operationList;
    private int currentOperation;
    private boolean started;

    public static abstract class Operation implements Runnable
    {
        public Operation()
        {
        }

        public float getProgress()
        {
            return 1;
        }

        public abstract boolean complete(float dt);

        public void done() {}
    }

    public OperationList()
    {
        this.operationList = new Array<Operation>();
        this.currentOperation = 0;
        this.started = false;
    }

    public Operation getCurrentOperation()
    {
        if (currentOperation < operationList.size)
        {
            return operationList.get(currentOperation);
        }

        return null;
    }

    public Operation operationCompleted()
    {
        currentOperation++;

        Operation currentOperation = getCurrentOperation();
        if (currentOperation != null)
        {
            return currentOperation;
        }
        else
        {
            release();
        }

        return null;
    }

    public void release()
    {
        if (!started)
        {
            throw new IllegalStateException("OperationList is not started!");
        }

        currentOperation = 0;
        operationList.clear();
        started = false;
    }

    public float getProgress()
    {
        Operation current = getCurrentOperation();
        if (current != null)
        {
            return ((float)currentOperation + current.getProgress()) / (float)operationList.size;
        }
        else
        {
            return 1;
        }
    }

    public void addOperation(Operation operation)
    {
        operationList.add(operation);
    }


    public boolean isStarted()
    {
        return started;
    }

    public void start()
    {
        synchronized (this)
        {
            if (started)
            {
                throw new IllegalStateException("Already started");
            }

            currentOperation = 0;
            Operation operation = getCurrentOperation();

            if (operation == null)
            {
                throw new IllegalStateException("Nothing to operate!");
            }

            started = true;
            operation.run();
        }
    }

    public void update(float dt)
    {
        synchronized (this)
        {
            if (started)
            {
                Operation operation = getCurrentOperation();
                if (operation != null && operation.complete(dt))
                {
                    Operation nextOperation = operationCompleted();

                    if (nextOperation == null)
                    {
                        completed();
                    }

                    operation.done();

                    if (nextOperation != null)
                    {
                        Gdx.app.postRunnable(nextOperation);
                    }
                }
            }
        }
    }

    private void completed()
    {
        started = false;
    }
}
