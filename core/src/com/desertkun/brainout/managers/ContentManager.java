package com.desertkun.brainout.managers;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.Json.Serializable;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.utils.JsonProcessor;
import com.esotericsoftware.minlog.Log;

import java.util.function.Consumer;


public class ContentManager
{
	private ObjectMap<String, Content> items;

    public interface ContentCreateCallback
    {
        void created(Content content);
    }

    public interface ContentUnloadPredicate
    {
        boolean test(Content content);
    }

	public Content get(String id)
	{
	    if (id == null)
	        return null;

		return items.get(id, null);
	}

    @SuppressWarnings("unchecked")
    public <T extends Content> T get(String id, Class<T> tClass)
    {
        Content content = get(id);
        if (BrainOut.R.instanceOf(tClass, content))
        {
            return (T)content;
        }

        return null;
    }

    public interface ContentQueryPredicate<T extends Content>
    {
        boolean isMatch(T check);
    }

    public <T extends Content> Array<T> queryContentTpl(Class<T> classOff, ContentQueryPredicate<T> predicate)
    {
        Array<T> res = new Array<>();

        for (ObjectMap.Entry<String, Content> entry: items)
        {
            //noinspection unchecked
            T value = (T)entry.value;

            if (BrainOut.R.instanceOf(classOff, value) && predicate.isMatch(value))
            {
                res.add(value);
            }
        }

        return res;
    }

    public <T extends Content> T queryOneContentTpl(Class<T> classOff, ContentQueryPredicate<T> predicate)
    {
        for (ObjectMap.Entry<String, Content> entry: items)
        {
            //noinspection unchecked
            T value = (T)entry.value;

            if (BrainOut.R.instanceOf(classOff, value) && predicate.isMatch(value))
            {
                return value;
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public <T extends Content> Array<T> queryContent(Class classOff, ContentQueryPredicate<T> predicate)
    {
        Array<T> res = new Array<T>();

        for (ObjectMap.Entry<String, Content> entry: items)
        {
            if (BrainOut.R.instanceOf(classOff, entry.value) && predicate.isMatch(((T) entry.value)))
            {
                res.add(((T) entry.value));
            }
        }

        return res;
    }
	
	public <T extends Content> Array<T> queryContent(Class<T> classOff)
	{
		Array<T> res = new Array<T>();

        for (ObjectMap.Entry<String, Content> entry: items)
        {
            if (BrainOut.R.instanceOf(classOff, entry.value))
            {
                res.add((T)entry.value);
            }
        }
		
		return res;
	}

    @SuppressWarnings("unchecked")
    public <T extends Content> boolean iterateContent(Class<T> classOff, ContentQueryPredicate<T> predicate)
    {
        for (ObjectMap.Entry<String, Content> entry: items)
        {
            if (BrainOut.R.instanceOf(classOff, entry.value))
            {
                if (predicate.isMatch(((T) entry.value)))
                    return true;
            }
        }

        return false;
    }


    @SuppressWarnings("unchecked")
    public <T> void queryContentGen(Class<T> classOff, Consumer<T> consumer)
    {
        for (ObjectMap.Entry<String, Content> entry: items)
        {
            if (BrainOut.R.instanceOf(classOff, entry.value))
            {
                consumer.accept(((T) entry.value));
            }
        }
    }

	public ContentManager()
	{
        items = new ObjectMap<>();
	}

	public ObjectMap<String, Content> getItems()
    {
        return items;
	}

    public void registerItem(String id, Content item)
    {
        items.put(id, item);
    }

    @SuppressWarnings("unchecked")
    public void loadContent(Json json, JsonValue jsonData, final ContentCreateCallback callback)
    {
        JsonProcessor<Content> processor = new JsonProcessor()
        {
            @Override
            public void objectCreated(Serializable obj, String name)
            {
                Content c = (Content)obj;
                c.setID(name);
                callback.created(c);

                registerItem(name, c);
            }
        };

        processor.load(json, jsonData);
    }

    public void unloadAllContent()
    {
        for (ObjectMap.Entry<String, Content> entry: items)
        {
            Content content = entry.value;
            content.dispose();
        }

        items.clear();
    }

    public void unloadContent(ContentUnloadPredicate predicate)
    {
        Array<String> removeList = new Array<>();

        for (ObjectMap.Entry<String, Content> entry: items)
        {
            Content content = entry.value;

            if (predicate.test(content))
                continue;

            content.dispose();
            removeList.add(entry.key);
        }

        for (String key : removeList)
        {
            items.remove(key);
        }
    }
}
