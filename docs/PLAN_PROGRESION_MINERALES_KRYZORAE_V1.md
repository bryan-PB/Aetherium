# Kryzorae — Plan de progresión de minerales, equipo y fabricación (v1)

**Estado:** paso 2 diseñado. **Primera parte de fase A implementada en código:** Brumacero, Umbracita y Prismalita (bloques, generación, botines, productos, fundición, locales). **Pendiente:** validación en juego y todo el equipamiento/forjas del resto de la hoja.  
**Objetivo:** referencia para elaborar la expansión por fases, conservando Minecraft 1.16.5, Forge 36.2.42, Java 8, mod ID `aetherium`, compatibilidad de partidas y rendimiento.

## Reglas de diseño

1. Endgame significa etapa final de Kryzorae, **no dimensión The End**. Ningún mineral nuevo, estructura ni requisito se coloca en The End.
2. Progresión propia en Overworld + Nether, sin dependencia obligatoria de Create, Curios, JEI, etc. Integraciones externas pueden ser apoyos opcionales.
3. Inspiración general de Terraria/Calamity: escalones de minería, refinamiento, equipo alternativo y materiales de jefes. No copiar minerales, sprites o recetas.
4. Nuevos recursos no necesitan necesariamente un set de armadura: algunos son catalizadores.
5. Evitar farm excesivo de jefes; sus drops desbloquean opciones, no cientos de barras por repetición.
6. Mantener IDs existentes: `aetherium:aetherium_sword` = Violeta; `aetherium:astral_sword` = Carmesí. Conservar diseños de sus armaduras.
7. El contenido visual y jugable debe poder rendir con la configuración de mods actual sin simplificar artificialmente los ataques. Todo beneficio de FPS/MSPT se mide.
8. Los valores de rareza, costes y equilibrio de esta hoja son **objetivos iniciales sujetos a pruebas**, no cambios hechos al juego.

## Estado real detectado en el repositorio antes de los cambios

- Aetherium existente: una feature Overworld que usa por defecto 2 intentos/chunk, vetas de tamaño hasta 4, entre Y=4–16.
- Violeta: espada con 2 lingotes de Aetherium y un palo; armadura con patrón vanilla de 24 lingotes en total. Se obtiene demasiado pronto para la etapa prevista.
- Carmesí: espada con 2 lingotes astrales y un palo; armadura con 24 lingotes astrales. Actualmente 4 lingotes astrales = 4 lingotes de Aetherium + 4 fragmentos astrales + 1 corazón astral.
- Guardián: deja 1 núcleo garantizado y 2–4 fragmentos de Grieta sin contar Saqueo.
- Verdugo Alado: deja 1 corazón garantizado y 3–6 fragmentos astrales sin contar Saqueo.
- El Sigilo del Guardián usa ojo de Ender y el Sello Astral usa perlas de Ender y 1 núcleo. Los ojos y perlas **no exigen visitar The End**, pero la revisión futura puede sustituirlos por materiales propios.
- Se conservan los jefes, el Códice, el equipo y sus identificadores actuales hasta implementar las recetas nuevas de forma completa.

## Catálogo de minerales y generación inicial

Los rangos son para **1.16.5** (Overworld 0–256), no alturas 1.18+. La configuración limitará intentos por chunk y permitirá desactivar cada generación. Los intentos no garantizan esa cantidad de vetas visibles: la roca, el espacio y las reglas de sustitución determinan el resultado.

