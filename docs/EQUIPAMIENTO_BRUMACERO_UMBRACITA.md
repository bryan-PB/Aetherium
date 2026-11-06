# Kryzorae — Expansión de equipamiento intermedio

**Implementado en código.** La evaluación visual dentro de Minecraft queda pendiente.

## Conjuntos y roles

| Conjunto | Defensa total | Usos de herramientas | Nivel de pico | Ventaja por conjunto completo |
| --- | ---: | ---: | ---: | --- |
| Brumacero | 16 | 425 | 2 (hierro) | 10% menos daño de criaturas hostiles |
| Umbracita | 14 | 950 | 2 (hierro) | +10% velocidad al caminar; +5% impulso vertical al saltar |

- Los dos conjuntos tienen 4 piezas de armadura y espada, pico, hacha, pala y azada.
- Todas las piezas se fabrican con los lingotes de su mineral y palos (cuando corresponda). Los patrones siguen Minecraft vanilla; el peto consume **8 lingotes**, nunca 9.
- Cada conjunto completo de armadura requiere **24 lingotes**. Juego completo de todas las herramientas: **11 lingotes** adicionales.
- Cada mineral está disponible en el Overworld, sin añadir contenido en The End ni nuevos minerales.
- Equipos Violeta y Carmesí y mecánicas de jefes quedan intactos.
- Las bonificaciones de Brumacero se aplican solo al recibir daño de monstruos, mediante evento (no cada tick).
- Umbracita aplica o retira su modificador de velocidad cada 20 ticks, solo en servidor; el salto se ajusta únicamente al evento de salto.
- No hay luces dinámicas, partículas adicionales, mobs, bloques, estaciones de fabricación ni dependencia adicional.
- Se mantienen los IDs de Kryzorae ya existentes y sus recetas actuales.

## Verificaciones manuales sugeridas

1. Sustituir JAR anterior por la nueva versión (solo un JAR de Kryzorae).
2. En creativo, buscar las 18 piezas y comprobar iconos y los dos conjuntos **puestos en un personaje**.
3. En supervivencia, obtener lingotes de Brumacero/Umbracita y comprobar las 18 recetas en mesa y JEI.
4. Equipo incompleto: no debe conceder bonificación de conjunto; equipo completo sí.
5. Brumacero: comprobar con golpes de zombie (daño reducido cerca de 10%, antes de otras reducciones).
6. Umbracita: comprobar velocidad y salto y que el bonus se retire al quitar una pieza o al morir.
7. Comprobar que los dos picos pueden minar minerales que requieren hierro y no se salten niveles avanzados.
8. Revisar armaduras Violeta/Carmesí y combates contra ambos jefes para detectar regresiones.
9. Verificar FPS/MSPT en exploración con #189 y con la expansión.

## Sobre las texturas

El script `tools/generate_intermediate_equipment_art.py` genera 18 sprites de 32×32 y 4 atlas de armadura 128×64 con Python estándar antes de Gradle. Las texturas son imágenes PNG normales y **no generan cálculos visuales en juego**. Si el diseño no convence en uso real, se pueden mejorar sin alterar IDs ni mecánicas.
