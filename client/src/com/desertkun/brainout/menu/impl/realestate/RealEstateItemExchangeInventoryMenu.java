package com.desertkun.brainout.menu.impl.realestate;

import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.client.SocialController;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.components.InventoryMoveSoundComponent;
import com.desertkun.brainout.content.components.RealEstateItemContainerComponent;
import com.desertkun.brainout.content.components.UniqueComponent;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.data.FreePlayMap;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.impl.AmountMenu;
import com.desertkun.brainout.menu.impl.ExchangeInventoryMenu;
import com.desertkun.brainout.menu.ui.ConsumableInventoryPanel;
import com.desertkun.brainout.menu.ui.InventoryPanel;
import com.desertkun.brainout.menu.ui.RealEstateItemInventoryPanel;
import com.esotericsoftware.minlog.Log;
import org.json.JSONObject;

public class RealEstateItemExchangeInventoryMenu extends ExchangeInventoryMenu
{
    private final ActiveData realEstateItem;

    public RealEstateItemExchangeInventoryMenu(PlayerData playerData, ActiveData realEstateItem)
    {
        super(playerData);
        this.realEstateItem = realEstateItem;
    }

    @Override
    protected boolean allowWeightUpgrade()
    {
        return false;
    }

    @Override
    protected boolean allowQuestsButton()
    {
        return false;
    }

    private boolean putItemIntoRealEstateItem(ConsumableRecord record, int amount, RealEstateItemInventoryPanel rsPanel)
    {
        String rsItemKey = rsPanel.getRealEstateItemKey();
        if (rsItemKey == null)
        {
            return false;
        }

        FreePlayMap map = rsPanel.getRsItem().getMap(FreePlayMap.class);
        if (map == null)
            return false;

        JSONObject args = new JSONObject();

        args.put("map", map.getDimension());
        args.put("key", rsItemKey);
        args.put("id", record.getId());
        args.put("amount", amount);

        BrainOutClient.SocialController.sendRequest("put_market_rs_item", args,
            new SocialController.RequestCallback()
        {
            @Override
            public void success(JSONObject response)
            {
                InventoryMoveSoundComponent snd = record.getItem().getContent().getComponent(InventoryMoveSoundComponent.class);

                if (snd != null)
                {
                    snd.play(playerData);
                }

                rsPanel.refresh();
            }

            @Override
            public void error(String reason)
            {
                if (Log.ERROR) Log.error(reason);
                Menu.playSound(MenuSound.denied);
            }
        });

        return true;
    }

    @Override
    protected boolean needStats()
    {
        return false;
    }

    @Override
    protected void addInventoryPanel(Table b)
    {
        b.add(targetPanel).size(228, 392).row();

        targetWeightUpgrades = new Table(BrainOutClient.Skin);
        targetWeightRoot = new Table(BrainOutClient.Skin);
        targetWeightRoot.setBackground("form-gray");
        targetWeightRoot.align(Align.right);
        targetWeightRoot.add(targetWeighInfo).pad(1);
        targetWeightRoot.add(targetWeightUpgrades).row();

        b.add(targetWeightRoot).expandX().fillX().row();

        targetFill = new ProgressBar(
            0, getMaxWeightForUser(), 0.05f, false,
            BrainOutClient.Skin, "progress-inventory");

        b.add(targetFill).expandX().fillX().padTop(-2).row();
        targetPanel.setHeight(targetPanel.getHeight() - targetWeightRoot.getHeight() - targetFill.getHeight());
    }

    private boolean withdrawItemFromMarket(
        RealEstateItemInventoryPanel.RealEstateItemInventoryRecord record, int amount, RealEstateItemInventoryPanel rsPanel)
    {
        String rsItemKey = rsPanel.getRealEstateItemKey();
        if (rsItemKey == null)
        {
            return false;
        }

        FreePlayMap map = rsPanel.getRsItem().getMap(FreePlayMap.class);
        if (map == null)
            return false;

        Content c = record.getRecord().getItem().getContent();
        if (c == null)
            return false;

        JSONObject args = new JSONObject();

        args.put("map", map.getDimension());
        args.put("key", rsItemKey);
        args.put("record", record.getKey());
        args.put("amount", amount);

        BrainOutClient.SocialController.sendRequest("withdraw_market_rs_item", args,
            new SocialController.RequestCallback()
        {
            @Override
            public void success(JSONObject response)
            {
                InventoryMoveSoundComponent snd = c.getComponent(InventoryMoveSoundComponent.class);

                if (snd != null)
                {
                    snd.play(playerData);
                }

                rsPanel.refresh();
            }

            @Override
            public void error(String reason)
            {
                if (Log.ERROR) Log.error(reason);
                Menu.playSound(MenuSound.denied);
            }
        });

        return true;
    }