| Mineral | Dimensión y altura Y | Generación objetivo | Herramienta mínima inicial | Qué desbloquea |
|---|---|---|---|---|
| Brumacero | Overworld Y20–64 | 2 intentos/chunk, vetas de 5 | piedra (nivel 1) | equipo de entrada |
| Umbracita | Overworld Y8–36 | 1 intento/chunk, vetas de 4 | hierro (nivel 2) | equipo de exploración |
| Prismalita | Overworld Y12–42 | 1 intento/chunk, vetas de 3 | hierro (nivel 2) | catalizador, runas, accesorios |
| Ferronita Abisal | Nether Y20–110 | 2 intentos/chunk, vetas de 3–5 | diamante (nivel 3) | equipamiento pesado y forja |
| Ignarita | Nether Y16–80 | 1 intento/chunk, vetas de 2–4 | diamante (nivel 3) | aleaciones y componentes térmicos |
| Resonita | Overworld Y5–24 | 50% de chunks con 1 intento, vetas de 3–4 | Ferronita/Netherita (nivel 4) | equipo avanzado, base Violeta |
| Aetherium *(existente)* | Overworld Y4–16 | mantener inicialmente 2 intentos/chunk, vetas hasta 4 | diamante (nivel 3), valor actual | material endgame tras refinamiento |
| Vaciorita | Nether Y16–56 | 25% de chunks con 1 intento, vetas de 2–3 | Ferronita/Netherita (nivel 4) | aleación astral, sin armadura propia |

- La generación del Nether buscará el bloque base apropiado; la del Overworld usará reemplazo de piedra natural, sin diseñar un nuevo bioma ahora.
- Los niveles de minería serán comprobados en Forge 1.16.5 antes de aplicarse.
- La disponibilidad de Aetherium en el mundo *no implica* disponibilidad anticipada de la espada/armadura endgame: la fabricación será la que establezca el desbloqueo.
- No regenerar minerales recorriendo chunks ya existentes ni hacer búsquedas de worldgen cada tick. En partidas antiguas, explorar chunks nuevos.

## Cuatro armaduras nuevas, dos existentes

| Set | Papel | Objetivo defensivo inicial (conjunto) | Diferencia jugable |
|---|---|---|---|
| Brumacero | Primer conjunto | alrededor del hierro (15–16) | protección accesible |
| Umbracita | Exploración | 17–18, inferior al diamante/netherita | leve movilidad y exploración, efecto no permanente por tick |
| Ferronita Abisal | Resistencia | 19–20, dureza moderada | aguante, peso y protección térmica limitada |
| Resonita | Avanzado | alrededor de diamante/netherita (20) | precisión y movilidad; alternativa a Ferronita |
| Violeta *(existente)* | Endgame tras Guardián | conservar primero estadísticas actuales (24 defensa, 4 dureza/pieza) | cortes de energía, estilo dimensional |
| Carmesí *(existente)* | Endgame superior tras Verdugo | conservar primero estadísticas actuales (28 defensa, 5 dureza/pieza) | sangrado, estilo agresivo |

Los 4 conjuntos nuevos tendrán armas y herramientas representativas, pero **no toda utilidad requiere equipo completo**. Ajustar daños con armas/encantamientos de Apotheosis y Dungeons Gear como pruebas opcionales, nunca como requisito. No subir estadísticas automáticamente por la simple presencia de otros mods.

## Costes y recetas de refinamiento propuestos

Mantener los patrones reconocibles de Minecraft: casco 5, peto 8, pantalones 7, botas 4 = **24 lingotes**, espada 2, pico 3, hacha 3, salvo mejoras de la Forja.

| Producto | Refinamiento objetivo | Coste de 1 armadura | Nota |
|---|---|---|---|
| Lingote Brumacero | 1 bruto -> 1 lingote (horno) | 24 brutos | comienzo |
| Lingote Umbracita | 1 bruto -> 1 lingote (horno) | 24 brutos | escasez de depósito; no exigir 48 por defecto |
| Fragmento Prismalita | 1 mineral -> 2 fragmentos | sin armadura | catalizadores reutilizables en varias etapas |
| Lingote Ferronita | Lote: 4 brutos Ferronita + 1 fragmento Ignarita -> 4 lingotes | 24 brutos + 6 Ignarita | Fragua de Aleaciones; sin combustión por tick |
| Ignarita refinada | 1 mineral -> 1 fragmento | sin armadura | componente térmico |
| Lingote Resonita | Lote: 4 brutos Resonita + 2 fragmentos Prismalita -> 4 lingotes | 24 brutos + 12 fragmentos Prismalita (6 minerales sin Fortuna) | Fragua de Aleaciones |
| Lingote Aetherium | conservar la fundición de bruto; usar en infusiones | endgame: **mejoras**, no patrón directo | material obtenible temprano, uso endgame bloqueado por Guardián |
| Vaciorita | 1 mineral -> 1 catalizador | sin armadura | aleación astral |

