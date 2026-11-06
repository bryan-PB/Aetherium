# Kryzorae — respuesta de los jefes a combos de Espada Carmesí

**Minecraft 1.16.5 · Forge 36.2.42.** Ajuste solicitado después de pruebas reales del jugador: algunos golpes seguidos de la Espada Carmesí dejaban a los jefes en una situación demasiado pasiva durante sus tiempos de espera.

## Objetivo

Evitar que pulsar ataque repetidamente mantenga a los jefes en una situación de ventaja excesiva para el jugador, **sin concederles invulnerabilidad**, sin cancelar sus animaciones ni lanzar contraataques inevitables. Las ventanas de castigo por esquivar bien un ataque siguen existiendo.

## Cambio 1 — cadencia del Sangrado contra jefes

- Un golpe acertado con la Carmesí sigue produciendo **su daño físico vanilla completo**.
- **Solo contra el Guardián y el Verdugo**, el Sangrado no puede ganar o refrescar una carga más de una vez cada **12 ticks (0,6 segundos)**.
- En consecuencia, clics muy rápidos no acumulan inmediatamente las tres cargas, mientras que ataques correctamente espaciados sí lo hacen.
- El límite sigue siendo **3 cargas**, **2/4/6 PS por segundo**, **5 segundos de duración** desde la última aplicación válida. No se vuelve a nerfear el daño.
- El control de cadencia se guarda por entidad objetivo, no por espada ni por jugador, evitando eludirlo cambiando de arma.
- Contra mobs normales se mantiene la aplicación original de Sangrado, para no quitarle identidad al arma.

## Cambio 2 — respuesta defensiva del Guardián de la Grieta

- Si recibe **3 aplicaciones válidas de Carmesí**, con un máximo de **34 ticks** entre aplicaciones y el jugador a corta distancia, y el Guardián está en estado neutral, reduce su espera y prepara **el golpe de suelo que ya tenía**.
- El golpe del Guardián sigue mostrando **24 ticks (1,2 segundos)** de aviso y el mismo radio, daño y retroceso. No añade ningún golpe instantáneo.
- La reacción tiene **160 ticks (8 segundos)** de enfriamiento para que el jugador no quede atrapado en contraataques constantes.
- Si está en una animación de ataque, despertando o vulnerable tras fallar una embestida, el contador **no interrumpe esa fase** ni anula el castigo merecido por el jugador.

## Cambio 3 — retirada táctica del Verdugo Alado

- Con el mismo patrón de **3 aplicaciones válidas** de Carmesí a corta distancia, estando en vuelo neutral, intenta **retroceder y ganar altura durante 16 ticks (0,8 segundos)**.
- La retirada utiliza movimiento y colisiones normales; **no es teletransporte**, y no otorga escudo ni reducción de daño.
- Produce una pequeña nube de humo para que el jugador reconozca el movimiento. Después vuelve a usar su patrón normal de plumas-cuchilla, ilusiones, rayo y embestidas.
- Tiene también **8 segundos** de enfriamiento.
- No se activa en el agua, durante las preparaciones ni durante la ventana de vulnerabilidad por esquivar sus embestidas.

## Balance conservado

| Mecánica | Valor |
| --- | --- |
| Salud Guardián | 450 PS |
| Salud Verdugo | 650 PS |
| Sangrado carmesí | 3 cargas, 2/4/6 PS/s, 5 segundos |
| Cadencia mínima de Sangrado sobre jefes | 12 ticks |
| Combo necesario para reacción defensiva | 3 aplicaciones válidas consecutivas (separadas hasta 34 ticks) |
| Enfriamiento de reacción | 160 ticks |
| Ataque defensivo Guardián | Golpe de suelo existente, aviso de 24 ticks |
| Defensa Verdugo | Retirada física de 16 ticks, sin daño |

**No cambian** los IDs de las espadas, los demás enemigos, las armaduras ni las estadísticas generales de ambos jefes.

## Verificación manual

- [ ] Probar clics rápidos contra cada jefe con la Carmesí. La vida debe bajar por los golpes normales, pero las cargas de Sangrado no deben crecer antes de 12 ticks.
- [ ] Probar golpes espaciados: tres deben acumular Sangrado III y permitir una reacción defensiva si el jefe está neutral y cerca.
- [ ] Guardián: observar aviso del golpe de suelo y poder esquivarlo como siempre; nunca debe desencadenarse durante una transición ni mientras está vulnerable por fallar su carga.
- [ ] Verdugo: comprobar la retirada gradual y que no atraviesa muros ni se teletransporta; tras ella debe recuperar sus patrones habituales.
- [ ] Esquivar una embestida completa y golpear al jefe durante su vulnerabilidad: debe seguir recibiendo daño, sin escapes artificiales.
- [ ] Repetir con otras armas, mobs normales, varios jugadores y mundos antiguos para descartar regresiones.

Las pruebas automáticas verifican reglas, contadores y compilación del código, pero **no acreditan el resultado visual ni el equilibrio real del combate**.
