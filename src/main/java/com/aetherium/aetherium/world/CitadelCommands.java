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
        int found = 0;
        for (CitadelLayout.RoomSpec room : CitadelLayout.rooms()) {
            if (manager.get(new ResourceLocation(
                    AetheriumMod.MOD_ID, "citadel/" + room.template)) != null) {
                found++;
            }
        }
        source.sendSuccess(new StringTextComponent(
                "[Kryzorae] Ciudadela: activada=" + enabled
                + " | Overworld=" + overworld
                + " | estructura=" + registered
                + " | configurada=" + configured
                + " | espaciado=" + spacing
                + " | NBT=" + found + "/" + CitadelLayout.rooms().size()), false);
        if (!enabled || !overworld || !registered || !configured || !spacing || found != CitadelLayout.rooms().size()) {
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
        // This treats the player's X/Z as the gateway candidate location.
        // Unlike the earlier probe, it checks ALL 15 surface-room footprints.
        int chosenY = CitadelStructure.findPlacementY(
                generator, player.getX(), player.getZ());
        boolean fullSite = chosenY >= 0;
        source.sendSuccess(new StringTextComponent(
                "[Kryzorae] Probe X=" + player.getX() + " Z=" + player.getZ()
                + " | Y superficie=" + height + " | suelo=" + ground.getBlock()
                + " | bioma admite ciudadela=" + biomeHasCitadel
                + " | columna apta=" + terrain
                + " | zona completa apta=" + fullSite
                + " | Y base=" + chosenY
                + " | espaciado=" + spacing
                + " | activo=" + enabled
                + " | Overworld=" + overworld), false);
        if (!overworld || !enabled || !spacing || !biomeHasCitadel || !fullSite) {
            source.sendFailure(new StringTextComponent(
                    "[Kryzorae] Al menos una condicion local falla. "
                    + "El terreno de toda la Ciudadela debe ser relativamente "
                    + "plano y seco. Busca una gran llanura o bosque sin barrancos. "
                    + "El comando no genera ni modifica chunks."));
            return 0;
        }
        source.sendSuccess(new StringTextComponent(
                "[Kryzorae] Las salas de superficie superan los filtros de "
                + "altura, agua y relieve. La generacion real requiere un "
                + "chunk candidato y terreno NUEVO. Usa /locate aetherium:citadel."), false);
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
        // Important: this command replaces terrain/builds in a ~65x200 area.
        // It is intentionally operator-only and not triggered by chunk ticks.
        BlockPos player = new BlockPos(
                source.getPosition().x, source.getPosition().y, source.getPosition().z);
        int x = player.getX() - 9;
        int z = player.getZ() + 14;
        // Use the SAME full-footprint site check and median Y as natural
        // generation. Previously preview only sampled the gateway, leaving
        // the distant sanctum and towers buried or floating on hills.
        int y = CitadelStructure.findPlacementY(
                world.getChunkSource().getGenerator(), x + 9, z + 7);
        if (y < 0) {
            source.sendFailure(new StringTextComponent(
                    "[Kryzorae] La zona de ~56x174 no es suficientemente plana "
                    + "y seca. Busca una llanura extensa. Preview no ha "
                    + "modificado bloques."));
            return 0;
        }
        BlockPos base = new BlockPos(x, y, z);
        for (int i = 0; i < CitadelLayout.rooms().size(); i++) {
            CitadelLayout.RoomSpec room = CitadelLayout.rooms().get(i);
            PlacementSettings placement = new PlacementSettings()
                    .setRotation(room.rotation)
                    .setMirror(room.mirror)
                    .addProcessor(JigsawReplacementStructureProcessor.INSTANCE)
                    .setIgnoreEntities(true);
            BlockPos roomOrigin = room.position(base);
            if (!templates.get(i).placeInWorld(
                    world, roomOrigin, roomOrigin, placement, world.random, 2)) {
                source.sendFailure(new StringTextComponent(
                        "[Kryzorae] Fallo al colocar la sala "
                        + CitadelLayout.rooms().get(i).template
                        + ". El terreno de prueba puede haberse modificado parcialmente."));
                return 0;
            }
            new CitadelPieces.Room(manager, room, base)
                    .placePreviewFoundations(world);
        }
        source.sendSuccess(new StringTextComponent(
                "[Kryzorae] Ciudadela de prueba de " + CitadelLayout.rooms().size()
                + " modulos construidos cerca de X="
                + (x+9) + " Y=" + y + " Z=" + z
                + ". Este comando modifica el terreno: usalo solo en mundo de pruebas."), true);
        return CitadelLayout.rooms().size();
    }
}
