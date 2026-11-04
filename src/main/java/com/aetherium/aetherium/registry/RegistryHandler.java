package com.aetherium.aetherium.registry;

import com.aetherium.aetherium.AetheriumMod;
import com.aetherium.aetherium.content.armor.AetheriumArmorMaterial;
import com.aetherium.aetherium.content.armor.AstralArmorMaterial;
import com.aetherium.aetherium.content.item.AetheriumAccessoryItem;
import com.aetherium.aetherium.content.item.AetheriumItemTier;
import com.aetherium.aetherium.content.item.AstralItemTier;
import com.aetherium.aetherium.content.item.AetheriumMagnetItem;
import com.aetherium.aetherium.content.item.AetheriumFlareItem;
import com.aetherium.aetherium.content.item.AetheriumSwordItem;
import com.aetherium.aetherium.content.item.AetheriumAxeItem;
import com.aetherium.aetherium.content.item.AstralAxeItem;
import com.aetherium.aetherium.content.item.AstralSwordItem;
import com.aetherium.aetherium.content.entity.RiftStalkerEntity;
import com.aetherium.aetherium.content.entity.LumiveloEntity;
import com.aetherium.aetherium.content.boss.AetherBoltEntity;
import com.aetherium.aetherium.content.boss.BossMirageEntity;
import com.aetherium.aetherium.content.boss.RiftGuardianEntity;
import com.aetherium.aetherium.content.boss.AstralDevourerEntity;
import com.aetherium.aetherium.content.item.BossSummoningItem;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EntityClassification;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ArmorItem;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.SwordItem;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fml.RegistryObject;