    @Override
    protected boolean move(InventoryPanel.InventoryRecord record, int amount, InventoryPanel from, InventoryPanel to)
    {
        if (from instanceof ConsumableInventoryPanel)
        {
            if (to instanceof RealEstateItemInventoryPanel)
            {
                return putItemIntoRealEstateItem(record.getRecord(), amount, (RealEstateItemInventoryPanel) to);
            }
        }
        else if ((from instanceof RealEstateItemInventoryPanel) && (to instanceof ConsumableInventoryPanel))
        {
            if (record instanceof RealEstateItemInventoryPanel.RealEstateItemInventoryRecord)
            {
                RealEstateItemInventoryPanel.RealEstateItemInventoryRecord found =
                        ((RealEstateItemInventoryPanel.RealEstateItemInventoryRecord) record);

                UniqueComponent u = found.getRecord().getItem().getContent().getComponent(UniqueComponent.class);

                if (amount == 1 || found.getRecord().getItem() instanceof InstrumentConsumableItem || u != null)
                {
                    return withdrawItemFromMarket(found, 1, (RealEstateItemInventoryPanel) from);
                }
                else
                {
                    if (split(record.getRecord()))
                    {
                        if (record.getRecord().getAmount() == 2)
                        {
                            return withdrawItemFromMarket(found, 1, (RealEstateItemInventoryPanel) from);
                        }
                        else
                        {
                            pushMenu(new AmountMenu(
                                    Math.max(record.getRecord().getAmount() / 2, 1),
                                    record.getRecord().getAmount()
                            ){
                                @Override
                                public void approve(int amount)
                                {
                                    withdrawItemFromMarket(found, amount, (RealEstateItemInventoryPanel) from);
                                }

                                @Override
                                public void cancel()
                                {

                                }
                            });

                            return true;
                        }
                    }
                    else
                    {
                        return withdrawItemFromMarket(found, amount, (RealEstateItemInventoryPanel) from);
                    }
                }
            }
        }

        return super.move(record, amount, from, to);
    }

    private InventoryPanel createRealEstateItemInventoryPanel(ActiveData realEstateItem)
    {
        return new RealEstateItemInventoryPanel(realEstateItem, dragAndDrop)
        {
            @Override
            public boolean hasCustomFriendlyButton()
            {
                return true;
            }

            @Override
            public TextButton customFriendlyButton()
            {
                return null;
            }

            @Override
            protected boolean clicked(InventoryRecord record, boolean primary)
            {
                if (primary)
                {
                    if (split(record.getRecord()))
                    {
                        if (record.getRecord().getAmount() == 2)
                        {
                            return move(record, 1, targetPanel, sourcePanel);
                        }
                        else
                        {
                            pushMenu(new AmountMenu(
                                    Math.max(record.getRecord().getAmount() / 2, 1),
                                    record.getRecord().getAmount()
                            ){
                                @Override
                                public void approve(int amount)
                                {
                                    move(record, amount, targetPanel, sourcePanel);
                                }

                                @Override
                                public void cancel()
                                {

                                }
                            });

                            return true;
                        }
                    }
                    else
                    {
                        return move(record, record.getRecord().getAmount(), targetPanel, sourcePanel);
                    }
                }

                return true;
            }

            @Override
            protected void itemHovered(InventoryRecord record, boolean b)
            {
                hoverItem(record, b);
            }

            @Override
            protected void updated(float weight)
            {
                super.updated(weight);

                updateTargetWeight(weight, this.getMaxWeightForUser());
            }
        };
    }

    public float getMaxWeightForUser()
    {
        return realEstateItem.getContent().getComponent(RealEstateItemContainerComponent.class).getWeightLimit();
    }

    @Override
    protected InventoryPanel createTargetPanel()
    {
        return createRealEstateItemInventoryPanel(realEstateItem);
    }
}
