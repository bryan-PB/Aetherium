# Kryzorae — Verdugo Alado: embestidas, ilusiones y nuevos disparos

**Minecraft 1.16.5 · Forge 36.2.42 · Java 8**  
**Nombre visible:** Kryzorae. **Identificadores de compatibilidad:** `aetherium` (sin cambiar).

## Problemas que motivaron la revisión

- La animación de embestida apenas se diferenciaba del vuelo normal. El jefe podía apuntar a un jugador con la cara mientras avanzaba en una dirección previamente fijada, provocando una embestida «de lado».
- Solo existía **un espejismo**, dibujado desde el render del jefe verdadero; las copias no tenían su propio render. Cualquier daño al jefe real lo hacía desaparecer, y acertar a la copia penalizaba al jugador.
- Los dos jefes continuaban lanzando sprites de **cristales** como proyectiles genéricos, pese a que sus nuevas identidades visuales eran un Verdugo Alado con alas y un Guardián con ataques de presión/viento.

## Verdugo Alado: embestida con postura y dirección legibles

- **Aviso:** 15 ticks en fase II, 12 ticks en fase III. Se queda quieto, gira para apuntar y **bloquea esa orientación** hasta terminar el avance. Aparecen señales discretas a lo largo de la dirección de la futura embestida.
- **Postura de preparación:** torso echado hacia atrás, garras y brazos recolocados, alas que se pliegan hacia atrás con **dos articulaciones por lado**. Se suspende el aleteo normal.
- **Impulso:** postura inclinada hacia adelante (~55° en el torso), garras hacia el frente, alas muy pegadas al cuerpo, piernas y faldones arrastrados por el impulso; humo oscuro y pequeñas chispas a su paso.
- **Colisión:** el hitbox se extiende a lo largo del movimiento de cada tick, para evitar que la velocidad haga saltar sobre el jugador sin detectar el golpe. Se comprueba la línea a la víctima contra bloques sólidos para no causar daño a través de muros.
- **Encadenamiento:** se conservan 2 embestidas en fase II / 3 en fase III, recuperación y ventana de contraataque al esquivar toda la cadena.

## Tres espejismos tipo Cerebro de Cthulhu

Referencia de comportamiento, no copia visual de sprites de Terraria: en modo experto, el Cerebro de Cthulhu muestra **tres imágenes espejo** translúcidas, cuya opacidad aumenta al perder salud.

- Fases II/III: **tres clones** de aspecto idéntico al Verdugo real. Las posiciones se calculan alrededor del **jugador** a partir de la posición relativa del jefe, en tres cuadrantes distintos; los movimientos del Verdugo se reflejan mientras persigue o embiste.
- Los clones son entidades renderizadas **cada una por separado** con `RenderType.entityTranslucent`. Esto impide que desaparezcan solo porque el cuerpo real esté fuera del área de render.
- Comienzan muy transparentes y **aumentan su opacidad** según baja la vida del jefe. Disponen de entrada/salida graduada y sin sombras.
- Duran como máximo 170 ticks. No hacen daño ni ralentizan cuando un jugador golpea una copia; se desvanecen con una pequeña nube de humo. Golpear al Verdugo auténtico **no elimina automáticamente** las demás ilusiones.
- Conservamos el hechizo de ilusión propio del Guardián, pero su render también pasa a una entidad independiente. La penalización de su copia, que formaba parte de su mecánica anterior, se conserva.
- Las ilusiones no reemplazan el hitbox real ni afectan a la barra de vida del jefe. El Verdugo real sigue siendo identificable por el daño recibido y su barra.

## Proyectiles nuevos: no se ven cristales

Se conserva el registro de entidad `aetherium:aether_bolt` para no romper datos antiguos, pero ya **no se dibuja un item `astral_fragment` o `rift_fragment`**. El proyectil utiliza un renderer de geometría propia, dos planos cruzados y un estilo sincronizado por entidad.

| Jefe | Proyectil | Apariencia y anticipación |
| --- | --- | --- |
| **Verdugo Alado** | **Plumas-cuchilla de hierro ennegrecido** | Negra y roja, con nervadura metálica clara. Las alas se abren, tres cuchillas temporales aparecen junto al pecho y aguanta **12 ticks** antes de la primera ráfaga. Estela de humo y chispas. |
| **Guardián de la Grieta** | **Cortes semicirculares de aire comprimido** | Arcos anchos gris claro y blanco con leve interior verdoso. Mantiene el cañón firme durante **12 ticks** de aviso antes de la primera descarga, con nube de presión y retroceso visual del arma. |

Se conservan el daño de impacto, la ralentización breve, las ráfagas escalonadas, la detección de bloques, los avisos y las duraciones ya existentes. Se retiran los sonidos genéricos de impacto superpuestos al sonido propio del enemigo. El resto de armas, armaduras, registros y recetas no se toca.

## Pruebas manuales imprescindibles

- [ ] Invocar Verdugo en fase II y III: comprobar la **silueta plegada y luego inclinada** de cada embestida, dirección de aviso, distancia y ausencia de daño a través de paredes.
- [ ] Verificar que los tres espejismos aparezcan **al mismo tiempo**, sean translúcidos y **sigan al jugador y al Verdugo**; comprobar que la transparencia aumenta al bajar su vida.
- [ ] Golpear primero una copia y luego al Verdugo real: atacar la copia no debe quitar vida al jugador ni cancelar los otros clones; atacar al real no debe eliminar los tres.
- [ ] Moverse lejos de las copias, girar la cámara y volver: asegurarse de que la entidad fake se dibuja incluso cuando el Verdugo está fuera del encuadre.
- [ ] Verificar la animación de las plumas-cuchilla durante el aviso y que al disparar se ven las **plumas** en lugar de un fragmento/cristal.
- [ ] Probar los cortes de aire comprimido del Guardián a distintas distancias y ángulos, tanto en fase normal como avanzada. Debe detenerse antes del disparo.
- [ ] Comprobar rendimiento con tres espejismos, sincronización en servidor multijugador y que desaparezcan cuando muere el jefe.
- [ ] Confirmar que los mundos existentes cargan jefes/objetos con sus IDs anteriores.

**Limitación:** Java/Gradle y las comprobaciones estáticas detectan errores de implementación, pero **no prueban** la visibilidad, la sincronización real ni la sensación de esquiva en Minecraft.
