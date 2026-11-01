# Aetherium — Tier Astral y objetos utilitarios (Parte 6)

## Progresión del tier final

El Devorador Astral entrega un Corazón Astral garantizado y Fragmentos Astrales. La receta `astral_ingot.json` combina:

- 1 Corazón Astral
- 4 Fragmentos Astrales
- 4 Lingotes de Aetherium

El resultado son 4 Lingotes Astrales. Los Lingotes Astrales son también el material de reparación del equipo final.

## Equipo Astral

| Pieza | Valor / atributo |
|---|---|
| Espada | 12 de daño nominal por los modificadores de la espada y el tier; velocidad -2,4; 6.000 usos |
| Pico | Velocidad de minería 13; nivel 6; 6.000 usos |
| Hacha | 12 de daño nominal aproximado; velocidad -3,0; 6.000 usos |
| Armadura completa | 28 puntos de defensa; dureza 5; resistencia al retroceso 0,25 |

El bonus de conjunto se comprueba cada 40 ticks en el servidor: Resistencia II, Velocidad I, Resistencia al Fuego y Visión Nocturna. El efecto solo se renueva cuando las cuatro piezas correctas están equipadas.

## Objetos utilitarios

### Imán de Aetherium

Clic derecho activa o desactiva el imán. Si está activo y se encuentra en el inventario principal u offhand del jugador, cada 10 ticks atrae hasta el número configurado de entidades de objetos dentro del radio configurado. El imán no teletransporta objetos: aplica una pequeña aceleración hacia el jugador, por lo que los obstáculos y la física del mundo siguen importando.

Valores iniciales de `aetherium-common.toml`:

```toml
[utility]
    magnetRadius = 4
    magnetMaxItems = 8
```

El radio acepta 2–6 bloques y el máximo de entidades por pulso acepta 1–16. No se ejecutan búsquedas mientras el imán está apagado o el jugador no lo lleva.

### Bengala de Aetherium

Se fabrica con polvo de Aetherium, pepitas de Aetherium y polvo de piedra luminosa, por lo que está disponible antes del jefe final. Clic derecho consume una bengala, concede Visión Nocturna durante 30 segundos y aplica Brillo durante 7 segundos a un máximo de 12 monstruos dentro de 12 bloques. Tiene 12 segundos de enfriamiento. El efecto se aplica en el servidor.

## Compatibilidad

Curios sigue siendo opcional. El equipo Astral es equipamiento vanilla de armadura/herramientas; no añade una dependencia nueva ni modifica las ranuras de Curios existentes.

## Pruebas recomendadas

1. Probar los objetos en un mundo de pruebas con `./gradlew runClient`.
2. Activar/desactivar el imán con clic derecho y verificar que solo atrae objetos con retraso de recogida terminado.
3. Usar la bengala cerca de monstruos y comprobar la duración de Visión Nocturna y Brillo.
4. Equipar el set Astral completo y comprobar los cuatro efectos; quitar una pieza y comprobar que dejan de renovarse.
5. Verificar recetas y reparación con Lingote Astral.
6. Probar con Curios instalado y sin Curios: el proyecto no requiere Curios para iniciar.

## Rendimiento

Las búsquedas del imán son opt-in, una vez cada 10 ticks, con radio pequeño y tope de entidades. La bengala se ejecuta únicamente bajo demanda y tiene un máximo de 12 objetivos. El bonus de armadura se actualiza cada 40 ticks.
