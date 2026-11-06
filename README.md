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

En Windows utiliza `gradlew.bat build`. Los lanzadores incluyen el arranque de **Gradle 7.6.4**, la misma versión utilizada en GitHub Actions. El paso `generateCitadelTemplates` se ejecuta automáticamente antes de empaquetar recursos: así, las **siete salas NBT de la Ciudadela** también se incluyen al compilar localmente. El JAR se genera en `build/libs/`. GitHub Actions también genera y valida los audios.

Curios es opcional; los accesorios pueden equiparse en los slots Necklace y Ring cuando Curios está instalado. Las habilidades de uso directo funcionan sin Curios.

Estado: desarrollo en pruebas. Una compilación exitosa no equivale a una prueba dentro del juego.

Para probar la generación natural, crea un mundo nuevo en el Overworld y ejecuta `/locate aetherium:citadel`. Los comandos `/kryzorae citadel status` y `/kryzorae citadel preview` ofrecen diagnóstico; **preview modifica el terreno**, úsalo solo en un mundo descartable. Si Forge `runData` falla en CI, revisa el aviso: el resultado verde de `build` no certifica por sí solo el arranque en juego.
