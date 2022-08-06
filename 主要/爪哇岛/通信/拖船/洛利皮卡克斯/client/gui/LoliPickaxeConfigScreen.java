package com.tighug.lolipickaxe.client.gui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.tighug.lolipickaxe.item.Tool.ILoli;
import com.tighug.lolipickaxe.network.NetworkHandler;
import com.tighug.lolipickaxe.player.LoliConfig;
import com.tighug.lolipickaxe.util.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.tighug.lolipickaxe.util.Utils.MODID;

@OnlyIn(value = Dist.CLIENT)
public class LoliPickaxeConfigScreen extends Screen {
    private static final ResourceLocation LOLI_PICKAXE_CONFIG_GUI_TEXTURE = new ResourceLocation(MODID, "textures/gui/loli_pickaxe_config.png");
    private final LoliConfig loliConfig = LoliConfig.getDefaultConfig();
    private final Map<Integer, Serializable> map = Maps.newHashMap();
    private final int max;
    private final List<LoliConfig.Type> values = Arrays.asList(LoliConfig.Type.values());
    private int anInt = 0;
    private Button booleanValue;
    private TranslationTextComponent textComponent;
    private TextFieldWidget otherValue;
    private LoliConfig.Type type;

    private LoliPickaxeConfigScreen() {
        super(StringTextComponent.EMPTY);
        this.minecraft = Minecraft.getInstance();
        assert this.minecraft.player != null;
        if (this.minecraft.player.getMainHandItem().getItem() instanceof ILoli){
            CompoundNBT compound = this.minecraft.player.getMainHandItem().getOrCreateTag().getCompound(ILoli.CONFIG);
            if (!compound.isEmpty()) loliConfig.replacement(LoliConfig.of(compound));
        }
        if (!Config.ALLOWABLE_KICK_PLAYER.get()) values.removeAll(Lists.newArrayList(LoliConfig.Type.KICK_PLAYER, LoliConfig.Type.KICK_PLAYER_MESSAGE));
        max = values.size() - 1;
    }

    @Override
    public void render(@NotNull MatrixStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
        renderBg(p_230430_1_);
        super.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
        this.renderLabels(p_230430_1_);
        this.otherValue.render(p_230430_1_,p_230430_2_,p_230430_3_,p_230430_4_);
    }

    @Override
    public void onClose() {
        this.setValue();
        if (OpenGui.UUID != null && !map.isEmpty()) {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putUUID("uuid", OpenGui.UUID);
            OpenGui.UUID = null;
            ListNBT listNBT = new ListNBT();
            for (Integer i : map.keySet()) {
                CompoundNBT nbt1 = new CompoundNBT();
                nbt1.putInt("id", i);
                if (map.get(i) instanceof Boolean) {
                    nbt1.putBoolean("value", (boolean) map.get(i));
                }
                else if(map.get(i) instanceof Number) {
                    nbt1.putDouble("value", ((Number) map.get(i)).doubleValue());
                }
                else {
                    nbt1.putString("value", (String) map.get(i));
                }
                listNBT.add(nbt1);
            }
            nbt.put(ILoli.CONFIG, listNBT);
            NetworkHandler.Pack.SET_CONFIG.sendToServer(nbt);
        }
        super.onClose();
    }