Las cantidades en la tabla son consumos teóricos **sin Fortuna, Saqueo ni botines alternativos**. Los materiales exactos, balances de experiencia y compatibilidad con hornos se verificarán durante la implementación.

## Ruta de progresión (sin The End)

```text
Hierro / exploración
  ├── Brumacero (equipo inicial)
  ├── Umbracita (equipo de exploración)
  └── Prismalita (catalizadores)
           |
      Nether + diamante
           |
    Ferronita + Ignarita
           |                         Fragua de Aleaciones                   |                             Resonita avanzada                      |                         |
   Sigilo del Guardián (receta propia, sin The End)
           |
     GUARDIÁN DE LA GRIETA
           |
    Núcleo + fragmentos
           |
     Forja de la Grieta
           ├── Resonita + Aetherium -> VIOLETA
           └── Sello Astral -> VERDUGO ALADO
                                 |
                 Corazón + fragmentos + Vaciorita
                                 |
                Ferronita + aleación astral -> CARMESÍ
```

No forzar a destruir la espada Violeta para crear la Carmesí. Son estilos de combate separados; la Carmesí se desbloquea más tarde, pero el usuario puede conservar ambos.

## Endgame: costes completos propuestos

### Equipo Violeta (ID existente, diseño conservado)

- 1 pieza de armadura Resonita correspondiente + **4 lingotes Aetherium** + **1 fragmento de Grieta** -> la pieza Violeta.
- 1 espada Resonita + **5 lingotes Aetherium** + **2 fragmentos de Grieta** -> Espada Violeta.
- **Set de armadura + espada:** 21 lingotes Aetherium, 6 fragmentos de Grieta y un set Resonita + espada Resonita.
- Un Guardián da 2–4 fragmentos (sin Saqueo), de modo que normalmente hacen falta **2–3 victorias** para ambas recompensas, no decenas.

### Equipo Carmesí (ID existente, diseño conservado)

- Tras la primera victoria contra el Verdugo, su **Corazón Astral se puede insertar** en la Forja de la Grieta para habilitar permanentemente el refinamiento astral de esa estación. El corazón es un desbloqueo de la estación, no un ingrediente consumido por cada barra. Si se rompe la forja, debe poder recuperarse el corazón o conservarse el estado de activación mediante un mecanismo seguro.
- Lote de 6 lingotes astrales: **4 lingotes Aetherium + 1 Vaciorita + 1 fragmento astral -> 6 lingotes astrales**, solo en forja habilitada.
- 1 pieza de Ferronita correspondiente + **4 lingotes astrales** + **1 fragmento astral** -> pieza Carmesí.
- 1 espada Ferronita + **5 lingotes astrales** + **2 fragmentos astrales** -> Espada Carmesí.
- **Set + espada:** 21 lingotes astrales, 6 fragmentos astrales para mejoras y, para producir 24 lingotes mediante 4 lotes: 16 Aetherium + 4 Vaciorita + 4 fragmentos astrales. **Total: 10 fragmentos astrales**, un corazón de desbloqueo y equipo base de Ferronita.
- El Verdugo deja 3–6 fragmentos por victoria sin Saqueo; objetivo inicial: **2–4 victorias** para armadura + espada.

Los picos y hachas endgame se fabricarán como mejoras equivalentes, con costes propios. Se evitará que su receta antigua de tres barras permita saltar la progresión; habrá que revisar **todas** las recetas del tier, no solo las dos espadas.

