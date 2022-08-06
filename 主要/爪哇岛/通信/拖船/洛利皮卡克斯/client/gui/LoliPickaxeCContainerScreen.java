package com.tighug.lolipickaxe.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.tighug.lolipickaxe.inventory.LoliPickaxeContainer;
import com.tighug.lolipickaxe.network.NetworkHandler;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

import static com.tighug.lolipickaxe.util.Utils.MODID;

@OnlyIn(value = Dist.CLIENT)
public class LoliPickaxeCContainerScreen extends ContainerScreen<LoliPickaxeContainer> {
    public static final ResourceLocation bg = new ResourceLocation(MODID,"textures/gui/container/loli_pickaxe_container.png");
    private Button pre;
    private Button next;

    public LoliPickaxeCContainerScreen(LoliPickaxeContainer p_i51105_1_, PlayerInventory p_i51105_2_, ITextComponent p_i51105_3_) {
        super(p_i51105_1_, p_i51105_2_, p_i51105_3_);
        this.imageHeight = 256;
        this.inventoryLabelY = 256;
        this.titleLabelX = 173;
        this.titleLabelY = 8;
    }

    @Override
    protected void init() {
        super.init();
        pre = addButton(new Button((width - 256) / 2 + 213, (height - 256) / 2 + 22, 20, 20,new StringTextComponent("<"), this::pre));
        next = addButton(new Button((width - 256) / 2 + 253, (height - 256) / 2 + 22, 20, 20,new StringTextComponent(">"), this::next));
    }

    @Override
    public void render(@Nonnull MatrixStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
        super.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
        this.renderTooltip(p_230430_1_, p_230430_2_, p_230430_3_);
        this.pre.render(p_230430_1_, p_230430_2_,p_230430_3_, p_230430_4_);
        this.next.render(p_230430_1_,p_230430_2_,p_230430_3_,p_230430_4_);
    }

    @Override
    protected void renderTooltip(@NotNull MatrixStack p_230459_1_, int p_230459_2_, int p_230459_3_) {
        super.renderTooltip(p_230459_1_, p_230459_2_, p_230459_3_);
    }

    @Override
    protected void renderLabels(@NotNull MatrixStack p_230451_1_, int p_230451_2_, int p_230451_3_) {
        this.font.draw(p_230451_1_, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 1);
        StringTextComponent str = new StringTextComponent(String.valueOf(this.menu.getPAGE()));
        int strSize = font.width(str) /2;
        this.font.draw(p_230451_1_, str, 203 - strSize, 27, 2);
    }


    @Override
    protected void renderBg(@Nonnull MatrixStack p_230450_1_, float p_230450_2_, int p_230450_3_, int p_230450_4_) {
        assert this.minecraft != null;
        this.minecraft.getTextureManager().bind(bg);
        int i = (this.width - 256) / 2+40;
        int j = (this.height - 256) / 2;
        blit(p_230450_1_, i, j, 0, 0, 256, 256, 256, 256);
    }

    @Override
    protected boolean checkHotbarKeyPressed(int p_195363_1_, int p_195363_2_) {
        if (p_195363_2_ == this.inventory.selected+2){
            return false;
        }
        return super.checkHotbarKeyPressed(p_195363_1_, p_195363_2_);
    }

    private void next(Button button){
        int i = this.menu.getPAGE();
        CompoundNBT nbt = new CompoundNBT();
        if (++i > this.menu.INVENTORY.maxPage) {
            nbt.putInt("int", 1);
            NetworkHandler.Pack.PAGE.sendToServer(nbt);
            this.menu.setPAGE(1);
        }
        else {
            nbt.putInt("int", i);
            NetworkHandler.Pack.PAGE.sendToServer(nbt);
            this.menu.setPAGE(i);
        }
    }

    private void pre(Button button){
        int i = this.menu.getPAGE();
        CompoundNBT nbt = new CompoundNBT();
        if (--i < 1) {
            nbt.putInt("int", this.menu.INVENTORY.maxPage);
            NetworkHandler.Pack.PAGE.sendToServer(nbt);
            this.menu.setPAGE(this.menu.INVENTORY.maxPage);
        }
        else {
            nbt.putInt("int", i);
            NetworkHandler.Pack.PAGE.sendToServer(nbt);
            this.menu.setPAGE(i);
        }
    }
}
