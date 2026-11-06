# Kryzorae — Ciudadela de la Grieta · Megaestructura modular de 17 salas

**Minecraft Java 1.16.5 / Forge 36.2.42.** Mod ID: `aetherium`. Consulta la [guía técnica completa](MEGAESTRUCTURA_FORGE_1_16_5.md) para el árbol de archivos, reglas de ensamblaje, NBT, spawners y tablas de botín.

## Encontrarla en el mundo

1. Sustituye el JAR antiguo: debe haber **un solo Kryzorae** en la carpeta `mods`.
2. Crea un mundo NUEVO normal (no superplano), Overworld, comandos habilitados. Los chunks antiguos no se regeneran.
3. Ejecuta `/kryzorae citadel status`: los recursos deben mostrar **NBT=17/17**.
4. Ejecuta `/kryzorae citadel probe` sobre suelo firme. El comando es de solo lectura y muestra bioma, terreno y registro.
5. Ejecuta `/locate aetherium:citadel`, visita las coordenadas y revisa también los niveles enterrados.

El plano ocupa aproximadamente 56 bloques en X por 174 en Z, con 17 módulos: portón, corredor, cuartel, patio, archivo, arena, umbral, santuario, cripta, forja, dos torres, barracones, prisión, galería y dos cámaras subterráneas. Contiene 19 cofres, tres spawners vanilla limitados y peligros visibles. Los bosses nuevos aún no están implementados.

## Vista previa SOLO en mundo descartable

`/kryzorae citadel preview` coloca todas las piezas de una vez usando el mismo plano y rotaciones que la generación natural. **Destruye/sobrescribe un área aproximada de 65×200 bloques, incluidos niveles bajo tierra**, y puede causar una pausa puntual; jamás usarlo en tu mundo principal ni cerca de construcciones. La vista previa no comprueba los soportes de terreno como la generación por chunks.

## Diagnóstico si algo falla

- `status`: verificar activo, Overworld, estructura configurada, espaciado y NBT 17/17.
- `probe`: comprobar bioma, columna local y **toda la huella de las 15 salas de superficie**. `zona completa apta=false` significa que hay agua, altura incompatible o desniveles excesivos; busca una zona más plana. No garantiza que el chunk actual sea candidato.
- `locate`: candidato según las reglas nativas y el mapa mundial.
- Si una sala no se coloca en `preview`, el comando señala su ID y puede dejar terreno parcialmente cambiado.

Config por defecto: `config/aetherium-common.toml` (`citadelEnabled=true`, `citadelSpacingChunks=26`, `citadelMinSeparationChunks=10`). La estructura admite zonas terrestres variadas y pantanos secos; no océanos, ríos, playas, Nether o End. La generación de la megaestructura **todavía requiere prueba en juego** y no debe calificarse de versión final únicamente por pasar la CI.
