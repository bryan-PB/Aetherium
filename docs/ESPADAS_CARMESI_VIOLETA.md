# Espadas Carmesí y Violeta — habilidades y modelos finales (primera implementación)

**Kryzorae, Minecraft 1.16.5 / Forge 36.2.42.** Se conserva el identificador interno `aetherium`; no se renombra la espada ya obtenida por los jugadores en sus partidas.

## Correspondencia con el equipo existente

| Nombre visible | ID de guardado | Idea | Icono |
| --- | --- | --- | --- |
| Espada Carmesí (roja) | `aetherium:astral_sword` | Sangrado continuo y acumulativo | `textures/item/astral_sword.png` (64×64) |
| Espada Violeta (morada) | `aetherium:aetherium_sword` | Corte diagonal de oscuridad de corto alcance | `textures/item/aetherium_sword.png` (64×64) |

Las nuevas texturas originales de ambas espadas siguen las siluetas de las referencias: **rojo/negro con dientes metálicos, engastes carmesí y hoja segmentada** frente a **violeta/índigo con remate curvo, guarda ornamentada y núcleo cristalino**.

## Espada Carmesí: sangrado

- Cada golpe cuerpo a cuerpo **acertado** aplica **una carga** del efecto perjudicial nuevo `aetherium:bleeding`.
- Máximo **5 cargas**. La pantalla de efectos muestra Sangrado I-V con icono de gota roja propio.
- Daño base por segundo: `4 × cargas` puntos de salud reales (PS, no corazones). Máximo `20 PS/s` o **10 corazones por segundo**.
- La duración de cada aplicación se refresca a **6 segundos (120 ticks)**. Como el usuario no indicó duración, se eligió una ventana de prueba de seis segundos. Las cargas desaparecen al terminar el efecto, al morir o al curarlo con un método que elimina efectos.
- Los tics de daño se disparan cada 20 ticks sobre el contador estable de la entidad; re-aplicar Sangrado **no retrasa** indefinidamente el próximo tic.
- Se calcula en el servidor, sin golpes invisibles extra ni sonidos electrónicos. El daño base ignora armadura y resistencia mágica, pero respeta reglas de daño del juego y modificaciones externas; el valor final puede verse afectado por invulnerabilidad o absorción.
- Eliminado el antiguo abanico de proyectiles al cuarto golpe y sus curaciones automáticas para dejar una identidad mecánica clara.

## Espada Violeta: cortes diagonales

- Cada balanceo válido **con la mano principal**, aunque no conecte con ningún mob, crea una onda diagonal 3D distinta del golpe vanilla.
- El ataque sigue infligiendo el daño físico normal. La onda se desplaza hacia donde mira el jugador, aproximadamente **4,3 bloques de recorrido** más su offset inicial; alcance total cercano a **5 bloques**, con anchura de barrido de `~1,9 bloques`.
- Daño de la onda: **la mitad del daño base nominal** de la espada, sin añadir encantamientos ni aumentos externos. Espada actual: `1 daño base del jugador + 5 del arma + 4 del material = 10 PS`; onda = **5 PS antes de mitigaciones**.
- Cada onda puede dañar a varios enemigos próximos a su trayectoria, **como máximo una vez a cada objetivo**. El golpe adicional contra el mismo enemigo que recibió el corte físico no debe perderse por los instantes de invulnerabilidad normales.
- Para impedir spam y duplicación de eventos de red, intervalo mínimo entre cortes **9 ticks (0,45 segundos)** por jugador. Es un límite pequeño en vez de cobrar durabilidad o maná extra.
- Duración visual de ocho ticks. Una entidad `aetherium:dark_slash` se sincroniza con clientes mediante el paquete Forge de aparición y utiliza un renderer de dos crescientes diagonales cruzadas (metal violeta, magenta y núcleo pálido), con partículas violetas en movimiento. El corte **se detiene en bloques sólidos**.
- El antiguo combo de onda circular cada tres golpes y su curación se eliminaron para evitar que la nueva habilidad conviva con un ataque no deseado.
- No se agregaron sonidos sintéticos a los swings: se mantiene el audio de impacto de Minecraft.

## Verificación automatizada

`tools/check_sword_combat.py` comprueba constantes, claves, registros y referencias del renderer. La compilación comprueba tipos reales de Forge, e incluye los dos PNG 64×64, el icono del efecto 18×18 y las clases del efecto y del proyectil en el JAR.

## Pruebas dentro de Minecraft todavía necesarias

- [ ] Golpear un mob de mucha vida 1, 2, 3, 4 y 5 veces con Carmesí y verificar `4/8/12/16/20 PS/s`; en el máximo no debe sobrepasar cinco cargas.
- [ ] Golpear repetidamente antes de que pase un segundo y confirmar que el tic de sangrado no se posterga continuamente.
- [ ] Verificar que Sangrado termina seis segundos después del último impacto y su icono aparece correctamente.
- [ ] Balancear Violeta en el aire: debe aparecer **un corte diagonal realmente visible** por swing válido y avanzar hacia donde se mira.
- [ ] Comprobar slash adicional contra un enemigo alcanzado por el filo físico, otro enemigo detrás y otro a 3–5 bloques.
- [ ] Comprobar que varios mobs reciben como mucho un golpe de cada proyectil y que las paredes detienen la onda.
- [ ] Comprobar audio, escala en mano/inventario, PvP y movimiento en tercera persona.
- [ ] Revisar mundos guardados: las espadas anteriores mantienen sus IDs y se transforman en las nuevas sin pérdida de inventario.

**Importante:** pasar Java/Forge CI no equivale a una prueba del cliente ni verifica numéricamente el daño sobre entidades reales con equipo/efectos de otros mods.
