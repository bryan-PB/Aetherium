package com.aetherium.aetherium.registry;

import com.aetherium.aetherium.AetheriumMod;
import com.aetherium.aetherium.content.armor.AetheriumArmorMaterial;
import com.aetherium.aetherium.content.armor.AstralArmorMaterial;
import com.aetherium.aetherium.content.armor.KryzoraeArmorItem;
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
import com.aetherium.aetherium.content.effect.BleedingEffect;
import com.aetherium.aetherium.content.projectile.DarkSlashEntity;
import com.aetherium.aetherium.content.boss.BossMirageEntity;
import com.aetherium.aetherium.content.boss.RiftGuardianEntity;
import com.aetherium.aetherium.content.boss.AstralDevourerEntity;
import com.aetherium.aetherium.content.item.BossSummoningItem;
import com.aetherium.aetherium.content.item.GuideBookItem;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EntityClassification;
import net.minecraft.potion.Effect;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraft.item.BlockItem;
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
    public static final DeferredRegister<Effect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.POTIONS, AetheriumMod.MOD_ID);

    public static final RegistryObject<Effect> BLEEDING =
            EFFECTS.register("bleeding", BleedingEffect::new);

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

    // Procedurally synthesized, original Aetherium cues: no Minecraft mob recordings.
    public static final RegistryObject<SoundEvent> ASTRAL_AMBIENT = sound("astral_ambient");
    public static final RegistryObject<SoundEvent> ASTRAL_AWAKEN = sound("astral_awaken");
    public static final RegistryObject<SoundEvent> ASTRAL_HURT = sound("astral_hurt");
    public static final RegistryObject<SoundEvent> ASTRAL_DEATH = sound("astral_death");
    public static final RegistryObject<SoundEvent> ASTRAL_LASER_CHARGE = sound("astral_laser_charge");
    public static final RegistryObject<SoundEvent> ASTRAL_LASER_FIRE = sound("astral_laser_fire");
    public static final RegistryObject<SoundEvent> ASTRAL_DASH = sound("astral_dash");
    public static final RegistryObject<SoundEvent> ASTRAL_MIRAGE = sound("astral_mirage");
    public static final RegistryObject<SoundEvent> ASTRAL_NOVA = sound("astral_nova");
    public static final RegistryObject<SoundEvent> GUARDIAN_AMBIENT = sound("guardian_ambient");
    public static final RegistryObject<SoundEvent> GUARDIAN_AWAKEN = sound("guardian_awaken");
    public static final RegistryObject<SoundEvent> GUARDIAN_HURT = sound("guardian_hurt");
    public static final RegistryObject<SoundEvent> GUARDIAN_DEATH = sound("guardian_death");
    public static final RegistryObject<SoundEvent> GUARDIAN_WINDUP = sound("guardian_windup");
    public static final RegistryObject<SoundEvent> GUARDIAN_CHARGE = sound("guardian_charge");
    public static final RegistryObject<SoundEvent> GUARDIAN_SWEEP = sound("guardian_sweep");
    public static final RegistryObject<SoundEvent> GUARDIAN_MIRAGE = sound("guardian_mirage");
    public static final RegistryObject<SoundEvent> STALKER_AMBIENT = sound("stalker_ambient");
    public static final RegistryObject<SoundEvent> STALKER_HURT = sound("stalker_hurt");
    public static final RegistryObject<SoundEvent> STALKER_DEATH = sound("stalker_death");
    public static final RegistryObject<SoundEvent> LUMIVELO_HURT = sound("lumivelo_hurt");
    public static final RegistryObject<SoundEvent> LUMIVELO_DEATH = sound("lumivelo_death");

    public static final RegistryObject<SoundEvent> AETHERIUM_EQUIP = sound("aetherium_equip");
    public static final RegistryObject<SoundEvent> ASTRAL_EQUIP = sound("astral_equip");
    public static final RegistryObject<SoundEvent> RELIC_ACTIVATION = sound("relic_activation");
    public static final RegistryObject<SoundEvent> FLARE_ACTIVATION = sound("flare_activation");
    public static final RegistryObject<SoundEvent> MAGNET_ON = sound("magnet_on");
    public static final RegistryObject<SoundEvent> MAGNET_OFF = sound("magnet_off");
    public static final RegistryObject<SoundEvent> BOSS_SUMMON = sound("boss_summon");

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

    // First expansion: raw Overworld ore deposits (no armor/weapons yet).
    public static final RegistryObject<Block> BRUMACERO_ORE = BLOCKS.register("brumacero_ore",
            () -> new Block(Block.Properties.of(Material.STONE)
                    .strength(3.0F, 5.0F)
                    .sound(SoundType.STONE)
                    .harvestTool(ToolType.PICKAXE)
                    .harvestLevel(1)
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> UMBRACITA_ORE = BLOCKS.register("umbracita_ore",
            () -> new Block(Block.Properties.of(Material.STONE)
                    .strength(3.5F, 6.0F)
                    .sound(SoundType.STONE)
                    .harvestTool(ToolType.PICKAXE)
                    .harvestLevel(2)
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> PRISMALITA_ORE = BLOCKS.register("prismalita_ore",
            () -> new Block(Block.Properties.of(Material.STONE)
                    .strength(3.5F, 6.0F)
                    .sound(SoundType.STONE)
                    .harvestTool(ToolType.PICKAXE)
                    .harvestLevel(2)
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

    /** Eight-tick diagonal wave spawned by every valid violet sword swing. */
    public static final RegistryObject<EntityType<DarkSlashEntity>> DARK_SLASH = ENTITIES.register("dark_slash",
            () -> EntityType.Builder.<DarkSlashEntity>of(DarkSlashEntity::new, EntityClassification.MISC)
                    .sized(0.48F, 0.48F).clientTrackingRange(10).updateInterval(1)
                    .build("aetherium:dark_slash"));

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

    // Inventory entries for the new stone deposits.
    public static final RegistryObject<Item> BRUMACERO_ORE_ITEM = ITEMS.register("brumacero_ore",
            () -> new BlockItem(BRUMACERO_ORE.get(),
                    new Item.Properties().tab(ItemGroup.TAB_BUILDING_BLOCKS)));
    public static final RegistryObject<Item> UMBRACITA_ORE_ITEM = ITEMS.register("umbracita_ore",
            () -> new BlockItem(UMBRACITA_ORE.get(),
                    new Item.Properties().tab(ItemGroup.TAB_BUILDING_BLOCKS)));
    public static final RegistryObject<Item> PRISMALITA_ORE_ITEM = ITEMS.register("prismalita_ore",
            () -> new BlockItem(PRISMALITA_ORE.get(),
                    new Item.Properties().tab(ItemGroup.TAB_BUILDING_BLOCKS)));

    // First-play guide, also craftable if a player loses it.
    public static final RegistryObject<Item> GUIDE_BOOK = ITEMS.register("guide_book",
            () -> new GuideBookItem(new Item.Properties().tab(ItemGroup.TAB_MISC).stacksTo(1)));

    // Basic ore products and catalysts for the upcoming crafting tiers.
    public static final RegistryObject<Item> RAW_BRUMACERO = ITEMS.register("raw_brumacero",
            () -> new Item(new Item.Properties().tab(ItemGroup.TAB_MATERIALS)));
    public static final RegistryObject<Item> BRUMACERO_INGOT = ITEMS.register("brumacero_ingot",
            () -> new Item(new Item.Properties().tab(ItemGroup.TAB_MATERIALS)));
    public static final RegistryObject<Item> RAW_UMBRACITA = ITEMS.register("raw_umbracita",
            () -> new Item(new Item.Properties().tab(ItemGroup.TAB_MATERIALS)));
    public static final RegistryObject<Item> UMBRACITA_INGOT = ITEMS.register("umbracita_ingot",
            () -> new Item(new Item.Properties().tab(ItemGroup.TAB_MATERIALS)));
    public static final RegistryObject<Item> PRISMALITA_SHARD = ITEMS.register("prismalita_shard",
            () -> new Item(new Item.Properties().tab(ItemGroup.TAB_MATERIALS)));

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
            () -> new KryzoraeArmorItem(AetheriumArmorMaterial.INSTANCE, EquipmentSlotType.HEAD,
                    new Item.Properties().tab(ItemGroup.TAB_COMBAT), false));
    public static final RegistryObject<Item> AETHERIUM_CHESTPLATE = ITEMS.register("aetherium_chestplate",
            () -> new KryzoraeArmorItem(AetheriumArmorMaterial.INSTANCE, EquipmentSlotType.CHEST,
                    new Item.Properties().tab(ItemGroup.TAB_COMBAT), false));
    public static final RegistryObject<Item> AETHERIUM_LEGGINGS = ITEMS.register("aetherium_leggings",
            () -> new KryzoraeArmorItem(AetheriumArmorMaterial.INSTANCE, EquipmentSlotType.LEGS,
                    new Item.Properties().tab(ItemGroup.TAB_COMBAT), false));
    public static final RegistryObject<Item> AETHERIUM_BOOTS = ITEMS.register("aetherium_boots",
            () -> new KryzoraeArmorItem(AetheriumArmorMaterial.INSTANCE, EquipmentSlotType.FEET,
                    new Item.Properties().tab(ItemGroup.TAB_COMBAT), false));

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
            () -> new KryzoraeArmorItem(AstralArmorMaterial.INSTANCE, EquipmentSlotType.HEAD,
                    new Item.Properties().tab(ItemGroup.TAB_COMBAT), true));
    public static final RegistryObject<Item> ASTRAL_CHESTPLATE = ITEMS.register("astral_chestplate",
            () -> new KryzoraeArmorItem(AstralArmorMaterial.INSTANCE, EquipmentSlotType.CHEST,
                    new Item.Properties().tab(ItemGroup.TAB_COMBAT), true));
    public static final RegistryObject<Item> ASTRAL_LEGGINGS = ITEMS.register("astral_leggings",
            () -> new KryzoraeArmorItem(AstralArmorMaterial.INSTANCE, EquipmentSlotType.LEGS,
                    new Item.Properties().tab(ItemGroup.TAB_COMBAT), true));
    public static final RegistryObject<Item> ASTRAL_BOOTS = ITEMS.register("astral_boots",
            () -> new KryzoraeArmorItem(AstralArmorMaterial.INSTANCE, EquipmentSlotType.FEET,
                    new Item.Properties().tab(ItemGroup.TAB_COMBAT), true));

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
        EFFECTS.register(modEventBus);
    }
}
