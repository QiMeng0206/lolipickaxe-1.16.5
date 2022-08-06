package com.tighug.lolipickaxe.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.datafixers.util.Pair;
import com.tighug.lolipickaxe.client.LoliCardUtil;
import com.tighug.lolipickaxe.item.lolicard.LoliCard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

@OnlyIn(value = Dist.CLIENT)
public class GUILoliCard extends Screen {
    private final ResourceLocation bg;
    private final int bgHeight;
    private final int bgWidth;
    private final double ratio;
    protected int renderWidth;
    protected int renderHeight;
    protected double dcx;
    protected double dcy;
    private double ds;
    private double lastMouseX = 0;
    private double lastMouseY = 0;
    private double lastMovedX = 0;
    private double lastMovedY = 0;

    private GUILoliCard() {
        super(StringTextComponent.EMPTY);
        minecraft = Minecraft.getInstance();
        assert minecraft.player != null;
        String s = minecraft.player.getMainHandItem().getOrCreateTag().getString(LoliCard.LoliCardID);
        bg = LoliCardUtil.getResourceLocation(s);
        Pair<Integer, Integer> pair = LoliCardUtil.getSize(s);
        bgHeight = pair.getFirst();
        bgWidth = pair.getSecond();
        ratio = (double) bgWidth / (double) bgHeight;
        ds = 1;
    }

    protected GUILoliCard(String nane) {
        super(StringTextComponent.EMPTY);
        String s = nane;
        bg = LoliCardUtil.getResourceLocation(s);
        Pair<Integer, Integer> pair = LoliCardUtil.getSize(s);
        bgHeight = pair.getFirst();
        bgWidth = pair.getSecond();
        ratio = (double) bgWidth / (double) bgHeight;
        ds = 1;
    }

    @Override
    public void render(@NotNull MatrixStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
        super.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
        renderBg(p_230430_1_);
    }

    @Override
    public void mouseMoved(double p_212927_1_, double p_212927_3_) {
        lastMovedX = lastMouseX - p_212927_1_;
        lastMovedY = lastMouseY - p_212927_3_;
        lastMouseX = p_212927_1_;
        lastMouseY = p_212927_3_;
        super.mouseMoved(p_212927_1_, p_212927_3_);
    }

    @Override
    public boolean mouseDragged(double p_231045_1_, double p_231045_3_, int p_231045_5_, double p_231045_6_, double p_231045_8_) {
        dcx -= lastMovedX;
        dcy -= lastMovedY;
        return super.mouseDragged(p_231045_1_, p_231045_3_, p_231045_5_, p_231045_6_, p_231045_8_);
    }

    @Override
    public boolean mouseScrolled(double p_231043_1_, double p_231043_3_, double p_231043_5_) {
        if (p_231043_5_ > 0) {
            if (ds < 3) ds += 0.1;
        } else {
            if (ds > 0.5) ds -= 0.1;
        }
        return super.mouseScrolled(p_231043_1_, p_231043_3_, p_231043_5_);
    }

    @Override
    public boolean keyPressed(int p_231046_1_, int p_231046_2_, int p_231046_3_) {
        switch (p_231046_1_) {
            case 61:
                if (ds < 2.8) ds += 0.2;
                else ds = 3;
                break;
            case 45:
                if (ds > 0.7) ds -= 0.2;
                else ds = 0.5;
                break;
            case 32:
                dcy = 0;
                dcx = 0;
                ds = 1;
                break;
        }
        return super.keyPressed(p_231046_1_, p_231046_2_, p_231046_3_);
    }

    protected void renderBg(@Nonnull MatrixStack p_230450_1_) {
        assert this.minecraft != null;
        this.minecraft.getTextureManager().bind(bg);
        int cx = (int) (this.width / 2 + dcx);
        int cy = (int) (this.height / 2 + dcy);
        double proportion;
        if (ratio < (double) width / (double) height) {
            proportion = (double) height / (double) bgHeight;
        } else {
            proportion = (double) width / (double) bgWidth;
        }
        renderWidth = (int) (bgWidth * proportion / 2 * ds);
        renderHeight = (int) (bgHeight * proportion / 2 * ds);
        blit(p_230450_1_, cx - renderWidth, cy - renderHeight, 0, 0, 2 * renderWidth, 2 * renderHeight, 2 * renderWidth, 2 * renderHeight);
    }

    public static class OpenGui {
        public OpenGui() {
            Minecraft.getInstance().setScreen(new GUILoliCard());
        }
    }
}
