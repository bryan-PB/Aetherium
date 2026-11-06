# Kryzorae — Expansión de equipamiento intermedio

**Implementado en código.** Umbracita ahora lleva sombrero 3D de ala ancha basado en las referencias aportadas. La evaluación visual dentro de Minecraft queda pendiente.

## Conjuntos y roles

| Conjunto | Defensa total | Usos de herramientas | Nivel de pico | Ventaja por conjunto completo |
| --- | ---: | ---: | ---: | --- |
| Brumacero | 16 | 425 | 2 (hierro) | 18% menos daño de monstruos; +25% resistencia al retroceso |
| Umbracita | 14 | 950 | 2 (hierro) | +15% velocidad al caminar; +15% impulso vertical al saltar |

- Los dos conjuntos tienen 4 piezas de armadura y espada, pico, hacha, pala y azada.
- **Herramientas — revisión gráfica 64×64:** ocho PNG nuevos fuente, cuatro por material (pico, hacha, pala y azada). Brumacero usa metal plateado, aristas de acero, pomo y engastes dorados con gema roja; Umbracita combina índigo, violeta oscuro y gemas magenta. Las siluetas son distintas por tipo y material: picos de doble garra, hachas de doble hoja frente a filos asimétricos, palas redondeadas frente a facetas angulares y azadas de hoja en gancho. Las espadas 64×64 anteriores se preservan sin cambios.
- **Fidelidad:** los diez PNG 64×64 se mantienen directamente en `src/main/resources/assets/aetherium/textures/item`. Ningún generador de atlas debe reemplazarlos. Las vistas previas se extraen del JAR final; las ilustraciones conceptuales no se consideran sprites de juego.
- **Rendimiento:** sprites estáticos + `item/handheld` vanilla; no se añaden animaciones, partículas, renders, entidades ni tareas por tick, y no se modifican las estadísticas.
- **Brumacero** conserva el casco cerrado blanco, dorado y rojo; **Umbracita** sustituye la antigua capucha por un **sombrero de ala ancha**, copa escalonada, cinta violeta con broche anaranjado, rostro abierto a la altura de los ojos y bufanda que cubre la boca. El ID del ítem sigue siendo `aetherium:umbracita_helmet` para mantener compatibilidad de partidas.
- **Brumacero — revisión ceremonial v2:** se conservan los colores marfil/acero, dorado y rojo, pero se refina la forma 3D de las cuatro piezas. El yelmo tiene visor oscuro y estrecho, mejilleras esculpidas, ceja dorada y cresta roja longitudinal. La coraza separa las dos placas del pecho, un emblema rojo central, láminas abdominales y faldones marfil. Las hombreras se superponen, los guanteletes tienen uniones de codo, las rodilleras se distinguen de las grebas y las botas llevan una puntera propia. Iconos 32×32 y atlas de acero pulido actualizados. No cambia ni el ID de ningún objeto ni sus recetas; los bonus se ajustaron posteriormente para resultar perceptibles.
- **Umbracita — v5 de vestimenta:** el peto ya no parece coraza pesada. Ahora tiene túnica/abrigo violeta oscuro abierto, solapas y ribetes lavanda, camisa interior oscura, correa de cuero diagonal, hebillas, insignia ámbar, funda utilitaria y faldones delanteros separados y traseros. Los pantalones pasan a ser piezas de tela estrechas con pliegues y una correa en un solo muslo. Las botas son de aventurero con puños, hebilla, puntera reforzada y tacón. También cambiaron los iconos de inventario de las tres piezas y los atlas 256×128.
- Las telas del abrigo no tienen simulación física: las extensiones se fijan a los pivotes del personaje para evitar entidades, capas de render adicionales o trabajo constante. Es una aproximación pixel-art deliberadamente compacta, no el dibujo de referencia convertido automáticamente en modelo.

- Todas las piezas se fabrican con los lingotes de su mineral y palos (cuando corresponda). Los patrones siguen Minecraft vanilla; el peto consume **8 lingotes**, nunca 9.
- Cada conjunto completo de armadura requiere **24 lingotes**. Juego completo de todas las herramientas: **11 lingotes** adicionales.
- Cada mineral está disponible en el Overworld, sin añadir contenido en The End ni nuevos minerales.
- Equipos Violeta y Carmesí y mecánicas de jefes quedan intactos.
- La reducción de daño de Brumacero se aplica solo al recibir daño de monstruos. Su resistencia al retroceso se habilita únicamente con el conjunto completo.
- Umbracita aplica o retira su modificador de velocidad cada 20 ticks, solo en servidor; el salto se ajusta únicamente al evento de salto en cliente y servidor para respetar la predicción de movimiento.
- No hay luces dinámicas, partículas adicionales, mobs, bloques, estaciones de fabricación ni dependencia adicional.
- Se mantienen los IDs de Kryzorae ya existentes y sus recetas actuales.

## Verificaciones manuales sugeridas

1. Sustituir JAR anterior por la nueva versión (solo un JAR de Kryzorae).
2. En creativo, buscar las 18 piezas y comprobar iconos y ambos conjuntos **puestos en un personaje**. Para Brumacero, comparar frente/perfil/espalda de yelmo, cresta roja, coraza dividida, hombreras superpuestas, rodilleras y puntera de botas.  Para Umbracita comprobar con F5: sombrero, abrigo con correa diagonal y faldones, pantalón más ceñido, botas con puños y punteras. Mirar por detrás y de lado, caminando, corriendo, saltando y agachándose; señalar si los faldones atraviesan demasiado las piernas.
3. En supervivencia, obtener lingotes de Brumacero/Umbracita y comprobar las 18 recetas en mesa y JEI.
4. Equipo incompleto: no debe conceder bonificación de conjunto; equipo completo sí.
5. Brumacero: comprobar con golpes de zombie (daño reducido cerca del 18%, antes de otras reducciones). El retroceso tiene una probabilidad mayor de bloquearse al llevar las cuatro piezas.
6. Umbracita: comprobar que al vestir las cuatro piezas aparece un aviso en la barra de acción, la velocidad aumenta 15% y el impulso de salto 15%. Comprobar retiro al quitar una pieza o al morir.
7. Comprobar que los dos picos pueden minar minerales que requieren hierro y no se salten niveles avanzados.
8. Revisar armaduras Violeta/Carmesí y combates contra ambos jefes para detectar regresiones.
9. Verificar FPS/MSPT en exploración con #189 y con la expansión.

## Sobre las texturas

Los scripts `tools/generate_intermediate_equipment_art.py` y `tools/generate_custom_armor_art.py` generan 18 sprites de 32×32 y, por último, sustituyen los iconos de armadura y crean cuatro atlas de armadura **256×128** con Python estándar antes de Gradle. Ambos conjuntos utilizan geometría articulada original mediante `IntermediateArmorModel.java`. Las texturas son imágenes PNG normales y **no generan cálculos visuales en juego**. Si el diseño no convence en uso real, se pueden mejorar sin alterar IDs ni mecánicas.
