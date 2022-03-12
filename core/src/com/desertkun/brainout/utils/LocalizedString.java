package com.desertkun.brainout.utils;

import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.L;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocalizedString 
{
    private static final Pattern VANILLA_PATTERN = Pattern.compile("^([A-Z0-9_]{3,})$");
    private static final Pattern VANILLA_COMPLEX_PATTERN = Pattern.compile("\\{([A-Z0-9_]{3,})}");

    public String id;
    private boolean complex;
	
	public LocalizedString(String id)
	{
		set(id);
	}
	
	public LocalizedString()
	{
		id = null;
	}

	public void set(String id)
	{
        this.id = id;
        this.complex = id != null && id.contains("{");
	}

    public boolean isValid()
    {
        return id != null && (complex || (!id.equals("") && L.has(id)));
    }

    public boolean isVanilla()
    {
        return id != null && VANILLA_PATTERN.matcher(id).matches();
    }

    public String get()
    {
        if (id == null)
        {
            return "???";
        }

        if (this.complex)
        {

            Matcher m = VANILLA_COMPLEX_PATTERN.matcher(id);

            StringBuffer sb = new StringBuffer();
            while (m.find())
            {
                m.appendReplacement(sb, L.get(m.group(1)));
            }

            m.appendTail(sb);

            return sb.toString();
        }

        return L.get(id);
    }

    public String get(String... format)
    {
        return L.get(id, format);
    }

    public String get(String formatValue)
    {
        if (id == null)
        {
            return "? " + formatValue + " ?";
        }

        if (this.complex)
        {

            Matcher m = VANILLA_COMPLEX_PATTERN.matcher(id);

            StringBuffer sb = new StringBuffer();
            while (m.find())
            {
                m.appendReplacement(sb, L.get(m.group(1)));
            }

            m.appendTail(sb);

            return sb.toString().replace("%s", formatValue);
        }

        return L.get(id, formatValue);
    }

    @Override
    public String toString()
    {
        return get();
    }

    public String getID()
	{
		return id;
	}
}
