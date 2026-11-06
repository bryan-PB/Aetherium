package com.aetherium.aetherium.world;

import com.aetherium.aetherium.AetheriumMod;
import com.aetherium.aetherium.config.AetheriumConfig;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.block.BlockState;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.Mirror;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.template.JigsawReplacementStructureProcessor;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.RegisterCommandsEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Operator-only diagnostic and explicit creative preview.
 * Never runs per tick or during biome generation; useful for investigating a
 * failed /locate without guessing whether the NBT or structure registry broke.
 */
public final class CitadelCommands {
    private CitadelCommands() {}

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("kryzorae")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("citadel")
                        .then(Commands.literal("status")
                                .executes(context -> status(context.getSource())))
                        .then(Commands.literal("probe")
                                .executes(context -> probe(context.getSource())))
                        .then(Commands.literal("preview")
                                .executes(context -> preview(context.getSource())))));
    }

    private static int status(CommandSource source) {
        ServerWorld world = source.getLevel();
        boolean enabled = AetheriumConfig.CITADEL_ENABLED.get();
        ResourceLocation id = new ResourceLocation(AetheriumMod.MOD_ID, "citadel");
        boolean registered = Structure.STRUCTURES_REGISTRY.get(id.toString()) == CitadelWorldGen.CITADEL.get();
        boolean configured = WorldGenRegistries.CONFIGURED_STRUCTURE_FEATURE.get(
                new ResourceLocation(AetheriumMod.MOD_ID, "configured_citadel")) != null;
        boolean spacing = world.getChunkSource().getGenerator().getSettings()
                .structureConfig().containsKey(CitadelWorldGen.CITADEL.get());
        boolean overworld = World.OVERWORLD.equals(world.dimension());
        TemplateManager manager = world.getServer().getStructureManager();
        String[] names = {"gateway","corridor","guardhouse","archive","arena_shell","crypt","forge"};
        int found = 0;
        for (String name : names) {
            if (manager.get(new ResourceLocation(AetheriumMod.MOD_ID, "citadel/" + name)) != null) {
                found++;
            }
        }
        source.sendSuccess(new StringTextComponent(
                "[Kryzorae] Ciudadela: activada=" + enabled
                + " | Overworld=" + overworld
                + " | estructura=" + registered
                + " | configurada=" + configured
                + " | espaciado=" + spacing
                + " | NBT=" + found + "/7"), false);
        if (!enabled || !overworld || !registered || !configured || !spacing || found != 7) {
            source.sendFailure(new StringTextComponent(
                    "[Kryzorae] Falta algun componente. Copia estas lineas o revisa latest.log."));
            return 0;
        }
        source.sendSuccess(new StringTextComponent(
                "[Kryzorae] Registro listo. /locate aetherium:citadel busca terreno NUEVO. "
                + "Para inspeccion inmediata en un mundo de prueba usa /kryzorae citadel preview."), false);
        return 1;
    }

    /**
     * Operator-only, read-only check of the CURRENT player column.
     * An eligible local biome/height is not proof that this random chunk is a
     * structure candidate or that previously generated chunks will change.
     * It is, however, direct evidence of whether biome loading injected the
     * configured structure at runtime (a failure /locate cannot explain).
     */
    private static int probe(CommandSource source) {
        ServerWorld world = source.getLevel();
        BlockPos player = new BlockPos(
                source.getPosition().x, source.getPosition().y, source.getPosition().z);
        ChunkGenerator generator = world.getChunkSource().getGenerator();
        Biome biome = world.getBiome(player);
        boolean enabled = AetheriumConfig.CITADEL_ENABLED.get();
        boolean overworld = World.OVERWORLD.equals(world.dimension());
        boolean biomeHasCitadel = biome.getGenerationSettings()
                .isValidStart(CitadelWorldGen.CITADEL.get());
        boolean spacing = generator.getSettings().structureConfig()
                .containsKey(CitadelWorldGen.CITADEL.get());
        int height = generator.getFirstOccupiedHeight(
                player.getX(), player.getZ(), Heightmap.Type.WORLD_SURFACE_WG);
        BlockState ground = generator.getBaseColumn(player.getX(), player.getZ())
                .getBlockState(new BlockPos(player.getX(), Math.max(0, height - 1), player.getZ()));
        boolean terrain = CitadelStructure.canGenerateOnColumn(
                generator, player.getX(), player.getZ());
        source.sendSuccess(new StringTextComponent(
                "[Kryzorae] Probe X=" + player.getX() + " Z=" + player.getZ()
                + " | Y superficie=" + height + " | suelo=" + ground.getBlock()
                + " | bioma admite ciudadela=" + biomeHasCitadel
                + " | terreno apto=" + terrain
                + " | espaciado=" + spacing
                + " | activo=" + enabled
                + " | Overworld=" + overworld), false);
        if (!overworld || !enabled || !spacing || !biomeHasCitadel || !terrain) {
            source.sendFailure(new StringTextComponent(
                    "[Kryzorae] Al menos una condicion local falla. "
                    + "Busca otro bioma terrestre o revisa configuracion. "
                    + "Este comando NO genera ni modifica chunks."));
            return 0;
        }
        source.sendSuccess(new StringTextComponent(
                "[Kryzorae] Esta columna supera las condiciones locales. "
                + "La aparicion real requiere ademas un chunk candidato de la cuadricula "
                + "y terreno NUEVO. Prueba /locate aetherium:citadel."), false);
        return 1;
    }

    private static int preview(CommandSource source) {
        ServerWorld world = source.getLevel();
        if (!World.OVERWORLD.equals(world.dimension())) {
            source.sendFailure(new StringTextComponent("Solo disponible en el Overworld."));
            return 0;
        }
        TemplateManager manager = world.getServer().getStructureManager();
        List<Template> templates = new ArrayList<>();
        for (CitadelLayout.RoomSpec room : CitadelLayout.rooms()) {
            Template template = manager.get(new ResourceLocation(
                    AetheriumMod.MOD_ID, "citadel/" + room.template));
            if (template == null) {
                source.sendFailure(new StringTextComponent(
                        "Falta el archivo NBT de la sala " + room.template + " en el JAR."));
                return 0;
            }
            templates.add(template);
        }

        // Manual opt-in, never triggered by worldgen. Spawn the same layout
        // as natural generation, near the operator, in a disposable test world.
        // Important: this command replaces terrain/builds in a ~60x100 area.
        BlockPos player = new BlockPos(
                source.getPosition().x, source.getPosition().y, source.getPosition().z);
        int x = player.getX() - 9;
        int z = player.getZ() + 14;
        // WORLD_SURFACE already returns the first free block above the surface.
        // Adding 1 here made the preview float a block higher than worldgen.
        int y = world.getHeight(Heightmap.Type.WORLD_SURFACE, player.getX(), z);
        if (y < 60 || y > 120) {
            source.sendFailure(new StringTextComponent("Busca una llanura o bosque entre Y 60 y 120."));
            return 0;
        }
        BlockPos base = new BlockPos(x, y, z);
        for (int i = 0; i < CitadelLayout.rooms().size(); i++) {
            PlacementSettings placement = new PlacementSettings()
                    .setRotation(Rotation.NONE)
                    .setMirror(Mirror.NONE)
                    .addProcessor(JigsawReplacementStructureProcessor.INSTANCE)
                    .setIgnoreEntities(true);
            BlockPos roomOrigin = CitadelLayout.rooms().get(i).position(base);
            if (!templates.get(i).placeInWorld(
                    world, roomOrigin, roomOrigin, placement, world.random, 2)) {
                source.sendFailure(new StringTextComponent(
                        "[Kryzorae] Fallo al colocar la sala "
                        + CitadelLayout.rooms().get(i).template
                        + ". El terreno de prueba puede haberse modificado parcialmente."));
                return 0;
            }
        }
        source.sendSuccess(new StringTextComponent(
                "[Kryzorae] Ciudadela de prueba de 7 salas construida cerca de X="
                + (x+9) + " Y=" + y + " Z=" + z
                + ". Este comando modifica el terreno: usalo solo en mundo de pruebas."), true);
        return 7;
    }
}
