# Prueba de generación natural — Ciudadela de la Grieta (Kryzorae 1.16.5)

**La Ciudadela ya está registrada como estructura del Overworld**, pero esta versión todavía es una **primera base arquitectónica**: los enemigos nuevos, minijefe y jefe final no están activos.

## Cómo localizarla realmente

1. **Sustituye** el JAR anterior por el nuevo, dejando solo **un** Kryzorae en `mods`. Utiliza Minecraft **1.16.5** con Forge **36.2.42**.
2. Lo mejor es crear un **mundo nuevo normal**, con trampas/comandos habilitados, para probar generación natural sin que los chunks explorados anteriormente lo impidan. Un mundo existente solo genera contenido en chunks todavía no visitados; haz copia de seguridad.
3. En el Overworld (no en Nether ni End), ejecuta: `/locate aetherium:citadel`. La estructura está registrada para ser encontrada por ese comando.
4. Dirígete a las coordenadas de la respuesta. El portón debería ser de piedra oscura, con torres; detrás aparecen el corredor, el cuartel, el archivo y la arena, además de dos alas laterales (cripta y forja).
5. Abre los cofres del portón, cuartel y archivo, revisa la iluminación y prueba si hay corredores sin salida o salas incrustadas en montañas.

**La ubicación no es inmediata:** hay un candidato de generación por cada región de **26×26 chunks** por defecto, con separación mínima de 10, condicionado a biomas adecuados (bosques, taiga, llanuras y colinas) y alturas seguras. No necesariamente aparece en cada región.

## Si no aparece

- En `config/aetherium-common.toml`, revisa `citadelEnabled = true` y los valores `citadelSpacingChunks = 26` / `citadelMinSeparationChunks = 10`.
- Asegúrate de que no estás en un mundo **superplano**; está excluido expresamente.
- Prueba un mundo normal nuevo, especialmente bosques/llanuras, y deja que se generen nuevas regiones.
- Si el comando `/locate aetherium:citadel` no reconoce la estructura, copia el **mensaje de error exacto y el archivo latest.log**. Si la encuentra pero el lugar está vacío, comparte las coordenadas y la semilla del mundo.
- Si aparecen solamente algunas salas, toma capturas desde arriba para revisar las uniones.

## Qué puedes usar en creativo de forma alternativa

Las siete plantillas todavía se pueden cargar individualmente con `/give @s minecraft:structure_block` en modo LOAD, por ejemplo:

`aetherium:citadel/gateway` · `aetherium:citadel/corridor` · `aetherium:citadel/guardhouse` · `aetherium:citadel/archive` · `aetherium:citadel/arena_shell` · `aetherium:citadel/crypt` · `aetherium:citadel/forge`

Esto sirve para inspeccionar detalles; para **verificar la generación natural**, utiliza el procedimiento de `/locate`, no el bloque de estructuras.

## Rendimiento y compatibilidad

El generador emplea `StructureSeparationSettings` y `JigsawManager` de Minecraft 1.16.5; máximo siete módulos en esta fase, sin procesar chunks ya explorados. No se añadieron bloques con tick ni spawners persistentes. La presencia en mundos con otros mods de worldgen todavía debe comprobarse jugando.

**Estado de QA:** compilación y validación del JAR automatizadas; inicio de registros mediante `runData` en CI en evaluación; no se ha probado todavía la ciudadela manualmente dentro del cliente Minecraft.
