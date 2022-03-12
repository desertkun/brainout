package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.SerializationException;
import com.badlogic.gdx.utils.XmlReader;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;

public class RichLabel extends Table
{
    public RichLabel(String text, Skin skin, String defaultStyle)
    {
        super(skin);

        parseEntities(this, text, skin, defaultStyle);
    }

    public enum RichElement
    {
        br,
        text,
        img,
        div,
        loc,
        define
    }

    public void update(String text, Skin skin, String defaultStyle)
    {
        reset();

        parseEntities(this, text, skin, defaultStyle);
    }

    public void parseEntities(Table data, String text, Skin skin, String defaultStyle)
    {
        XmlReader reader = new XmlReader();
        try
        {
            XmlReader.Element root;

            try
            {
                root = reader.parse("<root>" + text + "</root>");
            }
            catch (Exception ignored)
            {
                return;
            }

            if (root.getChildCount() == 0)
            {
                Label label = new Label(root.getText(), skin, defaultStyle);
                label.setWrap(true);
                label.setAlignment(Align.center);
                data.add(label).expandX().fillX().row();
            }
            else
            {
                parseEntities(data, root, skin, defaultStyle);
            }
        }
        catch (SerializationException ignored)
        {
            data.add(new Label(text, skin, defaultStyle)).expandX().fillX().row();
        }
    }

    public Cell<Actor> addItem(Table data, Actor actor, XmlReader.Element item)
    {
        Cell<Actor> cell = data.add(actor);

        if (item.getBooleanAttribute("expand", false))
        {
            cell.expandX();
        }

        if (item.getBooleanAttribute("fill", false))
        {
            cell.fillX();
        }

        int height = item.getIntAttribute("height", 0);
        if (height != 0)
        {
            cell.height(height);
        }

        int width = item.getIntAttribute("width", 0);
        if (width != 0)
        {
            cell.width(width);
        }

        cell.padLeft(item.getIntAttribute("padLeft", 0))
            .padRight(item.getIntAttribute("padRight", 0))
            .padTop(item.getIntAttribute("padTop", 0))
            .padBottom(item.getIntAttribute("padBottom", 0));

        int pad = item.getIntAttribute("pad", 0);
        if (pad != 0)
        {
            cell.pad(pad);
        }

        return cell;
    }

    public void parseEntities(Table data, XmlReader.Element root, Skin skin, String defaultStyle)
    {
        Table line = new Table();

        for (int i = 0, t = root.getChildCount(); i < t; i++)
        {
            XmlReader.Element item = root.getChild(i);
            String kind = item.getName();

            RichElement element;
            try
            {
                element = RichElement.valueOf(kind);
            }
            catch (IllegalArgumentException ignored)
            {
                continue;
            }

            switch (element)
            {
                case br:
                {
                    data.add(line).expandX().fillX().row();
                    line = new Table();

                    break;
                }
                case text:
                {
                    addLabel(line, item.getText(), item, skin, defaultStyle);

                    break;
                }
                case loc:
                {
                    addLabel(line, L.get(item.getText()), item, skin, defaultStyle);

                    break;
                }
                case img:
                {
                    TextureAtlas.AtlasRegion region = BrainOutClient.getRegion(item.getText());

                    if (region == null)
                        continue;

                    Image image = new Image(region);
                    Cell cell = addItem(line, image, item);

                    String align = item.getAttribute("align", null);
                    if (align != null)
                    {
                        if (align.equals("center"))
                            cell.center();
                        else if (align.equals("left"))
                            cell.left();
                        else if (align.equals("right"))
                            cell.right();
                    }

                    break;
                }
                case div:
                {
                    Table child = new Table(skin);

                    parseEntities(child, item, skin, defaultStyle);

                    addItem(line, child, item);

                    break;
                }
                case define:
                {
                    String id = item.getAttribute("id", null);

                    if (id != null)
                    {
                        String defineValue = BrainOut.PackageMgr.getDefine(id, "default");

                        XmlReader.Element cond = item.getChildByName(defineValue);

                        if (cond != null)
                        {
                            Table child = new Table(skin);
                            parseEntities(child, cond, skin, defaultStyle);

                            addItem(line, child, cond);
                        }
                    }

                    break;
                }
            }
        }

        data.add(line).expandX().fillX().row();
    }

    private void addLabel(Table line, String text, XmlReader.Element item, Skin skin, String defaultStyle)
    {
        String style = item.getAttribute("style", defaultStyle);
        Label label;

        try
        {
            label = new Label(text, skin, style);
        }
        catch (GdxRuntimeException ignored)
        {
            label = new Label(text, skin, defaultStyle);
        }

        addItem(line, label, item);

        if (item.getBooleanAttribute("expand", false))
        {
            label.setWrap(true);
        }

        String align = item.getAttribute("align", null);
        if (align != null)
        {
            int a = Align.center;

            if (align.equals("left"))
                a = Align.left;
            else if (align.equals("right"))
                a = Align.right;

            label.setAlignment(a);
        }
    }
}