    private void renderBg(@Nonnull MatrixStack p_230450_1_) {
        assert this.minecraft != null;
        this.minecraft.getTextureManager().bind(LOLI_PICKAXE_CONFIG_GUI_TEXTURE);
        int i = (this.width - 220) / 2;
        int j = (this.height - 120) / 2;
        blit(p_230450_1_, i, j, 0, 0, 220, 120, 256, 256);
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    private void renderLabels(@NotNull MatrixStack p_230451_1_) {
        int w = (this.width - font.width(textComponent)) /2;
        this.font.draw(p_230451_1_, textComponent, w, this.height / 2 - 40, 16777215);
    }

    @Override
    protected void init() {
        super.init();
        this.addButton(new Button(width / 2 - 100, height / 2 + 20, 200, 20,new TranslationTextComponent("gui.done"), (button) -> {
            assert this.minecraft != null;
            this.onClose();
            this.minecraft.setScreen(null);
        }));
        this.addButton(new Button(width / 2 - 100, height / 2 - 40, 20, 20,new StringTextComponent("<"), this::pre));
        this.addButton(new Button(width / 2 + 80, height / 2 - 40, 20, 20,new StringTextComponent(">"), this::next));        this.booleanValue = this.addButton(new Button(width / 2 - 40, height / 2 - 10, 80, 20,new TranslationTextComponent("false"), (button) -> {
            if (((TranslationTextComponent) button.getMessage()).getKey().equals("false")){
                button.setMessage(new TranslationTextComponent("true"));
                this.loliConfig.setValue(this.type,true);
                map.put(this.type.ordinal(), true);
            }
            else {
                button.setMessage(new TranslationTextComponent("false"));
                this.loliConfig.setValue(this.type,false);
                map.put(this.type.ordinal(), false);
            }
        }));
        this.otherValue = new TextFieldWidget(this.font, width / 2 - 80, height / 2 - 10, 160, 20, StringTextComponent.EMPTY);
        this.otherValue.setMaxLength(100);
        this.otherValue.visible = false;
        this.children.add(this.otherValue);
        this.setRenderElements();
    }

    private void setRenderElements() {
        type = values.get(anInt);
        if (type.getValue().equals(Boolean.class)) {
            this.booleanValue.visible = true;
            this.otherValue.visible = false;
            this.booleanValue.setMessage(new TranslationTextComponent(String.valueOf(loliConfig.getValue(type))));
        }
        else {
            this.booleanValue.visible = false;
            this.otherValue.visible = true;
            if (loliConfig.getValue(type) instanceof Integer) {
                this.otherValue.setSuggestion(" min : " + (int) type.getMin() + " , " + "max : " + (int) type.getMax());
                this.otherValue.setValue(String.valueOf(((int) loliConfig.getValue(type))));
            }
            else if (type.getValue() == Number.class) {
                this.otherValue.setSuggestion(" min : " + type.getMin() + " , " + "max : " + type.getMax());
                this.otherValue.setValue(String.valueOf(loliConfig.getValue(type)));
            }
            else {
                this.otherValue.setSuggestion("");
                this.otherValue.setValue((String) loliConfig.getValue(type));
            }
        }
        textComponent = new TranslationTextComponent("gui.loliPickaxe.config." + type.getName());
    }

    private boolean setValue() {
        if (this.otherValue.visible) {
            String string = this.otherValue.getValue();
            if (type.getValue() == String.class) {
                if (string.isEmpty()) {
                    this.map.put(type.ordinal(), this.loliConfig.setForDefault(type));
                    return true;
                }
                this.loliConfig.setValue(type, string);
                this.map.put(type.ordinal(), string);
            }
            else {
                StringBuilder sb = new StringBuilder();
                for (int i = 0 ; i < string.length(); ++i){
                    if ((string.charAt(i) >= 48 && string.charAt(i) <= 57)) {
                        sb.append(string.charAt(i));
                        continue;
                    }
                    if (string.charAt(i) == '.' && !sb.toString().contains(".")) {
                        sb.append(string.charAt(i));
                        continue;
                    }
                    if (string.charAt(i) == 'E' && !sb.toString().contains("E")) {
                        sb.append(string.charAt(i));
                    }
                }
                if (sb.toString().isEmpty()) {
                    this.map.put(type.ordinal(), this.loliConfig.setForDefault(type));
                    return true;
                }
                double d;
                try {
                    d = Double.parseDouble(sb.toString());
                }
                catch (ClassCastException ignored) {
                    return false;
                }
                this.loliConfig.setValue(type, d);
                this.map.put(type.ordinal(), d);
            }
            return true;
        }
        return false;
    }

    private void next(Button button) {
        this.setValue();
        if (++this.anInt > this.max) this.anInt = 0;
        setRenderElements();
    }

    private void pre(Button button) {
        this.setValue();
        if (--this.anInt < 0) this.anInt = this.max;
        setRenderElements();
    }

    public static class OpenGui {
        private static UUID UUID;

        public OpenGui(){
            Minecraft.getInstance().setScreen(new LoliPickaxeConfigScreen());
        }

        public static void openGui(UUID uuid){
            UUID = uuid;
        }
    }
}
