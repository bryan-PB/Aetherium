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

## Corrección de hombreras, brazos y guanteletes

La captura del usuario mostró piel y colores de su personaje visibles en los hombros, con hombreras diminutas y brazos demasiado lisos. Esta revisión centra la mejora **solo en las extremidades superiores**:

- Ambos sets llevan un brazo base continuo y opaco (hombro hasta muñeca), una placa de bíceps con UV independiente, codera con junta oscura y guantelete cerrado con bordes propios.
- Las hombreras están hechas de **varias capas anchas y un borde negro/metal visible frontal, lateral y posterior**. Se conectan al pivote del brazo para seguir el movimiento del jugador.
- **Violeta:** dos placas escalonadas sobre el hombro, aleta lateral larga, juntas índigo, borde magenta, brazalete con detalle de cristal.
- **Carmesí:** hombrera de hierro negro con placas lacadas rojas, punta externa de tres segmentos, placa de codo y antebrazo ancho con nudillos reforzados.
- Los atlas `layer_1` tienen zonas pintadas específicas para el frente y los laterales del brazo, la placa del bíceps (`UV 153,22`), la hombrera y el antebrazo. Los atlas `layer_2` no se alteran, pues solo dibujan piernas.
- El casco, pecho, piernas, IDs de registro, estadísticas y demás entidades no cambian.

**Pendiente de comprobar en el juego:** que al equipar solo la pechera se vean ambos brazos completos; que los hombros no dejen asomar la piel del jugador, en vistas frontal/trasera/3/4 y caminando.

## Corrección de la franja de piel expuesta (captura de noviembre)

La nueva captura del conjunto carmesí reveló una **franja turquesa procedente de la skin del jugador**, en la unión de brazo y torso. No procede de la paleta de la armadura. La causa geométrica estaba en el desplazamiento horizontal de los modelos originales de los brazos del jugador (Forge/Minecraft 1.16.5):

- Brazo derecho: cubo vanilla en `x = -3..1` respecto al pivote del brazo.
- Brazo izquierdo: cubo vanilla en `x = -1..3` respecto al pivote.
- La armadura custom anterior centraba los dos brazos en `x = 0`, dejando un borde del brazo original sin cubrir, especialmente cuando se animaba.

La corrección añade **un pivote secundario desplazado -1/+1 píxel** a todos los componentes de cada brazo y coloca una funda continua y opaca desde el hombro hasta la muñeca, con margen adicional para tapar incluso la capa exterior de la skin. Las hombreras, codera y guanteletes se desplazan con ese mismo pivote: se conserva su alineación y su animación. El casco, pecho y piernas no se modifican.

Un nuevo chequeo estático `tools/check_armor_arm_coverage.py` comprueba que el cubo opaco cubra tanto el brazo vanilla como su capa exterior (margen 0,25 px) en ambos lados. **No sustituye la comprobación de la pose real dentro de Minecraft.**

## Restricciones y pruebas manuales

Las capturas de diseño, incluidos los dos carteles Set Carmesí / Set Violeta, **no son capturas reales de Minecraft**. El modelo real está construido a base de cubos y tendrá una interpretación voxel más simplificada que la ilustración.

- [ ] Comparar en tercera persona frontal/trasera con las referencias (casco, pecho y hombros)
- [ ] Comprobar cada pieza por separado, sobretodo leggings y botas, para detectar z-fighting
- [ ] Girar cabeza, correr, agacharse, nadar y usar espada para revisar pivotes
- [ ] Revisar primera persona y el inventario, especialmente la visera y el brillo aparente de la gema
- [ ] Verificar soporte Forge 36.2.42 y carga de mundos existentes sin perder objetos

Los PNG y la compilación pueden validarse automáticamente, **pero solo ejecutar el juego** permite confirmar que se ven bien durante animaciones, iluminaciones y uso de capas de piel.
