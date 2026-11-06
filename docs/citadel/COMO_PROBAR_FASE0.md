# Cómo probar las primeras habitaciones de la Ciudadela — Kryzorae #248

**Esta versión es una prueba de estructura, no la expansión completa.** La Ciudadela todavía **no aparece por sí sola** explorando el Overworld. El mod conserva íntegros sus minerales, armas, armaduras, jefes y estadísticas anteriores.

## Prueba en mundo creativo (Minecraft Java 1.16.5 / Forge 36.2.42)

1. Instalar solo el nuevo JAR de Kryzorae y retirar la versión anterior de la carpeta `mods` para evitar duplicados.
2. Abrir un **mundo de prueba** en creativo y hacer una copia de seguridad de cualquier partida importante.
3. Ejecutar `/give @s minecraft:structure_block`, colocar el bloque y abrir su interfaz.
4. Cambiar el modo del bloque a **LOAD / CARGAR**.
5. Introducir uno de estos nombres tal como aparece: `aetherium:citadel/gateway`, `aetherium:citadel/corridor`, `aetherium:citadel/guardhouse`, `aetherium:citadel/archive` o `aetherium:citadel/arena_shell`.
6. Pulsar **LOAD** para mostrar la caja de dimensiones y una segunda vez para colocar los bloques. Si el bloque no encuentra la plantilla, apuntar el nombre exacto y revisar el JAR; no crearla de cero con comandos.
7. Revisar los accesos norte/sur (3 bloques de ancho × 4 de alto), muros, piso, luz de alma y cofres. Abrir cofres en un mundo de prueba y comprobar que entregan recursos, sin sellos ni drops de jefes.

## Qué debería verse

- **gateway:** portón de piedra, algunos detalles de obsidiana llorosa, cofre lateral.
- **corridor:** pasillo interior con dos salidas.
- **guardhouse:** cuarto de guardia con muebles sencillos y cofre.
- **archive:** sala con bibliotecas y cofre con recursos arcanos.
- **arena_shell:** patio/arena descubierta, sin enemigos, listo para implementar al Centinela posteriormente.

## Qué todavía no existe

- Generación natural en biomas, localización por `/locate`, unión automática de módulos, trampas.
- Los tres enemigos nuevos, minijefe y Custodio.
- Sello del Umbral, llave, reliquias, armas exclusivas y activación del boss.

## Pruebas y rendimiento

- No hay bloques del mod con procesos continuos, nuevos mobs vivos ni IA o partículas permanentes.
- Los cinco archivos NBT y sus tres tablas de botín fueron verificados contra el JAR en GitHub Actions.
- La integración natural debe realizarse mediante estructura real registrada en Forge 1.16.5, con separación por región y validación de chunks, no con generación forzada cada tick.

La captura más útil para revisar diseño será de las cinco habitaciones cargadas en el mundo, de frente y desde arriba. Los modelos NBT son prototipos arquitectónicos, no la decoración definitiva.
