# Kryzorae — Megaestructura modular nativa Forge 1.16.5

**Mod ID real:** `aetherium` · **Nombre del mod:** Kryzorae · **Structure ID:** `aetherium:citadel` · **Forge:** 36.2.42 · **Java:** 8 · **Mojang Official Mappings:** 1.16.5.

**Estado:** implementación arquitectónica ampliada a 17 módulos NBT; el arranque `runData`, la integridad del JAR, la conexión entre piezas y el rendimiento en Minecraft deben validarse para cada cambio. Los monstruos del diseño maestro, el Centinela y el Custodio todavía no están implementados. Los tres spawners actuales contienen criaturas **vanilla**, no enemigos originales del mod.

## 1. Árbol de archivos real

```text
src/main/java/com/aetherium/aetherium/
├── AetheriumMod.java
└── world/
    ├── CitadelWorldGen.java           # DeferredRegister, configured structure, biomas, separación
    ├── CitadelStructure.java          # Structure<NoFeatureConfig>, Start, suelo y colisiones
    ├── CitadelLayout.java             # 17 módulos, coordenadas, rotación, espejo
    ├── CitadelPieces.java             # TemplateStructurePiece, guardado NBT, cimientos
    └── CitadelCommands.java           # /kryzorae citadel status, probe y preview

src/main/resources/data/aetherium/
├── structures/citadel/                # *.nbt GENERADOS en el build, no un NBT monolítico
│   ├── gateway.nbt
│   ├── corridor.nbt
│   ├── guardhouse.nbt
│   ├── courtyard.nbt
│   ├── archive.nbt
│   ├── arena_shell.nbt
│   ├── threshold.nbt
│   ├── sanctum.nbt
│   ├── crypt.nbt
│   ├── forge.nbt
│   ├── watchtower.nbt
│   ├── barracks.nbt
│   ├── prison.nbt
│   ├── east_bastion.nbt
│   ├── west_gallery.nbt
│   ├── catacombs.nbt
│   └── archive_vault.nbt
├── worldgen/template_pool/citadel/
│   ├── start.json                     # gateway
│   ├── arena.json                     # arena_shell
│   └── {corridor,guardhouse,courtyard,archive,threshold,sanctum,
│        crypt,forge,watchtower,barracks,prison,east_bastion,
│        west_gallery,catacombs,archive_vault}.json
└── loot_tables/chests/
    ├── citadel_entrance.json
    ├── citadel_guardhouse.json
    ├── citadel_archive.json
    ├── citadel_supplies.json
    └── citadel_reliquary.json

docs/citadel/rooms.json               # plano fuente + posiciones + portales + cofres
tools/generate_citadel_templates.py    # Python 3 stdlib, genera los 17 NBT 1.16.5
tools/check_citadel_templates.py       # comprueba NBT, cofres, spawners y JAR
tools/check_citadel_worldgen.py        # comprueba 3D, rotación y conexiones
```

Los archivos de `structures/` **no** tienen por qué estar versionados: `generateCitadelTemplates` es dependencia obligatoria de `processResources` en `build.gradle`, por lo que cada JAR nuevo lleva las 17 piezas.

## 2. Registro nativo Forge

El código completo compilable se encuentra en `CitadelWorldGen.java`:

```java
public static final DeferredRegister<Structure<?>> STRUCTURES =
    DeferredRegister.create(ForgeRegistries.STRUCTURE_FEATURES, AetheriumMod.MOD_ID);

public static final RegistryObject<Structure<NoFeatureConfig>> CITADEL =
    STRUCTURES.register("citadel", () -> new CitadelStructure(NoFeatureConfig.CODEC));
```

En `AetheriumMod`, `CitadelWorldGen.register(modBus)` enlaza el DeferredRegister con el bus del mod. `onCommonSetup` registra la fábrica de piezas **antes de cargar mundos**, la estructura configurada y sus ajustes de separación en el registro mundial de 1.16.5. La cuadrícula usa 26 chunks de separación y 10 chunks de distancia mínima por defecto, configurable en `config/aetherium-common.toml`.

