package com.tighug.lolipickaxe.item;

import com.tighug.lolipickaxe.item.Tool.ItemLoliPickaxe;
import com.tighug.lolipickaxe.item.Tool.ItemSmallLoliPickaxe;
import com.tighug.lolipickaxe.item.lolicard.LoliCard;
import com.tighug.lolipickaxe.util.ModSoundEvents;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static com.tighug.lolipickaxe.util.Utils.MODID;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final RegistryObject<ItemLoliPickaxe> ITEM_LOLI_PICKAXE = ITEMS.register("loli_pickaxe", ItemLoliPickaxe::new);
    public static final RegistryObject<ItemSmallLoliPickaxe> ITEM_SMALL_LOLI_PICKAXE = ITEMS.register("loli_small_pickaxe", ItemSmallLoliPickaxe::new);
    public static final RegistryObject<LoliCard> LOLI_CARD = ITEMS.register("loli_card", LoliCard::new);
    public static final RegistryObject<LoliCard> LOLI_OPTIONAL_CARD = ITEMS.register("loli_optional_card", LoliCard.OptionalLoliCard::new);
    public static final RegistryObject<LoliCard.LoliCardAlbum> LOLI_CARD_ALBUM = ITEMS.register("loli_card_album", LoliCard.LoliCardAlbum::new);
    public static final RegistryObject<LoliCard.LoliCardAlbum> LOLI_ALL_CARD_ALBUM = ITEMS.register("loli_all_card_album", LoliCard.LoliAllCardAlbum::new);
    public static final RegistryObject<LoliRecord> LOLI_RECORD = ITEMS.register("loli_record", () -> new LoliRecord(ModSoundEvents.lolirecord));
    public static final RegistryObject<LoliRecord> TESTIFY_RECORD = ITEMS.register("testify_record", () -> new LoliRecord(ModSoundEvents.testify));
}
