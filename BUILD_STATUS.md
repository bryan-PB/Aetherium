# Estado de validación de Aetherium 0.1.0

## Revisiones estáticas realizadas

- 74 archivos JSON parsean correctamente.
- `mods.toml` parsea correctamente como TOML.
- 39 texturas PNG se pueden abrir/decodificar.
- Las referencias de texturas de los modelos de bloques e ítems apuntan a archivos existentes.
- Los 33 ítems registrados tienen modelo y traducción en español e inglés.
- Las recetas hacen referencia a IDs de Aetherium registrados.
- Los cuatro mobs/jefes con botín tienen sus loot tables; el proyectil (`aether_bolt`) no necesita botín.
- El ZIP acumulado incluye las Partes 1 a 6 en la misma raíz de proyecto.

## Compilación real

No se ejecutó `./gradlew build` en el entorno de creación. El entorno disponible cuenta con JDK 21; este proyecto Forge 1.16.5 / ForgeGradle 5.1 está preparado para JDK 8 y requiere descargar Gradle 6.8.3 y dependencias externas. No se debe interpretar la revisión estática como una compilación certificada.

## Cómo verificar en un PC con JDK 8

1. Instalar un JDK 8 de 64 bits y configurar `JAVA_HOME` para apuntar a él.
2. Comprobar `java -version` y confirmar que indique 1.8.
3. Abrir una terminal en la carpeta `Aetherium`.
4. Ejecutar `./gradlew genEclipseRuns` (Linux/macOS) o `gradlew.bat genEclipseRuns` (Windows), si se usa Eclipse.
5. Ejecutar `./gradlew build` o `gradlew.bat build`.
6. Si la compilación termina correctamente, buscar `build/libs/aetherium-0.1.0.jar`.

Curios es opcional. Para probar las ranuras de amuleto y anillo, instalar Curios API compatible con Forge 1.16.5 en el cliente/servidor de prueba. Aetherium mantiene un modo alternativo cuando Curios no está instalado.
