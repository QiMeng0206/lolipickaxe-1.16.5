package com.tighug.lolipickaxe.client.gui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import com.tighug.lolipickaxe.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.tighug.lolipickaxe.util.Utils.MODID;

public abstract class LoliPickaxeListScreen<T> extends Screen {
    private static final ResourceLocation bg = new ResourceLocation(MODID, "textures/gui/loli_pickaxe_list.png");
    protected final List<T> list = Lists.newArrayList();
    protected final ItoString<T> toString;
    protected static final int bgWidth = 176;
    protected static final int bgHigh = 166;
    private static final int x = 76;
    private static final int y = 15;
    private int maxPage = 1;
    private int page = 1;
    protected int maxLevel;

    protected LoliPickaxeListScreen(ItoString<T> toString) {
        super(StringTextComponent.EMPTY);
        this.toString = toString;
    }

    protected void addList(Collection<T> list){
        this.list.addAll(list);
        if (list.size() < 8){
            this.maxPage = 1;
        }else {
            this.maxPage = list.size() <= 12 ? 2 : (list.size() - 7) / 5 + 2;
        }
    }

    private static int getInt(int i){
        if (i <= 1) return 0;
        return 6 + 5 * (i - 2);
    }

    protected void setPage(){
        this.buttons.forEach(b -> {
            if (!(b instanceof LoliPickaxeListElement)) return;
            ((LoliPickaxeListElement) b).setPage(this.page);
        });
    }

    @Override
    public void render(@NotNull MatrixStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
        this.renderBg(p_230430_1_);
        this.renderLabels(p_230430_1_);
        super.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
    }

    @Override
    protected void init() {
        int anInt = 0;
        int i1 = (this.width - bgWidth) / 2;
        int j1 = (this.height - bgHigh) / 2;
        super.init();
        if (this.list.isEmpty()) return;
        if (this.maxPage > 1){
            for (int i = 0; i < this.maxPage; ++i){
                if (i != 0) this.addButton(new LoliPickaxeListElement(i1, j1, ++anInt, b -> {
                    this.page -= 1;
                    this.setPage();
                }));
                if (i == 0){
                    for (int j = 0; j < 6; ++j){
                        T t = list.get(j);
                        this.addButton(new LoliPickaxeListElement(i1, j1, ++anInt, this.toString.run(t)));
                    }
                }
                if (i == this.maxPage - 1){
                    for (int j = 0; j < this.list.size() - getInt(this.maxPage); ++j){
                        int index = getInt(this.maxPage) + j;
                        T t = list.get(index);
                        this.addButton(new LoliPickaxeListElement(i1, j1, ++anInt, this.toString.run(t)));
                    }
                }
                if (i != 0 && i != this.maxPage - 1){
                    for (int j = 0; j < 5; ++j){
                        int index = getInt(i + 1) + j;
                        T t = list.get(index);
                        this.addButton(new LoliPickaxeListElement(i1, j1, ++anInt, this.toString.run(t)));
                    }
                }
                if (i != this.maxPage - 1) this.addButton(new LoliPickaxeListElement(i1, j1, ++anInt, b -> {
                    this.page += 1;
                    this.setPage();
                }));
            }
        }
        else {
            for (int i = 1; i <= this.list.size(); ++i){
                T t = this.list.get(i - 1);
                this.addButton(new LoliPickaxeListElement(i1, j1, i, this.toString.run(t)));
            }
        }
        this.setPage();
    }

    protected void renderBg(@Nonnull MatrixStack p_230450_1_) {
        assert this.minecraft != null;
        this.minecraft.getTextureManager().bind(bg);
        int i = (this.width - bgWidth) / 2;
        int j = (this.height - bgHigh) / 2;
        blit(p_230450_1_, i, j, 0, 0, bgWidth, bgHigh, 256, 256);
    }

    protected void renderLabels(@NotNull MatrixStack p_230451_1_) {
    }

    protected T getElement(ITextComponent textComponent){
        for (T t : this.list){
            if (this.toString.run(t).equals(textComponent)) return t;
        }
        return null;
    }

