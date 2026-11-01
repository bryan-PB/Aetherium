# Perfil de optimización: equipos de bajos recursos

Perfil objetivo de desarrollo y juego:

- CPU: Intel Core i3-N305 (8 núcleos / 8 hilos).
- RAM: 8 GB LPDDR5 (aprox. 7,7 GB utilizables).
- GPU: Intel UHD integrada, con memoria compartida.
- Plataforma: Minecraft 1.16.5 + Forge 36.2.42 + Java 8 de 64 bits.

La prioridad es mantener una experiencia estable sin quitar las mecánicas principales ni degradar visualmente a los jefes.

## Presupuesto inicial de memoria

| Proceso | Ajuste inicial |
|---|---:|
| Minecraft (cliente) | `-Xms512M -Xmx3G` |
| Gradle (compilación) | `-Xmx2G` |
| Trabajadores Gradle | 2 como máximo |

No asignes 6–7 GB a Minecraft: el sistema operativo, el controlador gráfico y la Intel UHD también utilizan memoria. Si el juego presenta pausas por recolección de basura o falta de memoria, cambia una sola variable por vez y compara.

Argumentos iniciales sugeridos para el perfil de Minecraft:

```text
-Xms512M -Xmx3G -XX:+UseG1GC
```

No agregaremos colecciones de argumentos JVM experimentales sin pruebas reproducibles. Para compilar, cierra el cliente de Minecraft y otras aplicaciones pesadas.

## Ajustes gráficos iniciales en Minecraft 1.16.5

- Distancia de renderizado: 6–8 chunks.
- Gráficos: Rápidos.
- Iluminación suave: mínima o desactivada.
- Nubes: desactivadas.
- Partículas: reducidas.
- Mezcla de biomas: 0–2 bloques.
- Mipmap: 0–2.
- Límite de FPS: 45–60.
- Evitar shaders y paquetes de texturas de alta resolución.
- Si sigue faltando rendimiento gráfico, probar 1280×720.

Estos son valores de partida, no una garantía de FPS: el resultado depende también de la resolución, temperatura, controladores, sistema operativo y mods adicionales.

## Reglas obligatorias para el código de Aetherium

### Entidades e IA

- Usar `Goal`/`GoalSelector` y navegación de Minecraft; no ejecutar búsquedas globales de entidades en cada tick.
- Recalcular objetivos y rutas solo cuando sea necesario, con intervalos y condiciones razonables.
- Limitar grupos y frecuencia de aparición; evitar oleadas grandes de mobs.
- El Lumivelo tendrá comportamiento pacífico ligero y no mantendrá cálculos costosos cuando esté lejos.
- Los jefes no aparecerán de forma masiva ni natural en grandes cantidades. Se invocarán mediante encuentro/estructura/objeto; objetivo: máximo un jefe activo de cada tipo por mundo.
- No realizar escaneos de bloques de grandes radios continuamente.

### Combate, efectos y renderizado

- Usar la barra de jefe nativa (`ServerBossInfo`/`BossInfoServer`, según la API disponible en 1.16.5).
- Limitar partículas por ataque y evitar emitirlas en cada tick para cada jugador.
- Preferir partículas vanilla y efectos visuales locales al cliente cuando sea posible.
- No crear luces dinámicas, shaders propios ni renderizadores que dibujen geometría innecesaria.
- Mantener separados los registros y renderizadores del cliente de la lógica común/servidor.

### Mundo, bloques y recursos

- Generación de mineral mediante `ConfiguredFeature`/`BiomeLoadingEvent` de forma acotada; vetas pequeñas y frecuencia baja.
- No introducir estructuras gigantes ni generación que consulte muchos chunks vecinos.
- No usar `TileEntity`/`TileEntityType` (block entities) para bloques que no necesiten estado dinámico.
- Evitar actualizaciones de bloque, guardado NBT y sincronización de red si el estado no cambió.
- Texturas de bloques e ítems de 16×16 o 32×32; armaduras y criaturas preferentemente 64×64 cuando el diseño lo permita.
- Modelos de entidades con geometría contenida; sin animaciones que creen partículas o recalculen recursos cada fotograma.

### Memoria y ciclo de vida

