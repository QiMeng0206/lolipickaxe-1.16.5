package com.tighug.lolipickaxe.client.gui;

import com.mojang.datafixers.util.Pair;
import com.tighug.lolipickaxe.item.lolicard.LoliCard;
import com.tighug.lolipickaxe.network.NetworkHandler;
import com.tighug.lolipickaxe.client.LoliCardUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;

public class GUIOptionalLoliCard extends LoliPickaxeListScreen<String> {
    private String string;

    protected GUIOptionalLoliCard() {
        super(s -> new  TranslationTextComponent("item.LoliCard." + s));
        minecraft = Minecraft.getInstance();
        this.addList(LoliCardUtil.getCustomArtNames());
    }

    @Override
    public void onClose() {
        if (string != null) {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putString(LoliCard.LoliCardID, string);
            NetworkHandler.Pack.SET_LOLI_CARD.sendToServer(nbt);
        }
        super.onClose();
    }

    @Override
    protected void init() {
        int i = (this.width - bgWidth) / 2 + 10;
        int j = (this.height - bgHigh) / 2 + 15;
        this.addButton(new Button(i, j, 60, 20, new TranslationTextComponent("gui.done"), b -> this.close()));
        super.init();
        this.buttons.forEach(button -> {
            if (button instanceof LoliPickaxeListElement){
                ((LoliPickaxeListElement) button).isRenderLevel = false;
            }
        });
        this.onSetValue(() -> {
            List<Pair<String, Integer>> list1 = this.getList();
            if (!list1.isEmpty()) {
                this.string = list1.get(0).getFirst();
                close();
            }
        });
    }

    private void close() {
        assert this.minecraft != null;
        this.onClose();
        this.minecraft.setScreen(null);
    }

    public static class OpenGui {
        public OpenGui() {
            Minecraft.getInstance().setScreen(new GUIOptionalLoliCard());
        }
    }
}
