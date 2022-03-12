package com.desertkun.brainout.common.msg.client;

import java.util.Map;

public class CreateNewOrderMsg
{
    public String store;
    public String item;
    public int amount;
    public String currency;
    public String component;
    public OrderEnvironmentItem[] env;

    public static class OrderEnvironmentItem
    {
        public String key;
        public String value;
    }

    public CreateNewOrderMsg() {}

    public CreateNewOrderMsg(String store, String item, int amount, String currency,
                             String component, Map<String, String> env)
    {
        this.store = store;
        this.item = item;
        this.amount = amount;
        this.currency = currency;
        this.component = component;
        this.env = new OrderEnvironmentItem[env.size()];

        int i = 0;

        for (String key : env.keySet())
        {
            OrderEnvironmentItem e = new OrderEnvironmentItem();

            e.key = key;
            e.value = env.get(key);

            this.env[i] = e;

            i++;
        }
    }
}
