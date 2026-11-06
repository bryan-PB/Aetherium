package com.aetherium.aetherium.world;

import com.aetherium.aetherium.AetheriumMod;
import com.aetherium.aetherium.config.AetheriumConfig;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
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

    private static int preview(CommandSource source) {
        ServerWorld world = source.getLevel();
        if (!World.OVERWORLD.equals(world.dimension())) {
            source.sendFailure(new StringTextComponent("Solo disponible en el Overworld."));
            return 0;
        }
        TemplateManager manager = world.getServer().getStructureManager();
        String[] rooms = {"gateway","corridor","guardhouse","archive","arena_shell","crypt","forge"};
        int[][] locations = {{0,0},{4,15},{1,32},{1,49},{-4,66},{15,15},{-16,32}};
        List<Template> templates = new ArrayList<>();
        for (String name : rooms) {
            Template template = manager.get(new ResourceLocation(AetheriumMod.MOD_ID, "citadel/" + name));
            if (template == null) {
                source.sendFailure(new StringTextComponent(
                        "Falta el archivo NBT de la sala " + name + " en el JAR."));
                return 0;
            }
            templates.add(template);
        }

        // Manual opt-in, never triggered by worldgen. Spawn the entire
        // architectural layout near the operator so it can be visually tested.
        // Important: it replaces terrain/builds in an approximately 60x100 area.
        BlockPos player = source.getPosition();
        int x = player.getX() - 9;
        int z = player.getZ() + 14;
        int y = world.getHeight(Heightmap.Type.WORLD_SURFACE, player.getX(), z) + 1;
        if (y < 60 || y > 120) {
            source.sendFailure(new StringTextComponent("Busca una llanura o bosque entre Y 60 y 120."));
            return 0;
        }
        for (int i=0; i<rooms.length; i++) {
            PlacementSettings placement = new PlacementSettings()
                    .setRotation(Rotation.NONE)
                    .setMirror(Mirror.NONE)
                    .addProcessor(JigsawReplacementStructureProcessor.INSTANCE)
                    .setIgnoreEntities(true);
            BlockPos origin = new BlockPos(x + locations[i][0], y, z + locations[i][1]);
            templates.get(i).placeInWorld(world, origin, origin, placement, world.random, 2);
        }
        source.sendSuccess(new StringTextComponent(
                "[Kryzorae] Ciudadela de prueba de 7 salas construida cerca de X="
                + (x+9) + " Y=" + y + " Z=" + z
                + ". Este comando modifica el terreno: usalo solo en mundo de pruebas."), true);
        return 7;
    }
}