En Forge 1.16.5 el código del proyecto registra el decodificador con `IStructurePieceType.setPieceId(Room::new, "aetherium:citadel_room")`. Este es el método compatible con la API utilizada por el mod; no copiar ciegamente el nombre `Registry.STRUCTURE_PIECE` de otras versiones o mapeos.

## 3. Ensamblaje, rotación y colisiones

`CitadelStructure extends Structure<NoFeatureConfig>` y devuelve `Start::new` mediante `getStartFactory()`. Cada `Start.generatePieces`:

1. Calcula el suelo con `Heightmap.Type.WORLD_SURFACE_WG` y verifica una zona razonable mediante cinco muestras de altura sobre el eje del recorrido.
2. Establece la Y común de la planta baja; las criptas se colocan 9–10 bloques por debajo.
3. Recorre `CitadelLayout.rooms()` y carga cada NBT con `CitadelPieces.Room`.
4. Para cada pieza, prueba `MutableBoundingBox.intersects` antes de añadirla. Nunca añade una pieza que solape físicamente otra.
5. Finalmente llama a `calculateBoundingBox()`. La generación real utiliza la infraestructura nativa de `StructureStart` y `TemplateStructurePiece`, recortada por chunk.

Las salas se conectan con puertas libres de **3×4**. El manifest `docs/citadel/rooms.json` declara los portales `doors` y sus `doorRoles` (entrada/salida). El rol no depende de la dirección sin rotar. Los anexos `barracks`, `east_bastion` y `west_gallery` usan rotación de 180°. `CitadelLayout.RoomSpec.position` calcula el pivote real después de rotación/espejo; `CitadelPieces.Room` guarda `CitadelRotation` y `CitadelMirror` en el NBT de la partida.

**Importante:** los pools JSON y conectores Jigsaw son datos reales, pero el ensamblador actual es **determinista con `TemplateStructurePiece`**, no un `JigsawManager.addPieces` aleatorio. Es una decisión intencional: permite verificar de antemano la conectividad y las bounding boxes de esta gran estructura, y evita volver al problema del prototipo anterior, que creaba starts incompletos. Para añadir variaciones aleatorias en el futuro, primero debe existir un plan acotado de cajas y anclajes válidos.

## 4. Terreno, biomas y rendimiento

La Ciudadela puede aparecer en el **Overworld**, en biomas terrestres como PLAINS, FOREST, TAIGA y montaña; además admite SWAMP si el suelo candidato está seco. Excluye océanos, ríos, playas, Nether, End y dimensiones inadecuadas. Las categorías se filtran en `@SubscribeEvent(priority = EventPriority.HIGH)` sobre `BiomeLoadingEvent`, y la separación se inyecta en el generador del Overworld durante `WorldEvent.Load`.

El filtro de altura admite Y60–126. Las muestras del eje principal rechazan desniveles superiores a 20 bloques: una fortaleza de ~174 bloques de largo no debe empezar cruzando un acantilado. En cada módulo superficial, `CitadelPieces.Room.postProcess` coloca cuatro columnas cortas de piedra negra **hasta 20 bloques de profundidad**, solo donde haya aire o líquido y siempre dentro del bounding box del chunk proporcionado por Minecraft. Se detiene al tocar suelo sólido. No fuerza chunks vecinos ni añade rastreos por tick. En cumbres muy accidentadas se reduce deliberadamente la probabilidad de aparición; esta fase no pretende nivelar montañas completas.

## 5. Contenido interior y tablas de botín

Las 17 plantillas incluyen **19 cofres** y **3 spawners vanilla**. Los tres spawners usan zombis, esqueletos o arañas, `SpawnCount=2`, `MaxNearbyEntities=3`, alcance de jugador 14, y espera de 800–1400 ticks entre oleadas. No hay nuevas entidades del mod ni IA personalizada todavía.