/** Central registry entry point for all Aetherium content. */
public final class RegistryHandler {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, AetheriumMod.MOD_ID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, AetheriumMod.MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITIES, AetheriumMod.MOD_ID);
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, AetheriumMod.MOD_ID);

    private static RegistryObject<SoundEvent> sound(String id) {
        ResourceLocation location = new ResourceLocation(AetheriumMod.MOD_ID, id);
        return SOUNDS.register(id, () -> new SoundEvent(location));
    }

    public static final RegistryObject<SoundEvent> AETHERIUM_BLADE_HIT = sound("aetherium_blade_hit");
    public static final RegistryObject<SoundEvent> ASTRAL_BLADE_ARC = sound("astral_blade_arc");
    public static final RegistryObject<SoundEvent> RIFT_STALKER_POUNCE = sound("rift_stalker_pounce");
    public static final RegistryObject<SoundEvent> LUMIVELO_CHIME = sound("lumivelo_chime");
    public static final RegistryObject<SoundEvent> RIFT_GUARDIAN_SLAM = sound("rift_guardian_slam");
    public static final RegistryObject<SoundEvent> ASTRAL_DEVOURER_PULSE = sound("astral_devourer_pulse");

    // Blocks
    public static final RegistryObject<Block> AETHERIUM_ORE = BLOCKS.register("aetherium_ore",
            () -> new Block(Block.Properties.of(Material.STONE)
                    .strength(4.5F, 6.0F)
                    .sound(SoundType.STONE)
                    .harvestTool(ToolType.PICKAXE)
                    .harvestLevel(3)
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> AETHERIUM_BLOCK = BLOCKS.register("aetherium_block",
            () -> new Block(Block.Properties.of(Material.METAL)
                    .strength(5.0F, 9.0F)
                    .sound(SoundType.METAL)
                    .harvestTool(ToolType.PICKAXE)
                    .harvestLevel(3)
                    .requiresCorrectToolForDrops()));

    // Custom creatures: small tracking/update values limit network and ticking overhead.
    public static final RegistryObject<EntityType<RiftStalkerEntity>> RIFT_STALKER = ENTITIES.register("rift_stalker",
            () -> EntityType.Builder.<RiftStalkerEntity>of(RiftStalkerEntity::new, EntityClassification.MONSTER)
                    .sized(0.88F, 2.05F).clientTrackingRange(8).updateInterval(3).build("aetherium:rift_stalker"));
    public static final RegistryObject<EntityType<LumiveloEntity>> LUMIVELO = ENTITIES.register("lumivelo",
            () -> EntityType.Builder.<LumiveloEntity>of(LumiveloEntity::new, EntityClassification.CREATURE)
                    .sized(0.92F, 1.16F).clientTrackingRange(8).updateInterval(3).build("aetherium:lumivelo"));

    // Summoned bosses are not part of natural spawning. Their encounter items control when they appear.
    public static final RegistryObject<EntityType<RiftGuardianEntity>> RIFT_GUARDIAN = ENTITIES.register("rift_guardian",
            () -> EntityType.Builder.<RiftGuardianEntity>of(RiftGuardianEntity::new, EntityClassification.MONSTER)
                    .sized(1.70F, 3.10F).clientTrackingRange(12).updateInterval(3).build("aetherium:rift_guardian"));
    public static final RegistryObject<EntityType<AstralDevourerEntity>> ASTRAL_DEVOURER = ENTITIES.register("astral_devourer",
            () -> EntityType.Builder.<AstralDevourerEntity>of(AstralDevourerEntity::new, EntityClassification.MONSTER)
                    .sized(2.20F, 2.80F).clientTrackingRange(12).updateInterval(3).build("aetherium:astral_devourer"));
    public static final RegistryObject<EntityType<AetherBoltEntity>> AETHER_BOLT = ENTITIES.register("aether_bolt",
            () -> EntityType.Builder.<AetherBoltEntity>of(AetherBoltEntity::new, EntityClassification.MISC)
                    .sized(0.32F, 0.32F).clientTrackingRange(8).updateInterval(2).build("aetherium:aether_bolt"));

    /** Attackable phantom hitboxes; the parent boss renderer draws their copies. */
    public static final RegistryObject<EntityType<BossMirageEntity>> BOSS_MIRAGE = ENTITIES.register("boss_mirage",
            () -> EntityType.Builder.<BossMirageEntity>of(BossMirageEntity::new, EntityClassification.MISC)
                    .sized(2.0F, 3.1F).clientTrackingRange(12).updateInterval(1)
                    .build("aetherium:boss_mirage"));

    // Block items
    public static final RegistryObject<Item> AETHERIUM_ORE_ITEM = ITEMS.register("aetherium_ore",
            () -> new BlockItem(AETHERIUM_ORE.get(), new Item.Properties().tab(ItemGroup.TAB_BUILDING_BLOCKS)));
    public static final RegistryObject<Item> AETHERIUM_BLOCK_ITEM = ITEMS.register("aetherium_block",
            () -> new BlockItem(AETHERIUM_BLOCK.get(), new Item.Properties().tab(ItemGroup.TAB_BUILDING_BLOCKS)));

    // Materials
    public static final RegistryObject<Item> RAW_AETHERIUM = ITEMS.register("raw_aetherium",
            () -> new Item(new Item.Properties().tab(ItemGroup.TAB_MATERIALS)));
    public static final RegistryObject<Item> AETHERIUM_INGOT = ITEMS.register("aetherium_ingot",
            () -> new Item(new Item.Properties().tab(ItemGroup.TAB_MATERIALS)));
    public static final RegistryObject<Item> AETHERIUM_NUGGET = ITEMS.register("aetherium_nugget",
            () -> new Item(new Item.Properties().tab(ItemGroup.TAB_MATERIALS)));
    public static final RegistryObject<Item> AETHER_DUST = ITEMS.register("aether_dust",
            () -> new Item(new Item.Properties().tab(ItemGroup.TAB_MATERIALS)));

    // Exclusive boss materials: required by the summoning chain and the final tier in Part 6.
    public static final RegistryObject<Item> RIFT_CORE = ITEMS.register("rift_core",
            () -> new Item(new Item.Properties().tab(ItemGroup.TAB_MATERIALS)));
    public static final RegistryObject<Item> RIFT_FRAGMENT = ITEMS.register("rift_fragment",
            () -> new Item(new Item.Properties().tab(ItemGroup.TAB_MATERIALS)));
    public static final RegistryObject<Item> ASTRAL_HEART = ITEMS.register("astral_heart",
            () -> new Item(new Item.Properties().tab(ItemGroup.TAB_MATERIALS)));
    public static final RegistryObject<Item> ASTRAL_FRAGMENT = ITEMS.register("astral_fragment",
            () -> new Item(new Item.Properties().tab(ItemGroup.TAB_MATERIALS)));

    // Boss encounter items. The second seal is crafted from the Guardian's guaranteed core drop.
    public static final RegistryObject<Item> RIFT_SIGIL = ITEMS.register("rift_sigil",
            () -> new BossSummoningItem(false, new Item.Properties().tab(ItemGroup.TAB_COMBAT)));
    public static final RegistryObject<Item> ASTRAL_SEAL = ITEMS.register("astral_seal",
            () -> new BossSummoningItem(true, new Item.Properties().tab(ItemGroup.TAB_COMBAT)));


    // Advanced weapons and tools
    public static final RegistryObject<Item> AETHERIUM_SWORD = ITEMS.register("aetherium_sword",
            () -> new AetheriumSwordItem(AetheriumItemTier.INSTANCE, 5, -2.4F,
                    new Item.Properties().tab(ItemGroup.TAB_COMBAT)));
    public static final RegistryObject<Item> AETHERIUM_PICKAXE = ITEMS.register("aetherium_pickaxe",
            () -> new PickaxeItem(AetheriumItemTier.INSTANCE, 2, -2.8F,
                    new Item.Properties().tab(ItemGroup.TAB_TOOLS)));
    public static final RegistryObject<Item> AETHERIUM_AXE = ITEMS.register("aetherium_axe",
            () -> new AetheriumAxeItem(AetheriumItemTier.INSTANCE, 5.0F, -3.0F,
                    new Item.Properties().tab(ItemGroup.TAB_TOOLS)));

    // Full armor set
    public static final RegistryObject<Item> AETHERIUM_HELMET = ITEMS.register("aetherium_helmet",
            () -> new ArmorItem(AetheriumArmorMaterial.INSTANCE, EquipmentSlotType.HEAD,
                    new Item.Properties().tab(ItemGroup.TAB_COMBAT)));
    public static final RegistryObject<Item> AETHERIUM_CHESTPLATE = ITEMS.register("aetherium_chestplate",
            () -> new ArmorItem(AetheriumArmorMaterial.INSTANCE, EquipmentSlotType.CHEST,
                    new Item.Properties().tab(ItemGroup.TAB_COMBAT)));
    public static final RegistryObject<Item> AETHERIUM_LEGGINGS = ITEMS.register("aetherium_leggings",
            () -> new ArmorItem(AetheriumArmorMaterial.INSTANCE, EquipmentSlotType.LEGS,
                    new Item.Properties().tab(ItemGroup.TAB_COMBAT)));
    public static final RegistryObject<Item> AETHERIUM_BOOTS = ITEMS.register("aetherium_boots",
            () -> new ArmorItem(AetheriumArmorMaterial.INSTANCE, EquipmentSlotType.FEET,
                    new Item.Properties().tab(ItemGroup.TAB_COMBAT)));

    // Final Astral tier. Every piece consumes rare boss-derived Astral Ingots.
    public static final RegistryObject<Item> ASTRAL_INGOT = ITEMS.register("astral_ingot",
            () -> new Item(new Item.Properties().tab(ItemGroup.TAB_MATERIALS)));
    public static final RegistryObject<Item> ASTRAL_SWORD = ITEMS.register("astral_sword",
            () -> new AstralSwordItem(AstralItemTier.INSTANCE, 7, -2.4F,
                    new Item.Properties().tab(ItemGroup.TAB_COMBAT)));
    public static final RegistryObject<Item> ASTRAL_PICKAXE = ITEMS.register("astral_pickaxe",
            () -> new PickaxeItem(AstralItemTier.INSTANCE, 2, -2.8F,
                    new Item.Properties().tab(ItemGroup.TAB_TOOLS)));
    public static final RegistryObject<Item> ASTRAL_AXE = ITEMS.register("astral_axe",
            () -> new AstralAxeItem(AstralItemTier.INSTANCE, 7.0F, -3.0F,
                    new Item.Properties().tab(ItemGroup.TAB_TOOLS)));
    public static final RegistryObject<Item> ASTRAL_HELMET = ITEMS.register("astral_helmet",
            () -> new ArmorItem(AstralArmorMaterial.INSTANCE, EquipmentSlotType.HEAD,
                    new Item.Properties().tab(ItemGroup.TAB_COMBAT)));
    public static final RegistryObject<Item> ASTRAL_CHESTPLATE = ITEMS.register("astral_chestplate",
            () -> new ArmorItem(AstralArmorMaterial.INSTANCE, EquipmentSlotType.CHEST,
                    new Item.Properties().tab(ItemGroup.TAB_COMBAT)));
    public static final RegistryObject<Item> ASTRAL_LEGGINGS = ITEMS.register("astral_leggings",
            () -> new ArmorItem(AstralArmorMaterial.INSTANCE, EquipmentSlotType.LEGS,
                    new Item.Properties().tab(ItemGroup.TAB_COMBAT)));
    public static final RegistryObject<Item> ASTRAL_BOOTS = ITEMS.register("astral_boots",
            () -> new ArmorItem(AstralArmorMaterial.INSTANCE, EquipmentSlotType.FEET,
                    new Item.Properties().tab(ItemGroup.TAB_COMBAT)));

    // Two optional, survival-friendly utilities with low-frequency server logic.
    public static final RegistryObject<Item> AETHERIUM_MAGNET = ITEMS.register("aetherium_magnet",
            () -> new AetheriumMagnetItem(new Item.Properties().tab(ItemGroup.TAB_TOOLS)));
    public static final RegistryObject<Item> AETHERIUM_FLARE = ITEMS.register("aetherium_flare",
            () -> new AetheriumFlareItem(new Item.Properties().tab(ItemGroup.TAB_TOOLS)));

    // Spawn eggs are useful for testing and creative-mode access; natural spawning is separately configurable.
    public static final RegistryObject<Item> RIFT_STALKER_SPAWN_EGG = ITEMS.register("rift_stalker_spawn_egg",
            () -> new ForgeSpawnEggItem(RIFT_STALKER::get, 0x241637, 0x35D8D1,
                    new Item.Properties().tab(ItemGroup.TAB_MISC)));
    public static final RegistryObject<Item> LUMIVELO_SPAWN_EGG = ITEMS.register("lumivelo_spawn_egg",
            () -> new ForgeSpawnEggItem(LUMIVELO::get, 0x224B4C, 0xB3F8DD,
                    new Item.Properties().tab(ItemGroup.TAB_MISC)));

    // Curios-compatible accessories. Their tags are data-driven and Curios remains optional.
    public static final RegistryObject<Item> AETHERIUM_AMULET = ITEMS.register("aetherium_amulet",
            () -> new AetheriumAccessoryItem(AetheriumAccessoryItem.Type.AMULET,
                    new Item.Properties().tab(ItemGroup.TAB_COMBAT)));
    public static final RegistryObject<Item> AETHERIUM_RING = ITEMS.register("aetherium_ring",
            () -> new AetheriumAccessoryItem(AetheriumAccessoryItem.Type.RING,
                    new Item.Properties().tab(ItemGroup.TAB_COMBAT)));

    private RegistryHandler() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        ENTITIES.register(modEventBus);
        SOUNDS.register(modEventBus);
    }
}
