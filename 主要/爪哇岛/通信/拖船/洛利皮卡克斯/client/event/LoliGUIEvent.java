package com.tighug.lolipickaxe.client.event;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tighug.lolipickaxe.Lolipickaxe;
import com.tighug.lolipickaxe.item.Tool.ILoli;
import com.tighug.lolipickaxe.item.Tool.ItemLoliPickaxe;
import com.tighug.lolipickaxe.util.Config;
import com.tighug.lolipickaxe.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.CraftingResultSlot;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class LoliGUIEvent implements Lolipickaxe.LoliEvent {
    private static final Map<Item, List<String>> TAG = Maps.newHashMap();
    static final TextFormatting[] colors = { TextFormatting.GOLD, TextFormatting.BLUE, TextFormatting.GREEN, TextFormatting.AQUA, TextFormatting.RED, TextFormatting.LIGHT_PURPLE, TextFormatting.YELLOW };
    static int curColor = 0;
    static int tick = 0;
    private static final String loliPickaxe_speed = "TREE(3)";
    private static final String loliPickaxe_damage = "TREE(3)";
    public static final List<String> enchantment_id = Lists.newArrayList();

    @SuppressWarnings("rawtypes")
    @SubscribeEvent
    public static void onDrawForeground(GuiContainerEvent.DrawForeground event){
        ContainerScreen guiContainer = event.getGuiContainer();
        Minecraft minecraft = event.getGuiContainer().getMinecraft();
        if (guiContainer instanceof CreativeScreen) return;
        List<Slot> slots = Lists.newArrayList(guiContainer.getMenu().slots);
        for (Slot slot : slots){
            if (slot.container instanceof PlayerInventory || slot instanceof CraftingResultSlot) continue;
            ItemStack itemStack = slot.getItem();
            if (!itemStack.isEmpty() && itemStack.getItem() instanceof ILoli){
                assert minecraft.gameMode != null;
                assert minecraft.player != null;
                if (((ILoli) itemStack.getItem()).hasOwner(itemStack)) minecraft.gameMode.handleInventoryMouseClick(guiContainer.getMenu().containerId, slot.index, 0, ClickType.THROW, minecraft.player);
            }
        }
    }

    @SubscribeEvent
    public static void onLoliPickaxeTooltip(ItemTooltipEvent event) {
        ItemStack itemStack = event.getItemStack();
        if (itemStack.isEmpty()) return;
        List<ITextComponent> toolTip = event.getToolTip();
        if (Config.DISPLAY_TAGS.get()) {
            if (!TAG.containsKey(itemStack.getItem())) {
                Map<ResourceLocation, ITag<Item>> allTags = ItemTags.getAllTags().getAllTags();
                List<String> strings = Lists.newArrayList();
                for (ResourceLocation rl : allTags.keySet()){
                    ITag<Item> tag = allTags.get(rl);
                    if (tag.contains(itemStack.getItem())) {
                        strings.add(rl.toString());
                    }
                }
                if (!strings.isEmpty()) Collections.sort(strings);
                TAG.put(itemStack.getItem(), strings);
            }
            if (!TAG.get(itemStack.getItem()).isEmpty()) {
                toolTip.add(new StringTextComponent("Tags:").withStyle(TextFormatting.DARK_GRAY));
                for (String string : TAG.get(itemStack.getItem())) {
                    toolTip.add(new StringTextComponent("   ").append(string).withStyle(TextFormatting.DARK_GRAY));
                }
            }
        }
        if (Config.DISPLAY_COUNT.get() && itemStack.getCount() > 99) {
            toolTip.add(new TranslationTextComponent("container.loliPickaxe.stackCount").append(new StringTextComponent(" " + itemStack.getCount()).withStyle(TextFormatting.AQUA)));
        }
        if (itemStack.getItem() instanceof ItemLoliPickaxe){
            for (ITextComponent text : event.getToolTip()){
                if (text instanceof TextComponent){
                    if (text instanceof TranslationTextComponent){
                        TranslationTextComponent component = (TranslationTextComponent) text;
                        if (enchantment_id.contains(component.getKey())){
                            if (component.getSiblings().size() > 1){
                                if (component.getSiblings().get(1) instanceof TranslationTextComponent){
                                    TranslationTextComponent component1 = (TranslationTextComponent) component.getSiblings().get(1);
                                    int i;
                                    StringBuilder str = new StringBuilder();
                                    String s = component1.getKey();
                                    for (int j = s.length() - 4; j < s.length(); ++j){
                                        if (s.charAt(j) >= 48 && s.charAt(j) <= 57){
                                            str.append(s.charAt(j));
                                        }
                                    }
                                    try {
                                        i = Integer.parseInt(str.toString());
                                    }
                                    catch (ClassCastException ignored) {
                                        continue;
                                    }
                                    component.getSiblings().set(1, Utils.getTextComponent(i));
                                }
                            }
                            continue;
                        }

                    }
                    TextComponent component = (TextComponent) text;
                    for (int i = 0 ; i < component.getSiblings().size() ; ++i){
                        ITextComponent text1 = component.getSiblings().get(i);
                        if (text1 instanceof TranslationTextComponent){
                            if (1 < ((TranslationTextComponent) text1).getArgs().length){
                                Object object = ((TranslationTextComponent) text1).getArgs()[1];
                                if (object instanceof TranslationTextComponent){
                                    boolean b = itemStack.getOrCreateTag().getCompound(ILoli.CONFIG).getBoolean("attack");
                                    if (((TranslationTextComponent) object).getKey().equals("attribute.name.generic.attack_speed")){
                                        String str = loliPickaxe_speed;
                                        if (b) str = String.valueOf(itemStack.getOrCreateTag().getCompound(ILoli.CONFIG).getInt("loliPickaxeAttack_speed"));
                                        StringBuilder sb = new StringBuilder();
                                        for (int j = 0; j < str.length(); j++) {
                                            sb.append(colors[(curColor + j) % colors.length]);
                                            sb.append(str.charAt(j));
                                        }
                                        component.getSiblings().set(i,(new StringTextComponent("")).append(new TranslationTextComponent("attribute.modifier.equals.0",new StringTextComponent(sb.toString()),new TranslationTextComponent(Attributes.ATTACK_SPEED.getDescriptionId()))).withStyle(TextFormatting.DARK_GREEN));
                                    }else if (((TranslationTextComponent) object).getKey().equals("attribute.name.generic.attack_damage")){
                                        String str = loliPickaxe_damage;
                                        if (b) str = String.valueOf(itemStack.getOrCreateTag().getCompound(ILoli.CONFIG).getFloat("loliPickaxeAttack_damage"));
                                        StringBuilder sb = new StringBuilder();
                                        for (int j = 0; j < str.length(); j++) {
                                            sb.append(colors[(curColor + j) % colors.length]);
                                            sb.append(str.charAt(j));
                                        }
                                        component.getSiblings().set(i,(new StringTextComponent("")).append(new TranslationTextComponent("attribute.modifier.equals.0",new StringTextComponent(sb.toString()),new TranslationTextComponent(Attributes.ATTACK_DAMAGE.getDescriptionId()))).withStyle(TextFormatting.DARK_GREEN));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}
