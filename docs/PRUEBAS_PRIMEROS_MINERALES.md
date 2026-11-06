# Kryzorae — Pruebas in-game de la primera expansión minera

**Estado:** generado y compilado, pendiente de verificación dentro del cliente Minecraft 1.16.5 Forge 36.2.42.

## Contenido añadido

- `aetherium:brumacero_ore`: Overworld Y20–64, **12 intentos/chunk por defecto** (base 2 × densidad 6), veta 5, pico de piedra o superior. Da `raw_brumacero`; fundir en horno o alto horno produce `brumacero_ingot`.
- `aetherium:umbracita_ore`: Overworld Y8–36, **6 intentos/chunk por defecto** (base 1 × densidad 6), veta 4, pico de hierro o superior. Da `raw_umbracita`; fundir en horno o alto horno produce `umbracita_ingot`.
- `aetherium:prismalita_ore`: Overworld Y12–42, **6 intentos/chunk por defecto** (base 1 × densidad 6), veta 3, pico de hierro o superior. Da 2 `prismalita_shard` base (Fortuna puede aumentar), sin fundición.
- Los tres tienen bloques e iconos originales 16×16, botines con Toque de Seda y compatibilidad de Fortuna, modelos y nombres en cinco idiomas.
- Todas las frecuencias, alturas y tamaños se ajustan en `config/aetherium-common.toml`. **Nuevo ajuste:** `newOreDensityMultiplier = 6` multiplica la frecuencia de Brumacero, Umbracita y Prismalita, con un máximo de 24 intentos/chunk por cada uno. La versión anterior tenía base 2/1/1; la frecuencia efectiva pasa a 12/6/6 con el nuevo multiplicador. Los ajustes individuales originales se respetan, incluida la opción de poner una frecuencia en 0. Aetherium permanece en 2 intentos/chunk. Si ya tenías una configuración anterior, Forge añadirá la nueva clave al TOML; no es necesario borrar el archivo. La configuración de este tipo de generación se consulta al iniciar el juego; cambiarla requiere reiniciar.

## Cómo probar

1. Haz copia de seguridad de tu mundo y arranca con **solo un JAR de Kryzorae**. El Códice de la versión anterior debe funcionar.
2. Prueba en un **mundo nuevo** o explora **chunks no generados** del Overworld. No hay retro-generación para chunks viejos; una cueva ya explorada seguirá teniendo los mismos minerales que antes. Para comparar, prueba varios chunks nuevos en lugar de un solo tramo de cueva.
3. Localiza minerales en las alturas indicadas (F3) y verifica que las texturas se ven correctamente.
4. Comprueba los tres requisitos de pico: piedra para Brumacero, hierro para Umbracita y Prismalita. Si usas pico inferior no debe soltar material.
5. Prueba Toque de Seda (bloque completo) y Fortuna (aumenta botín bruto/cristales).
6. Funde Brumacero y Umbracita, en horno normal y alto horno. Prismalita suelta cristales directamente.
7. Activa el Códice: la página Primeros pasos contiene estos minerales y sus alturas.
8. Verifica FPS y tiempos de tick durante **generación de chunks nuevos**, y compara con #185 y #179. La mayor frecuencia incrementa las operaciones de generación, aunque **no añade lógica continua por tick**. Probar sin y con mods de cuevas.
9. Verifica que no se genera mineral nuevo en Nether ni en el End (solo Overworld).
10. Si no aparecen vetas o hay problemas de texturas/recetas, comparte coordenadas, F3 y latest.log.

## Fuera de esta compilación

- No se añadieron armaduras, espadas, picos ni hachas: sus diseños quedan **pendientes de discutir contigo**.
- Tampoco hay Fragua de Aleaciones, progresión endgame nueva, bioma, estructuras, ni minerales en el End.
- Las recetas endgame anteriores todavía existen hasta la fase de reforma completa; **aún no se ha bloqueado** la fabricación temprana de la espada Violeta.
