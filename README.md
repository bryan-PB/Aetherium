# Kryzorae

Mod de fantasía oscura y grietas dimensionales para Minecraft 1.16.5 (Forge 36.2.42), con jefes, cristales, criaturas y equipo propio.

## Compatibilidad

El nombre público ahora es **Kryzorae**. El identificador técnico del mod sigue siendo `aetherium` y los IDs de registro de los objetos, bloques, sonidos y entidades no cambian, para conservar la compatibilidad con los mundos ya creados. No elimines el mod anterior de un mundo existente sin una copia de seguridad.

## Compilación

Requiere Java 8 y Forge 36.2.42. Para compilar manualmente desde las fuentes, prepara los archivos OGG con Python 3 y FFmpeg:

```bash
python3 tools/generate_aetherium_sounds.py
./gradlew build
```

En Windows utiliza `gradlew.bat build`. Los lanzadores incluyen el arranque de **Gradle 7.6.4**, la misma versión utilizada en GitHub Actions. El paso `generateCitadelTemplates` se ejecuta automáticamente antes de empaquetar recursos: así, las **diecisiete salas NBT de la mega-Ciudadela** también se incluyen al compilar localmente. El JAR se genera en `build/libs/`. GitHub Actions también genera y valida los audios.

Curios es opcional; los accesorios pueden equiparse en los slots Necklace y Ring cuando Curios está instalado. Las habilidades de uso directo funcionan sin Curios.

Estado: desarrollo en pruebas. Una compilación exitosa no equivale a una prueba dentro del juego.

Para probar la generación natural, crea un mundo nuevo en el Overworld y ejecuta `/locate aetherium:citadel`. Los comandos `/kryzorae citadel status`, `/kryzorae citadel probe` y `/kryzorae citadel preview` ofrecen diagnóstico;  **preview modifica el terreno**, úsalo solo en un mundo descartable. La tarea de arranque `runData` está configurada con `setForceExit false` (ForgeGradle 5) para que no finalice forzosamente el daemon de Gradle. Esta prueba ahora es obligatoria en CI; un fallo hará que la comprobación sea roja, aunque se conservará el JAR compilado para diagnóstico. No sustituye una partida de prueba en Minecraft.

## Mega-Ciudadela (fase arquitectónica)

La Ciudadela usa **17 NBT independientes**, estructura nativa Forge 1.16.5, 19 cofres, tres spawners vanilla limitados, dos torres, dos cámaras bajo tierra y cinco tablas de botín. Los nuevos jefes aún no están desarrollados y falta revisar su aspecto/generación en Minecraft. [Guía técnica completa y árbol de fuentes](docs/citadel/MEGAESTRUCTURA_FORGE_1_16_5.md).

**Revisión de terreno:** la Ciudadela ahora busca una parcela seca y relativamente plana a lo largo de toda su extensión, utiliza la altura mediana del terreno y añade cimientos continuos bajo los muros. `preview` comprueba el emplazamiento antes de construir y también coloca cimientos. Para verificarlo, usa un mundo nuevo o chunks sin generar; las ruinas que ya aparecieron no cambian retroactivamente.

`/kryzorae citadel status` debe indicar `NBT=17/17`. **`preview` modifica hasta ~65×200 bloques y puede provocar una pausa puntual**, así que solo en un mundo creativo desechable.