    protected List<Pair<T, Integer>> getList(){
        List<Widget> list1 = Lists.newArrayList(this.buttons);
        list1.removeIf(b -> !(b instanceof LoliPickaxeListElement));
        list1.removeIf(b -> !((LoliPickaxeListElement) b).value);
        if (list1.isEmpty()) return Lists.newArrayList();
        Map<T, Integer> map = Maps.newHashMap();
        for (Widget w : list1){
            T t = getElement(w.getMessage());
            if (t != null) map.put(t, ((LoliPickaxeListElement) w).level);
        }
        List<Pair<T, Integer>> list2 = Lists.newArrayList();
        for (T t : map.keySet()){
            Pair<T, Integer> pair = Pair.of(t, map.get(t));
            list2.add(pair);
        }
        return list2;
    }

    protected void setLevel(int i){
        int j = Utils.clamp(i, 0, this.maxLevel);
        this.buttons.forEach(b -> {
            if (!(b instanceof LoliPickaxeListElement)) return;
            ((LoliPickaxeListElement) b).setLevel(j);
        });
    }

    protected void onSetValue(Runnable runnable) {
        this.buttons.forEach(b -> {
            if (b instanceof LoliPickaxeListElement) {
                ((LoliPickaxeListElement) b).onSetValue.add(runnable);
            }
        });
    }

    interface ItoString<T>{
        ITextComponent run(T t);
    }

    protected static class LoliPickaxeListElement extends Button {
        protected boolean value = false;
        private static final int bgWidth = 92;
        private static final int bgHigh = 19;
        private static final int u = 0;
        private static final int v = 166;
        private final Set<Runnable> onSetValue = Sets.newHashSet();
        private final boolean isEnd;
        private final int page;
        private int level = 1;
        private FontRenderer font;

        protected boolean isRenderLevel = true;

        private LoliPickaxeListElement(int x,int y, int i, ITextComponent p_i232255_5_) {
            super(x + LoliPickaxeListScreen.x, y + ((i % 7 == 0 ? 6 : (i % 7) - 1) * bgHigh + LoliPickaxeListScreen.y), bgWidth, bgHigh, p_i232255_5_, (button) -> ((LoliPickaxeListElement) button).setValue());
            this.isEnd = i % 7 == 0;
            this.page = (i + 6) / 7;
        }

        private LoliPickaxeListElement(int x,int y, int i, IPressable iPressable) {
            super(x + LoliPickaxeListScreen.x,y + (i % 7 == 0 ? LoliPickaxeListScreen.y + 6 * bgHigh : LoliPickaxeListScreen.y), bgWidth, bgHigh, i % 7 == 0 ? new TranslationTextComponent("gui.loliPickaxe.list.next") : new TranslationTextComponent("gui.loliPickaxe.list.pre"), iPressable);
            this.isEnd = i % 7 == 0;
            this.page = (i + 6) / 7;
            this.isRenderLevel = false;
        }

        @Override
        public void renderButton(@NotNull MatrixStack p_230431_1_, int p_230431_2_, int p_230431_3_, float p_230431_4_) {
            Minecraft minecraft = Minecraft.getInstance();
            font = minecraft.font;
            minecraft.getTextureManager().bind(LoliPickaxeListScreen.bg);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            int i = this.value ? u + bgWidth : u;
            int j = this.isEnd ? v + bgHigh : v;
            if (this.isHovered() && !this.value) {
                j += 2 * bgHigh;
            }
            this.blit(p_230431_1_, this.x, this.y, i, j, bgWidth, bgHigh);
            this.renderLabels(p_230431_1_);
        }

        private void renderLabels(@NotNull MatrixStack p_230451_1_) {
            ITextComponent message = this.isRenderLevel ? this.getMessage().copy().append(" ").append(Utils.getTextComponent(this.level)) : this.getMessage();
            int i = this.x + (this.width - font.width(message)) /2;
            int j = this.y + this.height / 2 - 5;
            this.font.draw(p_230451_1_, message, i, j, 16777215);
        }

        protected void setValue(){
            this.value = !this.value;
            onSetValue.forEach(Runnable::run);
        }

        private void setPage(int i){
            this.visible = i == this.page;
        }
        
        protected void setLevel(int i){
            if (!this.value) this.level = i;
        }
    }
}
