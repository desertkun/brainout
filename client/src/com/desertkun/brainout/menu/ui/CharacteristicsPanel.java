package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;

public class CharacteristicsPanel extends Table
{
    private Array<Char> chars;
    private float extraHeight;

    public interface Data
    {
        float getData();
    }

    public static abstract class View
    {
        public static String format(float d)
        {
            return Integer.toString((int)d);
        }

        public abstract void render(Data from, Table to);

        public boolean hasLabel()
        {
            return true;
        }

        public float getExpectedHeight()
        {
            return 24;
        }
    }

    public static class SimpleView extends View
    {
        private final String suffix;
        private final boolean sign;

        public SimpleView(String suffix)
        {
            this.suffix = suffix;
            this.sign = false;
        }

        public SimpleView(String suffix, boolean sign)
        {
            this.suffix = suffix;
            this.sign = sign;
        }

        public SimpleView()
        {
            this.suffix = null;
            this.sign = false;
        }

        public SimpleView(boolean sign)
        {
            this.suffix = null;
            this.sign = sign;
        }

        @Override
        public void render(Data from, Table to)
        {
            float data = from.getData();
            String value = (sign ? (data > 0 ? "+ " : (data < 0 ? "- ": "")) : "") + format(data);
            if (suffix != null)
            {
                value += " " + L.get(suffix);
            }
            Label valueLabel = new Label(value, BrainOutClient.Skin, "title-small");
            valueLabel.setAlignment(Align.right);

            to.add(valueLabel).expandX().fillX();
        }
    }

    public static class TextView extends View
    {
        private final String text;

        public TextView(String text)
        {
            this.text = text;
        }

        @Override
        public void render(Data from, Table to)
        {
            Label valueLabel = new Label(text, BrainOutClient.Skin, "title-small");
            valueLabel.setAlignment(Align.right);

            to.add(valueLabel).expandX().fillX();
        }
    }

    public static class SimpleIconView extends View
    {
        private final String suffix;
        private final String icon;

        public SimpleIconView(String suffix, String icon)
        {
            this.suffix = suffix;
            this.icon = icon;
        }

        public SimpleIconView()
        {
            this.suffix = null;
            this.icon = null;
        }

        @Override
        public void render(Data from, Table to)
        {
            String value = format(from.getData());
            if (suffix != null)
            {
                value += " " + L.get(suffix);
            }
            Label valueLabel = new Label(value, BrainOutClient.Skin, "title-small");
            valueLabel.setAlignment(Align.right);

            to.add(valueLabel).expandX().fillX().right();

            if (this.icon != null)
            {
                TextureAtlas.AtlasRegion region = BrainOutClient.getRegion(this.icon);

                if (region != null)
                {
                    Image icon = new Image(region);
                    to.add(icon).padLeft(4);
                }
            }
        }
    }

    public static class IconOnlyView extends View
    {
        private final String suffix;
        private final String icon;

        public IconOnlyView(String suffix, String icon)
        {
            this.suffix = suffix;
            this.icon = icon;
        }

        @Override
        public void render(Data from, Table to)
        {
            Label valueLabel = new Label(L.get(suffix), BrainOutClient.Skin, "title-small");
            valueLabel.setAlignment(Align.right);

            if (this.icon != null)
            {
                TextureAtlas.AtlasRegion region = BrainOutClient.getRegion(this.icon);

                if (region != null)
                {
                    Image icon = new Image(region);
                    icon.setScaling(Scaling.fit);
                    to.add(icon).maxHeight(16).expandX().right().padRight(-8);
                }
            }

            to.add(valueLabel).right();
        }
    }

    public static class FloatView extends View
    {
        private final String suffix;

        public FloatView(String suffix)
        {
            this.suffix = suffix;
        }

        public FloatView()
        {
            this.suffix = null;
        }

        @Override
        public void render(Data from, Table to)
        {
            String value = String.format("%.2f", from.getData());
            if (suffix != null)
            {
                value += " " + L.get(suffix);
            }
            Label valueLabel = new Label(value, BrainOutClient.Skin, "title-small");
            valueLabel.setAlignment(Align.right);

            to.add(valueLabel).expandX().fillX();
        }
    }

    public static class NofNView extends View
    {
        private final float max;

        public NofNView(float max)
        {
            this.max = max;
        }

        @Override
        public void render(Data from, Table to)
        {
            String value = format(from.getData()) + " / " + format(max);
            Label valueLabel = new Label(value, BrainOutClient.Skin, "title-small");
            valueLabel.setAlignment(Align.right);

            to.add(valueLabel).expandX().fillX();
        }
    }

    public static class ProgressView extends View
    {
        private final float min;
        private final float max;

        public ProgressView(float min, float max)
        {
            this.min = min;
            this.max = max;
        }

        @Override
        public void render(Data from, Table to)
        {
            ProgressBar progress = new ProgressBar(min, max,
                1, false, BrainOutClient.Skin, "progress-upgrades");

            progress.setValue(MathUtils.clamp(from.getData(), min, max));

            to.add(progress).right().expandX().width(192);

            Label valueLabel = new Label(format(from.getData()), BrainOutClient.Skin, "title-small");
            valueLabel.setAlignment(Align.right);

            to.add(valueLabel).width(88).padLeft(4);
        }
    }

    public static class Char
    {
        private String icon;
        private String title;
        private Data data;
        private View view;

        public Char(String icon, String title, Data data, View view)
        {
            this.icon = icon;
            this.title = title;
            this.data = data;
            this.view = view;
        }

        protected float getExpectedHeight()
        {
            return view.getExpectedHeight();
        }

        protected void render(Table item)
        {
            Table row = new Table();

            if (view.hasLabel())
            {
                if (icon != null)
                {
                    TextureRegion region = BrainOutClient.getRegion(icon);
                    if (region != null)
                    {
                        row.add(new Image(region));
                    }
                }
                else
                {
                    row.add();
                }

                if (title != null)
                {
                    Label titleLabel = new Label(L.get(title),
                            BrainOutClient.Skin, "title-yellow");
                    titleLabel.setAlignment(Align.left);

                    row.add(titleLabel).padLeft(12).expandX().left();
                }
                else
                {
                    row.add().expandX().left();
                }
            }

            Table value = new Table();
            view.render(data, value);
            row.add(value).expandX().fillX();

            item.add(row).expandX().fillX().row();
        }

    }

    public CharacteristicsPanel()
    {
        this.chars = new Array<>();
        this.extraHeight = 0;
    }

    public void add(String icon, String title, Data data, View view)
    {
        add(new Char(icon, title, data, view));
    }

    public void add(Char characteristic)
    {
        chars.add(characteristic);
        addExtraHeight(characteristic.getExpectedHeight());

        characteristic.render(this);
    }

    public float getExtraHeight()
    {
        return extraHeight;
    }

    protected void addExtraHeight(float extra)
    {
        extraHeight += extra;
    }

    public void update()
    {
        clear();

        for (Char characteristic : chars)
        {
            characteristic.render(this);
        }
    }
}