Las prisiones, criptas, el umbral y las cámaras auxiliares incluyen peligros **visibles** de magma y telarañas, dejando un pasillo central navegable. Las torres tienen varios pisos y escaleras de mano; el archivo y la cripta conducen a cámaras subterráneas por huecos en el suelo.

Las cinco tablas JSON se almacenan en `data/aetherium/loot_tables/chests/` y usan `weight` y `minecraft:set_count`. Las nuevas `citadel_supplies.json` y `citadel_reliquary.json` contienen hierro, comida, flechas, oro, esmeraldas y diamantes junto con IDs realmente registrados como `aetherium:raw_brumacero`, `aetherium:raw_umbracita`, `aetherium:brumacero_ingot`, `aetherium:prismalita_shard` y `aetherium:aether_dust`. **No** contienen `rift_core`, `astral_heart` ni otros objetos exclusivos de los jefes existentes. El botín final seguirá siendo provisional mientras no exista una puerta de progresión asociada al Custodio.

## 6. Crear y compilar

Con Java 8 y Python 3 instalados:

```bash
python3 tools/generate_citadel_templates.py
./gradlew build
python3 tools/check_citadel_templates.py
python3 tools/check_citadel_worldgen.py
```

En Windows usa `gradlew.bat build`; la tarea `generateCitadelTemplates` se ejecuta **automáticamente** dentro de Gradle. Para un build local completo también se deben preparar los sonidos con Python 3 + FFmpeg según el README y los generadores de los recursos gráficos si faltan.

Para validar un cambio se comprueba: NBT gzip con versión 2586, cada sala <=31×24×31, todas las coordenadas dentro del volumen, ausencia de duplicados, 19 cofres con LootTable, tres spawners con límite de población, escalas y puertos correctos, ocupación de piezas sin solapamientos, rutas unidas incluso tras rotación, igualdad de recursos en el JAR y arranque `runData` de Forge.

## 7. Prueba dentro del juego

Usar **mundo nuevo** con Forge 36.2.42 y el JAR más reciente:

```mcfunction
/kryzorae citadel status
/kryzorae citadel probe
/locate aetherium:citadel
```

`status` debe informar `NBT=17/17`. `/locate` encuentra candidatos de la estructura, no remodela chunks antiguos. Si deseas comprobar el diseño completo en un mundo creativo **descartable**, `/kryzorae citadel preview` coloca las piezas juntas. **Atención: modifica una zona aproximada de 65×200 bloques y puede causar una pausa puntual**; jamás ejecutarlo cerca de construcciones valiosas.

Recorrer cada conexión con F3, revisar 19 cofres, usar los dos accesos a subniveles, subir ambas torres y comprobar que los 3 spawners no ocasionan oleadas excesivas. Medir FPS/MSPT con la misma semilla en mundo normal/modificado. Todavía se requiere inspección visual in-game y pruebas con la colección habitual de mods antes de llamarla lista para producción.

## 8. Siguiente etapa artística y jugable

La arquitectura interna está separada del código de generación y puede rediseñarse por módulos. Cuando se apruebe un nuevo plano visual, el contenido de `rooms.json` y los sprites/NBT deberán actualizarse en conjunto, sin cambiar el ID `aetherium:citadel`. Los enemigos de la Ciudadela, el Centinela del Umbral, el sello y el Custodio siguen siendo fases posteriores y no se han introducido de forma engañosa como mobs vanilla.

**Código fuente completo** (ver la rama `fix/boss-attack-visibility`): `CitadelWorldGen.java`, `CitadelStructure.java`, `CitadelLayout.java`, `CitadelPieces.java`, `CitadelCommands.java`, `tools/generate_citadel_templates.py`. Los JSON completos están en las carpetas descritas; no son pseudocódigo.