- Reutilizar objetos inmutables y evitar crear listas, vectores, partículas o `ItemStack` temporales en bucles de tick.
- No almacenar referencias a `World`, entidades o jugadores después de que dejen de ser válidas.
- Limpiar tareas, objetivos y referencias al morir o descargarse una entidad.
- Usar cachés con límites cuando sean necesarias; no guardar datos derivados de forma ilimitada.
- Sin escáneres periódicos globales ni `Thread` propios para tareas del mundo.

## Configuración ajustable que se implementará en el mod

La configuración de Forge tendrá, como mínimo, estas opciones (los valores se validarán en las entregas de entidades y jefes):

| Opción | Valor inicial de diseño |
|---|---:|
| Multiplicador de aparición de mobs Aetherium | 0,75 |
| Máximo de Cazadores de Brecha por grupo | 2 |
| Máximo de Lumivelos por grupo | 2 |
| Multiplicador de partículas de Aetherium | 0,5 |
| Aparición natural de jefes | Desactivada |

Se usarán límites y validaciones para impedir valores negativos o excesivos. La configuración no alterará el equilibrio de combate de forma arbitraria.

## Compatibilidad con mods de rendimiento (opcionales)

Aetherium no dependerá de otros mods de optimización. El objetivo es funcionar con Forge limpio.

- **Rubidium para Forge 1.16.5:** opción de cliente para optimizar el renderizado. Instalar únicamente un archivo correspondiente a Minecraft 1.16.5 y probarlo con el conjunto real de mods.
- **FerriteCore para Forge 1.16.5:** opción para reducir consumo de memoria; puede instalarse en cliente y servidor según su distribución.
- No instalar varios mods que reemplacen el mismo motor de renderizado o iluminación a la vez. No asumir compatibilidad automática con OptiFine; probar las combinaciones por separado.

Referencias de compatibilidad consultadas:

- Rubidium: https://modrinth.com/mod/rubidium
- FerriteCore: https://modrinth.com/mod/ferrite-core

## Pruebas de rendimiento que deben hacerse en cada entrega

1. Arranque con Forge limpio y solo Aetherium.
2. Permanecer 5 minutos en un mundo nuevo sin mobs Aetherium cercanos.
3. Medir el mismo mundo con una cantidad controlada de mobs.
4. Medir cada jefe por separado durante sus fases y ataques.
5. Revisar `latest.log` y errores; comparar uso de memoria y estabilidad antes/después.
6. Repetir pruebas con y sin mods opcionales de rendimiento.

No se publicarán cifras de FPS ni promesas de rendimiento sin medirlas en el equipo objetivo o en un equipo comparable.

## Criaturas de Aetherium

- Los dos tipos usan `clientTrackingRange(8)` y `updateInterval(3)` para evitar actualizaciones de red demasiado frecuentes.
- Las criaturas usan modelos de cajas sencillas y texturas pequeñas (64×64).
- El Cazador aplica su impulso de embestida solo al cumplirse un intervalo y solo a distancia media.
- El Lumivelo revisa amenazas en un radio local solo cada 160 ticks; no hace consultas espaciales constantes.
- El peso de aparición de cada criatura se puede reducir o desactivar en `aetherium-common.toml`.


## Jefes y proyectiles (Parte 5)

- No existe aparición natural de jefes; el jugador controla el inicio del encuentro con un sello consumible.
- Se impide iniciar un segundo encuentro de Aetherium dentro del radio de seguridad.
- Proyectiles del Devorador Astral con enfriamiento de 82–98 ticks y vida máxima de 80 ticks.
- Cada ataque de área consulta únicamente entidades dentro de una caja acotada y procesa solo jugadores.
- El Guardián mantiene como máximo un Cazador de Brecha auxiliar creado por su habilidad.
- Las partículas se envían como ráfagas puntuales y pequeñas, no como emisores permanentes.
- Las barras de vida usan `ServerBossInfo` integrado en Minecraft, sin un HUD de actualización manual.

## Parte 6: herramientas y equipo Astral

- El imán no realiza búsquedas si está apagado o no está en el inventario del jugador.
- Cuando está activo, evalúa objetos cada 10 ticks dentro de un radio de 4 bloques y atrae hasta 8 por pulso (ambos límites son configurables).
- La bengala solo consulta monstruos cuando el jugador la utiliza; limita el efecto a 12 objetivos dentro de 12 bloques.
- El bonus de armadura Astral se calcula cada 40 ticks en el servidor.
- No se añaden partículas persistentes ni entidades auxiliares para estas utilidades.
