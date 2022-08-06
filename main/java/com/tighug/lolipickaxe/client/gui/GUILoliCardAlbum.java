package com.tighug.lolipickaxe.client.gui;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.tighug.lolipickaxe.client.LoliCardUtil;
import com.tighug.lolipickaxe.item.lolicard.LoliCard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Collections;
import java.util.List;

public class GUILoliCardAlbum extends LoliPickaxeListScreen<String>{
    private final List<String> id;
    private String string;

    protected GUILoliCardAlbum() {
        super(s -> new TranslationTextComponent("item.LoliCard." + s));
        minecraft = Minecraft.getInstance();
        assert minecraft.player != null;
        ItemStack mainHandItem = minecraft.player.getMainHandItem();
        if (mainHandItem.getItem() instanceof LoliCard.LoliAllCardAlbum) {
            id = LoliCardUtil.getCustomArtNames();
        }
        else {
            ListNBT nbt = mainHandItem.getOrCreateTag().getList(LoliCard.LoliCardID, 8);
            id = Lists.newArrayList();
            for (int i = 0; i < nbt.size(); ++i) {
                id.add(nbt.getString(i));
            }
        }
        Collections.sort(id);
        this.addList(id);
    }

    @Override
    protected void init() {
        int i = (this.width - bgWidth) / 2 + 10;
        int j = (this.height - bgHigh) / 2 + 15;
        this.addButton(new Button(i, j, 60, 20, new TranslationTextComponent("gui.done"), b -> {
            assert this.minecraft != null;
            this.onClose();
            this.minecraft.setScreen(null);
        }));
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
                openLoliCard();
            }
            this.buttons.forEach(button -> {
                if (button instanceof LoliPickaxeListElement){
                    ((LoliPickaxeListElement) button).value = false;
                }
            });
        });
    }

    private void openLoliCard() {
        assert this.minecraft != null;
        if (string != null) this.minecraft.setScreen(new guiLoliCard(string, this));
    }

    private void next(){
        int index = id.indexOf(string) + 1;
        if (index >= id.size()) index = 0;
        string = id.get(index);
        openLoliCard();
    }

    private void pre(){
        int index = id.indexOf(string) - 1;
        if (index < 0) index = id.size() - 1;
        string = id.get(index);
        openLoliCard();
    }

    private static class guiLoliCard extends GUILoliCard{
        private final GUILoliCardAlbum guiLoliCardAlbum;

        private guiLoliCard(String nane, GUILoliCardAlbum gui) {
            super(nane);
            guiLoliCardAlbum = gui;
        }

        @Override
        protected void init() {
            this.addButton(new Button(this.width - 80, this.height - 40, 60, 20, new TranslationTextComponent("gui.loliPickaxe.potion.return"), b -> {
                assert this.minecraft != null;
                this.minecraft.setScreen(guiLoliCardAlbum);
            }));
            this.addButton(new Button(10, (height - 30) / 2, 20, 20,new StringTextComponent("<"), b -> {
                guiLoliCardAlbum.pre();
            }));
            this.addButton(new Button(width - 30, (height - 30) / 2, 20, 20,new StringTextComponent(">"), b -> {
                guiLoliCardAlbum.next();
            }));
            super.init();
        }

        @Override
        public boolean keyPressed(int p_231046_1_, int p_231046_2_, int p_231046_3_) {
            switch (p_231046_1_) {
                case 262:
                    guiLoliCardAlbum.next();
                    break;
                case 263:
                    guiLoliCardAlbum.pre();
                    break;
            }
            return super.keyPressed(p_231046_1_, p_231046_2_, p_231046_3_);
        }
    }

    public static class OpenGui {
        public OpenGui() {
            Minecraft.getInstance().setScreen(new GUILoliCardAlbum());
        }
    }
}
