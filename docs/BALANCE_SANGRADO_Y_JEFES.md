# Kryzorae — primer ajuste de balance tras las espadas nuevas

**Minecraft 1.16.5, Forge 36.2.42.** Mantener los identificadores internos `aetherium`. Propuesta implementada como valores iniciales para pruebas de juego, no una certificación de dificultad.

## Referencias de salud verificadas

| Jefe | Juego/mod | Salud publicada |
| --- | --- | --- |
| Dragón del End | Minecraft Java | 200 PS |
| Wither | Minecraft Java | 300 PS |
| Ender Guardian | L_Ender's Cataclysm | 300 PS (armadura 20) |
| Ignis | L_Ender's Cataclysm | 450 PS (armadura 10) |
| Netherite Monstrosity | L_Ender's Cataclysm | 500 PS (armadura 10) |

Fuentes: [Minecraft Wiki — Ender Dragon](https://minecraft.wiki/w/Ender_dragon), [Minecraft Wiki — Wither](https://minecraft.wiki/w/Wither) y [página oficial de L_Ender's Cataclysm en CurseForge](https://www.curseforge.com/minecraft/mc-mods/lendercataclysm). Los atributos del mod Cataclysm pueden variar según la versión y los archivos de configuración. Las cifras son **referencias de diseño**, no valores que deban copiarse exactamente.

## Salud implementada en Kryzorae

| Jefe | Antes | Ahora | Armadura que se mantiene | Transiciones |
| --- | ---: | ---: | ---: | --- |
| Guardián de la Grieta | 260 PS | **450 PS** | 8 | Fase II al 50 %: **225 PS** |
| Verdugo Alado | 420 PS | **650 PS** | 10 | Fase II al 66 %: **429 PS**. Fase III al 33 %: **214,5 PS** |

Las fases se basan en porcentajes de `getMaxHealth()`; el código existente no necesita cambiar los umbrales. No se modifican velocidad, daño de sus ataques, cantidad de ilusiones o regeneración. No se introduce invulnerabilidad artificial, barra falsa ni inmunidad nueva al sangrado.

**Mundos existentes:** los **jefes nuevos** se generan con el máximo actualizado. Las entidades ya guardadas pueden conservar su `Health` actual al cargar el mundo y no se les cura automáticamente; invócalas de nuevo para evaluar el balance completo sin confundir el resultado.

## Espada Carmesí: reducción del sangrado

| Cargas | Antes | Ahora |
| --- | ---: | ---: |
| I | 4 PS/s | **2 PS/s** |
| II | 8 PS/s | **4 PS/s** |
| III | 12 PS/s | **6 PS/s** |
| IV | 16 PS/s | **Ya no existe** |
| V | 20 PS/s | **Ya no existe** |

- Límite de cargas: **3**, en lugar de 5.
- Daño periódico: **2 PS por segundo y carga**, antes 4 PS.
- Máximo a tres cargas: **6 PS/s**, equivalente a **3 corazones/s** de daño base.
- Duración desde el último golpe: **5 segundos (100 ticks)**, antes 6.
- Los golpes acertados añaden una carga hasta el límite, refrescan la duración y no generan efectos sonoros especiales por cada tick.
- El daño usa el sistema de daño normal del servidor, por lo que invulnerabilidad temporal, absorción y otros efectos/mods pueden reducir los PS realmente perdidos; los números aquí corresponden al daño **base intentado**.
- La espada no cambia su daño físico, alcance, animación ni textura. La Espada Violeta no cambia.
- Los nombres y descripciones del debuff se mantienen traducidos en inglés y cuatro variantes de español; tooltip de la espada carmesí actualizado.

**Motivo del ajuste:** antes una espada de 5 niveles podía causar 20 PS/s sostenidos aparte de sus golpes. Con un tope de 6 PS/s, el sangrado sigue notándose sobre enemigos normales y jefes sin dominar por sí solo el tiempo de la batalla.

## Comprobaciones

- [ ] Crear un Guardián nuevo y verificar 450/450 PS, fase II al bajar de 225.
- [ ] Crear un Verdugo nuevo y verificar 650/650 PS, fases II y III aproximadamente a 429 y 214,5.
- [ ] Golpear a un enemigo resistente con Carmesí una, dos y tres veces: verificar Sangrado I/II/III y que un cuarto golpe no muestre IV.
- [ ] Verificar duración de cinco segundos desde el último impacto, impacto cada segundo y daño visible sin reducción a cero por los fotogramas de invulnerabilidad.
- [ ] Comprobar que, contra jefes, el sangrado se nota sin eliminar fases enteras en segundos.
- [ ] Revisar barra de vida y combate con armadura/encantamientos; ajustar números de nuevo solo tras prueba real.

La compilación y las aserciones estáticas prueban constantes y recursos, no la dificultad, el tiempo real de batalla ni el resultado con otros mods.
