package com.desertkun.brainout.content.components;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.effect.EffectSet;
import com.desertkun.brainout.data.components.InstrumentActionsComponentData;
import com.desertkun.brainout.data.components.InstrumentAnimationComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.data.interfaces.LengthBonePointData;
import com.esotericsoftware.spine.Bone;
import com.esotericsoftware.spine.Slot;
import com.esotericsoftware.spine.attachments.Attachment;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.InstrumentActionsComponent")
public class InstrumentActionsComponent extends ContentComponent
{
    private ObjectMap<String, Action> actions;

    public enum ActionType
    {
        updateSlot,
        effect,
        animate
    }

    public InstrumentActionsComponent()
    {
        actions = new ObjectMap<>();
    }

    public abstract static class Action
    {
        public abstract void process(InstrumentData instrument);
        public abstract void read(JsonValue jsonData);
        public abstract void completeLoad(AssetManager assetManager);
    }

    public static class UpdateSlotAction extends Action
    {
        private String slotName;
        private boolean enabled;

        public static class VirtualAttachment extends Attachment
        {
            private final Attachment from;

            public VirtualAttachment(Attachment from)
            {
                super(from.getName());

                this.from = from;
            }

            public Attachment getFrom()
            {
                return from;
            }

            @Override
            public Attachment copy()
            {
                return new VirtualAttachment(from);
            }
        }

        @Override
        public void process(InstrumentData instrument)
        {
            InstrumentAnimationComponentData iac =
                instrument.getComponentWithSubclass(InstrumentAnimationComponentData.class);

            Slot slot = iac.getSkeleton().findSlot(slotName);

            if (slot != null)
            {
                if (enabled)
                {
                    if (slot.getAttachment() instanceof VirtualAttachment)
                    {
                        VirtualAttachment attachment = ((VirtualAttachment) slot.getAttachment());

                        slot.setAttachment(attachment.from);
                    }
                }
                else
                {
                    if (!(slot.getAttachment() instanceof VirtualAttachment))
                    {
                        if (slot.getAttachment() != null)
                            slot.setAttachment(new VirtualAttachment(slot.getAttachment()));
                    }
                }
            }
        }

        @Override
        public void read(JsonValue jsonData)
        {
            this.slotName = jsonData.getString("slot");
            this.enabled = jsonData.getBoolean("enabled", true);
        }

        @Override
        public void completeLoad(AssetManager assetManager)
        {

        }
    }

    public class EffectAction extends Action
    {
        private EffectSet effects;
        private JsonValue effectsSetValue;

        public EffectAction()
        {
            effects = new EffectSet();
        }

        @Override
        public void process(InstrumentData instrument)
        {
            InstrumentAnimationComponentData iac =
                    instrument.getComponentWithSubclass(InstrumentAnimationComponentData.class);

            effects.launchEffects(new EffectSet.EffectAttacher()
            {
                @Override
                public LaunchData attachDefault() {
                    return iac.getLaunchPointData();
                }

                @Override
                public LaunchData attachTo(String attachObject)
                {
                    if (instrument.getOwner() == null) return null;

                    Bone bone = iac.getSkeleton().findBone(attachObject);

                    if (bone == null)
                    {
                        return null;
                    }

                    return new LengthBonePointData(bone, iac.getInstrumentLaunch(), 0);
                }
            });
        }

        @Override
        public void read(JsonValue jsonData)
        {
            effectsSetValue = jsonData.get("effects");
        }

        @Override
        public void completeLoad(AssetManager assetManager)
        {
            effects.read(effectsSetValue);
            effectsSetValue = null;
        }
    }

    public class AnimateAction extends Action
    {
        private String animation;

        @Override
        public void process(InstrumentData instrument)
        {
            InstrumentAnimationComponentData anim =
                instrument.getComponentWithSubclass(InstrumentAnimationComponentData.class);

            if (anim == null)
                return;

            anim.getState().setAnimation(0, animation, false);
        }

        @Override
        public void read(JsonValue jsonData)
        {
            animation = jsonData.getString("animation");
        }

        @Override
        public void completeLoad(AssetManager assetManager)
        {

        }
    }

    public Action newAction(ActionType actionType)
    {
        switch (actionType)
        {
            case updateSlot:
                return new UpdateSlotAction();
            case effect:
                return new EffectAction();
            case animate:
                return new AnimateAction();
        }

        return null;
    }

    @Override
    public InstrumentActionsComponentData getComponent(ComponentObject componentObject)
    {
        return new InstrumentActionsComponentData((InstrumentData)componentObject, this);
    }

    public void process(InstrumentData instrument, String payload)
    {
        Action action = actions.get(payload);

        if (action != null)
        {
            action.process(instrument);
        }
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        if (jsonData.has("actions"))
        {
            for (JsonValue actionValue : jsonData.get("actions"))
            {
                ActionType actionType = ActionType.valueOf(actionValue.getString("type",
                        ActionType.updateSlot.toString()));
                Action action = newAction(actionType);

                if (action != null)
                {
                    action.read(actionValue);
                    actions.put(actionValue.getString("for"), action);
                }
            }
        }
    }

    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

        for (ObjectMap.Entry<String, Action> action : actions)
        {
            action.value.completeLoad(assetManager);
        }
    }
}