## Invocadores y materiales de jefe

- Guardián: reemplazar la receta simple actual con una combinación de Ferronita, Prismalita, Resonita y materiales de criaturas del mod. Ni ojos de Ender ni visitas a The End serán requisitos.
- Guardián: 1 Núcleo de Grieta garantizado + 2–4 fragmentos actuales. El núcleo servirá para construir una **Forja de la Grieta** (un desbloqueo duradero).
- Sello Astral: reformular con 2 fragmentos de Grieta y materiales refinados, **sin consumir un segundo Núcleo de Grieta**. La victoria contra el Guardián sigue siendo obligatoria para fabricar el sello.
- Verdugo: 1 Corazón Astral garantizado + 3–6 fragmentos astrales actuales. El corazón habilita la transformación avanzada de la Forja.
- La segunda invocación y el equipo Violeta podrán competir por fragmentos de Grieta, pero no por varios núcleos obligatorios. Reajustar costes tras pruebas reales.

## Estaciones de fabricación (sin motor de ticks innecesario)

1. **Fragua de Aleaciones:** recetas de 4 barras en lote, validación al pulsar «fabricar» y ningún proceso constante si no hay interacción. Solo nuevo bloque y pantalla si aportan valor; valorar primero recetas simples o estaciones ya existentes.
2. **Forja de la Grieta:** desbloqueada después del Guardián; transforma equipo existente con materiales de jefe. Activación astral mediante corazón del Verdugo para los objetos Carmesí.
3. Bloques sin `BlockEntity` con tick salvo que el diseño probado realmente lo necesite. Si se usa estado persistente, sincronizar solo en interacción.

## Despliegue gradual y compatibilidad

**Fase A:** Brumacero, Umbracita y Prismalita; recursos, botín, generación configurable, localización, JEI vía recetas y primeros equipos.

**Fase B:** Fragua de Aleaciones, Ferronita, Ignarita, Resonita; rutas alternativas Ferronita/Resonita y nuevas herramientas.

**Fase C:** Forja de la Grieta, invocadores actualizados, Violeta/Armadura Violeta por mejoras. Revisar recetas antiguas para impedir atajos. Mantener objetos de partidas existentes.

**Fase D:** Vaciorita, refinamiento astral, Carmesí/Armadura Carmesí por mejoras, revisión de costes tras enfrentar al Verdugo.

**Fase futura, no ahora:** mini-bioma propio, estructuras de gran escala, o futuras dimensiones. No mezclar con las fases A–D.

## Verificaciones antes de dar una fase por terminada

- Compilar Forge 1.16.5 + Java 8; validar registros, texturas, JSON, recetas, localizaciones y permisos de herramientas.
- Comprobar que todos los IDs antiguos funcionan en mundos guardados.
- Probar nuevas partidas y chunks no generados; sin overwrites de mundo antiguo.
- Verificar que no hay crafteos alternativos que permitan evitar al Guardián o al Verdugo.
- Medir FPS/MSPT en el mismo equipo del usuario, tanto minando como luchando con jefes. La referencia observada durante combate es 60 FPS con caídas ocasionales a ~48: **dato orientativo, no una garantía para futuras versiones**.
- Probar opcionalmente con Better Caves, Better Mineshafts, Create, JEI, Curios y los mods de rendimiento presentes en el pack.
- No añadir contenido a The End y no modificar las texturas aceptadas de Violeta/Carmesí.

## Decisiones por comprobar durante fase A

- Nombres definitivos (Brumacero, Umbracita y Prismalita son provisionales).
- Rarezas iniciales y ritmo de minería (no asumir que una armadura debe exigir 100 minerales).
- Si Fragua de Aleaciones puede usar recetas existentes antes de crear una interfaz propia.
- Cantidad final de fragmentos del Guardián y del Verdugo tras una prueba de combate real.
- Apariencia detallada de materiales y herramientas sin alterar las espadas/armaduras endgame ya aprobadas.
