# Jefes de Aetherium (Parte 5)

Los jefes se invocan manualmente mediante objetos de encuentro. No tienen aparición natural y el objeto no se consume si hay otro jefe de Aetherium activo a 40 bloques o si no hay espacio suficiente.

## Guardián de la Brecha

- **Vida:** 260 puntos.
- **Daño cuerpo a cuerpo:** 8 puntos.
- **Armadura:** 8; dureza 3.
- **Fase I:** golpes de área de radio 4,4, daño 8 y enfriamiento de 105 ticks.
- **Fase II (50% de vida):** radio 5,7, daño 11, ralentización breve y enfriamiento de 82 ticks. Puede invocar como máximo un Cazador de Brecha cerca, con un enfriamiento de 240 ticks.
- **Barra:** `ServerBossInfo` violeta que cambia a roja en fase II.
- **Drop garantizado:** Núcleo de Brecha. También deja 2–4 Fragmentos de Brecha.

Invocación:

```mcfunction
/give @p aetherium:rift_sigil
```

Usa el Sigilo del Guardián sobre un bloque con espacio libre encima.

## Devorador Astral

- **Vida:** 420 puntos.
- **Daño cuerpo a cuerpo:** 9 puntos.
- **Armadura:** 10; dureza 5.
- **Fase I (100–66%):** proyectil mágico de 8 de daño cada 88 ticks.
- **Fase II (66–33%):** pozo gravitacional que atrae jugadores en radio 8, aplica ralentización corta y dispara proyectiles.
- **Fase III (33–0%):** Nova Astral en radio 6,5 con 10 de daño, empuje y ceguera breve; sus proyectiles pueden salir en una segunda trayectoria con dispersión.
- **Barra:** azul, cambia a morada y finalmente roja.
- **Drops:** Corazón Astral garantizado, 3–6 Fragmentos Astrales y 2–3 Fragmentos de Brecha.

Invocación:

```mcfunction
/give @p aetherium:astral_seal
```

El Sello Astral se fabrica con el Núcleo de Brecha que deja el Guardián. Úsalo sobre un bloque con espacio libre encima.

## Comandos de prueba

```mcfunction
/summon aetherium:rift_guardian ~ ~ ~
/summon aetherium:astral_devourer ~ ~ ~
```

Los jefes también pueden invocarse directamente con esos comandos para depuración. Para la progresión normal se recomienda el uso de los sellos.

## Rendimiento

- No se generan entidades de proyectil de forma continua: el Devorador dispara con enfriamientos de 82–98 ticks.
- Cada golpe de área procesa únicamente jugadores dentro de su caja de alcance.
- La lluvia de partículas es una cantidad acotada por ataque; no hay partículas persistentes.
- El Guardián puede mantener como máximo un Cazador de Brecha invocado por su propia habilidad.
- La barra de jefe utiliza el sistema nativo de Minecraft y no necesita un HUD personalizado.
