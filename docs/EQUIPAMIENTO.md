# Kryzorae — rediseño de armas y armaduras

## Problema detectado

El equipamiento anterior se parecía a las versiones vanilla recoloreadas: iconos de 16x16 con tonos cian y morado, capas de armadura poco diferenciadas y un efecto sintético redundante en **cada impacto** de espada y hacha (hasta dos eventos en un combo). Ese sonido se superponía al sonido de golpe normal de Minecraft.

## Dos familias visuales nuevas

| Aspecto | Equipo básico (IDs `aetherium_*`) | Equipo avanzado (IDs `astral_*`) |
| --- | --- | --- |
| Identidad | Forja de acero templado | Armadura oscura de ejecución |
| Colores | Grafito, acero gris verdoso, latón envejecido | Metal ennegrecido, esmalte marfil, cobre y ámbar |
| Espada | Hoja recta robusta y guarda larga | Filo irregular con incisuras y nervadura clara |
| Hacha | Hacha de corte ancha y pesada | Hacha con pico enganchado y perfil dentado |
| Pico | Doble punta arqueada | Pico más agresivo con punta alargada |
| Casco | Yelmo cerrado de visor horizontal | Máscara reforzada con cresta y mejillas protegidas |
| Pechera | Hombreras, coraza segmentada y broche | Placas superpuestas, refuerzos y núcleo decorativo |
| Pantalones | Guardas de cadera y rodilla | Guardas reforzadas y tassets anchos |
| Botas | Puntera blindada y tobillo protegido | Sabatones con placas de rodilla y puntera pronunciada |

Los 14 iconos son texturas **RGBA 32x32** distintas; no se trata de duplicar el mismo icono y cambiarle el color. Las cuatro texturas de armadura equipada (`layer_1`, `layer_2` para ambas familias) se rehacen conservando el formato UV **64x32 de Minecraft 1.16.5** y sus transparencias.

## Silueta cuando se equipan

`KryzoraeArmorItem` conecta ocho piezas existentes con `KryzoraeArmorModel`. El modelo añade geometría 3D visible: reborde de visera, mejillas blindadas, peto sobresaliente, hombreras, brazales, rodilleras y botas con punta. Las piezas siguen los huesos vanilla y se muestran únicamente cuando están equipadas. El conjunto avanzado lleva hombreras y coraza más grandes.

## Sonido de combate

La prioridad aquí es **eliminar lo que molestaba**, sin añadir otra capa de sonido artificial:

- Los golpes comunes de espada y hacha no reproducen `aetherium_blade_hit` ni `astral_blade_arc`.
- Tampoco se reproducen esos efectos una segunda vez en las activaciones de combo; el tercer/cuarto golpe conserva sus partículas, proyectiles, daño y empuje.
- El audio habitual del impacto contra un enemigo lo maneja **Minecraft**, evitando el efecto electrónico repetitivo del mod.
- No se han eliminado los eventos antiguos del registro para evitar romper recursos que los referencien; simplemente las armas ya no los reproducen.

## Compatibilidad

Se conservan los **IDs `aetherium`** y `astral` de materiales, objetos, eventos, recetas y guardados. Kryzorae sigue siendo el nombre visible del mod. No se renombra el repositorio ni se cambia la rama principal.

## Verificación manual pendiente

- [ ] Comparar todos los iconos en inventario y en la mano, en resolución normal y escalado de GUI.
- [ ] Ponerse cada pieza por separado: no debe aparecer una armadura completa al equipar solo el casco.
- [ ] Caminar, agacharse, atacar, nadar y dormir con ambos conjuntos para comprobar que las hombreras acompañan los brazos.
- [ ] Comprobar visualmente el casco en primera persona, tercera persona y con un casco encima.
- [ ] Golpear mobs sin combo y con combo de cada arma; no debe oírse la antigua capa electrónica.
- [ ] Cargar un mundo existente y revisar inventarios, reparaciones, protecciones y efectos de conjunto.

La prueba automatizada verifica recursos y compilación; **no acredita la apariencia o el audio dentro de un cliente Minecraft real**.
