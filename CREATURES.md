# Aetherium — criaturas (Parte 4)

## Cazador de Brecha (`aetherium:rift_stalker`)

- Clasificación: `MONSTER`.
- Atributos base: 26 HP, 5 de daño, 3 de armadura, velocidad 0,29 y alcance de seguimiento 24.
- IA: ataque cuerpo a cuerpo, paseo, mirar jugadores, mirar alrededor y selección de jugador como objetivo.
- Habilidad: cuando detecta un objetivo a una distancia media (4–10 bloques) y está en el suelo, realiza una embestida direccional. Tiene 100 ticks de enfriamiento, para no repetir impulsos constantemente.
- Golpe especial: 22% de probabilidad de aplicar Lentitud I durante 2 segundos.
- Aparición: criatura hostil rara, solo en biomas de superficie fuera de Nether, End, océanos y ríos.
- Botín: 65% de probabilidad de 1–2 pepitas de Aetherium, más 12% de probabilidad de una perla de Ender.

## Lumivelo (`aetherium:lumivelo`)

- Clasificación: `CREATURE`.
- Atributos base: 18 HP, velocidad 0,27 y alcance de seguimiento 16.
- IA: evita monstruos dentro de 8 bloques, pasea y se acerca a jugadores que sostienen Aetherium bruto.
- Habilidad: cada 160 ticks (8 segundos) realiza una sola búsqueda local de monstruos en un radio de 6 bloques y les aplica Brillo durante 3 segundos. La comprobación no se ejecuta cada tick.
- Reproducción: se alimenta con Aetherium bruto.
- Aparición: rara, en bosques, llanuras, taigas, junglas y sabanas.
- Botín: 1–2 de Polvo de Aetherium y 8% de probabilidad de una pepita de Aetherium.

## Configuración

Archivo `config/aetherium-common.toml`:

```toml
[spawning]
    riftStalkerWeight = 8
    lumiveloWeight = 4
```

- Un peso `0` desactiva la aparición natural de esa criatura.
- Los valores indican peso relativo, no mobs por chunk.
- Los huevos de aparición se incluyen en la pestaña creativa para pruebas y construcción de mapas.

## Pruebas en mundo creativo

```mcfunction
/summon aetherium:rift_stalker ~ ~ ~
/summon aetherium:lumivelo ~ ~ ~
```

También puedes utilizar sus huevos de aparición desde el inventario creativo.

## Decisiones de rendimiento

- Modelos formados por cajas sencillas, sin geometría compleja.
- Texturas de entidad 64×64 y un objeto 16×16.
- `clientTrackingRange(8)` y `updateInterval(3)` en los dos tipos.
- No se generan enjambres de partículas.
- La habilidad de exploración del Lumivelo ejecuta su búsqueda local cada 160 ticks, no en cada tick.
- Peso de aparición predeterminado bajo y ajustable por configuración.
