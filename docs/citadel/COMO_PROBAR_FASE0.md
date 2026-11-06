# Kryzorae — Ciudadela de la Grieta · Prueba de la corrección de generación
**Versión de Minecraft:** Java 1.16.5, Forge 36.2.42. **Mod ID:** `aetherium`.

## Qué se corrigió
La versión anterior tenía dos problemas que podían hacer que no apareciera la Ciudadela:

- Rechazaba alturas menores de Y=65, habituales en llanuras y bosques. Ahora acepta terreno firme entre Y=60 y 126.
- Admitía solo cuatro categorías de bioma y descartaba lugares comunes como sabanas y desiertos. Ahora acepta muchos biomas terrestres y excluye océanos, ríos, playas, pantanos y otras dimensiones.

Además, la Ciudadela ya **no depende de un ensamblaje jigsaw aleatorio**. Las siete salas se distribuyen en posiciones fijas y comprobadas, usando piezas NBT auténticas de Minecraft; las conexiones se alinean y no hay salas solapadas. Conservamos los mismos recursos V3 y las tres tablas de botín.

## Cómo probar su aparición natural
1. Elimina el JAR antiguo de la carpeta `mods` y deja **solo uno de Kryzorae**.
2. Para probar generación, crea un **mundo normal NUEVO del Overworld**, con comandos activos. No uses un mundo superplano.
3. Escribe `/locate aetherium:citadel`. Si el juego devuelve coordenadas, usa `/tp @s X 95 Z` y busca la ciudadela también por debajo de tu altura, cerca del lugar indicado.
4. La distribución debe incluir portón, corredor, cuartel, archivo y arena, más cripta y forja laterales. Comprueba las puertas y abre los cofres.

**Importante:** los chunks ya generados en partidas antiguas no se regeneran; `/locate` encuentra la estructura como candidato de generación, pero no remodela terreno viejo. En un mundo nuevo la prueba es mucho más fiable.

## Si no aparece todavía: diagnóstico real
Con permisos de operador (trucos activados), escribe:

`/kryzorae citadel status`

Obtendrás información de: habilitación, dimensión, registro, estructura configurada, espaciado y **7/7 NBT**. Comparte una captura del mensaje si alguno aparece en `false`.

Para comprobar que los edificios realmente se pueden colocar, en un **mundo vacío de pruebas**, puedes escribir:

`/kryzorae citadel preview`

Este comando genera **una vez** las siete salas cerca del jugador, usando exactamente los archivos NBT del JAR. **SOBRESCRIBE un área aproximada de 50×95 bloques**: úsalo solamente en un mundo creativo descartable. La vista previa es una herramienta de diagnóstico; **no sustituye** a la generación natural.

## Configuración
- Archivo: `config/aetherium-common.toml`
- `citadelEnabled = true`
- `citadelSpacingChunks = 26` (distancia media por cuadrícula, en chunks)
- `citadelMinSeparationChunks = 10`

## Calidad y rendimiento
Generación limitada por el sistema nativo de estructuras y exclusivamente en nuevos chunks; ninguna entidad nueva, efectos visuales permanentes, bloque con tick personalizado ni búsqueda cada tick. Los comandos de diagnóstico solo se ejecutan cuando el jugador los invoca.

**Estado:** pruebas de compilación y arranque de registros automatizadas; generación visual real del cliente Minecraft requiere verificación en juego.
