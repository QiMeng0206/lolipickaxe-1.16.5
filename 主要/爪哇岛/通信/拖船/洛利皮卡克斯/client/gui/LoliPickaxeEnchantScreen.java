package com.tighug.lolipickaxe.client.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.datafixers.util.Pair;
import com.tighug.lolipickaxe.item.Tool.ItemLoliPickaxe;
import com.tighug.lolipickaxe.network.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

@OnlyIn(value = Dist.CLIENT)
public class LoliPickaxeEnchantScreen  extends LoliPickaxeListScreen<Enchantment>{
    private static final ItoString<Enchantment> toString = (enchantment -> {
        IFormattableTextComponent textComponent = (IFormattableTextComponent) enchantment.getFullname(1);
        textComponent.getSiblings().clear();
        if (Objects.requireNonNull(textComponent.getStyle().getColor()).serialize().equals("gray")){
            textComponent.withStyle(TextFormatting.WHITE);
        }
        return textComponent;
    });
    private final ItemStack itemStack;
    private final List<Pair<Enchantment, Integer>> enchants;
    private TextFieldWidget otherValue;

    private LoliPickaxeEnchantScreen() {
        super(toString);
        assert Minecraft.getInstance().player != null;
        this.itemStack = Minecraft.getInstance().player.getMainHandItem();
        List<Enchantment> enchantments = Lists.newArrayList(ForgeRegistries.ENCHANTMENTS.getValues());
        this.enchants = Lists.newArrayList();
        List<Enchantment> enchantments1 = Lists.newArrayList();
        enchantments.forEach(e -> {
            int i = EnchantmentHelper.getItemEnchantmentLevel(e, this.itemStack);
            if (i == 0) return;
            this.enchants.add(Pair.of(e, i));
            enchantments1.add(e);
        });
        enchantments.removeAll(enchantments1);
        enchantments1.addAll(enchantments);
        this.addList(enchantments1);
        this.maxLevel = 255;
    }

    @Override
    public void onClose() {
        List<Pair<Enchantment, Integer>> list1 = this.getList();
        ItemLoliPickaxe.enchant(this.itemStack, list1);
        NetworkHandler.Pack.ENCHANT.sendToServer(this.itemStack.getOrCreateTag());
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
        this.addButton(new Button(i, j + 25, 60, 20, new TranslationTextComponent("gui.loliPickaxe.list.setLevel"), b -> {
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
        this.addButton(new Button(i, j + 50, 60, 20, new TranslationTextComponent("gui.done"), b -> {
            assert this.minecraft != null;
            this.onClose();
            this.minecraft.setScreen(null);
        }));
        this.otherValue = new TextFieldWidget(this.font, i, j, 60, 20, StringTextComponent.EMPTY);
        this.otherValue.setMaxLength(5);
        this.otherValue.setValue("1");
        this.otherValue.setSuggestion("max : " + this.maxLevel);
        this.children.add(this.otherValue);
        super.init();
        this.buttons.forEach(button -> {
            if (button instanceof LoliPickaxeListElement){
                for (Pair<Enchantment, Integer> pair : this.enchants){
                    if (button.getMessage().equals(toString.run(pair.getFirst()))){
                        ((LoliPickaxeListElement) button).setLevel(pair.getSecond());
                        ((LoliPickaxeListElement) button).setValue();
                    }
                }
            }
        });
    }

    public static class OpenGui {
        public OpenGui (){
            Minecraft.getInstance().setScreen(new LoliPickaxeEnchantScreen());
        }
    }
}
