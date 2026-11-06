# Kryzorae — Biblioteca visual y control de calidad
## Identidad de materiales (revisión 2026-11-06)

Esta guía deriva de los **sprites que efectivamente contiene el mod**, no de ilustraciones conceptuales. En el juego, los objetos deben leerse primero en la barra rápida a 16 píxeles lógicos. Los PNG pueden tener más píxeles físicos, pero no deben depender de detalles invisibles al reducirse.

| Familia | Rasgo principal | Ítems base | Mena |
|---|---|---|---|
| Brumacero | Acero frío, plata marfil, ribetes dorados y acento rojo | Lingote metálico, bruto compacto, hacha de un solo filo | Vetas/metales incrustados en roca, textura natural aprobada V3 |
| Umbracita | Violeta profundo, sombras índigo y un pequeño brillo magenta | Fragmento angular, barra arcana y herramienta oscura | Depósitos angulares violetas, textura natural V3 |
| Prismalita | Cristal de varias caras, brillo cian con reflejos magenta | Fragmento facetado; **no** se convierte automáticamente en lingote | Cristales fracturados con roca visible |
| Aetherium | Azules cian y turquesa, apariencia de energía refinada | Bruto energético e ingote azul | Nodos turquesa repartidos de forma natural |
| Astral / Grieta | Contraste violeta-carmesí, fragmentos inusuales | Fragmento, corazón, sello, núcleo y sigilo | Objetos de progresión, no todos necesitan menas nuevas |

## Silueta antes que decoración

- **Espada**: hoja, guarda y mango diferenciados; no sacrificar la forma por pequeños destellos.
- **Hacha**: un filo dominante más grande que el pequeño contrapeso; nunca dos medias lunas simétricas que parezcan mariposas o picos. Probada en primer plano y 16px.
- **Pico**: dos extremos laterales estrechos; el centro debe unir claramente el mango.
- **Pala**: cabeza única, grande y maciza con borde exterior; no parecer una daga, hoja de hacha o media luna.
- **Azada**: hoja transversal reconocible, no una segunda variante de pico.
- **Mineral bruto**: silueta irregular y material perceptible; no recrear el lingote con otro color.
- **Lingote/barra**: volumen fácil de reconocer por cara superior, lateral sombreado y brillo controlado.
- **Accesorios**: marco o silueta exterior visible y símbolo interior sencillo.

## Reglas técnicas que protegen los FPS / MSPT

1. Objetos comunes: `item/handheld` o `item/generated` y PNG **estáticos**. No crear entidades, capas dinámicas, particles o lógica por tick para mejorar un icono.
2. Menas: mantener cubos estándar y los modelos `cube_bottom_top`. Todos los modelos deben señalar a archivos reales y conservar sus caras superior/lateral/inferior.
3. Conservar la rareza y generación actuales; cambiar un sprite nunca cambia `AetheriumWorldGen` o `AetheriumConfig`.
4. Evitar texturas excesivamente grandes. 48px para materiales refinados actuales; 64px para armas/herramientas pixel art con diseño lógico 32px. La legibilidad se evalúa a 16px.
5. Las pruebas automáticas verifican firmas, tamaños, colores/transparencia, SHA de diseños aprobados y recursos referenciados. No son sustituto de una captura en Minecraft.
6. Los sprites aprobados deben estar **en el repositorio y en el JAR de CI**. Una imagen conceptual nunca sustituye los recursos finales.

## Control de cambios

Antes de entregar cada compilación:
- Comparar PNG previo y nuevo **a 16px sobre fondo de barra rápida** y ampliados con vecino más próximo.
- Extraer el PNG del **JAR compilado** y comparar su SHA256 con el original aprobado.
- Verificar que el resto de las herramientas, minerales, estadísticas, armaduras y recompensas no cambió accidentalmente.
- Mantener cambios de diseño separados de cambios de mecánicas. Una vista previa no demuestra que el modelo se vea igual en primera/tercera persona.

*Estado:* revisadas las dos hachas y la colección de materiales; recursos naturales V3 preservados. Las demás familias se mantienen hasta que una revisión visual justifique reemplazarlas.
