package com.tighug.lolipickaxe.client.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.datafixers.util.Pair;
import com.tighug.lolipickaxe.item.Tool.ItemLoliPickaxe;
import com.tighug.lolipickaxe.network.NetworkHandler;
import com.tighug.lolipickaxe.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@OnlyIn(value = Dist.CLIENT)
public class LoliPickaxePotionScreen extends LoliPickaxeListScreen<Effect>{
    private final ItemStack itemStack;
    private final List<EffectInstance> effectInstances;
    private TextFieldWidget otherValue;

    private LoliPickaxePotionScreen() {
        super(Effect::getDisplayName);
        assert Minecraft.getInstance().player != null;
        this.itemStack = Minecraft.getInstance().player.getMainHandItem();
        List<Effect> effects = Utils.EFFECTS;
        List<Effect> Blacklist = ItemLoliPickaxe.getEffectBlacklist(this.itemStack);
        effects.removeAll(Blacklist);
        this.effectInstances = ItemLoliPickaxe.getEffects(itemStack);
        List<Effect> list1 = Lists.newArrayList();
        if (!this.effectInstances.isEmpty()){
            for (EffectInstance instance : this.effectInstances){
                list1.add(instance.getEffect());
            }
            effects.removeAll(list1);
        }
        list1.addAll(effects);
        this.addList(list1);
        this.maxLevel = 256;
    }

    @Override
    public void onClose() {
        List<Pair<Effect, Integer>> list1 = this.getList();
        List<EffectInstance> list2 = Lists.newArrayList();
        for (Pair<Effect, Integer> pair : list1){
            list2.add(new EffectInstance(pair.getFirst(), 1200, pair.getSecond() - 1));
        }
        ItemLoliPickaxe.setEffects(this.itemStack, list2);
        NetworkHandler.Pack.EFFECT.sendToServer(this.itemStack.getOrCreateTag());
        super.onClose();
    }

    @Override
    public void render(@NotNull MatrixStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
        super.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
        this.otherValue.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
    }

    @Override
    protected void init() {
        int i = (this.width - bgWidth) / 2 + 10;
        int j = (this.height - bgHigh) / 2 + 15;
        this.addButton(new Button(i, j, 60, 20, new TranslationTextComponent("gui.loliPickaxe.potion.blacklist"), b -> {
            assert this.minecraft != null;
            this.onClose();
            this.minecraft.setScreen(new PotionBlacklist());
        }));
        this.addButton(new Button(i, j + 50, 60, 20, new TranslationTextComponent("gui.loliPickaxe.list.setLevel"), b -> {
            String string = this.otherValue.getValue().trim();
            if (string.isEmpty()) return;
            StringBuilder str = new StringBuilder();
            for (int k = 0; k < string.length(); ++k){
                if (string.charAt(k) >= 48 && string.charAt(k) <= 57){
                    str.append(string.charAt(k));
                }
            }
            if (str.toString().isEmpty()) return;
            int k;
            try {
                k = Integer.parseInt(str.toString());
            }
            catch (ClassCastException ignored) {
                this.otherValue.setValue("1");
                return;
            }
            this.setLevel(k);
        }));
        this.addButton(new Button(i, j + 75, 60, 20, new TranslationTextComponent("gui.done"), b -> {
            assert this.minecraft != null;
            this.onClose();
            this.minecraft.setScreen(null);
        }));
        this.otherValue = new TextFieldWidget(this.font, i, j + 25, 60, 20, StringTextComponent.EMPTY);
        this.otherValue.setMaxLength(5);
        this.otherValue.setValue("1");
        this.otherValue.setSuggestion("max : " + this.maxLevel);
        this.children.add(this.otherValue);
        super.init();
        this.buttons.forEach(button -> {
            if (button instanceof LoliPickaxeListElement){
                for (EffectInstance instance : this.effectInstances){
                    if (button.getMessage().equals(instance.getEffect().getDisplayName())){
                        ((LoliPickaxeListElement) button).setLevel(instance.getAmplifier() + 1);
                        ((LoliPickaxeListElement) button).setValue();
                    }
                }
            }
        });
    }

    public static class OpenGui {
        public OpenGui (){
            Minecraft.getInstance().setScreen(new LoliPickaxePotionScreen());
        }
    }

    private static class PotionBlacklist extends LoliPickaxeListScreen<Effect>{
        private final ItemStack itemStack;
        private final List<Effect> Blacklist;

        private PotionBlacklist() {
            super(Effect::getDisplayName);
            assert Minecraft.getInstance().player != null;
            this.itemStack = Minecraft.getInstance().player.getMainHandItem();
            List<Effect> effects = Lists.newArrayList(ForgeRegistries.POTIONS.getValues());
            List<EffectInstance> effectInstances = ItemLoliPickaxe.getEffects(itemStack);
            if (!effectInstances.isEmpty()){
                effects.removeIf(effect -> {
                    for (EffectInstance instance : effectInstances){
                        if (instance.getEffect().equals(effect)){
                            return true;
                        }
                    }
                    return false;
                });
            }
            List<Effect> list1 = Lists.newArrayList();
            this.Blacklist = ItemLoliPickaxe.getEffectBlacklist(this.itemStack);
            if (!this.Blacklist.isEmpty()){
                list1.addAll(this.Blacklist);
                effects.removeAll(list1);
            }
            list1.addAll(effects);
            this.addList(list1);
        }

        @Override
        public void onClose() {
            List<Pair<Effect, Integer>> list1 = this.getList();
            List<Effect> list2 = Lists.newArrayList();
            for (Pair<Effect, Integer> pair : list1){
                list2.add(pair.getFirst());
            }
            ItemLoliPickaxe.setEffectBlacklist(this.itemStack, list2);
            NetworkHandler.Pack.EFFECT_BLACKLIST.sendToServer(this.itemStack.getOrCreateTag());
            super.onClose();
        }

        @Override
        protected void init() {
            int i = (this.width - bgWidth) / 2 + 10;
            int j = (this.height - bgHigh) / 2 + 15;
            this.addButton(new Button(i, j, 60, 20, new TranslationTextComponent("gui.loliPickaxe.potion.return"), b -> {
                assert this.minecraft != null;
                this.onClose();
                this.minecraft.setScreen(new LoliPickaxePotionScreen());
            }));
            this.addButton(new Button(i, j + 25, 60, 20, new TranslationTextComponent("gui.done"), b -> {
                assert this.minecraft != null;
                this.onClose();
                this.minecraft.setScreen(null);
            }));
            super.init();
            this.buttons.forEach(button -> {
                if (button instanceof LoliPickaxeListElement){
                    ((LoliPickaxeListElement) button).isRenderLevel = false;
                    for (Effect Effect : this.Blacklist){
                        if (button.getMessage().equals(Effect.getDisplayName())){
                            ((LoliPickaxeListElement) button).setValue();
                        }
                    }
                }
            });
        }
    }
}
