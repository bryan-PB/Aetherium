# Armaduras Carmesí y Violeta — revisión basada en las referencias

## Dos familias completamente nuevas

El usuario aportó dos referencias: armadura roja de criatura/guerrero con casco cornudo y máscara agresiva; armadura violeta de placas con cresta y una gema frontal. Esta implementación traduce sus **rasgos estructurales reconocibles** a geometría Minecraft, sin pegar una ilustración encima ni reproducir el dibujo original.

| | Violeta | Carmesí |
|---|---|---|
| ID del set original | `aetherium_helmet/chestplate/leggings/boots` | `astral_helmet/chestplate/leggings/boots` |
| Apariencia | acero índigo, placas púrpura, bandas magenta, cristal frontal | armadura rojo oscuro, rebordes negros, cuernos unidos al casco, mandíbula dentada, ojos ámbar |
| Casco | crestón central y dos puntas laterales cortas; visor negro enmarcado | dos cuernos de 3 piezas unidos al casco, frente central, visor oscuro, colmillos de armadura |
| Peto | placas con borde, gema tallada sobresaliente, faldones frontales y traseros | coraza de placas V, nervadura central, protecciones superpuestas |
| Hombros | placas angulares escalonadas | grandes hombreras con puntas estructurales |
| Extremidades | rodilleras y tobillos ornamentales, botas de suela ancha | guanteletes pesados, rodilleras reforzadas, botas pesadas con cresta |

## Cómo se evita el aspecto de armadura rota

1. `KryzoraeArmorModel` **reemplaza por completo los siete ModelRenderer de armadura vanilla** en lugar de superponer piezas encima de cubos viejos de 64×32.
2. Cada parte está conectada al pivote correcto del bípede. Los cuernos giran con la cabeza, las hombreras con el brazo y las rodilleras con sus respectivas piernas.
3. Las piernas del slot `LEGS` cubren cadera, muslos y rodillas, mientras que el slot `FEET` contiene únicamente el calzado y las cañas inferiores. **No se dibujan dos piernas enteras con texturas diferentes**.
4. Los atlas `aetherium_layer_1/2.png` y `astral_layer_1/2.png` ahora miden 256×128 y están texturizados según las nuevas UV. Los iconos de las ocho piezas utilizan sprites distintos de 32×32 con transparencia.
5. El resto de objetos, recetas, materiales y registros mantiene las claves internas antiguas; no se cambia `modId = aetherium`.

## Restricciones y pruebas manuales

Las capturas de diseño, incluidos los dos carteles Set Carmesí / Set Violeta, **no son capturas reales de Minecraft**. El modelo real está construido a base de cubos y tendrá una interpretación voxel más simplificada que la ilustración.

- [ ] Comparar en tercera persona frontal/trasera con las referencias (casco, pecho y hombros)
- [ ] Comprobar cada pieza por separado, sobretodo leggings y botas, para detectar z-fighting
- [ ] Girar cabeza, correr, agacharse, nadar y usar espada para revisar pivotes
- [ ] Revisar primera persona y el inventario, especialmente la visera y el brillo aparente de la gema
- [ ] Verificar soporte Forge 36.2.42 y carga de mundos existentes sin perder objetos

Los PNG y la compilación pueden validarse automáticamente, **pero solo ejecutar el juego** permite confirmar que se ven bien durante animaciones, iluminaciones y uso de capas de piel.
